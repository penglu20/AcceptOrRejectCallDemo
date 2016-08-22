package com.pl.acceptorrejectcalldemo.incomingcall;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.pl.acceptorrejectcalldemo.R;
import com.pl.acceptorrejectcalldemo.databinding.ActivityIncomingCallBinding;


public class IncomingCallActivity extends Activity implements IncomingContract.View{
    public static final String INCOMING_CALL_NAME="incoming_call_name";

    private IncomingContract.Presenter presenter;
    private ActivityIncomingCallBinding binding;
    private IncomingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this, R.layout.activity_incoming_call);
        presenter=new IncomingPresenter(this);
        viewModel=new IncomingViewModel(presenter);

        Intent intent=getIntent();
        String name=intent.getStringExtra(INCOMING_CALL_NAME);
        viewModel.name.set(name);
        binding.setViewModel(viewModel);
        presenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }

    @Override
    public void setPresenter(IncomingContract.Presenter presenter) {
        this.presenter=presenter;
    }

    @Override
    public Activity getActivity() {
        return this;
    }



}