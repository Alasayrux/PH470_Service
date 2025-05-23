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

/**
 * Class for maintaining the profile of a slave device on the master side. Initially, we assume that the device is fully
 * featured, and then we note function failures so that we know how requests should subsequently be sent.
 * 
 * @author mlohbihler
 */
public class SlaveProfile {
    private boolean writeMaskRegister = true;

    public void setWriteMaskRegister(boolean writeMaskRegister) {
        this.writeMaskRegister = writeMaskRegister;
    }

    public boolean getWriteMaskRegister() {
        return writeMaskRegister;
    }
}
