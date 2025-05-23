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
package com.service.Comunicacion.Modbus.modbus4And.ip.encap;

import com.service.Comunicacion.Modbus.modbus4And.base.ModbusUtils;
import com.service.Comunicacion.Modbus.modbus4And.ip.IpMessage;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.util.queue.ByteQueue;

public class EncapMessage extends IpMessage {
    public EncapMessage(ModbusMessage modbusMessage) {
        super(modbusMessage);
    }

    public byte[] getMessageData() {
        ByteQueue msgQueue = new ByteQueue();

        // Write the particular message.
        modbusMessage.write(msgQueue);

        // Write the CRC
        ModbusUtils.pushShort(msgQueue, ModbusUtils.calculateCRC(modbusMessage));

        // Return the data.
        return msgQueue.popAll();
    }
}
