package com.spambytes.prodstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.spambytes.prodstore.database.ItemDatabase;
import com.spambytes.prodstore.database.ProductDatabase;
import com.spambytes.prodstore.models.Item;
import com.spambytes.prodstore.models.Product;

import org.bson.Document;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

import static com.spambytes.prodstore.MainActivity.app;

public class DetailsActivity extends AppCompatActivity {

    @BindView(R.id.barcodeText) EditText barcodeText;
    @BindView(R.id.inputProductName) EditText inputProductName;
    @BindView(R.id.textMFD) EditText textMFD;
    @BindView(R.id.textBestBefore) EditText textBestBefore;
    @BindView(R.id.quantityText) EditText quantityText;
    @BindView(R.id.statusIndicator) View statusIndicator;
    @BindView(R.id.filledNameField) TextInputLayout nameLayout;
    @BindView(R.id.filledBBTextField) TextInputLayout bbLayout;
    @BindView(R.id.filledMFDField) TextInputLayout mfdLayout;
    @BindView(R.id.filledQuantityField) TextInputLayout quantityLayout;
    @BindView(R.id.saveButton) Button saveButton;

    private String barcode, selectColor, selectedImagePath;
    private boolean productExists;
    private ImageView imageView;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1, REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        createNotificationChannel();

        ButterKnife.bind(this);

        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");

//        imageView = findViewById(R.id.imageView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeText.setText(barcode);

        toolbar.setNavigationOnClickListener(v -> {
            //Don't save to database
            startActivity(new Intent(DetailsActivity.this, MainActivity.class));
            finish();
        });

        textMFD.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsActivity.this, (view, year, monthOfYear, dayOfMonth) ->
                    textMFD.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year), mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        selectColor = "#333333";
        selectedImagePath = "";

        ProductDatabase productDb = ProductDatabase.getInstance(this);
        String productName = productDb.ProductDao().fetchProduct(barcode);

        if (productName == null) {
            //Entry doesn't exist in the database, ask the user to enter the data
            inputProductName.setCursorVisible(true);
            inputProductName.setClickable(true);
            inputProductName.requestFocus();
            nameLayout.setError("Product details not found, kindly enter it manually");
            productExists = false;
        } else {
            productExists = true;
            inputProductName.setText(productName);
            inputProductName.setEnabled(false);
            textBestBefore.setEnabled(false);
            textBestBefore.setText(String.valueOf(productDb.ProductDao().fetchBestBefore(barcode)));
            selectColor = "#FDBE3B";
            setStatusIndicator();
        }

        saveButton.setOnClickListener(view -> saveDetail());

