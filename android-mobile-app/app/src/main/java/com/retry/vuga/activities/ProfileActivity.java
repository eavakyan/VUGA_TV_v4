package com.retry.vuga.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.retry.vuga.BuildConfig;
import com.retry.vuga.R;
import com.retry.vuga.bottomSheets.WebBottomSheet;
import com.retry.vuga.databinding.ActivityProfileBinding;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.GoogleLoginManager;
import com.revenuecat.purchases.Purchases;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileActivity extends BaseActivity {
    ActivityProfileBinding binding;
    WebBottomSheet webBottomSheet;
    CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        disposable = new CompositeDisposable();
        setBlur(binding.blurView, binding.rootLout, 10f);
        setUserDetail();
        setListeners();
    }

    private void setListeners() {

        binding.centerLoader.setOnClickListener(v -> {

        });

        binding.blurView.setOnClickListener(v -> {

        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.loutPrivacy.setOnClickListener(v -> {
            webBottomSheet = new WebBottomSheet(1);
            if (!webBottomSheet.isAdded()) {
                webBottomSheet.show(getSupportFragmentManager(), webBottomSheet.getClass().getSimpleName());
            }
        });

        binding.loutTearms.setOnClickListener(v -> {
            webBottomSheet = new WebBottomSheet(2);
            if (!webBottomSheet.isAdded()) {
                webBottomSheet.show(getSupportFragmentManager(), webBottomSheet.getClass().getSimpleName());
            }
        });

        binding.loutPro.setOnClickListener(v -> {

            startActivity(new Intent(this, ProActivity.class));

        });
        binding.loutRate.setOnClickListener(v -> {

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));

        });
        binding.loutDownloads.setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadsActivity.class));

        });

        binding.loutLanguage.setOnClickListener(v -> {
            startActivity(new Intent(this, LanguageActivity.class));

        });

        binding.imgEdit.setOnClickListener(v -> {

            startActivity(new Intent(this, EditProfileActivity.class));
        });

        binding.loutLogOut.setOnClickListener(v -> {

            showLogoutPopUp();
        });

        binding.loutDelete.setOnClickListener(v -> {

            showDeletePopUp();

        });

        binding.switchNoti.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, true);
                FirebaseMessaging.getInstance().subscribeToTopic(Const.FIREBASE_SUB_TOPIC);

            } else {
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, false);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.FIREBASE_SUB_TOPIC);


            }
        });


    }

    private void showLogoutPopUp() {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showLogoutDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {



                if (sessionManager.getUser().getLoginType() == 4) {

                    FirebaseAuth.getInstance().signOut();
                    logoutApi();


                }


                if (sessionManager.getUser().getLoginType() == 1) {

                    GoogleLoginManager googleLoginManager = new GoogleLoginManager(ProfileActivity.this);
                    googleLoginManager.onClickLogOut();
                    googleLoginManager.onSignOut = () -> logoutApi();


                }
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });


    }

    private void showDeletePopUp() {

        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showDeleteDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                if (sessionManager.getUser().getLoginType() == 4) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        user.delete();
                    }
                    deleteApi();


                }


                if (sessionManager.getUser().getLoginType() == 1) {

                    GoogleLoginManager googleLoginManager = new GoogleLoginManager(ProfileActivity.this);
                    googleLoginManager.onClickLogOut();

                    googleLoginManager.onSignOut = new GoogleLoginManager.OnSignOut() {
                        @Override
                        public void onSignOutSuccess() {
                            deleteApi();
                        }
                    };


                }
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });


    }


    private void logoutApi() {


        disposable.add(RetrofitClient.getService().logOutUser(sessionManager.getUser().getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposab -> {


                    binding.centerLoader.setVisibility(View.VISIBLE);

                })
                .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE)).doOnError(throwable -> Log.i("TAG", "getHomePageData: " + throwable.getMessage()))
                .subscribe((userLogout, throwable) -> {


                    if (userLogout != null && userLogout.getStatus()) {
                        Purchases.getSharedInstance().logOut();
                        Toast.makeText(ProfileActivity.this, userLogout.getMessage(), Toast.LENGTH_SHORT).show();
                        sessionManager.clear();
                        saveToken();

                        startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                        finishAffinity();


                    }


                }));

    }

    private void saveToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.i("TAG", "onComplete: from profile token : " + task.getResult());
                    sessionManager.saveFireBaseToken(task.getResult());
                }
            }
        });
    }

    private void deleteApi() {


        disposable.add(RetrofitClient.getService().deleteAccount(sessionManager.getUser().getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposab -> {

                    binding.centerLoader.setVisibility(View.VISIBLE);

                })
                .doOnError(throwable -> {
                    Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                })
                .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE)).doOnError(throwable -> Log.i("TAG", "getHomePageData: " + throwable.getMessage()))
                .subscribe((response, throwable) -> {


                    if (response != null && response.getStatus()) {
                        Purchases.getSharedInstance().logOut();
                        Toast.makeText(ProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                        sessionManager.clear();
                        saveToken();
                        startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                        finishAffinity();


                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    }


                }));

    }

    private void setUserDetail() {

        boolean notification = sessionManager.getBooleanValue(Const.DataKey.NOTIFICATION);
        binding.switchNoti.setChecked(notification);

        if (!sessionManager.getUser().getProfileImage().isEmpty()) {
            binding.imgUser.setVisibility(View.GONE);
            Glide.with(this).load(Const.IMAGE_URL + sessionManager.getUser().getProfileImage()).apply(
                    new RequestOptions().error(
                            R.color.edit_text_bg_color
                    ).priority(Priority.HIGH)
            ).into(binding.imgProfile);
        }

        binding.tvFullAme.setText(sessionManager.getUser().getFullname());
        setPremiumlayout();


    }


    private void setPremiumlayout() {
        boolean isPremium = sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM);

        if (isPremium) {
            Log.i("TAG", "setUserDetail: is premium ");
            binding.tvForPro.setText(getString(R.string.you_are));
            binding.loutPro.setEnabled(false);
//                binding.tvDate.setText(finalDate);
//                binding.loutDate.setVisibility(View.VISIBLE);
        } else {
            Log.i("TAG", "setUserDetail: is not premium ");

            binding.tvForPro.setText(getString(R.string.subscribe_to));
            binding.loutPro.setEnabled(true);
//                binding.loutDate.setVisibility(View.GONE);

        }
    }

}