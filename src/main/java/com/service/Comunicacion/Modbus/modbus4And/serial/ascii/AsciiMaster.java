/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.service.Comunicacion.Modbus.modbus4And.serial.ascii;

import java.io.IOException;

import com.service.Comunicacion.Modbus.modbus4And.exception.ModbusInitException;
import com.service.Comunicacion.Modbus.modbus4And.exception.ModbusTransportException;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusRequest;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusResponse;
import com.service.Comunicacion.Modbus.modbus4And.serial.SerialMaster;
import com.service.Comunicacion.Modbus.modbus4And.serial.SerialPortWrapper;
import com.service.Comunicacion.Modbus.modbus4And.serial.SerialWaitingRoomKeyFactory;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.MessageControl;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.StreamTransport;

public class AsciiMaster extends SerialMaster {
    private MessageControl conn;

    public AsciiMaster(SerialPortWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void init() throws ModbusInitException {
        super.init();

        AsciiMessageParser asciiMessageParser = new AsciiMessageParser(true);
        conn = getMessageControl();
        try {
            conn.start(transport, asciiMessageParser, null, new SerialWaitingRoomKeyFactory());
            if (getePoll() == null)
                ((StreamTransport) transport).start("Modbus ASCII master");
        }
        catch (IOException e) {
            throw new ModbusInitException(e);
        }
        initialized = true;
    }

    @Override
    public void destroy() {
        closeMessageControl(conn);
        super.close();
    }

    @Override
    public ModbusResponse sendImpl(ModbusRequest request) throws ModbusTransportException {
        // Wrap the modbus request in an ascii request.
        AsciiMessageRequest asciiRequest = new AsciiMessageRequest(request);

        // Send the request to get the response.
        AsciiMessageResponse asciiResponse;
        try {
            asciiResponse = (AsciiMessageResponse) conn.send(asciiRequest);
            if (asciiResponse == null)
                return null;
            return asciiResponse.getModbusResponse();
        }
        catch (Exception e) {
            throw new ModbusTransportException(e, request.getSlaveId());
        }
    }
}
