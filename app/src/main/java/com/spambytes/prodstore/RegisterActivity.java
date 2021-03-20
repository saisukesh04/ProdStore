package com.spambytes.prodstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.emailREditText) EditText emailREditText;
    @BindView(R.id.passwordREditText) EditText passwordREditText;
    @BindView(R.id.confirmPasswordEditText) EditText confirmPasswordEditText;
    @BindView(R.id.filledREmailTextField) TextInputLayout filledREmailTextField;
    @BindView(R.id.filledRPasswordTextField) TextInputLayout filledRPasswordTextField;
    @BindView(R.id.filledConfirmPasswordTextField) TextInputLayout filledConfirmPasswordTextField;
    @BindView(R.id.registerBtn) Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        registerBtn.setOnClickListener(v -> {
            String email = emailREditText.getText().toString();
            String password = passwordREditText.getText().toString();
            String confPassword = confirmPasswordEditText.getText().toString();
            filledREmailTextField.setError(null);
            filledRPasswordTextField.setError(null);
            filledConfirmPasswordTextField.setError(null);
            if (email.isEmpty()) {
                filledREmailTextField.setError("Field cannot be empty");
            }else if (password.isEmpty()){
                filledRPasswordTextField.setError("Field cannot be empty");
            }else if (password.length() < 8){
                filledRPasswordTextField.setError("Password should contain at least 8 characters");
            }else if (confPassword.isEmpty()){
                filledConfirmPasswordTextField.setError("Field cannot be empty");
            }else if (!confPassword.equals(password)){
                filledConfirmPasswordTextField.setError("Passwords don't match");
            }else{
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration successful. Logging in", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }else{
                                Toast.makeText(RegisterActivity.this, "Registration failed. Try again", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }
}