package com.service.Devices.Expansiones.Clases;

import androidx.appcompat.app.AppCompatActivity;

import com.service.Comunicacion.PuertosSerie.PuertosSerie;

public class SalidasC extends ExpansionBase{
    static  {
        Salidas=(12);
        Entradas=(0);
    };
    public static String Bauddef="115200";
    public static String StopBdef="1";
    public static String DataBdef="8";
    public static String Paritydef="0";
    public SalidasC(PuertosSerie Puerto, String id, AppCompatActivity activity) {
        super(Puerto, id, activity);

    }
}
