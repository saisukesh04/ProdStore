package com.spambytes.prodstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.loginButton) Button loginButton;
    @BindView(R.id.registerButton) Button registerButton;
    @BindView(R.id.emailEditText) EditText emailEditText;
    @BindView(R.id.passwordEditText) EditText passwordEditText;
    @BindView(R.id.filledEmailTextField) TextInputLayout filledEmailTextField;
    @BindView(R.id.filledPasswordTextField) TextInputLayout filledPasswordTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", firebaseAuth.getCurrentUser());
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            filledEmailTextField.setError(null);
            filledPasswordTextField.setError(null);
            if (!email.isEmpty() && !password.isEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }).addOnFailureListener(e -> {
                    filledEmailTextField.setError(" ");
                    filledPasswordTextField.setError("Invalid username or password");

                });
            } else if (emailEditText.getText().toString().isEmpty()) {
                filledEmailTextField.setError("Field cannot be empty");
            } else if (passwordEditText.getText().toString().isEmpty()) {
                filledPasswordTextField.setError("Field cannot be empty");
            }
        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}