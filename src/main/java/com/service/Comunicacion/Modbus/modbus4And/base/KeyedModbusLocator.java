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
package com.service.Comunicacion.Modbus.modbus4And.base;

import com.service.Comunicacion.Modbus.modbus4And.ExceptionResult;
import com.service.Comunicacion.Modbus.modbus4And.code.ExceptionCode;
import com.service.Comunicacion.Modbus.modbus4And.locator.BaseLocator;

public class KeyedModbusLocator<K> {
    private final K key;
    private final BaseLocator<?> locator;

    public KeyedModbusLocator(K key, BaseLocator<?> locator) {
        this.key = key;
        this.locator = locator;
    }

    public K getKey() {
        return key;
    }

    public BaseLocator<?> getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        return "KeyedModbusLocator(key=" + key + ", locator=" + locator + ")";
    }

    //
    ///
    /// Delegation.
    ///
    //
    public int getDataType() {
        return locator.getDataType();
    }

    public int getOffset() {
        return locator.getOffset();
    }

    public SlaveAndRange getSlaveAndRange() {
        return new SlaveAndRange(locator.getSlaveId(), locator.getRange());
    }

    public int getEndOffset() {
        return locator.getEndOffset();
    }

    public int getRegisterCount() {
        return locator.getRegisterCount();
    }

    public Object bytesToValue(byte[] data, int requestOffset) {
        try {
            return locator.bytesToValue(data, requestOffset);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // Some equipment will not return data lengths that we expect, which causes AIOOBEs. Catch them and convert
            // them into illegal data address exceptions.
            return new ExceptionResult(ExceptionCode.ILLEGAL_DATA_ADDRESS);
        }
    }
}
