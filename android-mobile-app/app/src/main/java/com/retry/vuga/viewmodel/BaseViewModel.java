package com.retry.vuga.viewmodel;

import androidx.lifecycle.ViewModel;

import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.CallBacks;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class BaseViewModel extends ViewModel {
    public CompositeDisposable disposable = new CompositeDisposable();


    public void registerUser(HashMap<String, RequestBody> hashMap, CallBacks.OnRegisterApi callBack) {

        disposable.add(RetrofitClient.getService().registerUser(hashMap).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    callBack.onSubscribe();
                })
                .doOnTerminate(() -> {
                    callBack.onTerminate();

//

                }).doOnError(throwable -> {
                    if (throwable != null) {
                        callBack.onError(throwable);

                    }
                })
                .subscribe((userRegistration, throwable) -> {

                    if (userRegistration != null && userRegistration.getData() != null) {
                        callBack.onSuccess(userRegistration.getData());

//

                    } else {
                        callBack.onError(throwable);
                    }


                }));


    }
}
