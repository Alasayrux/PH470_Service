package com.service.Comunicacion.Modbus.modbus4And.locator;

import java.nio.charset.Charset;

import com.service.Comunicacion.Modbus.modbus4And.code.DataType;
import com.service.Comunicacion.Modbus.modbus4And.code.RegisterRange;
import com.service.Comunicacion.Modbus.modbus4And.exception.IllegalDataTypeException;

public class StringLocator extends BaseLocator<String> {
    public static final Charset ASCII = Charset.forName("ASCII");

    private final int dataType;
    private final int registerCount;
    private final Charset charset;

    public StringLocator(int slaveId, int range, int offset, int dataType, int registerCount) {
        this(slaveId, range, offset, dataType, registerCount, ASCII);
    }

    public StringLocator(int slaveId, int range, int offset, int dataType, int registerCount, Charset charset) {
        super(slaveId, range, offset);
        this.dataType = dataType;
        this.registerCount = registerCount;
        this.charset = charset;
        validate();
    }

    private void validate() {
        super.validate(registerCount);

        if (range == RegisterRange.COIL_STATUS || range == RegisterRange.INPUT_STATUS)
            throw new IllegalDataTypeException("Only binary values can be read from Coil and Input ranges");

        if (dataType != DataType.CHAR && dataType != DataType.VARCHAR)
            throw new IllegalDataTypeException("Invalid data type");
    }

    @Override
    public int getDataType() {
        return dataType;
    }

    @Override
    public int getRegisterCount() {
        return registerCount;
    }

    @Override
    public String toString() {
        return "StringLocator(slaveId=" + getSlaveId() + ", range=" + range + ", offset=" + offset + ", dataType="
                + dataType + ", registerCount=" + registerCount + ", charset=" + charset + ")";
    }

    @Override
    public String bytesToValueRealOffset(byte[] data, int offset) {
        offset *= 2;
        int length = registerCount * 2;

        if (dataType == DataType.CHAR)
            return new String(data, offset, length, charset);

        if (dataType == DataType.VARCHAR) {
            int nullPos = -1;
            for (int i = offset; i < offset + length; i++) {
                if (data[i] == 0) {
                    nullPos = i;
                    break;
                }
            }

            if (nullPos == -1)
                return new String(data, offset, length, charset);
            return new String(data, offset, nullPos, charset);
        }

        throw new RuntimeException("Unsupported data type: " + dataType);
    }

    @Override
    public short[] valueToShorts(String value) {
        short[] result = new short[registerCount];
        int resultByteLen = registerCount * 2;

        int length;
        if (value != null) {
            byte[] bytes = value.getBytes(charset);

            length = resultByteLen;
            if (length > bytes.length)
                length = bytes.length;

            for (int i = 0; i < length; i++)
                setByte(result, i, bytes[i] & 0xff);
        }
        else
            length = 0;

        if (dataType == DataType.CHAR) {
            // Pad the rest with spaces
            for (int i = length; i < resultByteLen; i++)
                setByte(result, i, 0x20);
        }
        else if (dataType == DataType.VARCHAR) {
            if (length >= resultByteLen)
                // Ensure the last byte is a null terminator.
                result[registerCount - 1] &= 0xff00;
            else {
                // Pad the rest with null.
                for (int i = length; i < resultByteLen; i++)
                    setByte(result, i, 0);
            }
        }
        else
            throw new RuntimeException("Unsupported data type: " + dataType);

        return result;
    }

    private void setByte(short[] s, int byteIndex, int value) {
        if (byteIndex % 2 == 0)
            s[byteIndex / 2] |= value << 8;
        else
            s[byteIndex / 2] |= value;
    }
    //
    //    public static void main(String[] args) {
    //        StringLocator l1 = new StringLocator(1, RegisterRange.HOLDING_REGISTER, 0, DataType.CHAR, 4);
    //        StringLocator l2 = new StringLocator(1, RegisterRange.HOLDING_REGISTER, 0, DataType.VARCHAR, 4);
    //
    //        short[] s;
    //
    //        s = l1.valueToShorts("abcdefg");
    //        System.out.println(new String(l1.bytesToValue(toBytes(s), 0)));
    //
    //        s = l1.valueToShorts("abcdefgh");
    //        System.out.println(new String(l1.bytesToValue(toBytes(s), 0)));
    //
    //        s = l1.valueToShorts("abcdefghi");
    //        System.out.println(new String(l1.bytesToValue(toBytes(s), 0)));
    //
    //        s = l2.valueToShorts("abcdef");
    //        System.out.println(new String(l2.bytesToValue(toBytes(s), 0)));
    //
    //        s = l2.valueToShorts("abcdefg");
    //        System.out.println(new String(l2.bytesToValue(toBytes(s), 0)));
    //
    //        s = l2.valueToShorts("abcdefgh");
    //        System.out.println(new String(l2.bytesToValue(toBytes(s), 0)));
    //
    //        s = l2.valueToShorts("abcdefghi");
    //        System.out.println(new String(l2.bytesToValue(toBytes(s), 0)));
    //    }
    //
    //    private static byte[] toBytes(short[] s) {
    //        byte[] b = new byte[s.length * 2];
    //        for (int i = 0; i < s.length; i++) {
    //            b[i * 2] = (byte) ((s[i] >> 8) & 0xff);
    //            b[i * 2 + 1] = (byte) (s[i] & 0xff);
    //        }
    //        return b;
    //    }
}
