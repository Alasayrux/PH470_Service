package com.service.Comunicacion.PuertosSerie;

import android.content.res.Resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPort;

public class PuertosSerie {
    SerialPort serialPort=null;
    OutputStream mOutputStream;
    public static final String StrPortA ="/dev/ttyXRUSB0", StrPortB ="/dev/ttyXRUSB1", StrPortC ="/dev/ttyXRUSB2";
    InputStream mInputStream;
    String Puerto;

    int Baudrate=9600,StopBits=1,DataBits=8,Parity=0,FlowCon=0,Flags=0;

    public interface PuertosSerieListener {
        void onMsjPort(String data);
    }

    public static class SerialPortReader {
        private InputStream mInputStream;
        Thread readThread;
        private PuertosSerieListener PuertosSerieListener;
        private boolean running = false;

        public SerialPortReader(InputStream Port, PuertosSerieListener listener) {
            this.mInputStream = Port;
            this.PuertosSerieListener = listener;
        }


        public void startReading() {

            if (running || readThread != null && readThread.isAlive()) return; // Evita reiniciar si ya está corriendo
            running = true;
            readThread = new Thread(() -> {
                while (running) {
                    try {
                        byte[] buffer = new byte[1024];
                            int size = mInputStream.read(buffer);
                            if (size > 0) {
                                String str = new String(buffer, 0, size, StandardCharsets.ISO_8859_1);
                                PuertosSerieListener.onMsjPort(str);
                            }


                    } catch (IOException e) {
                        running = false;
                        if (Thread.currentThread().isInterrupted()) break; // Sale del ciclo si se interrumpe

                    }
                }

            });
            readThread.start();
        }

        public void stopReading() {
            running = false;
                if (readThread != null) {
                    readThread.interrupt();  // Interrumpe el hilo de lectura si está esperando
                    //readThread.join(); // Espera a que termine el hilo
                }
            }
    }

    public SerialPort open(String puerto, int baudrate, int stopBits, int dataBits, int parity, int flowCon, int flags){
        CountDownLatch latch = new CountDownLatch(1); // Inicializa el latch
        this.Puerto=puerto;
        Baudrate=baudrate;
        StopBits=stopBits;
        DataBits=dataBits;
        Parity=parity;
        FlowCon=flowCon;
        Flags=flags;

        File archivo = new File(puerto);
        if (archivo.exists()) {


                try {
                    serialPort = new SerialPort(archivo, baudrate, stopBits, dataBits, parity, flowCon, flags);
                    mOutputStream = serialPort.getOutputStream();
                    mInputStream = serialPort.getInputStream();
               } catch (IOException e) {

                } finally {
                    latch.countDown(); // Libera el latch
                }
            try {
                latch.await(333, TimeUnit.MILLISECONDS); // Espera a que se complete la inicialización
            } catch (InterruptedException e) {
                e.printStackTrace();
           }
            return serialPort;
        } else {
            return null;
        }
    }

    public InputStream getInputStream() {
        return mInputStream;
    }
    public boolean HabilitadoLectura() throws IOException {
        if(serialPort!=null){
           return serialPort.getInputStream().available() > 0;
        }else {
            return false;
        }
    }
    public int get_Puerto(){
        switch(Puerto){
            case StrPortA:{
                return 1;
            }
            case StrPortB:{
                return 2;
            }
            case StrPortC:{
                return 3;
            }
        }
        return 0;
    }
    public String read_2()
    {


        if(serialPort!=null){
            String str=null;
            int size=0;
            try {
                if (mInputStream == null)
                    return null;
                byte[] buffer = new byte[1024];
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(buffer);
                }
                if (size > 0) {
                    str=new String(buffer,0,size);

                }
            } catch (Exception e) {
                return null;
            }
            return str;
        }else{
            return "";
        }
    }
    public void flush() throws IOException {
        if(HabilitadoLectura()){
            read_2();
        }
    }
    public void close() throws IOException {
        if(serialPort!=null){
            serialPort.close();
            serialPort=null;
        }
    }
    public void write(String cmd)
    {
        if(serialPort!=null){
            byte[] mBuffer = cmd.getBytes();
            try {
                if (mOutputStream != null) {
                    mOutputStream.write(mBuffer);
                }
            } catch (IOException e) {
            }
        }else{

        }

    }

}
