package com.retry.vuga.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;

import com.retry.vuga.R;
import com.retry.vuga.adapters.AvatarColorAdapter;
import com.retry.vuga.databinding.ActivityCreateProfileBinding;
import com.retry.vuga.retrofit.RetrofitClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import android.util.Base64;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CreateProfileActivity extends BaseActivity implements AvatarColorAdapter.OnColorSelectedListener {

    private ActivityCreateProfileBinding binding;
    private CompositeDisposable disposable;
    private String selectedColor = "#FF5252";
    private boolean isKidsProfile = false;
    private int profileId = -1;
    private boolean isEditMode = false;
    private int selectedAvatarId = 1; // Default avatar ID
    private Integer profileAge = null;
    private Uri selectedImageUri = null;
    private File selectedImageFile = null;
    private String avatarType = "color"; // "color" or "custom"
    private String existingAvatarUrl = null;
    private boolean shouldRemovePhoto = false;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;

    private List<String> avatarColors = Arrays.asList(
            "#FF5252", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile);
        disposable = new CompositeDisposable();

        // Check if editing existing profile
        if (getIntent().hasExtra("profile_id")) {
            isEditMode = true;
            profileId = getIntent().getIntExtra("profile_id", -1);
            String profileName = getIntent().getStringExtra("profile_name");
            selectedColor = getIntent().getStringExtra("profile_color");
            selectedAvatarId = getIntent().getIntExtra("avatar_id", 1);
            isKidsProfile = getIntent().getBooleanExtra("is_kids", false);
            profileAge = getIntent().hasExtra("profile_age") ? getIntent().getIntExtra("profile_age", 0) : null;
            avatarType = getIntent().getStringExtra("avatar_type");
            existingAvatarUrl = getIntent().getStringExtra("profile_avatar_url");
            
            // Debug logging
            android.util.Log.d("CreateProfileActivity", "Edit Mode - Profile ID: " + profileId);
            android.util.Log.d("CreateProfileActivity", "Avatar Type: " + avatarType);
            android.util.Log.d("CreateProfileActivity", "Avatar URL: " + existingAvatarUrl);
            android.util.Log.d("CreateProfileActivity", "Avatar Color: " + selectedColor);

            binding.etProfileName.setText(profileName);
            binding.switchKidsProfile.setChecked(isKidsProfile);
            binding.tvTitle.setText("Edit Profile");
            binding.btnCreate.setText("Update Profile");
            
            // Set the color adapter to show the current selection
            if (selectedColor != null && selectedColor.startsWith("#")) {
                // If we don't have the exact color in our list, use the avatar ID to determine which color to highlight
                int colorIndex = avatarColors.indexOf(selectedColor);
                if (colorIndex == -1 && selectedAvatarId > 0 && selectedAvatarId <= 8) {
                    // Use avatar ID to determine which color in our palette to select
                    colorIndex = (selectedAvatarId - 1) % avatarColors.size();
                    if (colorIndex >= 0 && colorIndex < avatarColors.size()) {
                        selectedColor = avatarColors.get(colorIndex);
                    }
                }
            }
        }

        setupViews();
        setupListeners();
    }

    private void setupViews() {
        // Setup color adapter
        AvatarColorAdapter colorAdapter = new AvatarColorAdapter(this, avatarColors, selectedColor, this);
        binding.rvColors.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvColors.setAdapter(colorAdapter);

        // Check if we should show photo or color avatar
        // Priority: existing photo > color avatar
        android.util.Log.d("CreateProfileActivity", "setupViews - existingAvatarUrl: " + existingAvatarUrl);
        android.util.Log.d("CreateProfileActivity", "setupViews - isEditMode: " + isEditMode);
        
        // Check for valid custom image URL (must be http/https, not local avatar paths)
        boolean hasValidCustomImage = existingAvatarUrl != null && !existingAvatarUrl.isEmpty() 
                && !existingAvatarUrl.equals("null") && !existingAvatarUrl.equals("0")
                && (existingAvatarUrl.startsWith("http://") || existingAvatarUrl.startsWith("https://"));
        
        if (isEditMode && hasValidCustomImage) {
            // Profile has a valid photo URL - show it
            android.util.Log.d("CreateProfileActivity", "Showing existing photo");
            avatarType = "custom";  // Ensure avatar type is set correctly
            
            // The CardView contains both ImageView and TextView - keep CardView visible
            binding.viewPreviewAvatar.setVisibility(View.VISIBLE);
            binding.ivProfileImage.setVisibility(View.VISIBLE);
            binding.tvPreviewInitial.setVisibility(View.GONE);
            
            // Set card background to transparent when showing image
            binding.viewPreviewAvatar.setCardBackgroundColor(Color.TRANSPARENT);
            
            // Load the image with Glide - don't use placeholder/error images
            com.bumptech.glide.Glide.with(this)
                .load(existingAvatarUrl)
                .circleCrop()
                .into(binding.ivProfileImage);
        } else {
            // No valid photo or has default avatar - show color avatar
            android.util.Log.d("CreateProfileActivity", "Showing color avatar");
            avatarType = "color";
            existingAvatarUrl = null; // Clear any invalid URL
            updatePreview();
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Image upload listeners
        binding.btnEditImage.setOnClickListener(v -> selectImage());
        binding.btnUploadImage.setOnClickListener(v -> selectImage());
        
        // Update initials when name changes
        binding.etProfileName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Update preview if showing color avatar
                if (!"custom".equals(avatarType) || selectedImageFile == null) {
                    updatePreview();
                }
            }
        });

        binding.switchKidsProfile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isEditMode) {
                // Show age input dialog
                showAgeInputDialog();
            } else {
                isKidsProfile = isChecked;
                if (!isChecked) {
                    profileAge = null;
                }
                updatePreview();
            }
        });

        binding.btnCreate.setOnClickListener(v -> {
            String profileName = binding.etProfileName.getText().toString().trim();
            if (profileName.isEmpty()) {
                Toast.makeText(this, "Please enter profile name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate age for Kids Profile
            if (isKidsProfile && (profileAge == null || profileAge < 1 || profileAge >= 18)) {
                Toast.makeText(this, "Kids Profile requires age between 1 and 17", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                if (selectedImageFile != null) {
                    updateProfileWithImage(profileName);
                } else {
                    updateProfile(profileName);
                }
            } else {
                if (selectedImageFile != null) {
                    createProfileWithImage(profileName);
                } else {
                    createProfile(profileName);
                }
            }
        });
    }

    private void createProfile(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        int userId = sessionManager.getUser().getId();

        disposable.add(RetrofitClient.getService()
                .createProfile(userId, profileName, selectedAvatarId, isKidsProfile ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCreate.setEnabled(true);
                })
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus()) {
                        Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = response != null ? response.getMessage() : "Failed to create profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void updateProfile(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        int userId = sessionManager.getUser().getId();
        
        // If user selected a color avatar and previously had a custom photo, we need to remove it
        if (shouldRemovePhoto && "color".equals(avatarType)) {
            // First remove the custom avatar by updating with color avatar
            disposable.add(RetrofitClient.getService()
                    .removeProfileAvatar(profileId, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((removeResponse, removeThrowable) -> {
                        // After removing avatar, update the profile
                        disposable.add(RetrofitClient.getService()
                                .updateProfile(profileId, userId, profileName, selectedAvatarId, isKidsProfile ? 1 : 0)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnTerminate(() -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.btnCreate.setEnabled(true);
                                })
                                .subscribe((response, throwable) -> {
                                    if (response != null && response.isStatus()) {
                                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    } else {
                                        String message = response != null ? response.getMessage() : "Failed to update profile";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    }
                                }));
                    }));
        } else {
            // Normal update without removing avatar
            disposable.add(RetrofitClient.getService()
                    .updateProfile(profileId, userId, profileName, selectedAvatarId, isKidsProfile ? 1 : 0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCreate.setEnabled(true);
                    })
                    .subscribe((response, throwable) -> {
                        if (response != null && response.isStatus()) {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String message = response != null ? response.getMessage() : "Failed to update profile";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    }));
        }
    }

    private void updatePreview() {
        // Determine what to show based on current state
        boolean hasPhoto = false;
        
        if (selectedImageFile != null) {
            // User just selected a new image
            hasPhoto = true;
        } else if ("custom".equals(avatarType) && existingAvatarUrl != null 
                && !existingAvatarUrl.isEmpty() && !existingAvatarUrl.equals("null") 
                && !existingAvatarUrl.equals("0")
                && (existingAvatarUrl.startsWith("http://") || existingAvatarUrl.startsWith("https://"))) {
            // Has existing photo with valid URL and hasn't been replaced
            hasPhoto = true;
        }
        
        // The CardView always stays visible - we just change what's inside it
        binding.viewPreviewAvatar.setVisibility(View.VISIBLE);
        
        if (hasPhoto) {
            // Show photo inside the CardView
            binding.ivProfileImage.setVisibility(View.VISIBLE);
            binding.tvPreviewInitial.setVisibility(View.GONE);
            // Set card background to transparent when showing image
            binding.viewPreviewAvatar.setCardBackgroundColor(Color.TRANSPARENT);
        } else {
            // Show color avatar with initials
            binding.ivProfileImage.setVisibility(View.GONE);
            binding.tvPreviewInitial.setVisibility(View.VISIBLE);
            
            // Make sure we have a valid color
            if (selectedColor == null || selectedColor.isEmpty() || !selectedColor.startsWith("#")) {
                selectedColor = "#FF5252"; // Default red color
            }
            
            try {
                binding.viewPreviewAvatar.setCardBackgroundColor(Color.parseColor(selectedColor));
            } catch (Exception e) {
                binding.viewPreviewAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
            }

            String name = binding.etProfileName.getText().toString().trim();
            if (name.isEmpty() && isEditMode) {
                // In edit mode, use the name that was passed in
                name = getIntent().getStringExtra("profile_name");
            }
            String initials = generateInitials(name);
            binding.tvPreviewInitial.setText(initials);
        }
    }
    
    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "P";
        }
        
        String trimmedName = name.trim();
        String[] words = trimmedName.split("\\s+");
        
        if (words.length == 0) {
            return "P";
        } else if (words.length == 1) {
            // Single word - take first letter
            return words[0].substring(0, 1).toUpperCase();
        } else {
            // Multiple words - take first letter of first two words
            String firstInitial = words[0].substring(0, 1).toUpperCase();
            String secondInitial = words[1].substring(0, 1).toUpperCase();
            return firstInitial + secondInitial;
        }
    }

    @Override
    public void onColorSelected(String color) {
        selectedColor = color;
        // Map color to avatar ID (1-based index)
        // Limit to 1-8 range as backend might only have 8 default avatars
        int colorIndex = avatarColors.indexOf(color);
        if (colorIndex >= 0) {
            // Use modulo to wrap around if we have more colors than avatars
            selectedAvatarId = (colorIndex % 8) + 1;
        }
        
        // When a color is selected, switch to color avatar mode
        avatarType = "color";
        selectedImageFile = null;
        selectedImageUri = null;
        
        // Mark that we should remove photo only if there was an existing photo
        if (existingAvatarUrl != null && !existingAvatarUrl.isEmpty() 
                && !existingAvatarUrl.equals("null") && !existingAvatarUrl.equals("0")) {
            shouldRemovePhoto = true;
        }
        
        // Clear the existing avatar URL since user selected a color
        existingAvatarUrl = null;
        
        // Update the preview to show color avatar
        updatePreview();
    }

    private void showAgeInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Age");
        builder.setMessage("Kids Profile is for children under 18. Please enter the age:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Age (1-17)");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String ageStr = input.getText().toString().trim();
            if (ageStr.isEmpty()) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                binding.switchKidsProfile.setChecked(false);
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age >= 18) {
                    Toast.makeText(this, "Kids Profile age must be between 1 and 17", Toast.LENGTH_SHORT).show();
                    binding.switchKidsProfile.setChecked(false);
                    return;
                }

                profileAge = age;
                isKidsProfile = true;
                updatePreview();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                binding.switchKidsProfile.setChecked(false);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            binding.switchKidsProfile.setChecked(false);
            dialog.cancel();
        });

        builder.show();
    }

    private void selectImage() {
        if (checkPermission()) {
            openImagePicker();
        } else {
            requestPermission();
        }
    }
    
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_IMAGES
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_IMAGES
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                    PERMISSION_REQUEST_CODE);
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    PERMISSION_REQUEST_CODE);
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            avatarType = "custom";  // Switch to custom avatar when image is selected
            shouldRemovePhoto = false;  // We're adding a photo, not removing it
            try {
                // First, copy the original file to preserve EXIF data
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                File tempFile = new File(getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
                OutputStream tempOut = new FileOutputStream(tempFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    tempOut.write(buffer, 0, length);
                }
                tempOut.close();
                inputStream.close();
                
                // Now read the EXIF orientation
                ExifInterface exif = new ExifInterface(tempFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                
                // Load bitmap for processing
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                
                // DON'T apply rotation - keep original orientation
                // Just scale if needed
                int maxSize = 1024;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                float scale = 1.0f;
                
                if (width > maxSize || height > maxSize) {
                    if (width > height) {
                        scale = (float) maxSize / width;
                    } else {
                        scale = (float) maxSize / height;
                    }
                }
                
                Bitmap finalBitmap;
                if (scale < 1.0f) {
                    int newWidth = Math.round(width * scale);
                    int newHeight = Math.round(height * scale);
                    finalBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                } else {
                    finalBitmap = originalBitmap;
                }
                
                // Save compressed bitmap to file
                selectedImageFile = new File(getCacheDir(), "profile_image_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream out = new FileOutputStream(selectedImageFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                out.flush();
                out.close();
                
                // Copy EXIF orientation to the new file
                ExifInterface newExif = new ExifInterface(selectedImageFile.getAbsolutePath());
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientation));
                newExif.saveAttributes();
                
                // Clean up
                if (finalBitmap != originalBitmap) {
                    finalBitmap.recycle();
                }
                originalBitmap.recycle();
                tempFile.delete();
                
                // Display image in preview
                // Keep CardView visible, just show the image inside it
                binding.viewPreviewAvatar.setVisibility(View.VISIBLE);
                binding.viewPreviewAvatar.setCardBackgroundColor(Color.TRANSPARENT);
                binding.ivProfileImage.setVisibility(View.VISIBLE);
                binding.tvPreviewInitial.setVisibility(View.GONE);
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(binding.ivProfileImage);
                    
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void createProfileWithImage(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        int userId = sessionManager.getUser().getId();
        
        // First create the profile with color avatar
        disposable.add(RetrofitClient.getService()
                .createProfile(userId, profileName, selectedAvatarId, isKidsProfile ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus() && response.getProfile() != null) {
                        // Profile created, now upload the avatar image
                        int newProfileId = response.getProfile().getProfileId();
                        uploadAvatarImage(userId, newProfileId, true);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCreate.setEnabled(true);
                        String message = response != null ? response.getMessage() : "Failed to create profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }
    
    private void updateProfileWithImage(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        int userId = sessionManager.getUser().getId();
        
        // First update the profile name (keeping existing avatar)
        disposable.add(RetrofitClient.getService()
                .updateProfile(profileId, userId, profileName, selectedAvatarId, isKidsProfile ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus()) {
                        // Profile updated, now upload the avatar image
                        uploadAvatarImage(userId, profileId, false);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCreate.setEnabled(true);
                        String message = response != null ? response.getMessage() : "Failed to update profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }
    
    private void uploadAvatarImage(int userId, int profileId, boolean isNewProfile) {
        try {
            // Read the image file
            byte[] imageBytes = new byte[(int) selectedImageFile.length()];
            FileInputStream fis = new FileInputStream(selectedImageFile);
            fis.read(imageBytes);
            fis.close();
            
            // Convert to base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            
            // Upload to S3 via the dedicated endpoint
            disposable.add(RetrofitClient.getService()
                    .uploadProfileAvatar(userId, profileId, base64Image)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCreate.setEnabled(true);
                    })
                    .subscribe((response, throwable) -> {
                        if (response != null && response.isStatus()) {
                            String successMessage = isNewProfile ? "Profile created successfully" : "Profile updated successfully";
                            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            // Avatar upload failed, but profile was created/updated
                            String message = "Profile saved but avatar upload failed";
                            if (response != null && response.getMessage() != null) {
                                message = response.getMessage();
                            }
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnCreate.setEnabled(true);
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Still consider it successful since profile was created/updated
            setResult(RESULT_OK);
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        // Clean up temp file
        if (selectedImageFile != null && selectedImageFile.exists()) {
            selectedImageFile.delete();
        }
    }
}