package com.spambytes.prodstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText email = findViewById(R.id.emailREditText);
        EditText password = findViewById(R.id.passwordREditText);
        EditText confirmPassword = findViewById(R.id.confirmPasswordEditText);
        TextInputLayout emailLayout = findViewById(R.id.filledREmailTextField);
        TextInputLayout passwordLayout = findViewById(R.id.filledRPasswordTextField);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.filledConfirmPasswordTextField);
        Button register = findViewById(R.id.registerBtn);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLayout.setError(null);
                passwordLayout.setError(null);
                confirmPasswordLayout.setError(null);
                if (email.getText().toString().isEmpty()){
                    emailLayout.setError("Field cannot be empty");
                }else if (password.getText().toString().isEmpty()){
                    passwordLayout.setError("Field cannot be empty");
                }else if (password.getText().toString().length() < 8){
                    passwordLayout.setError("Password should contain at least 8 characters");
                }else if (confirmPassword.getText().toString().isEmpty()){
                    confirmPasswordLayout.setError("Field cannot be empty");
                }else if (!confirmPassword.getText().toString().equals(password.getText().toString())){
                    confirmPasswordLayout.setError("Passwords don't match");
                }else{
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), confirmPassword.getText().toString())
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Registration successful. Logging in", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(RegisterActivity.this, "Registration failed. Try again", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

    }
}