package com.retry.vuga.activities;

import static com.retry.vuga.utils.GoogleLoginManager.RC_SIGN_IN;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivitySignInBinding;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.utils.CallBacks;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.GoogleLoginManager;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.BaseViewModel;

import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SignInActivity extends BaseActivity {
    ActivitySignInBinding binding;
    SessionManager sessionManager;
    BaseViewModel viewModel;
    GoogleLoginManager googleLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new BaseViewModel()).createFor()).get(BaseViewModel.class);
        initialization();
        Global.createNotificationChannels(this);
        
        binding.progress.setOnClickListener(v -> {
            // Prevent clicks during loading
        });
        
        binding.btnGoogle.setOnClickListener(v -> {
            googleLoginManager = new GoogleLoginManager(this);
            googleLoginManager.checkSignInStatus(); // Debug: check current sign-in status
            setupGoogleSignInCallbacks();
            googleLoginManager.onLogin();
        });

        binding.btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmailLoginActivity.class);
            startActivity(intent);
        });
    }

    private void initialization() {
        sessionManager = new SessionManager(this);
    }

    private void setupGoogleSignInCallbacks() {
        googleLoginManager.onSignInSuccess = new GoogleLoginManager.OnSignInSuccess() {
            @Override
            public void onSignInSuccess(FirebaseUser user) {
                Log.d("SignInActivity", "Google Sign-In Success: " + user.getEmail());
                if (user != null) {
                    // Create user data for your backend
                    HashMap<String, RequestBody> hashMap = new HashMap<>();
                    hashMap.put(Const.ApiKey.fullname, RequestBody.create(Objects.requireNonNull(user.getDisplayName()), MediaType.parse("text/plain")));
                    hashMap.put(Const.ApiKey.email, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                    hashMap.put(Const.ApiKey.login_type, RequestBody.create(String.valueOf(1), MediaType.parse("text/plain")));
                    hashMap.put(Const.ApiKey.identity, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                    hashMap.put(Const.ApiKey.device_token, RequestBody.create(sessionManager.getFireBaseToken() == null ? "123" : sessionManager.getFireBaseToken(), MediaType.parse("text/plain")));
                    hashMap.put(Const.ApiKey.device_type, RequestBody.create(String.valueOf(1), MediaType.parse("text/plain")));

                    callRegistrationApi(hashMap);
                }
            }
        };

        googleLoginManager.onSignInError = new GoogleLoginManager.OnSignInError() {
            @Override
            public void onSignInError(String errorMessage) {
                binding.progress.setVisibility(View.GONE);
                Log.e("SignInActivity", "Google Sign-In Error: " + errorMessage);
                Toast.makeText(SignInActivity.this, "Google Sign-In failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (googleLoginManager != null) {
                googleLoginManager.handleSignInResult(task);
            }
        }
    }

    private void callRegistrationApi(HashMap<String, RequestBody> hashMap) {
        viewModel.registerUser(hashMap, new CallBacks.OnRegisterApi() {
            @Override
            public void onSubscribe() {
                binding.progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTerminate() {
                binding.progress.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable throwable) {
                binding.progress.setVisibility(View.GONE);
                if (throwable.getMessage() != null) {
                    Log.e("SignInActivity", "Registration API Error: " + throwable.getMessage());
                    Toast.makeText(SignInActivity.this, "Registration failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(UserRegistration.Data data) {
                binding.progress.setVisibility(View.GONE);
                sessionManager.saveUser(data);
                loginRevenueCat();
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}