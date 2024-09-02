package com.riady.printerlib;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.riady.printerlib.Command.Command;
import com.riady.printerlib.Command.PrinterCommand;
import com.riady.printerlib.Service.BluetoothService;

import static android.provider.Settings.Global.DEVICE_NAME;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_CONNECTION_LOST;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_DEVICE_NAME;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_READ;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_STATE_CHANGE;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_TOAST;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_UNABLE_CONNECT;
import static com.riady.printerlib.Service.BluetoothService.MESSAGE_WRITE;
import static com.riady.printerlib.Service.BluetoothService.TOAST;

public class Printer {
    private Context context;
    private String mConnectedDeviceName = null;
    public static int ALIGN_LEFT = 0;
    public static int ALIGN_CENTER = 1;

    public Printer(Context context) {
        this.context = context;
        Common.mService = new BluetoothService(context, mHandler);
    }

    public void Start(){
        if (Common.mService.getState() == BluetoothService.STATE_NONE) {
            Common.mService.start();
        }
    }

    public void Stop(){
        if (Common.mService != null)
            Common.mService.stop();
    }

    public void Connect(BluetoothDevice device){
        if (device != null) {
            Common.mService.connect(device);
        }
    }

    public int GetState(){
        if (Common.mService != null) {
            return Common.mService.getState();
        } else {
            return BluetoothService.STATE_NONE;
        }
    }

    public void SendDataString(String content, Integer align){
        if (align == ALIGN_CENTER) {
            Command.ESC_Align[2] = 0x01;
            PrinterCommand.SendDataByte(context, Command.ESC_Align);
            PrinterCommand.SendDataString(context, content);
            Command.ESC_Align[2] = 0x00;
            PrinterCommand.SendDataByte(context, Command.ESC_Align);
        } else {
            PrinterCommand.SendDataString(context, content);
        }
    }

    public void SendDataString(String content, Integer align, Integer width, Integer height, Integer font_type){
        if (align == ALIGN_CENTER) {
            Command.ESC_Align[2] = 0x01;
            PrinterCommand.SendDataByte(context, Command.ESC_Align);
            PrinterCommand.SendDataString(context, content, width, height, font_type);
            Command.ESC_Align[2] = 0x00;
            PrinterCommand.SendDataByte(context, Command.ESC_Align);
        } else {
            PrinterCommand.SendDataString(context, content, width, height, font_type);
        }
    }

    public void SendDataByte(byte[] content){
        PrinterCommand.SendDataByte(context, content);
    }

    public void PrintQRCode(String content, Integer width) {
        PrinterCommand.PrintQRCode(context, content, width, 1);
    }

    public void PrintPDF147Code(String content,Integer width, Integer height) {
        PrinterCommand.PrintPDF147Code(context,content,width,height);
    }

    public void PrintBMP(Bitmap mBitmap, int width) {
        byte[] data = PrinterCommand.POS_PrintBMP(mBitmap, width,0,0);
        PrinterCommand.SendDataByte(context,data);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(context,"connected to: " + mConnectedDeviceName,Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(context,"Bluetooth connecting ...",Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Toast.makeText(context,"Bluetooth listening ....",Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(context,"not connected printer",Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(context,
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context,
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_CONNECTION_LOST:
                    Toast.makeText(context, "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(context, "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
