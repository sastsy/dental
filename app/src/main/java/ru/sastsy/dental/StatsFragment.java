package ru.sastsy.dental;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class StatsFragment extends Fragment {

    private TextView textViewEvents;
    private PieChart pieChart;
    private static final int TEETH_NUMBER = 32;


    public StatsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        textViewEvents = view.findViewById(R.id.textViewOfEvents);
        pieChart = view.findViewById(R.id.pieChart);
        textViewEvents.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get user info to access the db
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        String userID = fAuth.getCurrentUser().getUid();
        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth");

        String[] toothStateList = getResources().getStringArray(R.array.tooth_state); // List with all possible options for tooth status

        collectionReference.document("events").get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            ArrayList<String> eventList = (ArrayList<String>) document.get("event");
            if (eventList != null) {
                ArrayList<String> reversed = new ArrayList<>(eventList);
                Collections.reverse(reversed);
                for (String event : reversed) {
                    textViewEvents.append("\n");
                    textViewEvents.append(Html.fromHtml(event)); // Convert string from Html in order to color only specific parts of the text
                    textViewEvents.append("\n");
                }
            }
        });

        ArrayList<Long> numericList = new ArrayList<>(Arrays.asList(new Long[15])); // List to store number of teeth with each status option
        Collections.fill(numericList, (long) 0);
        for (int i = 1; i <= TEETH_NUMBER; ++i) {
            collectionReference.document(String.valueOf(i)).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                ArrayList<Long> stateList = (ArrayList<Long>) document.get("state"); // Get tooth status from the db
                if (stateList != null) {
                    for (int j = 0; j < toothStateList.length; ++j) {
                        if (stateList.contains((long) j)) numericList.set(j, numericList.get(j) + 1); // Count how many teeth have each status option
                    }
                    collectionReference.document("stats").update("statistics", numericList); // Update overall tooth statuses count list to db
                }
            });
        }

        collectionReference.document("stats").get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            ArrayList<Long> stateList = (ArrayList<Long>) document.get("statistics");
            if (stateList != null) {
                ArrayList<PieEntry> entries = new ArrayList<>();
                for (int i = 0; i <= toothStateList.length; ++i) {
                    // If option has cases, show the number of teeth for it
                    if (stateList.get(i) != (long) 0) entries.add(new PieEntry(stateList.get(i), toothStateList[i].toUpperCase()));
                }
                setupPieChart();
                loadPieChartData(entries);
            }
        });
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("ЗУБЫ");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
    }

    private void loadPieChartData(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.JOYFUL_COLORS) {
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }


        PieDataSet dataSet = new PieDataSet(entries, "Зубы");
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new MyValueFormatter());

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);

        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    private static class MyValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }
}