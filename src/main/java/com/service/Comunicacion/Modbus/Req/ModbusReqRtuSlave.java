package com.service.Comunicacion.Modbus.Req;

import android.util.Log;

import com.zgkxzx.modbus4And.ModbusFactory;
import com.zgkxzx.modbus4And.ModbusSlaveSet;
import com.zgkxzx.modbus4And.ProcessImageListener;
import com.zgkxzx.modbus4And.exception.IllegalDataAddressException;
import com.zgkxzx.modbus4And.exception.ModbusInitException;
import com.zgkxzx.modbus4And.requset.ModbusParam;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ModbusReqRtuSlave {
    private final static String TAG = ModbusReqRtuSlave.class.getSimpleName();

    private static ModbusReqRtuSlave modbusReq;
    private ModbusSlaveSet mModbusSlave;
    private ModbusParam modbusParam = new ModbusParam();
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    private boolean isInit = false;

    private ModbusReqRtuSlave() {

    }

    /**
     * get modbus instance
     *
     * @return
     */
    public static synchronized ModbusReqRtuSlave getInstance() {
        if (modbusReq == null)
            modbusReq = new ModbusReqRtuSlave();
        return modbusReq;
    }

    /**
     * set modbus param
     *
     * @param modbusParam
     */
    public ModbusReqRtuSlave setParam(ModbusParam modbusParam) {
        this.modbusParam = modbusParam;
        return modbusReq;
    }

    /**
     * init modbus
     *
     * @throws ModbusInitException
     */
    public void init(final OnRequestBack<String> onRequestBack,String puerto, int baudrate,int dataBits,int stopbit,int parity) throws Exception {
        ModbusFactory mModbusFactory = new ModbusFactory();
        //SerialPort serialPort = null;
        //SerialPortWrapper serialPortWrapper = new JSerialCommWrapper(puerto,serialPort, baudrate, dataBits, stopbit, parity);
        //serialPortWrapper.open();
        //mModbusSlave = mModbusFactory.createRtuSlave(serialPortWrapper);
        mModbusSlave.addProcessImage(getModscanProcessImage(1));
        //mModbusMaster = mModbusFactory.createTcpMaster(params, modbusParam.keepAlive);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mModbusSlave.start();
                } catch (ModbusInitException e) {
                    mModbusSlave.stop();
                    isInit = false;
                    Log.d(TAG, "Modbus4Android init failed " + e);
                    onRequestBack.onFailed("Modbus4Android init failed ");

                }
                Log.d(TAG, "Modbus4Android init success");
                isInit = true;
                onRequestBack.onSuccess("Modbus4Android init success");


            }
        });

    }
    static BasicProcessImage getModscanProcessImage(int slaveId) {
        BasicProcessImage processImage = new BasicProcessImage(slaveId);
        processImage.setInvalidAddressValue(Short.MIN_VALUE);
        processImage.setHoldingRegister(0,(short) 0);
        processImage.setHoldingRegister(1,(short) 0);
        processImage.setHoldingRegister(2,(short) 0);
        processImage.setHoldingRegister(3,(short) 0);
        processImage.setHoldingRegister(4,(short) 0);
        processImage.setHoldingRegister(5,(short) 0);
        processImage.setHoldingRegister(6,(short) 0);
        processImage.setHoldingRegister(7,(short) 0);
        processImage.setHoldingRegister(8,(short) 0);
        processImage.setHoldingRegister(9,(short) 0);
        // Add an image listener.
        processImage.addListener(new ProcessImageListener() {
            @Override
            public void coilWrite(int i, boolean b, boolean b1) {

            }

            @Override
            public void holdingRegisterWrite(int i, short i1, short i2) {
                //i = offset
                //i1 = oldvalue
                //i2 = newvalue
                //System.out.println("HR at " + i + " was set from " + i1 + " to " + i2);
            }
        });
        return processImage;
    }

    /**
     * destory the modbus4Android instance
     */
    public void destory() {
        modbusReq = null;
        if (mModbusSlave != null) {
            mModbusSlave.stop();
        }
        isInit = false;
    }

    public short readRegister(int register) throws IllegalDataAddressException {
        final short[] registro = {123};
        if (!isInit) {
            return 123;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    registro[0] = mModbusSlave.getProcessImage(1).getHoldingRegister(register);
                } catch (IllegalDataAddressException e) {
                    throw new RuntimeException(e);
                }

            }
        });
            return registro[0];
    }

    public void setRegister(int register,short value) throws IllegalDataAddressException {
        if (!isInit) {
            return ;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                mModbusSlave.getProcessImage(1).setHoldingRegister(register,value);
            }
        });

    }


}
