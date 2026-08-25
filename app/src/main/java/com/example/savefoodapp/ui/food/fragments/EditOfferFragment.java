package com.example.savefoodapp.ui.food.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.image.ImageUtils;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditOfferFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private EditText etFoodName;
    private EditText etQuantity;
    private EditText etDescription;
    private EditText etExpiryDate;

    private MaterialButton btnViewImage;
    private MaterialButton btnChangeImage;
    private MaterialButton btnUpdateOffer;
    private MaterialButton btnCancel;

    // ====================================================
    // Repositories
    // ====================================================

    private FoodDonationRepository foodDonationRepository;
    private UserRepository userRepository;

    // ====================================================
    // Session
    // ====================================================

    private SessionManager sessionManager;

    // ====================================================
    // Organization
    // ====================================================

    private int organizationId = -1;

    // ====================================================
    // Offer
    // ====================================================

    private int offerId = -1;

    // Existing image in database
    private String existingImagePath = null;

    // New temporary image selected by camera
    private String newImagePath = null;

    // ====================================================
    // IMAGE / CAMERA
    // ====================================================

    private ActivityResultLauncher<String>
            cameraPermissionLauncher;

    private ActivityResultLauncher<Uri>
            cameraLauncher;

    // ====================================================
    // FLAG
    // ====================================================

    private boolean offerUpdatedSuccessfully = false;

    // ====================================================
    // CREATE VIEW
    // ====================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_edit_offer,
                container,
                false
        );
    }

    // ====================================================
    // VIEW CREATED
    // ====================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // ------------------------------------------------
        // Connect UI
        // ------------------------------------------------

        etFoodName =
                view.findViewById(
                        R.id.etFoodName
                );

        etQuantity =
                view.findViewById(
                        R.id.etQuantity
                );

        etDescription =
                view.findViewById(
                        R.id.etDescription
                );

        etExpiryDate =
                view.findViewById(
                        R.id.etExpiryDate
                );

        btnViewImage =
                view.findViewById(
                        R.id.btnViewImage
                );

        btnChangeImage =
                view.findViewById(
                        R.id.btnChangeImage
                );

        btnUpdateOffer =
                view.findViewById(
                        R.id.btnUpdateOffer
                );

        btnCancel =
                view.findViewById(
                        R.id.btnCancel
                );

        // ------------------------------------------------
        // Repositories
        // ------------------------------------------------

        foodDonationRepository =
                new FoodDonationRepository(
                        requireContext()
                );

        userRepository =
                new UserRepository(
                        requireContext()
                );

        foodDonationRepository.open();
        userRepository.open();

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Camera
        // ------------------------------------------------

        initializeCamera();

        // ------------------------------------------------
        // Get Offer ID
        // ------------------------------------------------

        Bundle arguments =
                getArguments();

        if (arguments != null) {

            offerId =
                    arguments.getInt(
                            "OFFER_ID",
                            -1
                    );
        }

        if (offerId <= 0) {

            showError(
                    "Offer not found"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Load Organization
        // ------------------------------------------------

        if (!loadOrganizationId()) {
            return;
        }

        // ------------------------------------------------
        // Load Existing Offer
        // ------------------------------------------------

        loadOffer();

        // ------------------------------------------------
        // Expiry Date
        // ------------------------------------------------

        etExpiryDate.setOnClickListener(
                view1 ->
                        showExpiryDatePicker()
        );

        // ------------------------------------------------
        // View Image
        // ------------------------------------------------

        btnViewImage.setOnClickListener(
                view1 ->
                        showCurrentImage()
        );

        // ------------------------------------------------
        // Change Image
        // ------------------------------------------------

        btnChangeImage.setOnClickListener(
                view1 ->
                        checkCameraPermissionAndOpen()
        );

        // ------------------------------------------------
        // Update Offer
        // ------------------------------------------------

        btnUpdateOffer.setOnClickListener(
                view1 ->
                        updateOffer()
        );

        // ------------------------------------------------
        // Cancel
        // ------------------------------------------------

        btnCancel.setOnClickListener(
                view1 ->
                        cancelEditing()
        );

        updateToolbar(
                "Edit Food Offer"
        );
    }

    // ====================================================
    // CAMERA INITIALIZATION
    // ====================================================

    private void initializeCamera() {

        // ------------------------------------------------
        // Camera Permission
        // ------------------------------------------------

        cameraPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .RequestPermission(),
                        granted -> {

                            if (granted) {

                                openCamera();

                            } else {

                                Toast.makeText(
                                        requireContext(),
                                        "Camera permission is required",
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
                        new ActivityResultContracts
                                .TakePicture(),
                        success -> {

                            if (success) {

                                Toast.makeText(
                                        requireContext(),
                                        "New image selected",
                                        Toast.LENGTH_SHORT
                                ).show();

                                btnViewImage.setVisibility(
                                        View.VISIBLE
                                );

                            } else {

                                deleteNewImage();
                            }
                        }
                );
    }

    // ====================================================
    // LOAD ORGANIZATION ID
    // ====================================================

    private boolean loadOrganizationId() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            goBack();

            return false;
        }

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            showError(
                    "User not found"
            );

            goBack();

            return false;
        }

        if (!"Food Institution".equals(
                user.getRole()
        )) {

            showError(
                    "Only food institutions can edit offers"
            );

            goBack();

            return false;
        }

        organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            showError(
                    "Food organization not found"
            );

            goBack();

            return false;
        }

        return true;
    }

    // ====================================================
    // LOAD OFFER
    // ====================================================

    private void loadOffer() {

        FoodDonation offer =
                foodDonationRepository
                        .getFoodDonationById(
                                offerId
                        );

        if (offer == null) {

            showError(
                    "Offer not found"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Ownership Check
        // ------------------------------------------------

        if (offer.getFoodOrganizationId()
                != organizationId) {

            showError(
                    "You are not authorized to edit this offer"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Existing Image
        // ------------------------------------------------

        existingImagePath =
                offer.getImagePath();

        if (TextUtils.isEmpty(
                existingImagePath
        )) {

            btnViewImage.setVisibility(
                    View.GONE
            );

        } else {

            btnViewImage.setVisibility(
                    View.VISIBLE
            );
        }

        // ------------------------------------------------
        // Fill Fields
        // ------------------------------------------------

        etFoodName.setText(
                offer.getFoodName()
        );

        etQuantity.setText(
                String.valueOf(
                        offer.getQuantity()
                )
        );

        etDescription.setText(
                offer.getDescription()
        );

        etExpiryDate.setText(
                offer.getExpiryDate()
        );
    }

    // ====================================================
    // SHOW CURRENT IMAGE
    // ====================================================

    private void showCurrentImage() {

        String imagePath;

        // If a new image was selected,
        // show the new one.
        if (!TextUtils.isEmpty(
                newImagePath
        )) {

            imagePath =
                    newImagePath;

        } else {

            imagePath =
                    existingImagePath;
        }

        if (TextUtils.isEmpty(
                imagePath
        )) {

            Toast.makeText(
                    requireContext(),
                    "No image available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        File imageFile =
                new File(
                        imagePath
                );

        if (!imageFile.exists()) {

            Toast.makeText(
                    requireContext(),
                    "Image file not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Bitmap bitmap =
                ImageUtils.decodeSampled(
                        imagePath,
                        1200,
                        1200
                );

        if (bitmap == null) {

            Toast.makeText(
                    requireContext(),
                    "Unable to load image",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // ImageView
        // ------------------------------------------------

        ImageView imageView =
                new ImageView(
                        requireContext()
                );

        imageView.setImageBitmap(
                bitmap
        );

        imageView.setAdjustViewBounds(
                true
        );

        imageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        int padding =
                (int) (
                        16
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );

        imageView.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        // ------------------------------------------------
        // Dialog
        // ------------------------------------------------

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Offer Image"
                )
                .setView(
                        imageView
                )
                .setPositiveButton(
                        "Close",
                        null
                )
                .show();
    }

    // ====================================================
    // CAMERA PERMISSION
    // ====================================================

    private void checkCameraPermissionAndOpen() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    // ====================================================
    // OPEN CAMERA
    // ====================================================

    private void openCamera() {

        try {

            // ------------------------------------------------
            // Remove previous temporary image
            // ------------------------------------------------

            deleteNewImage();

            // ------------------------------------------------
            // Create New Image File
            // ------------------------------------------------

            File photoFile =
                    createImageFile();

            // ------------------------------------------------
            // FileProvider URI
            // ------------------------------------------------

            Uri photoUri =
                    FileProvider.getUriForFile(
                            requireContext(),
                            requireContext()
                                    .getPackageName()
                                    + ".fileprovider",
                            photoFile
                    );

            // ------------------------------------------------
            // Launch Camera
            // ------------------------------------------------

            cameraLauncher.launch(
                    photoUri
            );

        } catch (IOException e) {

            Toast.makeText(
                    requireContext(),
                    "Failed to create image file",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ====================================================
    // CREATE IMAGE FILE
    // ====================================================

    private File createImageFile()
            throws IOException {

        String timeStamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.US
                ).format(
                        new Date()
                );

        String fileName =
                "OFFER_EDIT_"
                        + timeStamp
                        + "_";

        File storageDir =
                requireContext()
                        .getExternalFilesDir(
                                Environment
                                        .DIRECTORY_PICTURES
                        );

        if (storageDir == null) {

            throw new IOException(
                    "Picture directory unavailable"
            );
        }

        if (!storageDir.exists()
                && !storageDir.mkdirs()
                && !storageDir.exists()) {

            throw new IOException(
                    "Failed to create picture directory"
            );
        }

        File image =
                File.createTempFile(
                        fileName,
                        ".jpg",
                        storageDir
                );

        newImagePath =
                image.getAbsolutePath();

        return image;
    }

    // ====================================================
    // EXPIRY DATE PICKER
    // ====================================================

    private void showExpiryDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        String currentDate =
                etExpiryDate.getText()
                        .toString()
                        .trim();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        dateFormat.setLenient(false);

        if (!TextUtils.isEmpty(
                currentDate
        )) {

            try {

                Date parsedDate =
                        dateFormat.parse(
                                currentDate
                        );

                if (parsedDate != null) {

                    calendar.setTime(
                            parsedDate
                    );
                }

            } catch (ParseException ignored) {
                // Keep today's date.
            }
        }

        DatePickerDialog dialog =
                new DatePickerDialog(
                        requireContext(),
                        (view,
                         year,
                         month,
                         dayOfMonth) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.US,
                                            "%04d-%02d-%02d",
                                            year,
                                            month + 1,
                                            dayOfMonth
                                    );

                            etExpiryDate.setText(
                                    selectedDate
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.getDatePicker()
                .setMinDate(
                        System.currentTimeMillis()
                );

        dialog.show();
    }

    // ====================================================
    // UPDATE OFFER
    // ====================================================

    private void updateOffer() {

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
        // Food Name
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                foodName
        )) {

            etFoodName.setError(
                    "Please enter food name"
            );

            etFoodName.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Quantity
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                quantityText
        )) {

            etQuantity.setError(
                    "Please enter quantity"
            );

            etQuantity.requestFocus();

            return;
        }

        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            etQuantity.setError(
                    "Please enter a valid quantity"
            );

            etQuantity.requestFocus();

            return;
        }

        if (quantity < 0) {

            etQuantity.setError(
                    "Quantity cannot be negative"
            );

            etQuantity.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Expiry Date
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                expiryDate
        )) {

            etExpiryDate.setError(
                    "Please select expiry date"
            );

            etExpiryDate.requestFocus();

            return;
        }

        if (!isValidExpiryDate(
                expiryDate
        )) {

            etExpiryDate.setError(
                    "Please select a valid expiry date"
            );

            etExpiryDate.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Image Path
        // ------------------------------------------------
        //
        // No new image:
        // Repository keeps old image.
        //
        // New image:
        // Repository saves new image.
        // ------------------------------------------------

        String imagePathForUpdate =
                newImagePath;

        // ------------------------------------------------
        // Update Through Repository
        // ------------------------------------------------

        int result =
                foodDonationRepository
                        .updateFoodDonation(
                                offerId,
                                organizationId,
                                foodName,
                                quantity,
                                description,
                                expiryDate,
                                imagePathForUpdate
                        );

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (result > 0) {

            offerUpdatedSuccessfully =
                    true;

            // ------------------------------------------------
            // New image is now owned by DB record.
            // Do not delete it during cleanup.
            // ------------------------------------------------

            newImagePath = null;

            Toast.makeText(
                    requireContext(),
                    "Offer updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            goBack();

        } else {

            // ------------------------------------------------
            // Database update failed.
            //
            // New image is still temporary,
            // so delete it.
            // ------------------------------------------------

            deleteNewImage();

            Toast.makeText(
                    requireContext(),
                    "Failed to update offer",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ====================================================
    // EXPIRY VALIDATION
    // ====================================================

    private boolean isValidExpiryDate(
            String expiryDate
    ) {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        dateFormat.setLenient(false);

        try {

            Date date =
                    dateFormat.parse(
                            expiryDate
                    );

            if (date == null) {
                return false;
            }

            Date today =
                    new Date();

            String todayString =
                    dateFormat.format(
                            today
                    );

            Date todayDate =
                    dateFormat.parse(
                            todayString
                    );

            return todayDate != null
                    && !date.before(
                    todayDate
            );

        } catch (ParseException e) {

            return false;
        }
    }

    // ====================================================
    // DELETE NEW IMAGE
    // ====================================================

    private void deleteNewImage() {

        if (TextUtils.isEmpty(
                newImagePath
        )) {

            return;
        }

        ImageUtils.deleteImage(
                newImagePath
        );

        newImagePath = null;
    }

    // ====================================================
    // CANCEL EDITING
    // ====================================================

    private void cancelEditing() {

        if (!offerUpdatedSuccessfully) {

            deleteNewImage();
        }

        goBack();
    }

    // ====================================================
    // GO BACK
    // ====================================================

    private void goBack() {

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    // ====================================================
    // TOOLBAR
    // ====================================================

    private void updateToolbar(
            String title
    ) {

        if (requireActivity()
                instanceof FoodMainActivity) {

            ((FoodMainActivity)
                    requireActivity())
                    .updateToolbarTitle(
                            title
                    );
        }
    }

    // ====================================================
    // ERROR
    // ====================================================

    private void showError(
            String message
    ) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // ====================================================
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (!offerUpdatedSuccessfully) {

            deleteNewImage();
        }

        if (foodDonationRepository != null) {

            foodDonationRepository.close();
            foodDonationRepository = null;
        }

        if (userRepository != null) {

            userRepository.close();
            userRepository = null;
        }

        super.onDestroyView();
    }
}