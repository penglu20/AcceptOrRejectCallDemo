package com.pl.acceptorrejectcalldemo.incomingcall;


import com.pl.acceptorrejectcalldemo.BasePresenter;
import com.pl.acceptorrejectcalldemo.BaseView;

/**
 * Created by penglu on 2016/5/24.
 */
public class IncomingContract {
    public interface View extends BaseView<Presenter> {
    }
    public interface Presenter extends BasePresenter {
        void acceptCall();
        void rejectCall();
    }
}
