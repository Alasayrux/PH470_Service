package com.service.Comunicacion.PuertosSerie;
import java.util.HashMap;
import java.util.Map;

public class DeviceManager {


    private static DeviceManager instance;
    private static Map<Integer, PuertosSerie> DeviceMap = new HashMap<>();
    private DeviceMessageListener listener;

    public interface DeviceMessageListener {
        void DeviceListener(int Num, String data);
    }
    public void setListener(DeviceManager.DeviceMessageListener Listener){
        this.listener=Listener;
    }


    public static synchronized DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public void addDevice(int Num, PuertosSerie Device) {
        DeviceMap.put(Num, Device);
       // System.out.println("DEBUG Device "+ Num);
        PuertosSerie.SerialPortReader reader = new PuertosSerie.SerialPortReader(Device.getInputStream(), new PuertosSerie.PuertosSerieListener() {

            @Override
            public void onMsjPort(String data) {
                // Cuando recibes el mensaje, le añades el Num
                System.out.println("DEBUG Device "+data);
                if (listener != null) {

                    listener.DeviceListener(Num, data);
                }
            }
        });
        reader.startReading();
    }
    public static void sendCommandToDevice(int Num, String command) {
        PuertosSerie Device = DeviceMap.get(Num);
        if (Device != null) {
            Device.write(command);
        }
    }

}