package com.service.Expansiones.Clases;

import androidx.appcompat.app.AppCompatActivity;

import com.service.Comunicacion.PuertosSerie.PuertosSerie;

public class MixtoC extends ExpansionBase {
    static  { Bauddef="115200";StopBdef="1";DataBdef="8";
        Paritydef="0";
        Salidas=6;
        Entradas=6;
    };
    public MixtoC(PuertosSerie Puerto, String id, AppCompatActivity activity) {
        super(Puerto, id, activity);

    }


}
