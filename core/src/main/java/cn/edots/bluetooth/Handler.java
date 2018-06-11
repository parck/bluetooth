package cn.edots.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;


import java.util.HashSet;
import java.util.Set;

import cn.edots.bluetooth.v3.BlueV3Handler;
import cn.edots.bluetooth.v4.BlueV4Handler;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Author Parck
 * @Date 2018/5/23.
 * @Description
 */

public abstract class Handler {

    protected Context context;
    protected BluetoothAdapter bluetoothAdapter;
    protected Set<BluetoothDevice> devices;

    public static Handler newInstance(Context context) {
        Handler instance;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            instance = BlueV4Handler.newInstance();
        } else instance = new BlueV3Handler();
        instance.context = context;
        instance.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        instance.devices = new HashSet<>();
        return instance;
    }

    public abstract Observable startSearch();

    public abstract void stopSearch();

    public abstract Observable write(BluetoothDevice device, byte[] bytes);

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public Observable open() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean enable = bluetoothAdapter.enable();
                int i = 0;
                if (!enable) {
                    while (i < 5) {
                        enable = bluetoothAdapter.enable();
                        if (enable) break;
                        i++;
                    }
                }
                try {
                    Thread.sleep(3000);//隔1s
                    subscriber.onNext(enable);
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }
}
