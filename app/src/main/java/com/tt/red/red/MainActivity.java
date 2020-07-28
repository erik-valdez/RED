package com.tt.red.red;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "Main";

    StringBuilder messange;
    static final String endLine="\n";
    BluetoothConnectionService mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter=null;

    Marker marker_RED=null;
    Calendar rightnow;
    SimpleDateFormat mdformat;
    GoogleMap googleMap;
    ImageButton btnMove,btnSpeed,btnBack,btnStop,btnClean;
    MapView mapView;
    ProgressBar pbSpeed, pbBatterycar, pbBatteryBlu, pbContainer;
    ImageView ivAlert, ivSyncAlert,ivIcon;
    TextView tvState,tvtime;
    public LatLng RED = new LatLng(0,0);//(19.4111111, -99.20277777777778);
    static final LatLng USR = new LatLng(0,0);//19.4111111, -99.20277777777778);

    private final String Tag = "main";
    private final String msj0 = "0:0:0:0:0:0,0:0:0"+endLine;
    public int Emergency = 0;
    public int BatteryCar = 0;
    public int BatteryBlu = 0;
    public int Speed = 0;
    public int Container = 0;
    public double latitudRed = 19.411094;
    public double longitudRed = -99.199045;
    public int Communication = 0;
    public int State = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tvtime=findViewById(R.id.id_tvTime);
        btnBack=findViewById(R.id.id_btnBack);
        btnClean=findViewById(R.id.id_btnClean);
        btnMove=findViewById(R.id.id_btnMove);
        btnStop=findViewById(R.id.id_btnStop);
        btnSpeed=findViewById(R.id.id_btnSpeed);
        pbSpeed = findViewById(R.id.id_pbSpeed);
        pbBatterycar = findViewById(R.id.id_pbBatteryCar);
        pbBatteryBlu = findViewById(R.id.id_pbBatteryBlu);
        pbContainer = findViewById(R.id.id_pbContainer);
        ivAlert = findViewById(R.id.id_ivAlert);
        ivIcon = findViewById(R.id.id_ivIcon);
        ivSyncAlert = findViewById(R.id.id_ivSyncAlert);
        tvState = findViewById(R.id.id_tvState);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        messange=new StringBuilder();

        ivIcon.setImageResource(R.mipmap.icono);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("readMessage"));
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver,filter);

        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("No compatible")
                    .setMessage("Este telefono no es compatible con bluetooth")
                    .setPositiveButton("salir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);

        }else{
            Log.d("Main", "iniciando funcion Bleutooth Conection service");

            mBluetoothConnection.start();
        }

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnection.write("A");
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnection.write("B");
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnection.write("C");
            }
        });
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnection.write("D");
            }
        });
        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnection.write("E");
            }
        });


        try {
            if (googleMap == null) {
                mapView = findViewById(R.id.id_mvMapa);
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error llamanado callback: " + e);

        }

        decodification(msj0);

    }//end on create

    @Override
    protected  void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        Log.e(TAG,"onMapReady");
        if(marker_RED!=null){
            marker_RED.remove();
        }

        //googleMap.clear();
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RED,17));

        /*final Marker marker_USR = googleMap.addMarker(new MarkerOptions()
                .position(USR)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_video_input_antenna_white_18dp))
        );*/


        marker_RED = googleMap.addMarker(new MarkerOptions()
                .position(RED)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_chevron_down_white_48dp))
        );
        }

    BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text= intent.getStringExtra("the message");
            messange.append(text);
            //txvDisplay.setText(messange);
            Log.i("Main", "broadcastReceiver:"+messange);
            int endOfLineIndex = messange.indexOf(endLine);

            if (endOfLineIndex > 0) {
                String dataInPrint = messange.substring(0, endOfLineIndex);
                Log.d("Main", "string final="+dataInPrint);


                decodification(dataInPrint);
                //txvDisplay.setText("Dato: " + dataInPrint);-------------------------------------------------------------

                messange.delete(0, messange.length());

            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(Tag,action);

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4

                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                     Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                     Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode ==1 ) {
            if (resultCode == RESULT_CANCELED) {

                new AlertDialog.Builder(this)
                        .setTitle("Permiso denegado")
                        .setMessage("Aplicación cancelada")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else{
                Log.d("Main", "iniciando funcion Bluetooth Conection service");

                mBluetoothConnection.start();
            }

        }
    }//end onActivityResult



    public void decodification(String msj){

        String aux = msj;
        for (int i = 0; i < 8; i++) {
            int index = aux.indexOf(':');//Log.e(Tag,aux);
            switch (i) {
                case 0://bateryBluetooth level
                    try {
                        BatteryBlu = Integer.parseInt(aux.substring(0, index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 0 error format");
                        return;
                    }
                    pbBatteryBlu.setProgress(BatteryBlu);
                    Log.v(TAG,"case 0 ok");
                    break;
                case 1://batterycar level
                    try{
                    BatteryCar = Integer.parseInt(aux.substring(0, index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 1 error format");
                        return;
                    }
                    pbBatterycar.setProgress(BatteryCar);
                    Log.v(TAG,"case 1 ok");
                    break;
                case 2:
                    try{
                    Emergency = Integer.parseInt(aux.substring(0, index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 2 error format");
                        return;
                    }
                    if (Emergency == 1) {
                        ivAlert.setImageResource(R.mipmap.ic_alert_yellow600_36dp);
                    } else {
                        ivAlert.setImageResource(R.mipmap.ic_alert_grey600_36dp);
                    }
                    Log.v(TAG,"case 2 ok");
                    break;
                case 3:
                    try{
                    Speed = Integer.parseInt(aux.substring(0, index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 3 error format");
                        return;
                    }
                    pbSpeed.setProgress(Speed);
                    Log.v(TAG,"case 3 ok");
                    break;
                case 4:
                    try{
                    Container = Integer.parseInt(aux.substring(0, index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 4 error format");
                        return;
                    }
                    pbContainer.setProgress(Container);
                    Log.v(TAG,"case 4 ok");
                    break;
                case 5:

                    String temp = aux.substring(0, index);
                    int ind = temp.indexOf(',');
                    try{
                    latitudRed = Double.valueOf(temp.substring(0, ind));
                    longitudRed = Double.valueOf(temp.substring(ind + 1));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 5 error format");
                        return;
                    }
                    RED = new LatLng(latitudRed,longitudRed);
                    mapView.getMapAsync(this);
                    Log.v(TAG,"case 5 ok");

                    break;
                case 6:
                    try{
                    State=Integer.parseInt(aux.substring(0,index));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 6 error format");
                        return;
                    }
                    switch (State){
                        case 0:
                            tvState.setText("Estado: Detenido");
                            break;
                        case 1:
                            tvState.setText("Estado: Limpiado");
                            break;
                        case 2:
                            tvState.setText("Estado: Regresando");
                            break;
                        case 3:
                            tvState.setText("Estado: Avanzando");
                            break;
                    }
                    Log.v(TAG,"case 6 ok");
                    break;
                case 7:

                    try{
                    Communication =Integer.parseInt(aux.substring(0,aux.length()-1));
                    }catch (NumberFormatException e){
                        Log.e(TAG,"case 7 error format");
                        return;
                    }
                    if (Communication==0){
                        ivSyncAlert.setImageResource(R.mipmap.ic_sync_alert_grey600_36dp);
                    }else{
                        ivSyncAlert.setImageResource(R.mipmap.ic_sync_alert_red600_36dp);
                    }
                    Log.v(TAG,"case 7 ok");
                    break;
                default:
            }//end switch
            aux=aux.substring(index+1);
        }//end for
        rightnow= Calendar.getInstance();
        mdformat = new SimpleDateFormat("HH:mm:ss");
        String time =mdformat.format(rightnow.getTime());
        tvtime.setText(time);
        Log.v(Tag,time);


    }


}
