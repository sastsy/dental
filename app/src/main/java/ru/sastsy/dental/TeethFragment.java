package ru.sastsy.dental;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.Collections;


public class TeethFragment extends Fragment {

    private int clicked_tooth = 0; // stores clicked tooth number
    private static final int TEETH_NUMBER = 32;
    private static final long REMOVED_TOOTH = 3;
    private static final long CAVITY_TOOTH = 0;

    public TeethFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teeth, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView textView = getActivity().findViewById(R.id.textView);
        Button changeToothStateButton = getActivity().findViewById(R.id.change_btn);
        Button addEventButton = getActivity().findViewById(R.id.button10);
        Button toothSpecialitiesButton = getActivity().findViewById(R.id.button9);
        Button toothHistoryButton = getActivity().findViewById(R.id.button6);

        // Get user info to access the db
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        String userID = fAuth.getCurrentUser().getUid();
        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth"); // Database path to the documents with the teeth info

        Jaw jaw = new Jaw(userID);

        String[] toothStateList = getResources().getStringArray(R.array.tooth_state); // List with all possible options for tooth status
        boolean[] checkedState = new boolean[toothStateList.length]; // List to indicate which options the user chose from the list

        int[] imageButtons_id = {R.id.imageButton1, R.id.imageButton2, R.id.imageButton3, R.id.imageButton4,
                R.id.imageButton5, R.id.imageButton6, R.id.imageButton7, R.id.imageButton8, R.id.imageButton9,
                R.id.imageButton10, R.id.imageButton11, R.id.imageButton12, R.id.imageButton13, R.id.imageButton14,
                R.id.imageButton15, R.id.imageButton16, R.id.imageButton17, R.id.imageButton18, R.id.imageButton19,
                R.id.imageButton20, R.id.imageButton21, R.id.imageButton22, R.id.imageButton23, R.id.imageButton24,
                R.id.imageButton25, R.id.imageButton26, R.id.imageButton27, R.id.imageButton28, R.id.imageButton29,
                R.id.imageButton30, R.id.imageButton31, R.id.imageButton32};

        ImageButton[] imageButtonsList = new ImageButton[32];

