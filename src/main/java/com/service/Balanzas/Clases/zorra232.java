package com.service.Balanzas.Clases;

import androidx.appcompat.app.AppCompatActivity;

import com.service.BalanzaService;
import com.service.Comunicacion.OnFragmentChangeListener;
import com.service.Comunicacion.PuertosSerie.PuertosSerie;
import com.service.PreferencesDevicesManager;
import com.service.R;
import com.service.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class zorra232 extends BalanzaBase {
    public PuertosSerie.SerialPortReader readers = null;
    public static final String Nombre = "Zorra232";
    public PuertosSerie serialPort = null;
    PuertosSerie.PuertosSerieListener receiver = null;
    Boolean Modosingle =false;
    public static final  String Bauddef="9600",StopBdef="1",DataBdef="8", Paritydef="0";
    public zorra232(String puerto, int id, AppCompatActivity activity, OnFragmentChangeListener fragmentChangeListener, int numMultipleBza) {
        super(puerto, id, activity, fragmentChangeListener, numMultipleBza);
        try {
            this.serialPort = BalanzaService.getInstance().initPuertoSerie(puerto, Integer.parseInt(Bauddef), Integer.parseInt(DataBdef), Integer.parseInt(StopBdef), Integer.parseInt(Paritydef), 1, 0);
            Thread.sleep(300);
        } catch (InterruptedException e) {

        } finally {
            Estado=M_MODO_BALANZA;
            this.numBza = (this.serialPort.get_Puerto() * 10) + id; // si no tiene id seria :  10,20,3x  ; SI PUERTO 1 y 2 TIENEN ID ->(puerto.get_Puerto()*100)+numero;  Y CONTROLAR DE ALGUNA FORMA QUE ID NO TENGA 3 CIFRAS
        }
    }

    @Override public void stop(int numBza) {
        try {
            serialPort.close();
        } catch (IOException e) {
        }
        serialPort = null;
        readers.stopReading();
        readers = null;
        Estado = M_VERIFICANDO_MODO;
        mHandler.removeCallbacks(Bucle);
    }

    @Override
    public void init(int numBza){
       // System.out.println("Zorra232 init");
        pesoUnitario = PreferencesDevicesManager.getPesoUnitario(Nombre, this.numBza, activity);//getPesoUnitario();
        pesoBandaCero = PreferencesDevicesManager.getPesoBandaCero(Nombre, this.numBza, activity);
        PuntoDecimal = PreferencesDevicesManager.getPuntoDecimal(Nombre, this.numBza, activity);
        ultimaCalibracion = PreferencesDevicesManager.getUltimaCalibracion(Nombre, this.numBza, activity);
        if (this.serialPort != null) {
            receiverinit();
        }

    }
    private void receiverinit() {
        String filtro = "\u0002";
        receiver = new PuertosSerie.PuertosSerieListener() {

            @Override
            public void onMsjPort(String data) {
               // System.out.println("Zorra232 data"+ data);
                String[] array = new ArrayList<>().toArray(new String[0]);
                if (Objects.equals(Estado, M_MODO_BALANZA)) {
                    Boolean neg=false;
                    String data2 = "";
                    if(data.contains(filtro)){
                        Modosingle=false;
                    }
                    if(data.contains("No")||Modosingle){
                        Modosingle =true;
                        if(data.contains("N:")) {
                        try {

                                String neto = data.substring(data.indexOf("N:") + 2, data.indexOf("T:") - 1).trim().replace("kg", "");
                            //    System.out.println(neto);
                                Neto = Float.parseFloat(neto);
                                NetoStr = neto;

                        } catch (NumberFormatException e) {}
                        }
                        String tara="";
                        if(data.contains("T:")) {
                            try {
                                tara = data.substring(data.indexOf("T:") + 2).trim().replace("kg", "");
                            } catch (Exception e) {}
                        }else{
                            tara=data.trim().replace("kg","");
                        }
                        try {
                           // System.out.println(tara);
                                //String tara = data.substring(data.indexOf("T:")+2).trim().replace("kg","");
                            Tara= Float.parseFloat(tara);
                            TaraStr = tara;
                        } catch (NumberFormatException e) {}
                        Bruto = Neto+Tara;
                        BrutoStr=String.valueOf(Bruto);

                     //   System.out.println("N:"+ NetoStr+" T:"+TaraStr+" B:"+Bruto);
                    }else{
                        Modosingle =false;
                    String PD = data.substring(data.indexOf(filtro)+1,data.indexOf(filtro)+2);
                    //System.out.println("Zorra232 PD"+PD);
                    StringBuilder binary = new StringBuilder();
                    for (char c : PD.toCharArray()) {
                        binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0')).append(" ");
                    }
                    String bin= binary.toString();
                    switch (bin.trim()) {
                        case "00101010":
                            PuntoDecimal = 0;
                            break;
                        case "00101011":
                            PuntoDecimal = 1;
                            break;
                        case "00101100":
                            PuntoDecimal = 2;
                            break;
                        case "00101101":
                            PuntoDecimal = 3;
                            break;
                        case "00101110":
                            PuntoDecimal = 4;
                            break;
                    }
                    //System.out.println("PUNTODEC"+PuntoDecimal+" "+bin);
                    //if(PD.contains("+")){
                    //        PuntoDecimal=1;
                    //    }else if (PD.contains("*")){
                     //       PuntoDecimal=0;
                     //   }
                        array = data.split(" ");
                        String estadox = array[0];

                        if(estadox.contains("4")){
                            SobrecargaBool=true;
                            EstableBool=true;
                        } else if (estadox.contains(filtro+PD+"<")) {
                            SobrecargaBool=true;
                            EstableBool=false;
                        } else if (estadox.contains(filtro+PD+"8")) {
                            SobrecargaBool=false;
                            EstableBool=false;
                        } else if (estadox.contains(filtro+PD+"0")) {
                            SobrecargaBool=false;
                            EstableBool=true;
                        } else if (estadox.contains(filtro+PD+"6")) {
                            neg=true;
                        }
                        data2 = array[1];
                        data2 = data2.replace(filtro+PD+estadox, "");
                        data2 = data2.replace(" ", "");
                        data2 = data2.replace("\r\n", "");
                        data2 = data2.replace("\r", "");
                        data2 = data2.replace("\n", "");
                        data2 = data2.replace("\\u0007", "");
                        data2 = data2.replace("S", "");
                        data2 = data2.replace("E", "");
                        data2 = data2.replace("kg", "");
                        data2 = data2.replace(".", "");
                        try{
                            data2 = data2.substring(0,6);
                            StringBuilder sb = new StringBuilder(data2);
                            if(PuntoDecimal>=1){
                                sb.insert(data2.length()-PuntoDecimal, ".");
                            }
                            if(neg){
                                sb.insert(0,"-");
                            }
                            data2 = sb.toString();

                            data2= String.valueOf(Float.parseFloat(data2));

                        }catch (Exception e){}

                        if (Utils.isNumeric(data2)) { // data2 lo saque por que en minima no funciona, pero por ahi es parte de optima


                            BrutoStr =data2;// Utils.removeLeadingZeros(new BigDecimal(data));
                            Bruto = Float.parseFloat(data2);
                            if (TaraDigital == 0) {
                                Neto = Bruto - Tara;
                                NetoStr = String.valueOf(Neto);
                            } else {
                                Neto = Bruto - TaraDigital;
                                NetoStr = String.valueOf(Neto);
                            }
                            if (Neto > pico) {
                                pico = Neto;
                                picoStr = NetoStr;
                            }
                            if (Bruto < pesoBandaCero) {
                                BandaCero = true;
                            }
                        }
                    }
                }
            }
        };
        try {
            readers = new PuertosSerie.SerialPortReader(serialPort.getInputStream(), receiver);
            readers.startReading();

        } catch (Exception e) {
            Utils.Mensaje(e.getMessage(), R.layout.item_customtoasterror, activity);
        }
    }


    @Override public void setTara(int numBza) {
        Tara=getNeto(numBza);
        TaraStr=String.valueOf(Tara);
    }
}
