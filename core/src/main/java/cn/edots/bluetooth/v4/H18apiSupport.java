package cn.edots.bluetooth.v4;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @Author Parck
 * @Date 2018/5/23.
 * @Description
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class H18apiSupport extends BlueV4Handler {

    private BluetoothLeScanner scanner;
    private WCScanCallback scanCallback;

    @Override
    public Observable startSearch() {
        return Observable.create(new ObservableOnSubscribe<Set<BluetoothDevice>>() {
            @Override
            public void subscribe(ObservableEmitter<Set<BluetoothDevice>> subscriber) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                if (scanner == null) return;
                scanCallback = new WCScanCallback(subscriber);
                List<ScanFilter> filters = new ArrayList<>();
                ParcelUuid uuid = new ParcelUuid(uuids.get(0));
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(uuid).build();
                filters.add(filter);
                ScanSettings settings = new ScanSettings.Builder().build();
                scanner.startScan(filters, settings, scanCallback);
            }
        });
    }

    @Override
    public void stopSearch() {
        if (bluetoothAdapter.isEnabled() && scanner != null && scanCallback != null)
            scanner.stopScan(scanCallback);
    }


    // =============================================================================================
    // inner class
    // =============================================================================================
    class WCScanCallback extends ScanCallback {
        private ObservableEmitter<? super Set<BluetoothDevice>> subscriber;

        public WCScanCallback(ObservableEmitter<? super Set<BluetoothDevice>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            devices.add(result.getDevice());
            subscriber.onNext(devices);
            subscriber.onComplete();
            scanner.stopScan(this);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            subscriber.onError(null);
            subscriber.onComplete();
            scanner.stopScan(this);
        }
    }
}
