package cn.edots.bluetooth.v3;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import cn.edots.bluetooth.Handler;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Author Parck
 * @Date 2018/5/23.
 * @Description
 */

public class BlueV3Handler extends Handler {

    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public Observable<Set<BluetoothDevice>> startSearch() {
        return Observable.create(new Observable.OnSubscribe<Set<BluetoothDevice>>() {

            @Override
            public void call(Subscriber<? super Set<BluetoothDevice>> subscriber) {
                BlueV3Handler.this.devices.addAll(bluetoothAdapter.getBondedDevices());
                subscriber.onNext(bluetoothAdapter.getBondedDevices());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stopSearch() {
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public Observable write(final BluetoothDevice device, final byte[] bytes) {
        return Observable.create(new Observable.OnSubscribe<String>() {

            private OutputStream stream;

            @Override
            public void call(Subscriber<? super String> subscriber) {
                BluetoothSocket socket = null;
                try {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();
                    stream = socket.getOutputStream();
                    stream.write(bytes);
                    stream.flush();
                    subscriber.onNext("打印成功");
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                    subscriber.onCompleted();
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
