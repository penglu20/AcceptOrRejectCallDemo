A demo for accepting or rejecting a call in android.

The core function is in IncomingPresenter.java , named acceptCall() and rejectCall() .

PhoneListener is a BroadcastReceiver Listening to the call and starting the IncomingCallActivity.

If this demo can not work in your phone, please :

1. make sure it has get the full permissions ;
2. increase the sleep time in PhoneListener.
3. If it still does not work , please report a issue.


If you use the same code in a service such as automatic answering , acceptCall()  may not work , but you may do as follows:

1. start a empty small(1px*1px) activity with no content in it;
2. call the  acceptCall() after this activity started;
3. finish this activity when phone state changes.



Get some introduction from [Android通过程序接听或者挂断电话](http://www.jianshu.com/p/ab88284384e9).