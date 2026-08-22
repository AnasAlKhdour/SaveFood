package com.example.savefoodapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.ImageUtils;
import com.example.savefoodapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_offer);

        // ------------------------------------------------
        // Connect UI
        // ------------------------------------------------

        etFoodName =
                findViewById(R.id.etFoodName);

        etQuantity =
                findViewById(R.id.etQuantity);

        etDescription =
                findViewById(R.id.etDescription);

        etExpiryDate =
                findViewById(R.id.etExpiryDate);

        imgOfferPhoto =
                findViewById(R.id.imgOfferPhoto);

        btnTakePhoto =
                findViewById(R.id.btnTakePhoto);

        btnCreateOffer =
                findViewById(R.id.btnCreateOffer);

        btnCancel =
                findViewById(R.id.btnCancel);

        dbAdapter =
                new DBAdapter(this);

        sessionManager =
                new SessionManager(this);


        // ------------------------------------------------
        // Camera Permission
        // ------------------------------------------------

        cameraPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        granted -> {

                            if (granted) {

                                openCamera();

                            } else {

                                Toast.makeText(
                                        this,
                                        R.string.camera_permission_required,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );


        // ------------------------------------------------
        // Camera Result
        // ------------------------------------------------

        cameraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.TakePicture(),
                        success -> {

                            if (success) {

                                showPhotoPreview();

                            } else {

                                ImageUtils.deleteImage(
                                        currentPhotoPath
                                );

                                currentPhotoPath = null;
                            }
                        }
                );


        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        btnTakePhoto.setOnClickListener(
                v -> checkCameraPermissionAndOpen()
        );

        btnCreateOffer.setOnClickListener(
                v -> createOffer()
        );

        btnCancel.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // CREATE OFFER
    // =========================================================

    private void createOffer() {

        String foodName =
                etFoodName.getText()
                        .toString()
                        .trim();

        String quantityText =
                etQuantity.getText()
                        .toString()
                        .trim();

        String description =
                etDescription.getText()
                        .toString()
                        .trim();

        String expiryDate =
                etExpiryDate.getText()
                        .toString()
                        .trim();


        // ------------------------------------------------
        // Food Name Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(foodName)) {

            etFoodName.setError(
                    getString(R.string.enter_food_name)
            );

            etFoodName.requestFocus();

            return;
        }


        // ------------------------------------------------
        // Quantity Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(quantityText)) {

            etQuantity.setError(
                    getString(R.string.enter_quantity)
            );

            etQuantity.requestFocus();

            return;
        }

        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            etQuantity.setError(
                    getString(R.string.valid_quantity)
            );

            etQuantity.requestFocus();

            return;
        }

        if (quantity <= 0) {

            etQuantity.setError(
                    getString(
                            R.string.quantity_greater_than_zero
                    )
            );

            etQuantity.requestFocus();

            return;
        }


        // ------------------------------------------------
        // Expiry Date Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(expiryDate)) {

            etExpiryDate.setError(
                    getString(
                            R.string.enter_expiry_date
                    )
            );

            etExpiryDate.requestFocus();

            return;
        }


        // ------------------------------------------------
        // Get Logged-in User
        // ------------------------------------------------

        String userEmail =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(userEmail)) {

            Toast.makeText(
                    this,
                    R.string.user_session_not_found,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ------------------------------------------------
        // Open Database
        // ------------------------------------------------

        dbAdapter.open();


        User user =
                dbAdapter.getUser(userEmail);

        if (user == null) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    R.string.user_not_found,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ------------------------------------------------
        // Get Existing Organization
        // ------------------------------------------------

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    R.string.organization_not_found,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        /*
         * IMPORTANT:
         *
         * We do NOT ask for the location here.
         *
         * The organization location was already selected
         * during registration and saved inside
         * food_organizations.
         *
         * The offer only needs the organization ID.
         */


        // ------------------------------------------------
        // Create Food Donation
        // ------------------------------------------------

        FoodDonation donation =
                new FoodDonation(
                        0,
                        organizationId,
                        foodName,
                        quantity,
                        description,
                        expiryDate,
                        "AVAILABLE"
                );


        // ------------------------------------------------
        // Insert Donation
        // ------------------------------------------------

        long donationId =
                dbAdapter.insertFoodDonation(
                        donation
                );

        dbAdapter.close();


        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (donationId == -1) {

            Toast.makeText(
                    this,
                    R.string.failed_create_offer,
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    R.string.offer_created_successfully,
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }


    // =========================================================
    // CAMERA
    // =========================================================

    private void checkCameraPermissionAndOpen() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }


    private void openCamera() {

        try {

            File photoFile =
                    createImageFile();

            Uri photoUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName()
                                    + ".fileprovider",
                            photoFile
                    );

            cameraLauncher.launch(
                    photoUri
            );

        } catch (IOException e) {

            Toast.makeText(
                    this,
                    R.string.failed_create_image_file,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    private File createImageFile()
            throws IOException {

        String timeStamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.US
                ).format(new Date());

        String fileName =
                "OFFER_" + timeStamp + "_";

        File storageDir =
                getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                );

        if (storageDir != null
                && !storageDir.exists()) {

            storageDir.mkdirs();
        }

        File image =
                File.createTempFile(
                        fileName,
                        ".jpg",
                        storageDir
                );

        currentPhotoPath =
                image.getAbsolutePath();

        return image;
    }


    private void showPhotoPreview() {

        if (currentPhotoPath == null) {
            return;
        }

        Bitmap bitmap =
                ImageUtils.decodeSampled(
                        currentPhotoPath,
                        800,
                        800
                );

        if (bitmap != null) {

            imgOfferPhoto.setImageBitmap(
                    bitmap
            );
        }
    }
}