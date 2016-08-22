package com.pl.acceptorrejectcalldemo.incomingcall;

import android.database.Observable;
import android.databinding.ObservableField;
import android.view.View;

/**
 * Created by penglu on 2016/5/24.
 */
public class IncomingViewModel extends Observable {
    private IncomingContract.Presenter presenter;
    public IncomingViewModel(IncomingContract.Presenter presenter){
        this.presenter=presenter;
    }
    public ObservableField<String> name=new ObservableField<>();
    public void acceptCall(View view){
        presenter.acceptCall();
    }
    public void rejectCall(View view){
        presenter.rejectCall();
    }
}
