package id.sashini.code.miband2.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import id.sashini.code.miband2.R;
import id.sashini.code.miband2.Services.HeartRateService;

public class HeartRateActivity extends AppCompatActivity {

    private TextView heraRateText;
    private TextView deviceText;
    private Button connectButton;
    private Button startButton;
    private Button stopButton;
    private EditText thresholdRate;

    private Intent serviceIntent;


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_HEART_RATE_THRESHOLD = "MAX_HR";
    public static final int DEFAULT_HR = 90;

    BluetoothAdapter bluetoothAdapter;

    private String mDeviceName;
    private String mDeviceAddress;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Heart Rate Monitor");
        setSupportActionBar(toolbar);

        initializeObjects();
        initViews();
        initListeners();
    }

    private void initViews()
    {
        heraRateText=findViewById(R.id.textView_rate);
        deviceText=findViewById(R.id.textView_device);
        connectButton=findViewById(R.id.button_connect);
        startButton=findViewById(R.id.button_start);
        stopButton=findViewById(R.id.button_stop);
        thresholdRate=findViewById(R.id.editText_treashold);

        thresholdRate.setText("90");
    }

    private void initListeners()
    {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBoundedDevice();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(serviceIntent);
            }
        });
    }

    private void startService()
    {

        int rate=DEFAULT_HR;
        try {
            rate=Integer.parseInt(thresholdRate.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        serviceIntent =new Intent(this, HeartRateService.class);
        if(mDeviceAddress!=null && bluetoothAdapter.isEnabled())
        {
            Toast.makeText(this,"Connecting",Toast.LENGTH_SHORT).show();
            serviceIntent.putExtra(EXTRAS_DEVICE_ADDRESS,mDeviceAddress);
            serviceIntent.putExtra(EXTRAS_HEART_RATE_THRESHOLD,rate);
            startService(serviceIntent);
        }
        else {
            Toast.makeText(this,"No Mi band connected, Pair device first",Toast.LENGTH_SHORT).show();
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    void getBoundedDevice() {

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Toast.makeText(this,"Checking available devices",Toast.LENGTH_SHORT).show();

        if(!bluetoothAdapter.isEnabled())
        {
            deviceText.setText("Turn on the bluetooth");
            Toast.makeText(this,"Turn on the bluetooth to scan",Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                mDeviceAddress=bd.getAddress();
                deviceText.setText(bd.getName()+" connected");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(HeartRateService.HR_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister local broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String date = intent.getStringExtra(HeartRateService.HEART_RATE);
            heraRateText.setText(date);
        }
    };



}
