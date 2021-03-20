package com.spambytes.prodstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", firebaseAuth.getCurrentUser());
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_login);


        Button login = findViewById(R.id.loginButton);
        Button register = findViewById(R.id.registerButton);
        EditText email = findViewById(R.id.emailEditText);
        EditText password = findViewById(R.id.passwordEditText);
        TextInputLayout emailLayout = findViewById(R.id.filledEmailTextField);
        TextInputLayout passwordLayout = findViewById(R.id.filledPasswordTextField);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLayout.setError(null);
                passwordLayout.setError(null);
                if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    emailLayout.setError(" ");
                                    passwordLayout.setError("Invalid username or password");
                                }
                            });
                }else if (email.getText().toString().isEmpty()){
                    emailLayout.setError("Field cannot be empty");
                }else if (password.getText().toString().isEmpty()) {
                    passwordLayout.setError("Field cannot be empty");
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }
}