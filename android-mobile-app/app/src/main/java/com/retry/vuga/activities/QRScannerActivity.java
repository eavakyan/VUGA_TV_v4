package com.retry.vuga.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityQrScannerBinding;
import com.retry.vuga.model.RestResponse;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.SessionManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class QRScannerActivity extends BaseActivity {
    
    private static final String TAG = "QRScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private ActivityQrScannerBinding binding;
    private CompositeDisposable disposable = new CompositeDisposable();
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            sessionManager = new SessionManager(this);
            
            setupUI();
            
            // Check if opened via deep link
            Intent intent = getIntent();
            if (intent != null && intent.getData() != null) {
                String uri = intent.getData().toString();
                handleQRCode(uri);
            } else {
                checkCameraPermission();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing QR scanner: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.btnScan.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startQRScanner();
            }
        });
        
        binding.btnCancel.setOnClickListener(v -> finish());
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }
    
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan TV QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                handleQRCode(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void handleQRCode(String qrContent) {
        // Expected format: vuga://auth/tv/{session_token}
        if (qrContent != null && qrContent.startsWith("vuga://auth/tv/")) {
            String sessionToken = qrContent.replace("vuga://auth/tv/", "");
            authenticateTVSession(sessionToken);
        } else {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void authenticateTVSession(String sessionToken) {
        // Check if user is logged in
        if (sessionManager.getUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra("tv_session_token", sessionToken);
            startActivity(intent);
            finish();
            return;
        }
        
        showLoading(true);
        
        int userId = sessionManager.getUser().getId();
        
        try {
            disposable.add(RetrofitClient.getService()
                    .authenticateTVSession(sessionToken, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        response -> {
                            showLoading(false);
                            if (response != null && response.getStatus()) {
                                Toast.makeText(this, "TV authenticated successfully!", 
                                    Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, 
                                    response != null ? response.getMessage() : "Authentication failed", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            showLoading(false);
                            Log.e(TAG, "Authentication error", error);
                            
                            String errorMessage;
                            if (error instanceof java.net.UnknownHostException) {
                                errorMessage = "Cannot connect to server. Please check your internet connection.";
                            } else if (error instanceof java.net.ConnectException) {
                                errorMessage = "Server is not available. Please try again later.";
                            } else if (error instanceof java.net.SocketTimeoutException) {
                                errorMessage = "Connection timeout. Please check your internet connection.";
                            } else if (error instanceof retrofit2.HttpException) {
                                retrofit2.HttpException httpError = (retrofit2.HttpException) error;
                                if (httpError.code() == 404) {
                                    errorMessage = "TV authentication service not available. Please update the app.";
                                } else {
                                    errorMessage = "Server error: " + httpError.code();
                                }
                            } else {
                                errorMessage = "Error: " + error.getMessage();
                            }
                            
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    ));
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error setting up API call", e);
            Toast.makeText(this, "Error: Unable to connect to server", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnScan.setEnabled(!show);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}