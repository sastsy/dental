package ru.sastsy.dental;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
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

        Map<String, Object> user = new HashMap<>();

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        String userID = fAuth.getCurrentUser().getUid();

        CollectionReference collectionReference = fStore.collection("users").document(userID).collection("teeth");
        collectionReference.document("1").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    for (int i = 0; i <= 31; ++i) {
                        toothList[i] = new Tooth(i + 1);
                        user.put("name", toothList[i].name);
                        user.put("number", toothList[i].number);
                        user.put("state", toothList[i].state);
                        user.put("event", toothList[i].event);
                        collectionReference.document(String.valueOf(toothList[i].number)).set(user, SetOptions.merge());
                    }
                }
            }
        });
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
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new ExitFragment()).commit();
                break;
        }
        return true;
    };

}
