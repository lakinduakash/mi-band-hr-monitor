package id.sashini.code.miband2.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;

import id.sashini.code.miband2.Activities.HeartRateActivity;
import id.sashini.code.miband2.Helpers.CustomBluetoothProfile;
import id.sashini.code.miband2.R;

public class HeartRateService extends Service {

    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;
    BluetoothAdapter bluetoothAdapter;


    NotificationCompat.Builder builder;


    private final IBinder binder = new LocalBinder();

    public static final String NOTIFICATION_CHANNEL="my_channel";
    public static final int NOTIFICATION_ID=5;

    public static final String HR_ACTION="id.aashari.code.miband2.HR";
    public static final String HEART_RATE="HEART_RATE";

    int max_hr;

    MediaPlayer player;


    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {


        initializeObjects();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, "Heart Rate Service", NotificationManager.IMPORTANCE_LOW));
        }

    }

    public class LocalBinder extends Binder {
        HeartRateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HeartRateService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        max_hr = intent.getIntExtra(HeartRateActivity.EXTRAS_HEART_RATE_THRESHOLD,HeartRateActivity.DEFAULT_HR);

        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText("Started")
                .setContentTitle("Connecting")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        startConnecting(intent);

        return super.onStartCommand(intent, flags, startId);
    }


    void startConnecting(Intent intent) {

        String address = intent.getStringExtra(HeartRateActivity.EXTRAS_DEVICE_ADDRESS);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        Log.d("connected","bluetoothGatt");

        builder.setTicker("Connected").setContentTitle("Connected");
        startForeground(NOTIFICATION_ID,builder.build());
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();

    }

    @Override
    public void onDestroy() {
        stopPlay();
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        Log.d("disconnected","bluetoothGatt");
        super.onDestroy();
    }


    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }



    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            //txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();

            builder.setContentText("Heart Rate: " + data[1])
                    .setSubText(Arrays.toString(data));
            startForeground(NOTIFICATION_ID, builder.build());

            sendBroadCast(""+data[1]);

            if(max_hr<data[1])
                playSound();

            Log.v("rate", Arrays.toString(data));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };


    public void sendBroadCast(String value)
    {
        Intent intent = new Intent();
        intent.setAction(HR_ACTION);
        intent.putExtra(HEART_RATE,value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void playSound()
    {

        if(!player.isPlaying()){
            player.setLooping(true);
            player.start();
        }
        Log.v("play", "Sound play");
    }

    private void stopPlay()
    {
        if(player.isPlaying())
            player.stop();
    }
}
