package cn.edots.bluetooth.v4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Set;
import java.util.UUID;

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

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class L18apiSupport extends BlueV4Handler {

    private WCLeScanCallback scanCallback;

    @Override
    public Observable<Set<BluetoothDevice>> startSearch() {
        return Observable.create(new ObservableOnSubscribe<Set<BluetoothDevice>>() {
            @Override
            public void subscribe(ObservableEmitter<Set<BluetoothDevice>> subscriber) {
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

        private ObservableEmitter<? super Set<BluetoothDevice>> subscriber;

        public WCLeScanCallback(ObservableEmitter<? super Set<BluetoothDevice>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
            devices.add(device);
            subscriber.onNext(devices);
            subscriber.onComplete();
            bluetoothAdapter.stopLeScan(this);
        }
    }
}
