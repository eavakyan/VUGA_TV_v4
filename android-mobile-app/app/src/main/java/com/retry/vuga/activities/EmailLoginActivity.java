package com.retry.vuga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityEmailLoginBinding;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.utils.CallBacks;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.MyEditText;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.BaseViewModel;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class EmailLoginActivity extends BaseActivity {
    ActivityEmailLoginBinding binding;
    SessionManager sessionManager;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth mAuth;
    CompositeDisposable disposable;
    BaseViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_login);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new BaseViewModel()).createFor()).get(BaseViewModel.class);

        initView();
        initListeners();

    }


    @Override
    public void onBackPressed() {
        if (binding.loutForget.getVisibility() == View.VISIBLE) {
            binding.loutEmail.setVisibility(View.VISIBLE);
            binding.loutForget.setVisibility(View.GONE);
        } else if (binding.loutCreateAccount.getVisibility() == View.VISIBLE) {
            binding.loutEmail.setVisibility(View.VISIBLE);
            binding.loutCreateAccount.setVisibility(View.GONE);
        } else {

            super.onBackPressed();
        }
    }

    private void initListeners() {


        setTextWatcher(binding.etEmail);
        setTextWatcher(binding.etPass);
        setTextWatcher(binding.etEmailCreateAccount);
        setTextWatcher(binding.etFullnameCreate);
        setTextWatcher(binding.etPassCreate);
        setTextWatcher(binding.etConfirmPassCreate);
        setTextWatcher(binding.etEmailForget);



        binding.loutLoader.setOnClickListener(v -> {

        });
        binding.btnReset.setOnClickListener(v -> {
            String email = binding.etEmailForget.getText().toString();
            if (email.isEmpty()) {
                binding.etEmailForget.setError(getString(R.string.enter_email));
                return;
            }

            if (!email.matches(emailPattern)) {
                Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                return;
            }

            binding.loutLoader.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            binding.loutLoader.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                Toast.makeText(EmailLoginActivity.this, R.string.link_to_reset_password_has_been_sent, Toast.LENGTH_LONG).show();
                                binding.loutEmail.setVisibility(View.VISIBLE);
                                binding.loutForget.setVisibility(View.GONE);
                                binding.etEmail.setText(email);
                            } else {
                                Log.i("TAG", "onComplete: " + task.getException());

                                if (task.getException() instanceof FirebaseAuthException) {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    switch (errorCode) {
                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(EmailLoginActivity.this, R.string.no_account_with_this_email, Toast.LENGTH_SHORT).show();
                                            break;

                                        default:
                                            Toast.makeText(EmailLoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(EmailLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        });

        binding.btnForget.setOnClickListener(v -> {

            binding.loutForget.setVisibility(View.VISIBLE);
            binding.loutEmail.setVisibility(View.GONE);
            binding.loutCreateAccount.setVisibility(View.GONE);


        });

        binding.btnShowHideSignIn.setOnClickListener(v -> {
            binding.setShowPass(!binding.getShowPass());


        });


        binding.btnShowHideCreate.setOnClickListener(v -> {
            binding.setShowPass(!binding.getShowPass());


        });



        binding.btnSignUp.setOnClickListener(v -> {

            binding.loutEmail.setVisibility(View.GONE);
            binding.loutCreateAccount.setVisibility(View.VISIBLE);


        });

        binding.btnSignIn.setOnClickListener(v -> {

            binding.loutCreateAccount.setVisibility(View.GONE);
            binding.loutEmail.setVisibility(View.VISIBLE);


        });

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmailCreateAccount.getText().toString();
            String fullName = binding.etFullnameCreate.getText().toString();
            String pass = binding.etPassCreate.getText().toString();
            String passConfirm = binding.etConfirmPassCreate.getText().toString();

            if (email.isEmpty()) {
                binding.etEmailCreateAccount.setError("");
                return;
            }

            if (!email.matches(emailPattern)) {
                Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                return;
            }

            if (fullName.isEmpty()) {
                binding.etFullnameCreate.setError("");
                return;
            }

            if (pass.isEmpty()) {
                binding.etPassCreate.setError("");
                return;
            }

            if (passConfirm.isEmpty()) {
                binding.etConfirmPassCreate.setError("");
                return;
            }


            if (!pass.equals(passConfirm)) {
                Toast.makeText(this, getString(R.string.password_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }
            binding.loutLoader.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            binding.loutLoader.setVisibility(View.GONE);

                            if (task.isSuccessful()) {

                                Log.d("TAG", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    HashMap<String, RequestBody> hashMap = new HashMap<>();

                                    hashMap.put(Const.ApiKey.fullname, RequestBody.create(Objects.requireNonNull(fullName), MediaType.parse("text/plain")));
                                    hashMap.put(Const.ApiKey.email, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                                    hashMap.put(Const.ApiKey.login_type, RequestBody.create(String.valueOf(4), MediaType.parse("text/plain")));
                                    hashMap.put(Const.ApiKey.identity, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                                    hashMap.put(Const.ApiKey.device_token, RequestBody.create(sessionManager.getFireBaseToken() == null ? "123" : sessionManager.getFireBaseToken(), MediaType.parse("text/plain")));
                                    hashMap.put(Const.ApiKey.device_type, RequestBody.create(String.valueOf(1), MediaType.parse("text/plain")));

                                    user.sendEmailVerification();
                                    Toast.makeText(EmailLoginActivity.this, R.string.verification_link_sent, Toast.LENGTH_LONG).show();
                                    onBackPressed();
                                    callRegistrationApi(hashMap, false);
                                }

                            } else {

                                if (task.getException() instanceof FirebaseAuthException) {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    switch (errorCode) {

                                        case "ERROR_WEAK_PASSWORD":
                                            Toast.makeText(EmailLoginActivity.this, R.string.password_at_least_six_char, Toast.LENGTH_SHORT).show();
                                            break;

                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            Toast.makeText(EmailLoginActivity.this, R.string.email_is_in_by_another_account, Toast.LENGTH_SHORT).show();
                                            break;

                                        default:
                                            Toast.makeText(EmailLoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();


                                    }


                                } else {
                                    Toast.makeText(EmailLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });


        });


        binding.btnContinue.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String pass = binding.etPass.getText().toString();
            if (email.isEmpty()) {
                binding.etEmail.setError(getString(R.string.enter_email));
                return;
            }
            if (!email.matches(emailPattern)) {
                Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.isEmpty()) {
                binding.etPass.setError(getString(R.string.enter_password));
                return;
            }
            binding.loutLoader.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            binding.loutLoader.setVisibility(View.GONE);


                            if (task.isSuccessful()) {

                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();


                                if (user != null) {

                                    if (!user.isEmailVerified()) {
                                        Log.i("TAG", "onComplete: email not verified ");
                                        user.sendEmailVerification();
                                        Toast.makeText(EmailLoginActivity.this, R.string.verification_link_sent, Toast.LENGTH_SHORT).show();

                                    } else {
                                        HashMap<String, RequestBody> hashMap = new HashMap<>();
                                        hashMap.put(Const.ApiKey.fullname, RequestBody.create("fullname", MediaType.parse("text/plain")));
                                        hashMap.put(Const.ApiKey.email, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                                        hashMap.put(Const.ApiKey.login_type, RequestBody.create(String.valueOf(4), MediaType.parse("text/plain")));
                                        hashMap.put(Const.ApiKey.identity, RequestBody.create(Objects.requireNonNull(user.getEmail()), MediaType.parse("text/plain")));
                                        hashMap.put(Const.ApiKey.device_token, RequestBody.create(sessionManager.getFireBaseToken() == null ? "123" : sessionManager.getFireBaseToken(), MediaType.parse("text/plain")));
                                        hashMap.put(Const.ApiKey.device_type, RequestBody.create(String.valueOf(1), MediaType.parse("text/plain")));
                                        callRegistrationApi(hashMap, true);
                                    }
                                }

                            } else {

                                if (task.getException() instanceof FirebaseAuthException) {
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                    switch (errorCode) {


                                        case "ERROR_INVALID_CREDENTIAL":

                                        case "ERROR_USER_MISMATCH":
                                            Toast.makeText(EmailLoginActivity.this, R.string.invalid_email_or_password, Toast.LENGTH_SHORT).show();
                                            break;

                                        case "ERROR_INVALID_EMAIL":

                                            Toast.makeText(EmailLoginActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                                            binding.etEmail.setError("");
                                            break;

                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(EmailLoginActivity.this, R.string.invalid_password, Toast.LENGTH_SHORT).show();
                                            binding.etPass.setError("");

                                            break;


                                        case "ERROR_USER_DISABLED":
                                            Toast.makeText(EmailLoginActivity.this, R.string.account_is_disabled_by_firebase, Toast.LENGTH_SHORT).show();

                                            break;


                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(EmailLoginActivity.this, R.string.no_account_with_this_email, Toast.LENGTH_SHORT).show();
                                            break;

                                        default:
                                            Toast.makeText(EmailLoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();


                                    }

                                } else {
                                    Toast.makeText(EmailLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });


        });


    }

    private void setTextWatcher(MyEditText myEditText) {

        myEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    myEditText.setError(null);
                }
            }
        });


    }


    private void callRegistrationApi(HashMap<String, RequestBody> hashMap, boolean goNext) {


        viewModel.registerUser(hashMap, new CallBacks.OnRegisterApi() {
            @Override
            public void onSubscribe() {
                binding.loutLoader.setVisibility(View.VISIBLE);

            }

            @Override
            public void onTerminate() {
                binding.loutLoader.setVisibility(View.GONE);

            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable.getMessage() != null) {
                    Log.i("TAG", "callRegistrationApi: " + throwable.getMessage());
                }

            }

            @Override
            public void onSuccess(UserRegistration.Data data) {


                if (goNext) {
                    sessionManager.saveUser(data);
                    loginRevenueCat();
                    
                    // Always show profile selection after login
                    startActivity(new Intent(EmailLoginActivity.this, ProfileSelectionActivity.class));
                    finish();
                }


            }
        });


    }


    private void initView() {

        disposable = new CompositeDisposable();

        binding.loutEmail.setVisibility(View.VISIBLE);
        binding.loutLoader.setVisibility(View.GONE);
        binding.loutCreateAccount.setVisibility(View.GONE);
        binding.loutForget.setVisibility(View.GONE);
        binding.setShowPass(false);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}