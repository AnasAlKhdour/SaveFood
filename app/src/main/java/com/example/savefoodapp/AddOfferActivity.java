package com.example.savefoodapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.savefoodapp.utils.ImageUtils;

public class AddOfferActivity extends AppCompatActivity {

    private EditText etFoodName;
    private EditText etQuantity;
    private EditText etDescription;
    private EditText etExpiryDate;

    private Button btnCreateOffer;
    private Button btnCancel;
    private ImageView imgOfferPhoto;
    private Button btnTakePhoto;
    private String currentPhotoPath = null;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_offer);

        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etDescription = findViewById(R.id.etDescription);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        imgOfferPhoto = findViewById(R.id.imgOfferPhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        btnCreateOffer = findViewById(R.id.btnCreateOffer);
        btnCancel = findViewById(R.id.btnCancel);

        dbAdapter = new DBAdapter(this);
        sessionManager = new SessionManager(this);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        openCamera();
                    } else {
                        Toast.makeText(
                                this,
                                "Camera permission is required",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        showPhotoPreview();
                    } else {
                        ImageUtils.deleteImage(currentPhotoPath);
                        currentPhotoPath = null;
                    }
                }
        );

        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());

        btnCreateOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOffer();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void createOffer() {

        String foodName = etFoodName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Food name validation
        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Please enter food name");
            etFoodName.requestFocus();
            return;
        }

        // Quantity validation
        if (TextUtils.isEmpty(quantityText)) {
            etQuantity.setError("Please enter quantity");
            etQuantity.requestFocus();
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid quantity");
            etQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }

        // Expiry date validation
        if (TextUtils.isEmpty(expiryDate)) {
            etExpiryDate.setError("Please enter expiry date");
            etExpiryDate.requestFocus();
            return;
        }

        // Get logged-in user's email
        String userEmail = sessionManager.getUserEmail();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(
                    this,
                    "User session not found",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Get current user from database
        dbAdapter.open();

        User user = dbAdapter.getUser(userEmail);

        if (user == null) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    "User not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int organizationId = user.getOrganizationId();

        if (organizationId <= 0) {

            long newOrgId = dbAdapter.insertFoodOrganization(
                    user.getName(),
                    "-",
                    "-"
            );

            if (newOrgId == -1) {

                dbAdapter.close();

                Toast.makeText(
                        this,
                        "Failed to create food organization",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            int updated = dbAdapter.updateUserOrganizationId(
                    userEmail,
                    (int) newOrgId
            );

            if (updated == 0) {

                dbAdapter.close();

                Toast.makeText(
                        this,
                        "Failed to link organization",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            organizationId = (int) newOrgId;
        }
        // Create FoodDonation object
        FoodDonation donation = new FoodDonation(
                0,
                organizationId,
                foodName,
                quantity,
                description,
                expiryDate,
                "AVAILABLE",
                currentPhotoPath
        );

        // Insert donation
        long donationId = dbAdapter.insertFoodDonation(donation);

        dbAdapter.close();

        if (donationId == -1) {

            Toast.makeText(
                    this,
                    "Failed to create offer",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Offer created successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();

            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            cameraLauncher.launch(photoUri);

        } catch (IOException e) {
            Toast.makeText(
                    this,
                    "Failed to create image file",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
        ).format(new Date());

        String fileName = "OFFER_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
        );

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                fileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void showPhotoPreview() {

        if (currentPhotoPath == null) return;

        Bitmap bitmap = ImageUtils.decodeSampled(
                currentPhotoPath,
                800,
                800
        );

        if (bitmap != null) {
            imgOfferPhoto.setImageBitmap(bitmap);
        }
    }
}