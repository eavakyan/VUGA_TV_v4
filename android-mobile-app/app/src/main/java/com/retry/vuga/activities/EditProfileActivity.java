package com.retry.vuga.activities;

import static android.provider.MediaStore.MediaColumns.DATA;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityEditProfileBinding;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.imageuplod.Compressor;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileActivity extends BaseActivity {
    ActivityEditProfileBinding binding;
    CompositeDisposable disposable;
    HashMap<String, RequestBody> hashMap;
    private File compressFile;
    private MultipartBody.Part body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);


        initialization();
        setUserDetails();
        setListeners();
    }


    ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            String[] filePathColumn = {DATA};
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            cursor.close();
                            binding.imgUser.setVisibility(View.GONE);
                            Glide.with(EditProfileActivity.this)
                                    .load(new File(picturePath))
                                    .circleCrop()
                                    .into(binding.imgProfile);//got photo

                            //  make image compress file
                            File imgFile = new File(picturePath);
                            compressFile = Compressor.getDefault(EditProfileActivity.this).compressToFile(imgFile);
                        }
                    }
                }
            });

    private void setListeners() {


        binding.loutLoader.setOnClickListener(v -> {

        });


        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                selectImageLauncher.launch(intent);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

                Toast.makeText(EditProfileActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        };

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        binding.btnUpdate.setOnClickListener(v -> {

            if (binding.etFullname.getText().toString().isEmpty()) {

                Toast.makeText(this, getString(R.string.user_name_cant_be_empty), Toast.LENGTH_SHORT).show();

            }
//            else if (binding.etEmail.getText().toString().isEmpty()) {
//
//                Toast.makeText(this, getString(R.string.email_cant_be_empty), Toast.LENGTH_SHORT).show();
//
//            }
            else {
                if (compressFile != null) {
                    RequestBody requestBody = RequestBody.create(compressFile, MediaType.parse("multipart/form-data"));
                    body = MultipartBody.Part.createFormData(Const.ApiKey.profile_image, compressFile.getName(), requestBody);
                }
                updateProfile();
            }

        });

        binding.imgProfile.setOnClickListener(v -> {
            boolean tiramisu = false;
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            String[] permissionT = new String[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                tiramisu = true;
                permissionT = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
            }


            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage(getString(R.string.reject_message))
                    .setPermissions(tiramisu ? permissionT : permission)
                    .check();

        });
    }

    private void updateProfile() {


        hashMap.put(Const.ApiKey.user_id, RequestBody.create(String.valueOf(sessionManager.getUser().getId()), MediaType.parse("text/plain")));
        hashMap.put(Const.ApiKey.fullname, RequestBody.create(binding.etFullname.getText().toString(), MediaType.parse("text/plain")));
//        hashMap.put(Const.ApiKey.email, RequestBody.create(binding.etEmail.getText().toString(), MediaType.parse("text/plain")));


        disposable.add(RetrofitClient.getService().updateProfile(hashMap, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    binding.loutLoader.setVisibility(View.VISIBLE);
                })
                .doOnTerminate(() -> {
                    binding.loutLoader.setVisibility(View.GONE);
                }).doOnError(throwable -> {

                    binding.loutLoader.setVisibility(View.GONE);

                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();


                    Log.i("TAG", "EditProfileApi: error " + throwable.getMessage());
                })
                .subscribe((userRegistration, throwable) -> {


                    if (userRegistration != null) {

                        if (userRegistration.getStatus()) {

                            sessionManager.saveUser(userRegistration.getData());
                            Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, userRegistration.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }


                }));


    }

    private void initialization() {
        disposable = new CompositeDisposable();
        hashMap = new HashMap<>();

    }

    private void setUserDetails() {


        if (!sessionManager.getUser().getProfileImage().isEmpty()) {
            binding.imgUser.setVisibility(View.GONE);
            // Add timestamp to force cache refresh
            String imageUrl = Const.IMAGE_URL + sessionManager.getUser().getProfileImage() + "?t=" + System.currentTimeMillis();
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .error(R.color.edit_text_bg_color)
                            .priority(Priority.HIGH)
                            .skipMemoryCache(true)  // Skip memory cache
                            .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis())))  // Force refresh
                    .into(binding.imgProfile);
        }
        binding.etFullname.setText(sessionManager.getUser().getFullname());
//        binding.etEmail.setText(sessionManager.getUser().getEmail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



    }


}