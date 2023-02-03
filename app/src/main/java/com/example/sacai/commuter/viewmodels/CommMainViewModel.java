package com.example.sacai.commuter.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommMainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> result = new MutableLiveData<Boolean>();
    private final MutableLiveData<String> msg = new MutableLiveData<String>();
    public void setData(boolean item) {
        result.setValue(item);
    }
    public LiveData<Boolean> getResult() {
        return result;
    }

    public void setMsg(String item) {
        msg.setValue(item);
    }

    public LiveData<String> getMsg() {
        return msg;
    }
}
