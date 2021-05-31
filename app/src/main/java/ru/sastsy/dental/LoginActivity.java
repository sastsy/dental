package ru.sastsy.dental;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLoginBtn;
    CircularProgressIndicator progressBar;
    TextView mCreateBtn, forgotTextLink;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.email_signin);
        mPassword = findViewById(R.id.password_signin);
        progressBar = findViewById(R.id.progress_circular);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.signin);
        mCreateBtn = findViewById(R.id.createText);
        forgotTextLink = findViewById(R.id.forgotPassword);

        if (fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mLoginBtn.setOnClickListener(v -> {

            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)){
                mEmail.setError("Требуется email");
                return;
            }

            if (TextUtils.isEmpty(password)){
                mPassword.setError("Требуется пароль");
                return;
            }

            if (password.length() < 6){
                mPassword.setError("Пароль должен быть не менее 6 символов");
                return;
            }

            if (fAuth.getCurrentUser() != null && !fAuth.getCurrentUser().isEmailVerified()) {
                Toast.makeText(LoginActivity.this, "Email не подтвержден!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Вы успешно авторизовались", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Ошибка!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            });

        });

        mCreateBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));

        forgotTextLink.setOnClickListener(v -> {

            final EditText resetMail = new EditText(v.getContext());
            final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Восстановить пароль?");
            passwordResetDialog.setMessage("Введите email, чтобы получить ссылку для восстановления");
            passwordResetDialog.setView(resetMail);

            passwordResetDialog.setPositiveButton("ДА", (dialog, which) -> {
                // extract the email and send reset link
                String mail = resetMail.getText().toString();
                fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show()).
                        addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show());

            });

            passwordResetDialog.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });

            passwordResetDialog.create().show();

        });
    }
}