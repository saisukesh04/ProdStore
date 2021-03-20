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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity {

    private EditText inputProductName, textMFD, textBB, textQuantity;
    private TextInputLayout nameLayout, bbLayout, mfdLayout, quantityLayout;
    private View statusIndicator;
    private String selectColor, selectedImagePath, barcode, productName;
    private ImageView imageView;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1, REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        createNotificationChannel();

        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");

        inputProductName = findViewById(R.id.inputProductName);
        textMFD = findViewById(R.id.textDateTime);
        textQuantity = findViewById(R.id.quantityEditText);
        textBB = findViewById(R.id.textBB);
        imageView = findViewById(R.id.imageView);
        statusIndicator = findViewById(R.id.statusIndicator);
        nameLayout = findViewById(R.id.filledNameTextField);
        bbLayout = findViewById(R.id.filledBBTextField);
        mfdLayout = findViewById(R.id.filledDateTextField);
        quantityLayout = findViewById(R.id.filledQuantityTextField);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Don't save to database
                startActivity(new Intent(DetailsActivity.this, MainActivity.class));
                finish();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.save) {
                    saveDetail();
                    return true;
                }
                return false;
            }
        });

        textMFD.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            textMFD.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        selectColor = "#333333";
        selectedImagePath = "";

        //intMiscellaneous();
        setStatusIndicator();

        //Let productName be the string of the product name obtained from the database
        if (productName == null) {
            //Entry doesn't exist in the database, ask the user to enter the data
            inputProductName.setCursorVisible(true);
            inputProductName.setFocusable(true);
            inputProductName.setClickable(true);
            textBB.setCursorVisible(true);
            textBB.setFocusable(true);
            textBB.setClickable(true);
            inputProductName.requestFocus();
            nameLayout.setError("Product details not found, kindly enter it manually");
        }

    }

    private void saveDetail() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(inputProductName.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(textBB.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(textQuantity.getWindowToken(), 0);
        inputProductName.clearFocus();
        textBB.clearFocus();
        textQuantity.clearFocus();
        nameLayout.setError(null);
        bbLayout.setError(null);
        mfdLayout.setError(null);
        quantityLayout.setError(null);

        if (inputProductName.getText().toString().trim().isEmpty()) {
            nameLayout.setError("Product name cannot be empty");
        } else if (textMFD.getText().toString().isEmpty()) {
            mfdLayout.setError("Manufacturing date cannot be empty");
        } else if (textBB.getText().toString().trim().isEmpty()) {
            bbLayout.setError("Best before date cannot be empty");
        } else if (textQuantity.getText().toString().equals("0") || textQuantity.getText().toString().isEmpty()) {
            quantityLayout.setError("Quantity cannot be empty or zero");
        } else {
            //Get product name and best before from database using barcode variable
            //Check is same product with same MFD exists
            productName = inputProductName.getText().toString();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date mfDate = sdf.parse(textMFD.getText().toString());
                int bestBefore = 5;
                Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mfDate);
                calendar.add(Calendar.DAY_OF_MONTH, bestBefore);

                Date expDate = sdf.parse(sdf.format(calendar.getTime()));

                Intent intent = new Intent(DetailsActivity.this, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(DetailsActivity.this, (int) System.currentTimeMillis(), intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                //Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, expDate.getYear());
                calendar.set(Calendar.MONTH, expDate.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, expDate.getDate());
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            int quantity = Integer.parseInt(textQuantity.getText().toString());
            //Save it to database and OnSuccess move to MainActivity
            startActivity(new Intent(DetailsActivity.this, MainActivity.class));
            finish();
        }
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
        super.onBackPressed();
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