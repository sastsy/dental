package ru.sastsy.dental;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class StatsFragment extends Fragment {

    TextView textViewEvents, statView;


    public StatsFragment() {
        // Required empty public constructor
    }

    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        textViewEvents = view.findViewById(R.id.textViewOfEvents);
        statView = view.findViewById(R.id.textViewOfStats);
        textViewEvents.setMovementMethod(new ScrollingMovementMethod());
        statView.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        String userID = fAuth.getCurrentUser().getUid();
        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth");



        String[] toothStateList = getResources().getStringArray(R.array.tooth_state);

        collectionReference.document("events").get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            ArrayList<String> eventList = (ArrayList<String>) document.get("event");
            ArrayList<String> reversed = new ArrayList<>(eventList);
            Collections.reverse(reversed);
            //if (eventList.isEmpty()) textView.setText();
            for (String event : reversed) {
                /*textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                textView.setText(Html.fromHtml(event));
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.textview_rect));
                textView.setPadding(10, 20, 10, 0);
                linearLayout.addView(textView);*/
                textViewEvents.append("\n");
                textViewEvents.append(Html.fromHtml(event));
                textViewEvents.append("\n");
            }
        });


        /*for (int i = 0; i < toothStateList.length; ++i) {
            collectionReference.document("stats").update(String.valueOf(i), 0);
            for (int j = 1; j <= 32; ++j) {
                int finalI = i;
                collectionReference.document(String.valueOf(j)).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Long> stateList = (ArrayList<Long>) document.get("state");
                    if (stateList.contains((long) finalI)) collectionReference.document("stats").update(String.valueOf(finalI), FieldValue.increment(1));
                });
            }
        }*/
        //long[] numericList = new long[15];
        ArrayList<Long> numericList = new ArrayList<>(Arrays.asList(new Long[15]));
        Collections.fill(numericList, (long) 0);
        for (int i = 1; i <= 32; ++i) {
            collectionReference.document(String.valueOf(i)).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                ArrayList<Long> stateList = (ArrayList<Long>) document.get("state");
                //System.out.println(stateList);
                for (int j = 0; j < toothStateList.length; ++j) {
                    if (stateList.contains((long) j)) numericList.set(j, numericList.get(j) + 1);
                }
                collectionReference.document("stats").update("statistics", numericList);
            });
        }

        //System.out.println(Arrays.toString(numericList));

        collectionReference.document("stats").get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            ArrayList<Long> stateList = (ArrayList<Long>) document.get("statistics");
            for (int i = 0; i <= toothStateList.length; ++i) {
                if (stateList.get(i) != (long) 0) statView.append(Html.fromHtml("<br>" + "<font color=\"#FF9088\">" + toothStateList[i] + ": " + "</font>" + stateList.get(i) + "<br>"));
                //System.out.println(Arrays.toString(numericList));
            }
        });
    }
}