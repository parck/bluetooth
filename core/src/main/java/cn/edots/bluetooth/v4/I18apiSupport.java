package cn.edots.bluetooth.v4;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class I18apiSupport extends H18apiSupport {

    @Override
    public Observable write(final BluetoothDevice device, final byte[] bytes) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> subscriber) {
                device.connectGatt(context, false, new BluetoothGattCallback() {

                    private BluetoothGattCharacteristic currentCharacteristic;
                    private BluetoothGatt currentGatt;
                    private boolean done;
                    private int writeSize = 0;

                    @Override//连接成功
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (BluetoothGatt.STATE_CONNECTED == newState) gatt.discoverServices();
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
                                writeCharacteristic(0);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        if (status == BluetoothGatt.GATT_SUCCESS) {// 写入成功
                            if (writeSize >= bytes.length) {
                                subscriber.onNext("打印完成");
                                gatt.disconnect();
                                gatt.close();
                                subscriber.onComplete();
                            } else {
                                writeCharacteristic(writeSize);
                            }
                        } else if (status == BluetoothGatt.GATT_FAILURE) { // 写入失败
                            subscriber.onNext("打印失败");
                            gatt.disconnect();
                            gatt.close();
                            subscriber.onComplete();
                        } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) { // 没有权限
                            subscriber.onNext("没有权限");
                            gatt.disconnect();
                            gatt.close();
                            subscriber.onComplete();
                        }
                    }

                    private void writeCharacteristic(int index) {
                        byte[] data = new byte[20];
                        for (int i = 0; i < data.length; i++) {
                            if ((index + i) < bytes.length) {
                                data[i] = bytes[index + i];
                                writeSize++;
                            } else break;

                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Log.d("I18apiSupport", e.getMessage() == null ? "" : e.getMessage());
                        }
                        currentCharacteristic.setValue(data);
                        currentGatt.writeCharacteristic(currentCharacteristic);
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
