package cn.edots.bluetooth.v3;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import cn.edots.bluetooth.Handler;
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

public class BlueV3Handler extends Handler {

    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public Observable<Set<BluetoothDevice>> startSearch() {
        return Observable.create(new ObservableOnSubscribe<Set<BluetoothDevice>>() {

            @Override
            public void subscribe(ObservableEmitter<Set<BluetoothDevice>> subscriber) {
                BlueV3Handler.this.devices.addAll(bluetoothAdapter.getBondedDevices());
                subscriber.onNext(bluetoothAdapter.getBondedDevices());
                subscriber.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stopSearch() {
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public Observable write(final BluetoothDevice device, final byte[] bytes) {
        return Observable.create(new ObservableOnSubscribe<String>() {

            private OutputStream stream;

            @Override
            public void subscribe(ObservableEmitter<String> subscriber) {
                BluetoothSocket socket = null;
                try {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();
                    stream = socket.getOutputStream();
                    stream.write(bytes);
                    stream.flush();
                    subscriber.onNext("打印成功");
                    subscriber.onComplete();
                } catch (IOException e) {
                    Log.d("BlueV3Handler", e.getMessage() == null ? "" : e.getMessage());
                    subscriber.onError(e);
                    subscriber.onComplete();
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            Log.d("BlueV3Handler", e.getMessage() == null ? "" : e.getMessage());
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.d("BlueV3Handler", e.getMessage() == null ? "" : e.getMessage());
                        }
                    }
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
