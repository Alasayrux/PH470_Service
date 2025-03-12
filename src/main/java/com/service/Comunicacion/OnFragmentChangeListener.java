package com.service.Comunicacion;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public interface OnFragmentChangeListener {
    void openFragmentService(Fragment fragment, Bundle arg);
    void openFragmentPrincipal();
    int getUsuarioLvl();
}