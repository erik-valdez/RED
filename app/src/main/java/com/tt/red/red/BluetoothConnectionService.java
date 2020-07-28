package com.tt.red.red;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionService {
    //varibles

    private BluetoothAdapter mBluetoothAdapter=null;
    private BluetoothSocket mBluetoothSocket = null;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    BluetoothDevice device;
    Context mContext;
    public boolean flagLC=false;



    Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder DataStringIN = new StringBuilder();

    private static final UUID mUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String btSlaveAddres="FC:A8:9A:00:46:79";
    String TAG="BluetoothConnectionService";

    public BluetoothConnectionService(Context context) {
        Log.d(TAG, "Bluetooth connection service constructor starts");
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mConnectThread=new ConnectThread();
        mConnectThread.run();


    }//endStart

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }//end Connected


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(mUuid);
    }
    private class ConnectThread extends Thread {


        public ConnectThread() {
            Log.d(TAG, "ConnectThread: started.");
            BluetoothDevice device1 = mBluetoothAdapter.getRemoteDevice(btSlaveAddres);
            device=device1;
        }

        @Override
        public void run() {
            super.run();
            try {
                Log.e(TAG,"Creando Socket");
                mBluetoothSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Log.e(TAG,"La creación del socket falló");

            }
            try {
                Log.d(TAG,"Intentando conexion al socket");
                mBluetoothSocket.connect();
                if(mBluetoothSocket.isConnected()){
                    Toast.makeText(mContext, "Conexión con el receptor exitosa ", Toast.LENGTH_LONG).show();
                    Log.e(TAG,"conección con el receptor exitosa");
                    connected(mBluetoothSocket,device);
                }
            } catch (IOException e) {
                Log.e(TAG,"La conección al socket falló");
                try {
                    Log.e(TAG,"cerrando el socket");
                    mBluetoothSocket.close();
                    new AlertDialog.Builder(mContext)
                            .setTitle("No conectado")
                            .setMessage("No se detectó el bluetooth del receptor")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);
                                }
                            })
                            .setIcon(android.R.drawable.ic_lock_power_off)
                            .show();
                } catch (IOException e2) {
                    Log.e(TAG,"error al cerrar el socket");
                }
            }
          //--
        }



        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }//end connectthread


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket=socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            byte[] buffer = new byte[2048];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + readMessage);
                    Intent readMessageIntent= new Intent("readMessage");
                    readMessageIntent.putExtra("the message",readMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(readMessageIntent);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    System.exit(0);
                    break;
                }

            }

        }
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                Log.e(TAG,"error al enviar datos");
            }
        }

    }
    public void write(String out) {
        ConnectedThread r;
        Log.d(TAG, "write: Write Called.");
        mConnectedThread.write(out);
    }//endWrite








}
