package com.retry.vuga.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public MutableLiveData<Boolean> blurScreen = new MutableLiveData<>(true);
    public MutableLiveData<Boolean> hideTopBar = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> hideBottomSheet = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> hideNavBar = new MutableLiveData<>(false);

}
