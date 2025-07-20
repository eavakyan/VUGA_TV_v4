package com.retry.vuga.utils;

import com.retry.vuga.model.HomePage;
import com.retry.vuga.model.UserRegistration;

public interface CallBacks {

    public interface OnRegisterApi {

        void onSubscribe();

        void onTerminate();

        void onError(Throwable throwable);

        void onSuccess(UserRegistration.Data data);
    }

    public interface GetHomePageData {

        void onSubscribe();

        void onTerminate();

        void onError(Throwable throwable);

        void onSuccess(HomePage homePage);
    }
}
