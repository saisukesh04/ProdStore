package com.spambytes.prodstore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.spambytes.prodstore.adapters.ItemAdapter;
import com.spambytes.prodstore.database.ItemDatabase;
import com.spambytes.prodstore.models.Item;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class MainActivity extends AppCompatActivity {

    public static App app;
    private FirebaseAuth mAuth;
    final private String AppID = "prodstoredb-gqobq";
    private EditText barcodeEditText;

    @BindView(R.id.imageAddButton) ImageView addButton;
    @BindView(R.id.logoutBtn) ImageView logoutBtn;
    @BindView(R.id.listRecyclerView) RecyclerView listRecyclerView;
    public static TextView no_items_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        Realm.init(this);

        app = new App(new AppConfiguration.Builder(AppID).build());

        List<Item> items = ItemDatabase.getInstance(MainActivity.this).ItemDao().loadAllItems();

        LinearLayoutManager mLayout = new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false);
        ItemAdapter itemAdapter = new ItemAdapter(items);

        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(mLayout);
        listRecyclerView.setAdapter(itemAdapter);

        no_items_text = findViewById(R.id.no_items_text);
        if(items.size() == 0)
            no_items_text.setVisibility(View.VISIBLE);

        addButton.setOnClickListener(v -> {

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.manual_entry_dialog);
            Button findProductBtn = dialog.findViewById(R.id.findProductBtn);
            TextInputLayout barcodeLayout = dialog.findViewById(R.id.filledBarcodeTextField);
            ImageView scan_barcode = dialog.findViewById(R.id.scan_barcode);
            barcodeEditText = dialog.findViewById(R.id.barcodeEditText);
            // if button is clicked, close the custom dialog
            findProductBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    barcodeLayout.setError(null);
                    if (barcodeEditText.getText().toString().isEmpty() || barcodeEditText.getText().toString().length() != 13) {
                        barcodeLayout.setError("Enter a valid 13-digit barcode");
                    } else {
                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(barcodeEditText.getWindowToken(), 0);
                        barcodeEditText.clearFocus();
                        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                        intent.putExtra("barcode", barcodeEditText.getText().toString());
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }
            });
            scan_barcode.setOnClickListener(view -> {
                IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                intentIntegrator.setPrompt("Scan a barcode");
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.initiateScan();
            });
            dialog.show();
        });

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
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
                // if the intentResult is not null we'll set the content and format of scan message
                barcodeEditText.setText(intentResult.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}