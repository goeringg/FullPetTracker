package com.example.pettrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Thread.sleep;

public class LocationStringActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    Marker petMarker;
    Marker phoneMarker;
    LocationListener locationListener;
    LocationManager mLocationManager;

    PendingIntent permissionIntent;
    UsbDevice device;
    FT_Device ftDev = null;
    D2xxManager ftD2xx;
    Context serial_context;
    UsbManager manager;
    UsbDeviceConnection connection;

    Handler locationHandler;
    LocationHandlerThread locationHandlerThread;

    Toast toast;
    int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public static final int readLength = 23;

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

                            try {
                                ftD2xx = D2xxManager.getInstance(serial_context);
                                ftD2xx.createDeviceInfoList(serial_context);

                                ftDev = ftD2xx.openByIndex(serial_context, 0);

                                toast = Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_LONG);

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
        setContentView(R.layout.activity_location_string);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);

        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng phoneLocation = new LatLng(location.getLatitude(), location.getLongitude());
                phoneMarker.setPosition(phoneLocation);

            }
        };

        serial_context = getApplicationContext();


        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        /*HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
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
        }*/

        locationHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                Double latitude = bundle.getDouble("lat");
                Double longitude = bundle.getDouble("long");
                LatLng location = new LatLng(latitude, longitude);
                petMarker.setPosition(location);
            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng gunnison = new LatLng(38.5, -106.5);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(gunnison , 14.0f));
        petMarker = map.addMarker(new MarkerOptions().position(new LatLng(13,0)).title("pet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        phoneMarker = map.addMarker(new MarkerOptions().position(gunnison).title("phone")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    public class LocationHandlerThread extends HandlerThread {

        public LocationHandlerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                iavailable = ftDev.getQueueStatus();

                if (iavailable > 0) {
                    if (iavailable > readLength) {
                        iavailable = readLength;
                    }
                }
                readData = new byte[readLength];
                readDataToText = new char[readLength];
                ftDev.read(readData, iavailable);
                for (int i = 0; i < iavailable; i++) {
                    readDataToText[i] = (char) readData[i];
                }

                //toast = Toast.makeText(getApplicationContext(), Arrays.toString(readDataToText), Toast.LENGTH_SHORT);
                //toast.show();
                String locationString = new String(readDataToText);

                //String locationString = Arrays.toString(readDataToText);
                //locationString = locationString.substring(1, locationString.length()-1);
                //textView.setText(locationString);

                //Grayson's code to be written here!

                String[] petLocationArray = locationString.split("/");

                double petLat;
                double petLong;
                if (petLocationArray.length != 2) {
                    petLat = 33.72002356224238;
                    petLong = -39.08091233839987;
                } else {
                    petLat = Double.parseDouble(petLocationArray[0]);
                    petLong = Double.parseDouble(petLocationArray[1]);
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", petLat);
                bundle.putDouble("long", petLong);
                message.setData(bundle);
                locationHandler.sendMessage(message);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHandlerThread.quit();
    }
}