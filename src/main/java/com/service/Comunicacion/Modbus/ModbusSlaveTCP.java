package com.service.Comunicacion.Modbus;



import static android.content.ContentValues.TAG;

import android.util.Log;

import com.service.Comunicacion.Modbus.Req.ModbusReqTCPslave;
import com.zgkxzx.modbus4And.requset.ModbusReq;
import com.zgkxzx.modbus4And.requset.OnRequestBack;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ModbusSlaveTCP {


    /**
     *
     *
     *
     * NO FUNCIONA
     *
     *
     *
     * */

    public static ModbusReqTCPslave init() throws Exception {
        ModbusReqTCPslave modbusReq=null;
        CountDownLatch connectionLatch = new CountDownLatch(1);

        ModbusReqTCPslave.getInstance()
                .init(new OnRequestBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess " + s);
                        connectionLatch.countDown();
                    }

                    @Override
                    public void onFailed(String msg) {
                        Log.d(TAG, "onFailed " + msg);
                        connectionLatch.countDown();
                    }
                });
        try {
            connectionLatch.await();
            modbusReq = ModbusReqTCPslave.getInstance();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return modbusReq;

    }

    public void readCoil_100(int IdSlave) {

        ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                Log.d(TAG, "readHoldingRegisters onSuccess " + Arrays.toString(data));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, 0, 100);


    }



    public void readCoil_Registro(int IdSlave,int Registro) {

        ModbusReq.getInstance().readHoldingRegisters(new OnRequestBack<short[]>() {
            @Override
            public void onSuccess(short[] data) {
                Log.d(TAG, "readHoldingRegisters onSuccess " + Arrays.toString(data));
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "readHoldingRegisters onFailed " + msg);
            }
        }, 1, Registro-1, 1);


    }
}