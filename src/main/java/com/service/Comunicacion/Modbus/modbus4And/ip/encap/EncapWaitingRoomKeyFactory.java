package com.service.Comunicacion.Modbus.modbus4And.ip.encap;

import com.service.Comunicacion.Modbus.modbus4And.ip.IpMessage;
import com.service.Comunicacion.Modbus.modbus4And.msg.ModbusMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.IncomingResponseMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.OutgoingRequestMessage;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.WaitingRoomKey;
import com.service.Comunicacion.Modbus.modbus4And.sero.messaging.WaitingRoomKeyFactory;

public class EncapWaitingRoomKeyFactory implements WaitingRoomKeyFactory {
    @Override
    public WaitingRoomKey createWaitingRoomKey(OutgoingRequestMessage request) {
        return createWaitingRoomKey(((IpMessage) request).getModbusMessage());
    }

    @Override
    public WaitingRoomKey createWaitingRoomKey(IncomingResponseMessage response) {
        return createWaitingRoomKey(((IpMessage) response).getModbusMessage());
    }

    public WaitingRoomKey createWaitingRoomKey(ModbusMessage msg) {
        return new EncapWaitingRoomKey(msg.getSlaveId(), msg.getFunctionCode());
    }

    class EncapWaitingRoomKey implements WaitingRoomKey {
        private final int slaveId;
        private final byte functionCode;

        public EncapWaitingRoomKey(int slaveId, byte functionCode) {
            this.slaveId = slaveId;
            this.functionCode = functionCode;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + functionCode;
            result = prime * result + slaveId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EncapWaitingRoomKey other = (EncapWaitingRoomKey) obj;
            if (functionCode != other.functionCode)
                return false;
            if (slaveId != other.slaveId)
                return false;
            return true;
        }
    }
}
