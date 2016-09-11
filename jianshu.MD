这篇文章教你如何帮助用户自动接听或者挂断来电。当然并不是我原创的代码，我只不过是把stackoverflow上的一些代码整合了一下，做个代码的二传手。

###源码
[AcceptOrRejectCallDemo](https://github.com/l465659833/AcceptOrRejectCallDemo)
源码中用了MVP的模式，只是最简单的使用，如果不熟悉的话刚好可以学学，逻辑部分在IncomingPresenter类中。


###首先需要监听来电的广播

在AndroidManifest文件中添加：

```xml
<receiver android:name=".incomingcall.PhoneListener">
    <intent-filter android:priority="1000">
        <action android:name="android.intent.action.PHONE_STATE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.NEW_OUTGOING_CALL" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

当然还有权限：

```
    <uses-permission android:name="android.permission.MODIFY_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

然后实现PhoneListener来实现来电监听:

```java

public class PhoneListener extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("PhoneListener",action);
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
        } else {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            String incoming_number = "";
            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    incoming_number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    try {
                        //3s后再开启activity，是为了挡在系统的接听界面之前
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent tmpI = new Intent(context, IncomingCallActivity.class);
                    tmpI.putExtra(IncomingCallActivity.INCOMING_CALL_NAME,incoming_number);
                    tmpI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(tmpI);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}

```

###然后就是接听或者挂断了

#####先说挂断

新建aidl文件：

```
package com.android.internal.telephony;
interface ITelephony{
    boolean endCall();
    void answerRingingCall();
}
```

要注意包路径不能修改，要放置到正确路径下才行哦。

然后在需要挂断的时候，调用：

```java
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

```

很简单吧，其实就是通过反射的方式盗用Android系统的aidl通信来实现挂断的功能。

#####再说接听

看到挂断电话的代码后大家都能猜到，只要同样调用aidl的answerRingingCall()方法就可以接听了，是的，在4.1一下的版本中只有这样就行了：


```java
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
```

可惜在4.1以上的版本中，谷歌给这个方法的调用设置了权限，如果不是系统应用，会收到permissDeny的异常。
不过没关系，这当然难不倒我们，看代码：

```
    private static final String MANUFACTURER_HTC = "HTC";
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
        
```

看代码很容易看出来，这里实际上是通过模拟无线耳机的按键来接听电话的。而对HTP的手机，需要进行一点特殊的处理，也就是通过广播的形式，让手机误以为连上了无线耳机。

以上功能在6.0的三星S6上实测过，也在许多其他我能拿到的手机上测试过，都可以正常挂断和接听。不过没测试过的手机肯定更多，如果无法接听可以给我留言，咱们一起研究。
不过请首先确保只是接听或者挂断功能不正常，而不是监听来电的功能不正常。

###坑

我们做自动接听或者挂断的功能的时候，有可能是没有界面展示的，而是在service中直接就帮用户接听了，这种情况下是无法正常工作的，可能是android对非前台的应用做了一些限制。
怎么办呢？其实只要创建一个Activity就行了：

1. 监听到来电的时候，创建并显示一个1px*1px大小的空Activity；
2. 在这个Activity中调用上述的acceptCall()方法;
3. 监听到电话状态改变（被接听或者被挂断）以后，finish()掉这个Activity。