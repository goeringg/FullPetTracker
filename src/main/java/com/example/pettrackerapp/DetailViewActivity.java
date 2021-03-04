package com.example.pettrackerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.HashMap;
import java.util.Iterator;

public class DetailViewActivity extends AppCompatActivity {

    String name;
    String type;
    int id;
    TextView petNameTextView;
    TextView petTypeTextView;

    //device globals
    PendingIntent permissionIntent;
    UsbDevice device;
    FT_Device ftDev = null;
    D2xxManager ftD2xx;
    Context serial_context;
    UsbManager manager;
    UsbDeviceConnection connection;


    Toast toast;

    private static final String ACTION_USB_PERMISSION = "com.example.prototype1.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            petNameTextView.setText("dit it");
                            try {
                                ftD2xx = D2xxManager.getInstance(serial_context);
                                ftD2xx.createDeviceInfoList(serial_context);

                                ftDev = ftD2xx.openByIndex(serial_context, 0);

                                toast = Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT);

                                toast.show();

                                ftDev.setBaudRate(9600);
                                //locationHandlerThread = new LocationHandlerThread("name");
                                //locationHandlerThread.start();

                            } catch (D2xxManager.D2xxException e) {
                                e.printStackTrace();
                                toast = Toast.makeText(getApplicationContext(), "cannot connect", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }
                    else{

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        Intent intent = getIntent();
        petNameTextView = findViewById(R.id.DetailPetNameTextView);
        petTypeTextView = findViewById(R.id.DetailPetTypeTextView);

        serial_context = getApplicationContext();


        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);


        id = intent.getIntExtra("_id", 0);
        name = intent.getStringExtra("name");
        type = intent.getStringExtra("type");
        petNameTextView.setText(name);
        petTypeTextView.setText(type);


    }

    public void onRadioSetup(View view){
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();

        }
        if(device!=null){
            manager.requestPermission(device, permissionIntent);

            connection = manager.openDevice(device);

            //SerialReadAsyncTask serialReadAsyncTask = new SerialReadAsyncTask();
            //serialReadAsyncTask.execute("");
            //run thread task
        }
    }

    public void onLaunchLocation(View view){
        Intent intent = new Intent(getApplicationContext(), LocationStringActivity.class);
        startActivity(intent);
    }
}