package cn.edots.bluetooth.v4;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BlueV4Handler extends Handler {

    protected Set<String> serviceUUIDs = new HashSet<>();
    protected Set<String> characteristicUUIDs = new HashSet<>();
    protected List<UUID> uuids = new ArrayList<>();
    public List<BluetoothGattService> services = new ArrayList<>();

    public static BlueV4Handler newInstance() {
        BlueV4Handler instance;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            instance = new H18apiSupport();
        } else instance = new L18apiSupport();
        for (String uuid : instance.serviceUUIDs) {
            try {
                instance.uuids.add(UUID.fromString(uuid));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public Observable write(final BluetoothDevice device, final byte[] bytes) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                device.connectGatt(context, false, new BluetoothGattCallback() {

                    private BluetoothGattCharacteristic currentCharacteristic;
                    private BluetoothGatt currentGatt;
                    private boolean done;

                    @Override//连接成功
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        gatt.discoverServices();
                    }

                    @Override//找到服务了
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        if (status == BluetoothGatt.GATT_SUCCESS) {   //找到服务了
                            for (BluetoothGattService service : gatt.getServices()) {
                                if (serviceUUIDs.contains(service.getUuid().toString().toUpperCase())
                                        || serviceUUIDs.contains(service.getUuid().toString().toLowerCase()))
                                    services.add(service);
                            }
                        }
                        for (BluetoothGattCharacteristic characteristic : services.get(0).getCharacteristics()) {
                            if (characteristicUUIDs.contains(characteristic.getUuid().toString().toLowerCase())
                                    || characteristicUUIDs.contains(characteristic.getUuid().toString().toUpperCase())) {
                                this.currentCharacteristic = characteristic;
                                this.currentGatt = gatt;
                                this.done = false;
                                writeCharacteristic();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        if (status == BluetoothGatt.GATT_SUCCESS) {// 写入成功
                            if (done) {
                                subscriber.onNext("打印完成");
                                gatt.disconnect();
                                gatt.close();
                                subscriber.onCompleted();
                            } else {
                                done = true;
                                writeCharacteristic();
                            }
                        } else if (status == BluetoothGatt.GATT_FAILURE) { // 写入失败
                            subscriber.onNext("打印失败");
                            gatt.disconnect();
                            gatt.close();
                            subscriber.onCompleted();
                        } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) { // 没有权限
                            subscriber.onNext("没有权限");
                            gatt.disconnect();
                            gatt.close();
                            subscriber.onCompleted();
                        }
                    }

                    private void writeCharacteristic() {
                        currentCharacteristic.setValue(bytes);
                        currentGatt.writeCharacteristic(currentCharacteristic);
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
