package ru.sastsy.dental;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import ru.sastsy.dental.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.santalu.maskara.widget.MaskEditText;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Scope;


public class TeethFragment extends Fragment {

    private int clicked_tooth = 0;
    private Tooth[] toothList = new Tooth[32];
    private ImageButton[] imageButtonsList = new ImageButton[32];
    private String[] toothStateList;
    private boolean[] checkedState;
    private ArrayList<Integer> copyList = new ArrayList<>();
    private Event[] eventsList;

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
        final String TAG = "TAG";

        TextView textView = getActivity().findViewById(R.id.textView);
        Button changeToothStateButton = getActivity().findViewById(R.id.change_btn);
        Button addEventButton = getActivity().findViewById(R.id.button10);
        Button toothSpecialitiesButton = getActivity().findViewById(R.id.button9);
        Button toothHistoryButton = getActivity().findViewById(R.id.button6);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        String userID = fAuth.getCurrentUser().getUid();
        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth");

        Map<String, Object> user = new HashMap<>();

        toothStateList = getResources().getStringArray(R.array.tooth_state);
        checkedState = new boolean[toothStateList.length];
        int[] imageButtons_id = {R.id.imageButton1, R.id.imageButton2, R.id.imageButton3, R.id.imageButton4,
                R.id.imageButton5, R.id.imageButton6, R.id.imageButton7, R.id.imageButton8, R.id.imageButton9,
                R.id.imageButton10, R.id.imageButton11, R.id.imageButton12, R.id.imageButton13, R.id.imageButton14,
                R.id.imageButton15, R.id.imageButton16, R.id.imageButton17, R.id.imageButton18, R.id.imageButton19,
                R.id.imageButton20, R.id.imageButton21, R.id.imageButton22, R.id.imageButton23, R.id.imageButton24,
                R.id.imageButton25, R.id.imageButton26, R.id.imageButton27, R.id.imageButton28, R.id.imageButton29,
                R.id.imageButton30, R.id.imageButton31, R.id.imageButton32};

        int[] redImages_id = {R.drawable.red1, R.drawable.red2, R.drawable.red3, R.drawable.red4, R.drawable.red5,
                R.drawable.red6, R.drawable.red7, R.drawable.red8, R.drawable.red9, R.drawable.red10,
                R.drawable.red11, R.drawable.red12, R.drawable.red13, R.drawable.red14, R.drawable.red15,
                R.drawable.red16, R.drawable.red17, R.drawable.red18, R.drawable.red19, R.drawable.red20,
                R.drawable.red21, R.drawable.red22, R.drawable.red23, R.drawable.red24, R.drawable.red25,
                R.drawable.red26, R.drawable.red27, R.drawable.red28, R.drawable.red29, R.drawable.red30,
                R.drawable.red31, R.drawable.red32};

        int[] whiteImages_id = {R.drawable.white1, R.drawable.white2, R.drawable.white3, R.drawable.white4,
                R.drawable.white5, R.drawable.white6, R.drawable.white7, R.drawable.white8, R.drawable.white9,
                R.drawable.white10, R.drawable.white11, R.drawable.white12, R.drawable.white13, R.drawable.white14,
                R.drawable.white15, R.drawable.white16, R.drawable.white17, R.drawable.white18, R.drawable.white19,
                R.drawable.white20, R.drawable.white21, R.drawable.white22, R.drawable.white23, R.drawable.white24,
                R.drawable.white25, R.drawable.white26, R.drawable.white27, R.drawable.white28, R.drawable.white29,
                R.drawable.white30, R.drawable.white31, R.drawable.white32};

        for (int i = 0; i <= 31; ++i) {
            imageButtonsList[i] = getActivity().findViewById(imageButtons_id[i]);
            toothList[i] = new Tooth(i + 1);
            int finalI = i;
            imageButtonsList[i].setOnClickListener(v -> {
                imageButtonsList[clicked_tooth].setImageResource(whiteImages_id[clicked_tooth]);
                clicked_tooth = finalI;
                imageButtonsList[finalI].setImageResource(redImages_id[finalI]);
                textView.setText(toothList[finalI].name);
                toothSpecialitiesButton.setVisibility(View.VISIBLE);
                changeToothStateButton.setVisibility(View.VISIBLE);
                addEventButton.setVisibility(View.VISIBLE);
                toothHistoryButton.setVisibility(View.VISIBLE);
            });
        }

        changeToothStateButton.setOnClickListener(v -> collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).get()
                .addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            toothList[clicked_tooth].state = (ArrayList<Long>) document.get("state");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
            builder.setTitle(toothList[clicked_tooth].name.toUpperCase());

