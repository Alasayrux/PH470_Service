package com.service;

import static com.service.Utils.Mensaje;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.service.Balanzas.Clases.ANDGF3000;
import com.service.Balanzas.Clases.BalanzaBase;
import com.service.Balanzas.Clases.ITW410.ITW410_FORM;
import com.service.Balanzas.Clases.Minima.MINIMA_I;
import com.service.Balanzas.Clases.Optima.OPTIMA_I;
import com.service.Balanzas.Clases.SPIDER3;
import com.service.Balanzas.Clases.zorra232;
import com.service.Comunicacion.Modbus.ModbusMasterRtu;
import com.service.Comunicacion.Modbus.Req.ModbusReqRtuMaster;
import com.service.Comunicacion.OnFragmentChangeListener;
import com.service.Balanzas.Clases.R31P30_I;
import com.service.Comunicacion.Impresora.ImprimirEstandar;
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
    private PuertosSerie serialPortA=null,serialPortC=null,serialPortB=null;
    private ServiceFragment Servicefragment= new ServiceFragment();
    public Balanza Balanzas;
   // public  Balanza Services;
    ModbusReqRtuMaster ModbusA = null,ModbusB = null,ModbusC = null;
    public Printer Impresoras;
    //leandrito

    private static boolean initializeDevicesbool=true,initializeescannerbool=true, initializexpansionesbool =true;


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

    public static enum ModelosClasesBzas {
            Optima(OPTIMA_I.class), Minima(MINIMA_I.class), R31p30(R31P30_I.class), ITW410(ITW410_FORM.class), Spider3(SPIDER3.class), Andgf3000(ANDGF3000.class), zebra232(zorra232.class);//, NuevaBza(OPTIMA_I.class);
            public Class<? extends BalanzaBase> clase;
            ModelosClasesBzas( Class<? extends BalanzaBase> clase) {
                this.clase = clase ;//.getDeclaredConstructor().newInstance();
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


    private static BalanzaService Service=null;
    public  AppCompatActivity activity;
    public OnFragmentChangeListener fragmentChangeListener;
    private BalanzaService( AppCompatActivity activity, OnFragmentChangeListener fragmentChangeListener) {
        Service = this;
        this.activity =  activity;
        this.fragmentChangeListener = fragmentChangeListener;
    }
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
            Service= new BalanzaService(activity,fragmentChangeListener);
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

    /**
     * Inicializa un puerto serie con los parámetros proporcionados.
     * Si el puerto ya ha sido inicializado, reutiliza la instancia existente.
     *
     * @param Puerto El nombre del puerto serie a inicializar (por ejemplo, "PuertoSerie 1", "PuertoSerie 2", "PuertoSerie 3").
     * @param baudrate La velocidad de transmisión en baudios.
     * @param databits El número de bits de datos.
     * @param stopbit El número de bits de parada (generalmente 1 o 2).
     * @param parity El tipo de paridad (Ninguna, Paridad Par, Paridad Impar).
     * @param flowcon El control de flujo (generalmente 0 para desactivado).
     * @param flags Otros flags relacionados con la configuración del puerto.
     * @return La instancia del objeto `PuertosSerie` inicializado.
     */
    public PuertosSerie initPuertoSerie(String Puerto, int baudrate, int databits, int stopbit, int parity, int flowcon, int flags){
        PuertosSerie puertoserie = new PuertosSerie();
        CountDownLatch latch = new CountDownLatch(1);
        switch (Puerto){
            case PuertosSerie.StrPortA:{
                if(serialPortA==null) {
                    puertoserie.open(Puerto,baudrate,stopbit,databits,parity,flowcon,flags);
                    if(puertoserie!=null) {
                        serialPortA=puertoserie;
                    }
                }else{
                    puertoserie=serialPortA;
                }
                latch.countDown();
                break;
            }
            case PuertosSerie.StrPortB:{
                if(serialPortB==null) {
                    puertoserie.open(Puerto,baudrate,stopbit,databits,parity,flowcon,flags);
                    if(puertoserie!=null) {
                        serialPortB=puertoserie;
                    }
                }else{
                    puertoserie=serialPortB;
                }
                latch.countDown();
                break;
            }
            case PuertosSerie.StrPortC:{
                if(serialPortC==null) {
                    puertoserie.open(Puerto,baudrate,stopbit,databits,parity,flowcon,flags);
                    if(puertoserie!=null) {
                        serialPortC=puertoserie;
                    }
                }else{
                    puertoserie=serialPortC;
                }
                latch.countDown();
                break;
            }
        }
        if(puertoserie.get_Puerto()!=0){
         //   System.out.println( Puerto+" INICIALIZADO PUERTO"+baudrate+" "+stopbit+" "+databits+" "+parity);
        }
        try {
            latch.await(2000,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        return puertoserie;
    }




    private void SettingsDef(){
        String tipo="";
        int num=0;int numbza=0;
        SharedPreferences Preferencias=activity.getApplicationContext().getSharedPreferences("devicesService",Context.MODE_PRIVATE);
        ArrayList<classDevice> Arraydevicesaux= new ArrayList<classDevice>();
        while(num<5){
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
                        PreferencesDevicesManager.addDevice(x,activity);
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
            ModbusA=null;ModbusB=null;ModbusC=null;
            serialPortB=null;serialPortA=null;serialPortC=null;
            int i=1;
            Balanzas auxbalanzas =  (Balanzas) Balanzas;
            for (Balanza Balanza : auxbalanzas.balanzas.values()) {
                Balanza.stop(i);
                i++;
            }
            auxbalanzas.balanzas.clear();
            Balanzas = auxbalanzas;
        }
       //Utils.clearCache(activity.getApplicationContext());
        SettingsDef();
        Balanzas = new Balanzas();
      //  Services= (Balanza)Balanzas;
        Impresoras = new Impresoras();
            ArrayList<classDevice> balanzasList = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Balanza"),activity);
            if(balanzasList.size()>=1 && balanzasList.get(0).getSeteo()){
               Balanzas auxbalanzas = (Balanzas) Balanzas;
                auxbalanzas.initializateBalanza(balanzasList);
                Balanzas = auxbalanzas;
            }else{
                Mensaje("El servicio tuvo error fatal",R.layout.item_customtoasterror,activity);
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
            ArrayList<classDevice> Impresoralista = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Impresora"),activity);
            try{
            if (Impresoralista != null & Impresoralista.get(numprint)!=null) {
                int type=0;
                switch (Impresoralista.get(numprint).getSalida()) {
                    case "PuertoSerie 1": {
                        initPuertoSerie(PuertosSerie.StrPortA, 9600, 8, 1, 0, 0, 0);
                        type=1; break;
                    }
                    case "PuertoSerie 2": {
                        initPuertoSerie(PuertosSerie.StrPortB, 9600, 8, 1, 0, 0, 0);
                        type=2;
                        break;
                    }
                    case "PuertoSerie 3": {
                        initPuertoSerie(PuertosSerie.StrPortC, 9600, 8, 1, 0, 0, 0);
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
                 ImprimirEstandar Impresora = new ImprimirEstandar(activity.getApplicationContext(), activity, etiqueta, numprint, serialPortA, type, Impresoralista.get(numprint));
                Impresora.EnviarEtiqueta();

            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Mensaje("No existe esta Impresora en configuracion Service", R.layout.item_customtoasterror, activity);
                    }
                });

            }
            }catch(Exception e){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Mensaje("No existe esta Impresora en configuracion Service", R.layout.item_customtoasterror, activity);
                    }
                });
            }
        }

    }
    /**
     * Inicializa la conexión Modbus en el puerto especificado con los parámetros dados.
     *
     * @param Port El puerto serie a utilizar para la conexión Modbus (puede ser "PuertoSerie 1", "PuertoSerie 2", "PuertoSerie 3").
     * @param Baud La velocidad de transmisión en baudios.
     * @param Stopbit El número de bits de parada (generalmente 1 o 2).
     * @param databit El número de bits de datos.
     * @param Parity El tipo de paridad (Ninguna, Paridad Par, Paridad Impar).
     * @return El objeto `ModbusReqRtuMaster` configurado con los parámetros dados.
     */
    public ModbusReqRtuMaster initializatemodbus(String Port, int Baud, int Stopbit, int databit, int Parity) {
        CountDownLatch latch = new CountDownLatch(1);
        ModbusReqRtuMaster Modbus = null;
        try {
            ModbusMasterRtu modbusMasterRtu = new ModbusMasterRtu();
            switch (Port) {
                case PuertosSerie.StrPortA: {
                    // if (ModbusA == null) {
                    ModbusA = modbusMasterRtu.init(Port, Baud, databit, Stopbit, Parity);
                    //}
                    Modbus = ModbusA;
                    break;
                }
                case PuertosSerie.StrPortB: {
                    //if (ModbusB == null) {
                    ModbusB = modbusMasterRtu.init(Port, Baud, databit, Stopbit, Parity);
                    // }

                    Modbus = ModbusB;
                    break;
                }
                case PuertosSerie.StrPortC: {
                    //  if (ModbusC == null) {
                    ModbusC = modbusMasterRtu.init(Port, Baud, databit, Stopbit, Parity);
                    // }
                    Modbus = ModbusC;
                    break;
                }
            }
        } catch (Exception e) {
          //  System.out.println("ERROR MODBUS" +e.getMessage());
        } finally {
//                System.out.println("SETEADO MODBUS");
            latch.countDown();
        }
        try {
            latch.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.getMessage();
        }
     //   System.out.println("INIT MODBUS "+ModbusA.get_Puerto());//+" "+ModbusB.get_Puerto()+" "+ModbusC.get_Puerto());
        return Modbus;
    }
    
    private class Balanzas implements Balanza{
        private Map<Integer, Balanza> balanzas = new HashMap<>();
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
            boolean[] arr = PreferencesDevicesManager.get_numeroSalidasBZA(activity);

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
                                    BalanzaBase bza = ModelosClasesBzas.values()[i].getClase().getDeclaredConstructor(String.class, int.class, AppCompatActivity.class, OnFragmentChangeListener.class, int.class).newInstance(puerto, balanza.getID(), activity, fragmentChangeListener,j);
                                    bza.init(balanzas.size() + 1);
                                    balanzas.put(balanzas.size()+1, bza);
                                //}
                                latch.countDown();

                                // habria que controlar si tiene 2 bzas, si tiene modbus etc.
                            } catch (IllegalAccessException | InvocationTargetException |
                                     InstantiationException | NoSuchMethodException e) {
                            } finally {
                            }
                        }
                    }
                    try{
                        latch.await(balanzasList.size() * 2000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {

                    }
            }
        }

            @Override
        public String getEstado(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getEstado(numBza);
            }
            return null;
        }
        @Override
        public void setEstado(int numBza, String estadoBZA) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setEstado(numBza,estadoBZA);
            }
        }



        @Override
        public void setID(int numID,int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setID(numID,numBza);
            }
        }
        @Override
        public Integer getID(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getID(numBza);
            }else{
                return null;
            }
        }
        @Override
        public Float getNeto(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getNeto(numBza);
            }
            return   null;
        }
        @Override
        public String getNetoStr(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {

                return balanza.getNetoStr(numBza);
            }else{
            }
            return null;
        }

        @Override
        public Float getBruto(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBruto(numBza);
            }
            return null;
        }

        @Override
        public String getBrutoStr(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBrutoStr(numBza);
            }
            return null;
        }

        @Override
        public Float getTara(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTara(numBza);
            }
            return null;
        }

        @Override
        public String getTaraStr(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTaraStr(numBza);
            }
            return null;
        }

        @Override
        public void setTara(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setTara(numBza);
            }
        }

        @Override
        public void setCero(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setCero(numBza);
            }
        }

        @Override
        public void setTaraDigital(int numBza, float TaraDigital) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setTaraDigital(numBza, TaraDigital);
            }
        }

        @Override
        public String getTaraDigital(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getTaraDigital(numBza);
            }
            return null;
        }

        @Override
        public Boolean getBandaCero(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBandaCero(numBza);
            }
            return null;
        }

        @Override
        public void setBandaCero(int numBza, Boolean bandaCeroi) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setBandaCero(numBza,bandaCeroi);
            }
        }

        @Override
        public Float getBandaCeroValue(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getBandaCeroValue(numBza);
            }
            return null;
        }

        @Override
        public void setBandaCeroValue(int numBza, float bandaCeroValue) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.setBandaCeroValue(numBza,bandaCeroValue);
            }
        }

        @Override
        public Boolean getEstable(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getEstable(numBza);
            }
            return null;
        }

        @Override
        public String format(int numero, String peso) {
            Balanza balanza = balanzas.get(numero);
            if (balanza != null) {
                return balanza.format(numero,peso);
            }
            return null;
        }

        @Override
        public String getUnidad(int numBza) {
            try {
                Balanza balanza = balanzas.get(numBza);
                if (balanza != null) {
                    return balanza.getUnidad(numBza);
                }

            } catch (IllegalArgumentException e) {
                //mainActivity.Mensaje("Error:"+e.getMessage(), R.layout.item_customtoasterror);
            }
            return null;

        }

        @Override
        public String getPicoStr(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getPicoStr(numBza);
            }
            return null;
        }

        @Override
        public Float getPico(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                return balanza.getPico(numBza);
            }
            return null;
        }
        @Override
        public void init(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.init(numBza);
            }
        }

        @Override
        public void escribir(String msj, int numBza) {
            balanzas.get(numBza).escribir(msj, numBza);
        }
        @Override
        public void stop(int numBza) {
            for (int i = 0; i < balanzas.size(); i++) {
                balanzas.get(i).stop(0);
            }
        }
        @Override
        public void start(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
                balanza.start(numBza);
            }
        }
        @Override
        public Boolean calibracionHabilitada(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
               return balanza.calibracionHabilitada(numBza);
            }
            return null;
        }
        @Override
        public void openCalibracion(int numBza) {
                Balanza balanza = balanzas.get(numBza);
                if (balanza != null) {
                    balanza.openCalibracion(numBza);
                }else{
                    Mensaje("Balanza "+numBza+" debe inicializarse",R.layout.item_customtoasterror,activity);
                }
        }
        @Override
        public Boolean getSobrecarga(int numBza) {
            Balanza balanza = balanzas.get(numBza);
            if (balanza != null) {
               return balanza.getSobrecarga(numBza);
            }
            return null;
        }
    }

    /**
     * Obtiene la instancia del gestor de expansiones.
     * @return la instancia de ExpansionManager.
     */
    public ExpansionManager getInstanceExpansiones(){
        return ExpansionManager.getInstance();
    }
    /**
     * Inicializa las expansiones, configurando el listener y configurando los puertos serie.
     * @param Listener el listener para las actualizaciones de expansiones.
     */
    public  void initExpansiones(ExpansionManager.ExpansionesMessageListener Listener) {
        //System.out.println("Expansiones init");
        if(initializexpansionesbool) {
            initializexpansionesbool = false;
            ExpansionManager.getInstance().setListener(Listener);
            ArrayList<classDevice> ExpansionesList = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Expansion"),activity);
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
                    port =  initPuertoSerie(strpuerto, Integer.parseInt(Expansion.getDireccion().get(0)), Integer.parseInt(Expansion.getDireccion().get(1)), Integer.parseInt(Expansion.getDireccion().get(2)), Integer.parseInt(Expansion.getDireccion().get(3)),0,0);
                } catch (NumberFormatException e) {
                }finally {
                for (int c = 0; c < ModelosClasesExpansiones.values().length; c++) {
                 //   System.out.println(Objects.equals(Expansion.getModelo(), ModelosClasesExpansiones.values()[c].name()));
                    if (Objects.equals(Expansion.getModelo(), ModelosClasesExpansiones.values()[c].name())) {
                            ExpansionBase Exp = null;
                            try {
                                Exp = ModelosClasesExpansiones.values()[c].getClase().getDeclaredConstructor(PuertosSerie.class, String.class, AppCompatActivity.class).newInstance(port, String.valueOf(Expansion.getID()), activity);
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
    /**
     * Obtiene la instancia del gestor de dispositivos.
     * @return la instancia de DeviceManager.
     */
    public DeviceManager getInstanceDevices(){
        return DeviceManager.getInstance();
    }
    /**
     * Inicializa los dispositivos, configurando el listener y configurando los puertos serie.
     * @param Listener el listener para las actualizaciones de dispositivos.
     */
    public  void initDevices(DeviceManager.DeviceMessageListener Listener) {

        //System.out.println("Device init");
        if(initializeDevicesbool) {
            initializeDevicesbool = false;
            DeviceManager.getInstance().setListener(Listener);
            ArrayList<classDevice> Devicelist = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Expansion"),activity);
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
                port =  initPuertoSerie(strpuerto, Integer.parseInt(Device.getDireccion().get(0)), Integer.parseInt(Device.getDireccion().get(1)), Integer.parseInt(Device.getDireccion().get(2)), Integer.parseInt(Device.getDireccion().get(3)),0,0);
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
    /**
     * Obtiene la instancia del gestor de escáneres.
     * @return la instancia de EscannerManager.
     */
    public EscannerManager getInstanceEscaneres(){
        return EscannerManager.getInstance();
    }
    /**
     * Inicializa los escáneres, configurando el listener y configurando los puertos serie.
     * @param Listener el listener para las actualizaciones de escáneres.
     */
    public  void initEscanner(EscannerManager.ScannerMessageListener Listener) {
       // System.out.println("escanner init");
        if(initializeescannerbool) {
            initializeescannerbool = false;
            EscannerManager.getInstance().setListener(Listener);
            ArrayList<classDevice> Escannerlist = PreferencesDevicesManager.get_listPorTipo(PreferencesDevicesManager.obtenerIndiceTipo("Escaner"),activity);
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
                port =  initPuertoSerie(strpuerto, Integer.parseInt(Escaner.getDireccion().get(0)), Integer.parseInt(Escaner.getDireccion().get(1)), Integer.parseInt(Escaner.getDireccion().get(2)), Integer.parseInt(Escaner.getDireccion().get(3)),0,0);

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


    //------------------------------------ Fin  inicializacion ---------------------------------------------------









    /**
     * Envía un mensaje a un dispositivo escáner específico.
     * @param num el número de dispositivo escáner al que se enviará el mensaje.
     * @param Msj el mensaje que se enviará al dispositivo escáner.
     */
  public  void escribirEscaner(int num,String Msj){
        DeviceManager.getInstance().sendCommandToDevice(num,Msj);
    }

    /**
     * Abre el fragmento del servicio y pasa los argumentos necesarios.
     */
    public void openServiceFragment(){
        ServiceFragment fragment = Servicefragment.newInstance(this);
        Bundle args = new Bundle();
        args.putSerializable("instanceService", this);
        fragmentChangeListener.openFragmentService( fragment,args);
    }
}
