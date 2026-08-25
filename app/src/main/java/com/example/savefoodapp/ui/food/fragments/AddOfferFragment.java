package com.example.savefoodapp.ui.food.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddOfferFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private EditText etFoodName;
    private EditText etQuantity;
    private EditText etDescription;
    private EditText etExpiryDate;

    private CheckBox cbFresh;
    private CheckBox cbNoMold;
    private CheckBox cbNoBadSmell;
    private CheckBox cbProperStorage;
    private CheckBox cbPackagingIntact;
    private CheckBox cbSafeToConsume;

    private Button btnCreateOffer;
    private Button btnCancel;
    private Button btnTakePhoto;

    // ====================================================
    // Image
    // ====================================================

    private String currentPhotoPath = null;

    private ActivityResultLauncher<String>
            cameraPermissionLauncher;

    private ActivityResultLauncher<Uri>
            cameraLauncher;

    private boolean offerCreatedSuccessfully =
            false;

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
                R.layout.fragment_add_offer,
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

        cbFresh =
                view.findViewById(
                        R.id.cbFresh
                );

        cbNoMold =
                view.findViewById(
                        R.id.cbNoMold
                );

        cbNoBadSmell =
                view.findViewById(
                        R.id.cbNoBadSmell
                );

        cbProperStorage =
                view.findViewById(
                        R.id.cbProperStorage
                );

        cbPackagingIntact =
                view.findViewById(
                        R.id.cbPackagingIntact
                );

        cbSafeToConsume =
                view.findViewById(
                        R.id.cbSafeToConsume
                );

        btnTakePhoto =
                view.findViewById(
                        R.id.btnTakePhoto
                );

        btnCreateOffer =
                view.findViewById(
                        R.id.btnCreateOffer
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
        // Date Picker
        // ------------------------------------------------

        etExpiryDate.setOnClickListener(
                v ->
                        showExpiryDatePicker()
        );

        // ------------------------------------------------
        // Camera
        // ------------------------------------------------

        initializeCamera();

        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        btnTakePhoto.setOnClickListener(
                v ->
                        checkCameraPermissionAndOpen()
        );

        btnCreateOffer.setOnClickListener(
                v ->
                        createOffer()
        );

        btnCancel.setOnClickListener(
                v ->
                        cancelOfferCreation()
        );

        updateToolbar(
                "Add Food Offer"
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

                                Toast.makeText(
                                        requireContext(),
                                        R.string.photo_saved_successfully,
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                deletePendingPhoto();
                            }
                        }
                );
    }

    // ====================================================
    // EXPIRY DATE PICKER
    // ====================================================

    private void showExpiryDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

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

        // ------------------------------------------------
        // Prevent Past Dates
        // ------------------------------------------------

        dialog.getDatePicker()
                .setMinDate(
                        System.currentTimeMillis()
                );

        dialog.show();
    }

    // ====================================================
    // CREATE OFFER
    // ====================================================

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

        if (TextUtils.isEmpty(
                foodName
        )) {

            etFoodName.setError(
                    getString(
                            R.string.enter_food_name
                    )
            );

            etFoodName.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Quantity Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                quantityText
        )) {

            etQuantity.setError(
                    getString(
                            R.string.enter_quantity
                    )
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
                    getString(
                            R.string.valid_quantity
                    )
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

        if (TextUtils.isEmpty(
                expiryDate
        )) {

            etExpiryDate.setError(
                    getString(
                            R.string.enter_expiry_date
                    )
            );

            etExpiryDate.requestFocus();

            return;
        }

        if (!isValidExpiryDate(
                expiryDate
        )) {

            etExpiryDate.setError(
                    getString(
                            R.string.valid_expiry_date
                    )
            );

            etExpiryDate.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Food Conditions
        // ------------------------------------------------

        if (!areFoodConditionsConfirmed()) {

            Toast.makeText(
                    requireContext(),
                    R.string.confirm_food_conditions,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Get Logged-in User
        // ------------------------------------------------

        String userEmail =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(
                userEmail
        )) {

            Toast.makeText(
                    requireContext(),
                    R.string.user_session_not_found,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Get User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        userEmail
                );

        if (user == null) {

            Toast.makeText(
                    requireContext(),
                    R.string.user_not_found,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Organization ID
        // ------------------------------------------------

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            Toast.makeText(
                    requireContext(),
                    R.string.organization_not_found,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

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
                        "AVAILABLE",
                        currentPhotoPath
                );

        // ------------------------------------------------
        // Insert Through Repository
        // ------------------------------------------------

        long donationId =
                foodDonationRepository
                        .insertFoodDonation(
                                donation
                        );

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (donationId == -1) {

            // Offer was not created.
            // Delete the pending image.

            deletePendingPhoto();

            Toast.makeText(
                    requireContext(),
                    R.string.failed_create_offer,
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            // Image now belongs to
            // the saved database record.

            offerCreatedSuccessfully =
                    true;

            currentPhotoPath = null;

            Toast.makeText(
                    requireContext(),
                    R.string.offer_created_successfully,
                    Toast.LENGTH_SHORT
            ).show();

            // ------------------------------------------------
            // Go To My Offers
            // ------------------------------------------------

            if (requireActivity()
                    instanceof FoodMainActivity) {

                ((FoodMainActivity)
                        requireActivity())
                        .openMyOffers();

            } else {

                // Fallback in case the fragment
                // is used outside FoodMainActivity.

                goBack();
            }
        }
    }

    // ====================================================
    // FOOD CONDITION VALIDATION
    // ====================================================

    private boolean areFoodConditionsConfirmed() {

        return cbFresh.isChecked()
                && cbNoMold.isChecked()
                && cbNoBadSmell.isChecked()
                && cbProperStorage.isChecked()
                && cbPackagingIntact.isChecked()
                && cbSafeToConsume.isChecked();
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

            // Remove unused previous photo.
            deletePendingPhoto();

            File photoFile =
                    createImageFile();

            Uri photoUri =
                    FileProvider.getUriForFile(
                            requireContext(),
                            requireContext()
                                    .getPackageName()
                                    + ".fileprovider",
                            photoFile
                    );

            cameraLauncher.launch(
                    photoUri
            );

        } catch (IOException e) {

            Toast.makeText(
                    requireContext(),
                    R.string.failed_create_image_file,
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
                "OFFER_"
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

        currentPhotoPath =
                image.getAbsolutePath();

        return image;
    }

    // ====================================================
    // DELETE PENDING PHOTO
    // ====================================================

    private void deletePendingPhoto() {

        if (currentPhotoPath == null) {

            return;
        }

        ImageUtils.deleteImage(
                currentPhotoPath
        );

        currentPhotoPath = null;
    }

    // ====================================================
    // CANCEL
    // ====================================================

    private void cancelOfferCreation() {

        if (!offerCreatedSuccessfully) {

            deletePendingPhoto();
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
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (!offerCreatedSuccessfully) {

            deletePendingPhoto();
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