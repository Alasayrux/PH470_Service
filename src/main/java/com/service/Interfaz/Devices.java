package com.service.Interfaz;

import com.service.Comunicacion.PuertosSerie.DeviceManager;

public interface Devices {
    /**
     * Obtiene la instancia del gestor de dispositivos.
     * @return la instancia de DeviceManager.
     */
     DeviceManager getInstance();
    /**
     * Inicializa los dispositivos, configurando el listener y configurando los puertos serie.
     * @param Listener el listener para las actualizaciones de dispositivos.
     */
      void init(DeviceManager.DeviceMessageListener Listener);
    /**
     * Envía un mensaje a un dispositivo escáner específico.
     * @param num el número de dispositivo escáner al que se enviará el mensaje.
     * @param Msj el mensaje que se enviará al dispositivo escáner.
     */
    void Write(int num, String Msj);
}
