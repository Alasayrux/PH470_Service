package com.service.Expansiones.Clases;

import androidx.appcompat.app.AppCompatActivity;

import com.service.Comunicacion.PuertosSerie.PuertosSerie;

public class EntradasC extends ExpansionBase{
     static  { Bauddef="115200";StopBdef="1";DataBdef="8";
        Paritydef="0";
         Salidas=0;
         Entradas=12;
    };

    public EntradasC(PuertosSerie Puerto, String id, AppCompatActivity activity) {
        super(Puerto, id, activity);

    }

}
