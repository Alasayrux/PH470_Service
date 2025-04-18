/*
    Copyright (C) 2006-2007 Serotonin Software Technologies Inc.
 	@author Matthew Lohbihler
 */
package com.service.Comunicacion.Modbus.modbus4And.sero;

/**
 * @author Matthew Lohbihler
 */
public class ShouldNeverHappenException extends RuntimeException {
    private static final long serialVersionUID = -1;
    
    public ShouldNeverHappenException(String message) {
        super(message);
    }

    public ShouldNeverHappenException(Throwable cause) {
        super(cause);
    }
}