//        intMiscellaneous();
//        setStatusIndicator();
    }

    private void saveDetail() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(inputProductName.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(textBestBefore.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(quantityText.getWindowToken(), 0);
        inputProductName.clearFocus();
        textBestBefore.clearFocus();
        quantityText.clearFocus();
        nameLayout.setError(null);
        bbLayout.setError(null);
        mfdLayout.setError(null);
        quantityLayout.setError(null);

        String productName = inputProductName.getText().toString();
        String quantity = quantityText.getText().toString();
        String manufactureDate = textMFD.getText().toString();
        String bestBefore = textBestBefore.getText().toString();

        if (productName.trim().isEmpty())
            nameLayout.setError("Product name cannot be empty");
        else if (manufactureDate.isEmpty())
            mfdLayout.setError("Manufacturing date cannot be empty");
        else if (bestBefore.trim().isEmpty())
            bbLayout.setError("Best before date cannot be empty");
        else if (quantity.equals("0") || quantity.isEmpty())
            quantityLayout.setError("Quantity cannot be empty or zero");
        else {
            //Get product name and best before from database using barcode variable
            if (!productExists)
                saveProductToMongo(barcode, productName, bestBefore);

            Date expDate = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date mfDate = sdf.parse(manufactureDate);
//                int bestBefore = 5;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mfDate);
                calendar.add(Calendar.DAY_OF_MONTH, (Integer.parseInt(bestBefore)*30));

                expDate = sdf.parse(sdf.format(calendar.getTime()));

                Intent intent = new Intent(DetailsActivity.this, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(DetailsActivity.this, (int) System.currentTimeMillis(), intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                //Calendar calendar = Calendar.getInstance();
                calendar.setTime(expDate);
                calendar.add(Calendar.DAY_OF_MONTH, -7);

                Date remDate = sdf.parse(sdf.format(calendar.getTime()));

                calendar.setTime(remDate);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            Item item = new Item(productName, String.valueOf(expDate), Integer.parseInt(quantity));
            //Save it to database and OnSuccess move to MainActivity
            ItemDatabase itemDb = ItemDatabase.getInstance(DetailsActivity.this);
            itemDb.ItemDao().insertItem(item);
            Toast.makeText(DetailsActivity.this, "Item added to database",Toast.LENGTH_LONG).show();
            startActivity(new Intent(DetailsActivity.this, MainActivity.class));
            finish();
        }
    }

    private void saveProductToMongo(String barcodeText, String productName, String bestBefore) {
        app.loginAsync(Credentials.anonymous(), new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if (!result.isSuccess()) {
                    Toast.makeText(DetailsActivity.this, "There is an error connecting to database", Toast.LENGTH_LONG).show();
                    Log.e("Info", result.getError().toString());
                } else {
                    User user = app.currentUser();
                    Log.e("Info", "User: " + user);
                    MongoClient mongoClient = user.getMongoClient("mongodb-atlas");
                    MongoDatabase mongoDatabase = mongoClient.getDatabase("ProdStoreDB");
                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("User_Item_List");

                    mongoCollection.insertOne(new Document("userId", user.getId())
                            .append("barcode", barcodeText).append("productName", productName)
                            .append("bestBefore", bestBefore))
                            .getAsync(docResult -> {
                                if (docResult.isSuccess()) {
                                    Toast.makeText(DetailsActivity.this, "Added to database", Toast.LENGTH_LONG).show();
                                    Product product = new Product(Long.parseLong(barcodeText), productName, Integer.parseInt(bestBefore));
                                    ProductDatabase.getInstance(DetailsActivity.this).ProductDao().insertProduct(product);
                                    updateItemCountMongo(mongoDatabase);
                                }else {
                                    Toast.makeText(DetailsActivity.this, "Error: " + docResult.getError(), Toast.LENGTH_LONG).show();
                                    Log.e("Error", docResult.getError().toString());
                                }
                            });
                }
            }
        });
    }

    public void updateItemCountMongo(MongoDatabase mongoDatabase) {

        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("User_Item_List");
        Document queryFilter = new Document().append("barcode","8906086572108");

        RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(queryFilter).iterator();
        findTask.getAsync(task -> {
            if(task.isSuccess()){

                MongoCursor<Document> results = task.get();
                if(results.hasNext()) {
                    Document result = results.next();
                    int count = Integer.parseInt(String.valueOf(result.get("count")));
                    result.append("count", String.valueOf(count + 1));

                    mongoCollection.updateOne(queryFilter, result).getAsync(result1 -> {
                        if(result1.isSuccess())
                            Log.e("Info","Updated");
                        else
                            Log.e("Error", result1.getError().toString());
                    });
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReminderChannel";
            String description = "Channel for reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyUs", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void intMiscellaneous() {
        final LinearLayout layoutMiscellenous = findViewById(R.id.layoutMiscellenous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellenous);
        layoutMiscellenous.findViewById(R.id.textMiscellenous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMiscellenous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellenous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellenous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellenous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellenous.findViewById(R.id.imageColor5);

        layoutMiscellenous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setStatusIndicator();

            }
        });
        layoutMiscellenous.findViewById(R.id.viewColor2).setOnClickListener(v -> {
            selectColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setStatusIndicator();
        });

        layoutMiscellenous.findViewById(R.id.viewColor3).setOnClickListener(v -> {
            selectColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setStatusIndicator();
        });

        layoutMiscellenous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColor = "#3A52Fc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setStatusIndicator();
            }
        });

        layoutMiscellenous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setStatusIndicator();

            }
        });

        layoutMiscellenous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DetailsActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }


            }
        });
    }

    private void setStatusIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) statusIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectColor));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);

                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(DetailsActivity.this)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setTitle("Save product?")
                .setMessage("If you discard, you won't be reminded if this product expires")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveDetail();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Don't save to database
                        startActivity(new Intent(DetailsActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
}