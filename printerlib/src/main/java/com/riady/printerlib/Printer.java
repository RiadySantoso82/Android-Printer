package com.riady.printerlib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.riady.printerlib.Command.PrinterCommand;
import com.riady.printerlib.Service.BluetoothService;

public class Printer {
    private Context context;

    public Printer(Context context,BluetoothService mService) {
        this.context = context;
        Common.mService = mService;
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
        Common.mService.connect(device);
    }

    public void SendDataString(String content){
        PrinterCommand.SendDataString(context, content);
    }

    public void SendDataByte(byte[] content){
        PrinterCommand.SendDataByte(context, content);
    }

    public void PrintPDF147Code(String content,Integer width, Integer height) {
        PrinterCommand.PrintPDF147Code(context,content,width,height);
    }
}
