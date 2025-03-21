package com.service;

import static com.service.Utils.Mensaje;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.service.Balanzas.Clases.ANDGF3000;
import com.service.Balanzas.Clases.BalanzaBase;
import com.service.Balanzas.Clases.GestorPuertoSerie;
import com.service.Balanzas.Clases.ITW410.ITW410_FORM;
import com.service.Balanzas.Clases.Minima.MINIMA_I;
import com.service.Balanzas.Clases.Optima.OPTIMA_I;
import com.service.Balanzas.Clases.R31P30_I;
import com.service.Balanzas.Clases.SPIDER3;
import com.service.Balanzas.Clases.zorra232;
import com.service.Comunicacion.Impresora.ImprimirEstandar;
import com.service.Comunicacion.OnFragmentChangeListener;
import com.service.Comunicacion.PuertosSerie.DeviceManager;
import com.service.Comunicacion.PuertosSerie.EscannerManager;
import com.service.Expansiones.Clases.ExpansionManager;
import com.service.Comunicacion.PuertosSerie.PuertosSerie;
import com.service.Expansiones.Clases.AnalogicoC;
import com.service.Expansiones.Clases.EntradasC;
import com.service.Expansiones.Clases.ExpansionBase;
import com.service.Expansiones.Clases.MixtoC;
import com.service.Expansiones.Clases.SalidasC;
import com.service.Interfaz.Balanza;
import com.service.Interfaz.Printer;
import com.service.Interfaz.classDevice;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public  class BalanzaService implements Serializable {

   // public  Balanza Services;
    public com.service.Interfaz.Expansiones Expansiones = new Expansiones();
    public com.service.Interfaz.Devices Devices = new Devices();
    public com.service.Interfaz.Escaneres Escaneres = new Escaneres();

    public Printer Impresoras  = new Impresoras();
    //leandrito
    protected GestorPuertoSerie Puertos = GestorPuertoSerie.getInstance();

    private static boolean initializeDevicesbool=true,initializeescannerbool=true, initializexpansionesbool =true;
    //holanda

    public static enum ModelosClasesExpansiones {
        Entradas(EntradasC.class), Salidas(SalidasC.class),Combinadas(MixtoC.class),Analogicos(AnalogicoC.class);
        public Class<? extends ExpansionBase> clase;
        ModelosClasesExpansiones( Class<? extends ExpansionBase> clase) {
            this.clase = clase ;//.getDeclaredConstructor().newInstance();
        }

        public Class<? extends ExpansionBase> getClase() {
            return clase;
        }
        public ArrayList<String> getConfiguraciones() {
            try {
                ArrayList<String> list= new ArrayList<>();
                list.add(getFieldValueStr("Bauddef"));
                list.add(getFieldValueStr("StopBdef"));
                list.add(getFieldValueStr("DataBdef"));
                list.add(getFieldValueStr("Paritydef"));
                return list;
            } catch (Exception e) {
                System.err.println("Error obteniendo configuraciones: " + e.getMessage());
                return new ArrayList<>(); // Devuelve una lista vacía en caso de error
            }
        }
        private String getFieldValueStr(String fieldName) throws Exception {
            return (String)clase.getField(fieldName).get(null);
        }
        private Boolean getFieldValueBool(String fieldName) throws Exception {
            return (Boolean) clase.getField(fieldName).get(null);
        }
        private int getFieldValueInt(String fieldName) throws Exception {
            return (int)clase.getField(fieldName).get(null);
        }
    }

    public  enum ModelosClasesBzas {
            Optima(OPTIMA_I.class), Minima(MINIMA_I.class), R31p30(R31P30_I.class), ITW410(ITW410_FORM.class), Spider3(SPIDER3.class), Andgf3000(ANDGF3000.class), zebra232(zorra232.class);//, NuevaBza(OPTIMA_I.class);
            public Class<? extends BalanzaBase> clase;
            ModelosClasesBzas( Class<? extends BalanzaBase> clase) {
                this.clase = clase ;//.getDeclaredConstructor().newInstance();
            }
        public boolean compararInstancia(int nbza) {
                BalanzaBase balanza = BalanzaService.getInstance().Balanzas.getBalanza(nbza);
                return clase.isInstance(balanza);
            }
           public Class<? extends BalanzaBase> getClase() {
               return clase;
           }
           public int GetnumMultiBzas() {
               try {
                   return getFieldValueInt("nBalanzas");
               } catch (Exception e) {
                   System.err.println("Error obteniendo numeromultiplebalanza: " + e.getMessage());
                   return 1; // Devuelve una lista vacía en caso de error
               }
            }
            //aaaaaaaaaaaaaaaaaaaaaaOOOOOOOOO
        public boolean getTieneCal() {
            try {
                return  getFieldValueBool("TieneCal");
            } catch (Exception e) {
                System.err.println("Error obteniendo TieneCal: " + e.getMessage());
                return false;
            }
        }
           public ArrayList<String> getConfiguraciones() {
               try {
                   ArrayList<String> list= new ArrayList<>();
                           list.add(getFieldValueStr("Bauddef"));
                           list.add(getFieldValueStr("StopBdef"));
                   list.add(getFieldValueStr("DataBdef"));
                   list.add(getFieldValueStr("Paritydef"));
                   return list;
               } catch (Exception e) {
                   System.err.println("Error obteniendo configuraciones: " + e.getMessage());
                   return new ArrayList<>(); // Devuelve una lista vacía en caso de error
               }
                }
        private String getFieldValueStr(String fieldName) throws Exception {
            return (String)clase.getField(fieldName).get(null);
        }
        private Boolean getFieldValueBool(String fieldName) throws Exception {
            return (Boolean) clase.getField(fieldName).get(null);
        }
        private int getFieldValueInt(String fieldName) throws Exception {
            return (int)clase.getField(fieldName).get(null);
        }
    }

    private static ComService ComService ;
    private static BalanzaService Service=null;
    /**
     * Inicializa el servicio `BalanzaService` si aún no está inicializado.
     * Si el servicio ya está inicializado, retorna la instancia existente.
     *
     * @param activity La actividad que solicita la inicialización del servicio, que se usará en el contexto del servicio.
     * @param fragmentChangeListener El listener para manejar cambios de fragmento en la actividad.
     * @return La instancia de `BalanzaService`.
     */
    public static BalanzaService init(AppCompatActivity activity, OnFragmentChangeListener fragmentChangeListener){
        if(Service==null){
            Service= new BalanzaService();
            ComService = com.service.ComService.init(activity,fragmentChangeListener);
            Service.init(false);
        }
        return Service;
    }
    /**
     * Obtiene la instancia actual de `BalanzaService`.
     *
     * @return La instancia de `BalanzaService`.
     */
    public static BalanzaService getInstance(){
        return Service;
    }

    // inicio init funciones ---
    private void SettingsDef(){
        String tipo="";
        int num=0;int numbza=0;
        SharedPreferences Preferencias=ComService.activity.getApplicationContext().getSharedPreferences("devicesService",Context.MODE_PRIVATE);
        while(num<PreferencesDevicesManager.salidaMap.size()){
            classDevice x=new classDevice();
            tipo= Preferencias.getString("Tipo_"+num,"fin");
            if(tipo.equals("fin")&&num==0){
                tipo="Balanza";
            }
            Boolean seteo=false;
            seteo= Preferencias.getBoolean("seteo_"+num,false);
            switch (num){
                case 0:{
                    if(!seteo && tipo.equals("Balanza")){ // num siempre sera 0
                        ArrayList<String> arrayList=new ArrayList<>();
                        arrayList = ModelosClasesBzas.values()[num].getConfiguraciones();
                        x.setTipo(PreferencesDevicesManager.obtenerTipoPorIndice(num));
                        x.setND(0);
                        x.setNDL(1);
                        x.setDireccion(arrayList);
                        x.setSeteo(true);
                        x.setSalida("PuertoSerie 1");
                        x.setID(0);
                        PreferencesDevicesManager.addDevice(x,ComService.activity);
                    }
                    break;
                }
            }
            num++;
        }
        ;
    }
    protected void init(Boolean reset) {

        if(reset != null && reset){
            Puertos.ModbusA=null;Puertos.ModbusB=null;Puertos.ModbusC=null;
            Puertos.serialPortB=null;Puertos.serialPortA=null;Puertos.serialPortC=null;
            int i=1;
            for (BalanzaBase Balanza : balanzasInstancia.balanzas.values()) {
                Balanza.stop(i);
                i++;
            }
            balanzasInstancia.balanzas.clear();
            balanzasInstancia.balanzas= new HashMap<>();
        }
       //Utils.clearCache(ComService.activity.getApplicationContext());
        SettingsDef();
        balanzasInstancia = new Balanzas(); // Tipo concreto
        Balanzas = balanzasInstancia;

            ArrayList<classDevice> balanzasList = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Balanza"),ComService.activity);
            if(!balanzasList.isEmpty() && balanzasList.get(0).getSeteo()){
                balanzasInstancia.initializateBalanza(balanzasList);
                Balanzas = balanzasInstancia;
            }else{
                Mensaje("El servicio tuvo error fatal",R.layout.item_customtoasterror,ComService.activity);
            }
    }
    private class Impresoras implements Printer {

        /**
         * Envía una etiqueta para imprimir a la impresora configurada en el índice proporcionado.
         *
         * @param numImpresora El número de la impresora que se desea utilizar (1, 2, 3, etc.).
         * @param etiqueta El contenido de la etiqueta que se desea imprimir.
         */
        public  void ImprimirEstandar(Integer numImpresora, String etiqueta) {

            Integer numprint=numImpresora-1;
            ArrayList<classDevice> Impresoralista = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Impresora"),ComService.activity);
            try{
            if (Impresoralista != null & Impresoralista.get(numprint)!=null) {
                int type=0;
                PuertosSerie puerto=null;
                switch (Impresoralista.get(numprint).getSalida()) {
                    case "PuertoSerie 1": {
                        puerto= Puertos.initPuertoSerie(PuertosSerie.StrPortA, 9600, 8, 1, 0, 0, 0);
                        type=1; break;
                    }
                    case "PuertoSerie 2": {
                        puerto=  Puertos.initPuertoSerie(PuertosSerie.StrPortB, 9600, 8, 1, 0, 0, 0);
                        type=2;
                        break;
                    }
                    case "PuertoSerie 3": {
                        puerto= Puertos.initPuertoSerie(PuertosSerie.StrPortC, 9600, 8, 1, 0, 0, 0);
                        type=3;
                        break;
                    }
                    case "USB": {
                        type=4;
                        break;
                    }
                    case "Red": {
                        type=5;
                        break;
                    }
                    case "Bluetooth": {
                        type=6;
                        break;
                    }
                }
                 ImprimirEstandar Impresora = new ImprimirEstandar(ComService.activity.getApplicationContext(), ComService.activity, etiqueta, numprint, puerto, type, Impresoralista.get(numprint));
                Impresora.EnviarEtiqueta();

            } else {
                ComService.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Mensaje("No existe esta Impresora en configuracion Service", R.layout.item_customtoasterror, ComService.activity);
                    }
                });

            }
            }catch(Exception e){
                ComService.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Mensaje("No existe esta Impresora en configuracion Service", R.layout.item_customtoasterror, ComService.activity);
                    }
                });
            }
        }

    }

   private class Expansiones implements com.service.Interfaz.Expansiones {

        @Override
        public ExpansionManager getInstance(){
            return ExpansionManager.getInstance();
        }
        @Override
        public  void init(ExpansionManager.ExpansionesMessageListener Listener) {
            //System.out.println("Expansiones init");
            if(initializexpansionesbool) {
                initializexpansionesbool = false;
                ExpansionManager.getInstance().setListener(Listener);
                ArrayList<classDevice> ExpansionesList = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Expansion"),ComService.activity);
                int i = 0;
                PuertosSerie port=null;
                for (classDevice Expansion : ExpansionesList
                ) {
                    //System.out.println("Expansion SETEANDO");
                    String strpuerto="";
                    try {
                        switch (Expansion.getSalida()) {
                            case "PuertoSerie 1": {
                                strpuerto= PuertosSerie.StrPortA;
                                break;
                            }
                            case "PuertoSerie 2": {
                                strpuerto= PuertosSerie.StrPortB;
                                break;
                            }
                            case "PuertoSerie 3": {
                                strpuerto= PuertosSerie.StrPortC;
                                break;
                            }
                        }
                        port =  Puertos.initPuertoSerie(strpuerto, Integer.parseInt(Expansion.getDireccion().get(0)), Integer.parseInt(Expansion.getDireccion().get(1)), Integer.parseInt(Expansion.getDireccion().get(2)), Integer.parseInt(Expansion.getDireccion().get(3)),0,0);
                    } catch (NumberFormatException e) {
                    }finally {
                        for (int c = 0; c < ModelosClasesExpansiones.values().length; c++) {
                            //   System.out.println(Objects.equals(Expansion.getModelo(), ModelosClasesExpansiones.values()[c].name()));
                            if (Objects.equals(Expansion.getModelo(), ModelosClasesExpansiones.values()[c].name())) {
                                ExpansionBase Exp = null;
                                try {
                                    Exp = ModelosClasesExpansiones.values()[c].getClase().getDeclaredConstructor(PuertosSerie.class, String.class, AppCompatActivity.class).newInstance(port, String.valueOf(Expansion.getID()), ComService.activity);
                                } catch (Exception e) {
                                }finally{
                                    ExpansionManager.getInstance().addExpansion(i, Exp);

                                }
                            }
                        }
                        ExpansionManager.getInstance().init();
                    }

                }
            }else{
                ExpansionManager.getInstance().setListener(Listener);
            }
        }

    }

    private class Devices implements com.service.Interfaz.Devices {
        @Override
        public DeviceManager getInstance(){
            return DeviceManager.getInstance();
        }
        @Override
        public  void init(DeviceManager.DeviceMessageListener Listener) {

            //System.out.println("Device init");
            if(initializeDevicesbool) {
                initializeDevicesbool = false;
                DeviceManager.getInstance().setListener(Listener);
                ArrayList<classDevice> Devicelist = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Expansion"),ComService.activity);
                int i = 0;
                for (classDevice Device : Devicelist
                ) {
                    PuertosSerie port = null;
                    String strpuerto="";
//                System.out.println("Device SETEANDO");
                    switch (Device.getSalida()) {
                        case "PuertoSerie 1": {
                            strpuerto= PuertosSerie.StrPortA;
                            break;
                        }
                        case "PuertoSerie 2": {
                            strpuerto= PuertosSerie.StrPortB;
                            break;
                        }
                        case "PuertoSerie 3": {
                            strpuerto= PuertosSerie.StrPortC;
                            break;
                        }
                    }
                    port =  Puertos.initPuertoSerie(strpuerto, Integer.parseInt(Device.getDireccion().get(0)), Integer.parseInt(Device.getDireccion().get(1)), Integer.parseInt(Device.getDireccion().get(2)), Integer.parseInt(Device.getDireccion().get(3)),0,0);
                    if(port!=null) {
                        // System.out.println("DEBUG Device add in "+Device.getSalida());
                        DeviceManager.getInstance().addDevice(i, port);
                        i++;
                    }

                }
            }else{
                DeviceManager.getInstance().setListener(Listener);
            }
        }
        @Override
        public void Write(int num, String Msj){
            DeviceManager.getInstance().sendCommandToDevice(num,Msj);
        }
    }
    private class Escaneres implements com.service.Interfaz.Escaneres{
        @Override
        public EscannerManager getInstance(){
            return EscannerManager.getInstance();
        }
        @Override
        public  void init(EscannerManager.ScannerMessageListener Listener) {
            // System.out.println("escanner init");
            if(initializeescannerbool) {
                initializeescannerbool = false;
                EscannerManager.getInstance().setListener(Listener);
                ArrayList<classDevice> Escannerlist = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Escaner"),ComService.activity);
                int i = 0;
                for (classDevice Escaner : Escannerlist
                ) {
                    PuertosSerie port = null;
                    String strpuerto="";
//                System.out.println("Device SETEANDO");
                    switch (Escaner.getSalida()) {
                        case "PuertoSerie 1": {
                            strpuerto= PuertosSerie.StrPortA;
                            break;
                        }
                        case "PuertoSerie 2": {
                            strpuerto= PuertosSerie.StrPortB;
                            break;
                        }
                        case "PuertoSerie 3": {
                            strpuerto= PuertosSerie.StrPortC;
                            break;
                        }
                    }
                    port =  Puertos.initPuertoSerie(strpuerto, Integer.parseInt(Escaner.getDireccion().get(0)), Integer.parseInt(Escaner.getDireccion().get(1)), Integer.parseInt(Escaner.getDireccion().get(2)), Integer.parseInt(Escaner.getDireccion().get(3)),0,0);

                    if(port!=null) {
                        EscannerManager.getInstance().addScanner(i, port);
                        //System.out.println("escanner add in "+Escaner.getSalida());
                        i++;
                    }

                }
            }else{
                EscannerManager.getInstance().setListener(Listener);
            }
        }
    }
    private Balanzas balanzasInstancia;
    public Balanza Balanzas;
    protected class Balanzas implements Balanza{
        private Map<Integer, BalanzaBase> balanzas = new HashMap<>();
        /**
         * Obtiene una balanza específica desde la lista de balanzas.
         *
         * @param numBza El índice de la balanza en la colección.
         * @return La balanza correspondiente al índice especificado.
         */
        @Override
        public BalanzaBase getBalanza(int numBza){
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBalanza(numBza);
            }else{
                return null;
            }
        };
        /**
         * Inicializa las balanzas con la lista proporcionada y configura sus puertos.
         *
         * @param balanzasList Una lista de dispositivos de clase `classDevice` que contienen la información de las balanzas a inicializar.
         */
        private void initializateBalanza(ArrayList<classDevice> balanzasList) {
            int numeroSalidas = 0;
            boolean[] arr = PreferencesDevicesManager.get_numeroSalidasBZA(ComService.activity);
            for (Boolean x : arr) {
                if (x) {
                    numeroSalidas++;

                }
            }

            CountDownLatch latch = new CountDownLatch(balanzasList.size());
            for (classDevice balanza: balanzasList) {//for (int posicionBza = 0; posicionBza < balanzasList.size(); posicionBza++) {
                String puerto = "";
                switch (balanza.getSalida()) {
                    case "PuertoSerie 1": {
                        puerto = PuertosSerie.StrPortA;
                        break;
                    }
                    case "PuertoSerie 2": {

                        puerto = PuertosSerie.StrPortB;
                        break;
                    }
                    case "PuertoSerie 3": {
                        puerto = PuertosSerie.StrPortC;
                        break;
                    }
                    case "Red": {
                        puerto="REDIP ( o por ahi es ssid?)";
                    }
                    case "Bluetooth": {

                        puerto="Mac";
                    }
                    case "USB": {
                        puerto="";
                    }
                }
                for (int i = 0; i < ModelosClasesBzas.values().length; i++) {
                    //        System.out.println("DEBUG BALANZA MODELO "+balanza.getModelo()+"   "+ModelosClasesBzas.values()[i].name()+ "EQUAL "+Objects.equals(balanza.getModelo(),ModelosClasesBzas.values()[i].name()));
                    if(Objects.equals(balanza.getModelo(),ModelosClasesBzas.values()[i].name())){
                        try {
                            int j=1;
                            //int MultipleBZA = ModelosClasesBzas.values()[i].GetnumMultiBzas();
                            //for (int j = 1; j < MultipleBZA+1; j++) {

                            //     System.out.println("DEBUG CHANGE BZA size " + balanzas.size());
                            BalanzaBase bza = ModelosClasesBzas.values()[i].getClase().getDeclaredConstructor(String.class, int.class, AppCompatActivity.class, OnFragmentChangeListener.class, int.class).newInstance(puerto, balanza.getID(), ComService.activity, ComService.fragmentChangeListener,j);
                            bza.init(balanzas.size() + 1);
                            balanzas.put(balanzas.size()+1, bza);
                            //}
                            latch.countDown();

                            // habria que controlar si tiene 2 bzas, si tiene modbus etc.
                        } catch (IllegalAccessException | InvocationTargetException |
                                 InstantiationException | NoSuchMethodException e) {
                            System.out.println("OLA?"+e.getMessage());
                        } finally {
                        }
                    }
                }
            }
            try{
                latch.await(balanzasList.size() * 2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println("OLA?2"+e.getMessage());
            }
        }
        public String getEstado(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getEstado(numBza);
            }
            return null;
        }
        public void setEstado(int numBza, String estadoBZA) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setEstado(numBza,estadoBZA);
            }
        }
        @Override
        public Float getNeto(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getNeto(numBza);
            }
            return   null;
        }
        @Override
        public String getNetoStr(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {

                return balanza.getNetoStr(numBza);
            }else{
            }
            return null;
        }

        @Override
        public Float getBruto(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBruto(numBza);
            }
            return null;
        }

        @Override
        public String getBrutoStr(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBrutoStr(numBza);
            }
            return null;
        }

        @Override
        public Float getTara(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTara(numBza);
            }
            return null;
        }

        @Override
        public String getTaraStr(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTaraStr(numBza);
            }
            return null;
        }

        @Override
        public void setTara(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setTara(numBza);
            }
        }

        @Override
        public void setCero(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setCero(numBza);
            }
        }

        @Override
        public void setTaraDigital(int numBza, float TaraDigital) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setTaraDigital(numBza, TaraDigital);
            }
        }

        @Override
        public String getTaraDigital(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTaraDigital(numBza);
            }
            return null;
        }

        @Override
        public Boolean getEstable(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getEstable(numBza);
            }
            return null;
        }

        @Override
        public String format(int numero, String peso) {
            BalanzaBase balanza = balanzas.get(numero);
            if (balanza != null) {
                return balanza.format(numero,peso);
            }
            return null;
        }

        @Override
        public String getUnidad(int numBza) {
            try {
                BalanzaBase balanza = balanzas.get(numBza);
                if (balanza != null) {
                    return balanza.getUnidad(numBza);
                }

            } catch (IllegalArgumentException e) {
                //mainComService.activity.Mensaje("Error:"+e.getMessage(), R.layout.item_customtoasterror);
            }
            return null;

        }

        @Override
        public String getPicoStr(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getPicoStr(numBza);
            }
            return null;
        }

        @Override
        public Float getPico(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getPico(numBza);
            }
            return null;
        }
        public void init(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.init(numBza);
            }
        }
        public void stop(int numBza) {
            for (int i = 0; i < balanzas.size(); i++) {
                balanzas.get(i).stop(0);
            }
        }

        public void openCalibracion(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.openCalibracion(numBza);
            }else{
                Mensaje("Balanza "+numBza+" debe inicializarse",R.layout.item_customtoasterror,ComService.activity);
            }
        }
        @Override
        public Boolean getSobrecarga(int numBza) {
            BalanzaBase balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getSobrecarga(numBza);
            }
            return null;
        }
    }


    //------------------------------------ Fin  inicializacion ---------------------------------------------------











    /**
     * Abre el fragmento del servicio y pasa los argumentos necesarios.
     */

}
