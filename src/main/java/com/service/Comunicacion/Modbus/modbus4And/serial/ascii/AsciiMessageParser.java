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


import com.service.Comunicacion.Modbus.modbus4And.base.BaseMessageParser;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.IncomingMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.util.queue.ByteQueue;

public class AsciiMessageParser extends BaseMessageParser {
    public AsciiMessageParser(boolean master) {
        super(master);
    }

    @Override
    protected IncomingMessage parseMessageImpl(ByteQueue queue) throws Exception {
        if (master)
            return AsciiMessageResponse.createAsciiMessageResponse(queue);
        return AsciiMessageRequest.createAsciiMessageRequest(queue);
    }
}
