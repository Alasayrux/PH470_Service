package com.service.Comunicacion.Modbus.modbus4And.sero.messaging;

public class DefaultMessagingExceptionHandler implements MessagingExceptionHandler {
    public void receivedException(Exception e) {
        e.printStackTrace();
    }
}
