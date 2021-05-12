package ru.sastsy.dental;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ru.sastsy.dental.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private int clicked_tooth = 0;
    private Tooth[] toothList = new Tooth[32];
    private ImageButton[] imageButtonsList = new ImageButton[33];
    private String[] toothStateList;
    private boolean[] checkedState;
    private ArrayList<Integer> copylistOfState = new ArrayList<>();
    private Event[] eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnNavigationItemSelectedListener(navListener);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        navView.setSelectedItemId(R.id.navigation_teeth);
        getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new TeethFragment()).commit();

        String userID = fAuth.getCurrentUser().getUid();

        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth");
        collectionReference.document("1").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i <= 31; ++i) {
                        toothList[i] = new Tooth(i + 1);
                        map.put("name", toothList[i].name);
                        map.put("number", toothList[i].number);
                        map.put("state", toothList[i].state);
                        map.put("event", toothList[i].event);
                        collectionReference.document(String.valueOf(toothList[i].number)).set(map, SetOptions.merge());
                    }
                    map.clear();
                    for (int i = 0; i < getResources().getStringArray(R.array.tooth_state).length; ++i) {
                        map.put(String.valueOf(i), 0);
                    }
                    collectionReference.document("stats").set(map);
                    Map<String, ArrayList<String>> map2 = new HashMap<>();
                    map2.put("event", new ArrayList<>());
                    collectionReference.document("events").set(map2);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        assert fragment != null;
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        switch(item.getItemId()) {
            case R.id.navigation_teeth:
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new TeethFragment()).commit();
                break;
            case R.id.navigation_stats:
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new StatsFragment()).commit();
                break;
            case R.id.navigation_camera:
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new CameraFragment()).commit();
                break;
            case R.id.navigation_exit:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }
        return true;
    };

}
