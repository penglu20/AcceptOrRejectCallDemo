package com.pl.acceptorrejectcalldemo.incomingcall;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.internal.telephony.ITelephony;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by penglu on 2016/5/24.
 */
public class IncomingPresenter implements IncomingContract.Presenter{
    private static final String TAG="IncomingPresenter";

    private static final String MANUFACTURER_HTC = "HTC";
    private IncomingContract.View view;
    private KeyguardManager keyguardManager;
    private AudioManager audioManager;
    private CallStateReceiver callStateReceiver;

    public IncomingPresenter(IncomingContract.View view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void resume() {
        updateWindowFlags();
        registerCallStateReceiver();
    }

    @Override
    public void destroy() {
        if (callStateReceiver != null) {
            view.getActivity().unregisterReceiver(callStateReceiver);
            callStateReceiver = null;
        }
    }


    private void registerCallStateReceiver() {
        if (callStateReceiver==null) {
            callStateReceiver = new CallStateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            view.getActivity().registerReceiver(callStateReceiver, intentFilter);
        }
    }


    private void updateWindowFlags() {
        //通过设置windowFlag，可以在锁屏的时候直接显示出界面，具体效果试一下就知道了
        keyguardManager = (KeyguardManager)view.getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        audioManager = (AudioManager) view.getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            view.getActivity().getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            view.getActivity().getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }


    public void acceptCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.answerRingingCall();
        } catch (Exception e) {
            Log.e(TAG, "for version 4.1 or larger");
            acceptCall_4_1();
        }
    }

    public void acceptCall_4_1() {
        //模拟无线耳机的按键来接听电话
        // for HTC devices we need to broadcast a connected headset
        boolean broadcastConnected = MANUFACTURER_HTC.equalsIgnoreCase(Build.MANUFACTURER)
                && !audioManager.isWiredHeadsetOn();
        if (broadcastConnected) {
            broadcastHeadsetConnected(false);
        }
        try {
            try {
                Runtime.getRuntime().exec("input keyevent " +
                        Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
            } catch (IOException e) {
                // Runtime.exec(String) had an I/O problem, try to fall back
                String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_HEADSETHOOK));
                Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                KeyEvent.KEYCODE_HEADSETHOOK));
                view.getActivity().sendOrderedBroadcast(btnDown, enforcedPerm);
                view.getActivity().sendOrderedBroadcast(btnUp, enforcedPerm);
            }
        } finally {
            if (broadcastConnected) {
                broadcastHeadsetConnected(false);
            }
        }
    }

    @Override
    public void rejectCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "", e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "", e);
        } catch (Exception e) {
        }
    }

    private void broadcastHeadsetConnected(boolean connected) {
        Intent i = new Intent(Intent.ACTION_HEADSET_PLUG);
        i.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        i.putExtra("state", connected ? 1 : 0);
        i.putExtra("name", "mysms");
        try {
            view.getActivity().sendOrderedBroadcast(i, null);
        } catch (Exception e) {
        }
    }



    private class CallStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getAction();
            //无论接听还是挂断，都应该关闭界面
            view.getActivity().finish();
        }
    }



}
