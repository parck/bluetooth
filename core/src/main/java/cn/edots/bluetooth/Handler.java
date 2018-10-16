package cn.edots.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import java.util.HashSet;
import java.util.Set;

import cn.edots.bluetooth.v3.BlueV3Handler;
import cn.edots.bluetooth.v4.BlueV4Handler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author Parck
 * @Date 2018/5/23.
 * @Description
 */

public abstract class Handler {

    protected Context context;
    protected BluetoothAdapter bluetoothAdapter;
    protected Set<BluetoothDevice> devices;

    public static Handler newInstance(Context context,String[] uuids) {
        Handler instance;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            instance = BlueV4Handler.newInstance(uuids);
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
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) {
                boolean enable = bluetoothAdapter.enable();
                int i = 0;
                if (!enable) {
                    while (i < 5) {
                        enable = bluetoothAdapter.enable();
                        if (enable) break;
                        i++;
                    }
                }
                if (!enable) {
                    subscriber.onError(null);
                    subscriber.onComplete();
                    return;
                }
                try {
                    Thread.sleep(3000);//隔1s
                    subscriber.onNext(true);
                    subscriber.onComplete();
                } catch (InterruptedException e) {
                    Log.d("Handler", e.getMessage() == null ? "" : e.getMessage());
                    subscriber.onError(e);
                    subscriber.onComplete();
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }
}
