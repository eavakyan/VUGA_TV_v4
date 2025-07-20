package com.retry.vuga.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleLoginManager {

    public static final int RC_SIGN_IN = 100;
    private final GoogleSignInClient mGoogleSignInClient;
    private final Activity context;
    private final FirebaseAuth mAuth;
    public OnSignOut onSignOut;
    public OnSignInSuccess onSignInSuccess;
    public OnSignInError onSignInError;

    public GoogleLoginManager(Activity context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign-In with Firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("561887003471-pg4j8e8mq7ad7b38jt18jd5407f0ra0r.apps.googleusercontent.com") // Web client ID from google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void onLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        context.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("GoogleLoginManager", "Google Sign-In Account: " + (account != null ? account.getEmail() : "null"));
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                if (onSignInError != null) {
                    onSignInError.onSignInError("Google account is null");
                }
            }
        } catch (ApiException e) {
            Log.w("GoogleLoginManager", "signInResult:failed code=" + e.getStatusCode());
            String errorMessage = "Google sign in failed";
            switch (e.getStatusCode()) {
                case 12501:
                    errorMessage = "Sign in was cancelled by user";
                    break;
                case 12500:
                    errorMessage = "Sign in failed - check your internet connection";
                    break;
                case 7:
                    errorMessage = "Network error - check your internet connection";
                    break;
                case 10:
                    errorMessage = "Developer error - check your configuration";
                    break;
                default:
                    errorMessage = "Google sign in failed with code: " + e.getStatusCode();
                    break;
            }
            if (onSignInError != null) {
                onSignInError.onSignInError(errorMessage);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("GoogleLoginManager", "Starting Firebase authentication with Google token");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("GoogleLoginManager", "signInWithCredential:success");
                        if (onSignInSuccess != null) {
                            onSignInSuccess.onSignInSuccess(task.getResult().getUser());
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("GoogleLoginManager", "signInWithCredential:failure", task.getException());
                        String errorMessage = "Authentication failed";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("network")) {
                                    errorMessage = "Network error - check your internet connection";
                                } else if (exceptionMessage.contains("invalid")) {
                                    errorMessage = "Invalid credentials";
                                } else {
                                    errorMessage = "Authentication failed: " + exceptionMessage;
                                }
                            }
                        }
                        if (onSignInError != null) {
                            onSignInError.onSignInError(errorMessage);
                        }
                    }
                });
    }

    public void onClickLogOut() {
        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(context, task -> {
            // Sign out from Firebase
            mAuth.signOut();
            if (onSignOut != null) {
                onSignOut.onSignOutSuccess();
            }
        });
    }

    public void checkSignInStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            Log.d("GoogleLoginManager", "User is already signed in: " + account.getEmail());
        } else {
            Log.d("GoogleLoginManager", "No user is currently signed in");
        }
        
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.d("GoogleLoginManager", "Firebase user is signed in: " + mAuth.getCurrentUser().getEmail());
        } else {
            Log.d("GoogleLoginManager", "No Firebase user is currently signed in");
        }
    }

    public interface OnSignOut {
        void onSignOutSuccess();
    }

    public interface OnSignInSuccess {
        void onSignInSuccess(com.google.firebase.auth.FirebaseUser user);
    }

    public interface OnSignInError {
        void onSignInError(String errorMessage);
    }
}
