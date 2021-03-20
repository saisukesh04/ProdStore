package com.spambytes.prodstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @BindView(R.id.imageAddButton)ImageView addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);

        addButton.setOnClickListener(v -> {
            /*final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.enterthedetailsactivity);
            dialog.show();*/
            /*IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Scan a barcode");
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();*/
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.mannualentrydialog);
            Button dialogButton = dialog.findViewById(R.id.mannualFindButton);
            EditText barcodeEditText = dialog.findViewById(R.id.barcodeEditText);
            TextInputLayout barcodeLayout = dialog.findViewById(R.id.filledBarcodeTextField);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    barcodeLayout.setError(null);
                    if (barcodeEditText.getText().toString().isEmpty() || barcodeEditText.getText().toString().length() != 13) {
                        barcodeLayout.setError("Enter a valid 13-digit barcode");
                    } else {
                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(barcodeEditText.getWindowToken(), 0);
                        barcodeEditText.clearFocus();
                        Intent intent = new Intent(MainActivity.this, EnterTheDetailsActivity.class);
                        intent.putExtra("barcode", barcodeEditText.getText().toString());
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                Intent intent = new Intent(MainActivity.this, EnterTheDetailsActivity.class);
                intent.putExtra("barcode", intentResult.getContents());
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}