            for (int i = 0; i < checkedState.length; ++i) {
                if (toothList[clicked_tooth].state.contains((long) i)) checkedState[i] = true;
                else checkedState[i] = false;
            }
            builder.setMultiChoiceItems(toothStateList, checkedState, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!toothList[clicked_tooth].state.contains(which)) toothList[clicked_tooth].state.add((long) which);
                }
                else toothList[clicked_tooth].state.remove(Long.valueOf(which));
            });
            builder.setCancelable(false);
            builder.setPositiveButton("СОХРАНИТЬ", (dialog, which) -> {
                collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).update("state", toothList[clicked_tooth].state);
            });
            builder.setNegativeButton("ОТМЕНИТЬ", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog change_state_dialog = builder.create();
            change_state_dialog.show();

        }));

        addEventButton.setOnClickListener(v -> collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).get()
                .addOnCompleteListener(task -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                    builder.setTitle(R.string.AlertDialogEvent);
                    View dialogView = getLayoutInflater().inflate(R.layout.event_dialog, null);
                    MaskEditText date = dialogView.findViewById(R.id.date);
                    EditText place = dialogView.findViewById(R.id.place);
                    TextInputEditText doctor = dialogView.findViewById(R.id.doctor);
                    TextInputEditText comment = dialogView.findViewById(R.id.comment);
                    builder.setCancelable(false);

                    DocumentSnapshot document = task.getResult();
                    toothList[clicked_tooth].event = (ArrayList<String>) document.get("event");

                    StringBuilder history_string = new StringBuilder();
                    builder.setPositiveButton("СОХРАНИТЬ", (dialog, which) -> {
                        Event event = new Event(date.getMasked(), place.getText().toString(), doctor.getText().toString(), comment.getText().toString());
                        if (!event.date.equals("")) history_string.append("<font color=\"#FF9088\">" + "ДАТА: " + "</font>").append(event.date);
                        if (!event.place.equals("")) history_string.append("<br>").append("<font color=\"#FF9088\">" + "МЕСТО: " + "</font>").append(event.place);
                        if (!event.doctor.equals("")) history_string.append("<br>").append("<font color=\"#FF9088\">" + "ДОКТОР: " + "</font>").append(event.doctor);
                        if (!event.comment.equals("")) history_string.append("<br>").append("<font color=\"#FF9088\">" + "КОММЕНТАРИЙ: " + "</font>").append(event.comment);
                        toothList[clicked_tooth].addEvent(history_string.toString());
                        collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).update("event", toothList[clicked_tooth].event);
                        collectionReference.document("events").update("event", FieldValue.arrayUnion(history_string.toString()));
                    });
                    builder.setNegativeButton("ОТМЕНИТЬ", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    AlertDialog addEventDialog = builder.create();
                    addEventDialog.setView(dialogView);
                    addEventDialog.show();
                }));

        toothSpecialitiesButton.setOnClickListener(v -> {

            collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                toothList[clicked_tooth].state = (ArrayList<Long>) document.get("state");

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                builder.setTitle(toothList[clicked_tooth].name.toUpperCase());
                builder.setCancelable(true);
                StringBuilder state_string = new StringBuilder();
                for (int i = 0; i < toothStateList.length; ++i) {
                    if (toothList[clicked_tooth].state.contains((long) i)) state_string.append(toothStateList[i].toUpperCase()).append("\n");
                }
                builder.setMessage(state_string);
                AlertDialog showCurrentStateDialog = builder.create();
                showCurrentStateDialog.show();
            });
        });

        toothHistoryButton.setOnClickListener(v -> {
            collectionReference.document(String.valueOf(toothList[clicked_tooth].number)).get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();
                        toothList[clicked_tooth].event = (ArrayList<String>) document.get("event");
                        //System.out.println(document.toObject(Event.class).doctor);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                        builder.setTitle("ИСТОРИЯ");
                        builder.setCancelable(true);
                        StringBuilder history_string = new StringBuilder();
                        ArrayList<String> reversed = new ArrayList<>(toothList[clicked_tooth].event);
                        Collections.reverse(reversed);
                        for (String event : reversed) {
                            history_string.append("<br><br>").append(event);
                        }
                        builder.setMessage(Html.fromHtml(history_string.toString()));
                        builder.setNegativeButton("ЗАКРЫТЬ", (dialog, which) -> {
                            dialog.dismiss();
                        });
                        AlertDialog showHistoryDialog = builder.create();
                        showHistoryDialog.show();
                    });
        });

    }
}