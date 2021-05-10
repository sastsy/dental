package ru.sastsy.dental;

import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


public class StatsFragment extends Fragment {

    TextView textView;


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
        textView = view.findViewById(R.id.textViewOfEvents);
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

            System.out.println(eventList);

            for (String event : reversed) {
                /*textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                textView.setText(Html.fromHtml(event));
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.textview_rect));
                textView.setPadding(10, 20, 10, 0);
                linearLayout.addView(textView);*/
                textView.append("\n");
                textView.append(Html.fromHtml(event));
                textView.append("\n");
                System.out.println("Set!!!!");
            }
        });


        /*for (int i = 0; i < toothStateList.length; ++i) {
            collectionReference.document("stats").update(String.valueOf(i), 0);
            for (int j = 0; j < 32; ++j) {
                int finalI = i;
                collectionReference.document(String.valueOf(j)).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Long> stateList = (ArrayList<Long>) document.get("state");
                    if (stateList.contains((long) finalI)) collectionReference.document("stats").update(String.valueOf(finalI), FieldValue.increment(1));
                });
            }
        }*/
    }
}