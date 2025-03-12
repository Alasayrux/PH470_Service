package com.service.Comunicacion.Impresora.Tipos;

import android.content.Context;
import android.hardware.usb.UsbManager;

import androidx.appcompat.app.AppCompatActivity;
import com.jws.jwsapi.common.impresora.tipos.usb.more.DiscoveredPrinterListAdapter;
import com.jws.jwsapi.common.impresora.tipos.usb.more.SelectedPrinterManager;
import com.service.Comunicacion.Impresora.StringUtils;
import com.service.R;
import com.service.Utils;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.FieldDescriptionData;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ImprimirUSB {

    private final AppCompatActivity context;
    List<FieldDescriptionData> variablesList = new ArrayList<>();
    Connection connection;
    DiscoveredPrinterListAdapter discoveredPrinterListAdapter;
    Map<Integer, String> vars;
    public ImprimirUSB(AppCompatActivity context) {
        this.context = context;
    }


    public void print(String label, AppCompatActivity context, Boolean memory, List<String> memoryList) {
        Runnable myRunnable = () -> {
            try {
                discoveredPrinterListAdapter = new DiscoveredPrinterListAdapter(context);
                UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    UsbPrinterManager printerManager = new UsbPrinterManager(context);
                    boolean success = printerManager.printLabel(label);
                    int i=0;
                    while(!success && i<3){
                        try{
                            success = printerManager.printLabel(label);
                            Thread.sleep(500);
                         //   System.out.println("reintentando imprimir");
                            i++;
                        }catch (Exception e){}
                    }
                    System.out.println(success);
                    if (success) {
                    } else {
                        Utils.Mensaje("Impresora no encontrada en usb",R.layout.item_customtoasterror,context);
                    }
            } catch (Exception e) {
                Utils.Mensaje("usb init:" + e.getMessage(), R.layout.item_customtoasterror, context);
            }
        };

        Thread myThread = new Thread(myRunnable);
        myThread.start();


    }
}
