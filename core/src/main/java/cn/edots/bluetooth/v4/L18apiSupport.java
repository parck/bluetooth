package cn.edots.bluetooth.v4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Set;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Author Parck
 * @Date 2018/5/23.
 * @Description
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class L18apiSupport extends BlueV4Handler {

    private WCLeScanCallback scanCallback;

    @Override
    public Observable<Set<BluetoothDevice>> startSearch() {
        return Observable.create(new Observable.OnSubscribe<Set<BluetoothDevice>>() {
            @Override
            public void call(final Subscriber<? super Set<BluetoothDevice>> subscriber) {
                scanCallback = new WCLeScanCallback(subscriber);
                bluetoothAdapter.startLeScan(uuids.toArray(new UUID[uuids.size()]), scanCallback);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stopSearch() {
        if (scanCallback != null) bluetoothAdapter.stopLeScan(scanCallback);
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    class WCLeScanCallback implements BluetoothAdapter.LeScanCallback {

        private Subscriber<? super Set<BluetoothDevice>> subscriber;

        public WCLeScanCallback(Subscriber<? super Set<BluetoothDevice>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
            devices.add(device);
            subscriber.onNext(devices);
            subscriber.onCompleted();
            bluetoothAdapter.stopLeScan(this);
        }
    }
}