        for (int i = 0; i < TEETH_NUMBER; ++i) {
            imageButtonsList[i] = getActivity().findViewById(imageButtons_id[i]);
            jaw.addTooth(new Tooth(i + 1));
            int finalI = i;
            // Update current tooth status and set tooth color depending on it
            collectionReference.document(String.valueOf(jaw.getTooth(i).getNumber())).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                jaw.getTooth(finalI).setState((ArrayList<Long>) document.get("state"));
                if (jaw.getTooth(finalI).getState().contains(REMOVED_TOOTH))
                    imageButtonsList[finalI].setColorFilter(Color.parseColor("#BAFFFFFF"), PorterDuff.Mode.SRC_ATOP);
                else if (jaw.getTooth(finalI).getState().contains(CAVITY_TOOTH))
                    imageButtonsList[finalI].setColorFilter(Color.parseColor("#65FF0000"), PorterDuff.Mode.SRC_ATOP);
            });

            imageButtonsList[i].setOnClickListener(v -> {
                if (jaw.getTooth(clicked_tooth).getState().contains(REMOVED_TOOTH))
                    imageButtonsList[clicked_tooth].setColorFilter(Color.parseColor("#BAFFFFFF"), PorterDuff.Mode.SRC_ATOP);
                else if (jaw.getTooth(clicked_tooth).getState().contains(CAVITY_TOOTH))
                    imageButtonsList[clicked_tooth].setColorFilter(Color.parseColor("#65FF0000"), PorterDuff.Mode.SRC_ATOP);
                else imageButtonsList[clicked_tooth].clearColorFilter();

                clicked_tooth = finalI;
                imageButtonsList[finalI].setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                textView.setText(jaw.getTooth(finalI).getName());
                // Show hidden buttons
                toothSpecialitiesButton.setVisibility(View.VISIBLE);
                changeToothStateButton.setVisibility(View.VISIBLE);
                addEventButton.setVisibility(View.VISIBLE);
                toothHistoryButton.setVisibility(View.VISIBLE);
            });
        }

        changeToothStateButton.setOnClickListener(v ->
                collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber())) // AlertDialog that changes status of the tooth
                .get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            jaw.getTooth(clicked_tooth).setState((ArrayList<Long>) document.get("state")); // Set status of the tooth from db
            AlertDialog.Builder builder = makeDialogBuilder(jaw.getTooth(clicked_tooth).getName().toUpperCase(), null);

            for (int i = 0; i < checkedState.length; ++i) {
                // If tooth status contains one of the conditions, boolean list indicates it
                checkedState[i] = jaw.getTooth(clicked_tooth).getState().contains((long) i);
            }

            builder.setMultiChoiceItems(toothStateList, checkedState, (dialog, which, isChecked) -> {
                if (isChecked) {
                    // If tooth status does not yet contain the checked item, add it to the status
                    if (!jaw.getTooth(clicked_tooth).getState().contains((long) which)) jaw.getTooth(clicked_tooth).getState().add((long) which);
                }
                // If the item was not selected, remove it from the status
                else jaw.getTooth(clicked_tooth).getState().remove(Long.valueOf(which));
            });

            builder.setPositiveButton("СОХРАНИТЬ", (dialog, which) -> {
                collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber()))
                        .update("state", jaw.getTooth(clicked_tooth).getState()); // Update status of the tooth in the db
            });

            builder.setNegativeButton("ОТМЕНИТЬ", (dialog, which) -> dialog.dismiss());

            AlertDialog changeStateDialog = builder.create();
            changeStateDialog.show();

        }));

        addEventButton.setOnClickListener(v ->
                collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber())).get() // AlertDialog that adds new event for the tooth
                .addOnCompleteListener(task -> {
                    AlertDialog.Builder builder = makeDialogBuilder("ДОБАВИТЬ СОБЫТИЕ", null);

                    View dialogView = getLayoutInflater().inflate(R.layout.event_dialog, null);
                    MaskEditText date = dialogView.findViewById(R.id.date);
                    EditText place = dialogView.findViewById(R.id.place);
                    TextInputEditText doctor = dialogView.findViewById(R.id.doctor);
                    TextInputEditText comment = dialogView.findViewById(R.id.comment);

                    DocumentSnapshot document = task.getResult();
                    jaw.getTooth(clicked_tooth).setEvent((ArrayList<String>) document.get("event")); // Set list of events from db
                    StringBuilder historyString = new StringBuilder();

                    builder.setPositiveButton("СОХРАНИТЬ", (dialog, which) -> {
                        Event event = new Event(date.getMasked(), place.getText().toString(), doctor.getText().toString(), comment.getText().toString());
                        makeHistoryString(historyString, event); // Process fields of the form and turn them into a string
                        jaw.getTooth(clicked_tooth).addEvent(historyString.toString());

                        collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber()))
                                .update("event", jaw.getTooth(clicked_tooth).getEvent()); // Update list of events of the tooth in the db
                        collectionReference.document("events")
                                .update("event", FieldValue.arrayUnion(historyString.toString())); // Update list of overall events in the db
                    });

                    builder.setNegativeButton("ОТМЕНИТЬ", (dialog, which) -> dialog.dismiss());

                    AlertDialog addEventDialog = builder.create();
                    addEventDialog.setView(dialogView);
                    addEventDialog.show();
                }));

        toothSpecialitiesButton.setOnClickListener(v ->
                collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber())) // AlertDialog that shows current status of the tooth
                .get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            jaw.getTooth(clicked_tooth).setState((ArrayList<Long>) document.get("state"));
            StringBuilder state_string = new StringBuilder();

            for (int i = 0; i < toothStateList.length; ++i) {
                if (jaw.getTooth(clicked_tooth).getState().contains((long) i)) state_string.append(toothStateList[i].toUpperCase()).append("\n");
            }

            AlertDialog.Builder builder = makeDialogBuilder(jaw.getTooth(clicked_tooth).getName().toUpperCase(), state_string.toString());
            AlertDialog currentStateDialog = builder.create();
            currentStateDialog.show();
        }));

        toothHistoryButton.setOnClickListener(v ->
                collectionReference.document(String.valueOf(jaw.getTooth(clicked_tooth).getNumber())).get() // AlertDialog that shows tooth's history of events
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    jaw.getTooth(clicked_tooth).setEvent((ArrayList<String>) document.get("event")); // Set list of events from the db

                    StringBuilder historyString = new StringBuilder();
                    ArrayList<String> reversed = new ArrayList<>(jaw.getTooth(clicked_tooth).getEvent());
                    Collections.reverse(reversed);

                    for (String event : reversed) {
                        historyString.append("<br><br>").append(event);
                    }

                    AlertDialog.Builder builder = makeDialogBuilder("ИСТОРИЯ", null);

                    builder.setMessage(Html.fromHtml(String.valueOf(historyString))); // Convert string from Html in order to color only specific parts of the text
                    builder.setNegativeButton("ЗАКРЫТЬ", (dialog, which) -> dialog.dismiss());

                    AlertDialog historyDialog = builder.create();
                    historyDialog.show();
                }));
    }

    /* Simplify creation of AlertDialogs */
    protected AlertDialog.Builder makeDialogBuilder(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        return builder;
    }

    /* Process the string to Html format */
    protected void makeHistoryString(StringBuilder historyString, Event event) {
        if (!event.getDate().equals("")) historyString.append("<font color=\"#FF9088\">" + "ДАТА: " + "</font>").append(event.getDate());
        if (!event.getPlace().equals("")) historyString.append("<br>").append("<font color=\"#FF9088\">" + "МЕСТО: " + "</font>").append(event.getPlace());
        if (!event.getDoctor().equals("")) historyString.append("<br>").append("<font color=\"#FF9088\">" + "ДОКТОР: " + "</font>").append(event.getDoctor());
        if (!event.getComments().equals("")) historyString.append("<br>").append("<font color=\"#FF9088\">" + "КОММЕНТАРИЙ: " + "</font>").append(event.getComments());
    }
}