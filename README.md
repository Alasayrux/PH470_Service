**********************************************************Inicializacion BalanzaService**********************************************************

        BalanzaService.init(AppCompatActivity,OnFragmentChangeListener) ( Devuelve Service ) Llamar 1 Sola vez luego
        BalanzaService.getInstance() Devuelve Service

************************************************Inicializacion Dispositivos,Expansiones,Escaneres************************************************
Dispositivos: con la misma idea que las balanzas esto podria hacerse de varias formas, la mas simple de forma individual.
Master y Slave funcionan con RTU y TCP
                
                try{
                    dispositivoBase x =BalanzaService.getInstance().Dispositivos.getDispositivo(1);
                    if(BalanzaService.ModelosClasesDispositivos.Dispositivo.compararInstancia(1)){
                        dispositivoBase.rs232 rs232 = (dispositivoBase.rs232) x;
                        rs232.init(new dispositivoBase.rs232.DeviceMessageListenerRs232() {
                            @Override
                            public void DeviceListener(int Num, String data) {
                                com.service.Utils.Mensaje("LLEGO: "+data,R.layout.item_customtoasterror,mainActivity);
                                rs232.Write("Respuesta");
                            }
                        });

                    }
                    if(BalanzaService.ModelosClasesDispositivos.Master.compararInstancia(1)){ //funcion bloqueantes en los modbus master

                        Modbus.Master Master = ( Modbus.Master) x;
                        try {
                            Master.init();
                        } catch (Exception e) {
                        }
                        Master.leerHoldingRegister(0, Modbus.ClasesModbus.Float, new Modbus.RegisterCallback() {
                                    @Override
                                    public void finish(String result) {
                                        System.out.println("FLOAT"+ result);
                                    }
                                });

                              Master.leerHoldingRegister(0, Modbus.ClasesModbus.Long, new Modbus.RegisterCallback() {
                                    @Override
                                    public void finish(String result) {
                                        System.out.println("long"+ result);
                                    }
                                });
                        Master.leerCoil(0, new Modbus.CoilCallback() {
                            @Override
                            public void finish(Boolean result) {

                            }
                        });
                            }
                    if(BalanzaService.ModelosClasesDispositivos.Slave.compararInstancia(1)){
                        Modbus.Slave mSlave = (Modbus.Slave) BalanzaService.getInstance().Dispositivos.getDispositivo(1);;
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSlave.init(new Modbus.Slave.DeviceMessageListenerM_Slave() {
                                    @Override
                                    public void CoilChange(int Num, int nRegistro, boolean oldVal, boolean newVal) {
                                        Utils.Mensaje("EN EL DISPOSITIVO "+Num+" REGISTRO "+nRegistro+" EL VALOR CAMBIO DE "+oldVal+" AL "+newVal, R.layout.item_customtoasterror, mainActivity);
                                    }

                                    @Override
                                    public void RegisterChange(int Num, int nRegistro, Short oldVal, Short newVal) {
                                        Utils.Mensaje("EN EL DISPOSITIVO "+Num+" REGISTRO "+nRegistro+" EL VALOR CAMBIO DE "+oldVal+" AL "+newVal,                 R.layout.item_customtoasterror, mainActivity);

                                    }
                                },getBasicProcessImage(mSlave));
                            }
                        });
                        mSlave.publicarHoldingRegister(0, Modbus.Slave.ClasesModbus.Float,"10.2943456789990");
                    }



                }catch (Exception e){
                    System.out.println("ERROR DEVICE "+e.getMessage());
                }
            }
        };
Nota Dispositivos: El slave de tcp habre el puerto en 1502, pero es redirijido a la 502 para establecer la conexion ahi.

         BalanzaService.getInstance().Escaneres.init(new EscannerManager.ScannerMessageListener() {
            @Override
            public void EscannerListener(int num, String data) {
                
            }
        });( Devuelve EscaneresManager ) Llamar 1 Sola vez luego
        BalanzaService.getInstance().getInstanceEscaneres()  Devuelve EscaneresManager

        BalanzaService.getInstance().Expansiones.init(new ExpansionManager.ExpansionesMessageListener() {
            @Override
            public void ExpansionListener(int num, ArrayList<Integer> data) {
                
            }
        }); ( Devuelve ExpansionesManager ) Llamar 1 Sola vez luego 
        
        BalanzaService.getInstance().getInstanceExpansiones()  Devuelve ExpansionesManager

****************************************************************Poliformismo****************************************************************
Tanto en Dipositivos como en balanza hay una estructura de interfaces que permite realizar estos casteos haciendo mas amigable la interfaz del usuario del service

        Balanza bzaEstandar = BalanzaService.getInstance().Balanzas; // Casteo a una balanza especifica
        Balanza bzaEspecifica = bzaEstandar.getBalanza(1);
        if(BalanzaService.ModelosClasesBzas.ITW410.compararInstancia(1)){
            Balanza.ITW410 bza = (Balanza.ITW410)bzaEspecifica;
            bza.Itw410FrmGetUltimoIndice(0);
        };
        for (int i = 0; i <BalanzaService.ModelosClasesBzas.values().length ; i++) { // Ejecutar codigo segun modelo
            if(BalanzaService.ModelosClasesBzas.values()[i].compararInstancia(1)) {
                BalanzaService.ModelosClasesBzas modelo = BalanzaService.ModelosClasesBzas.values()[i];
                switch (modelo) {
                    case Optima: {
                       
                    }
                    case ITW410: {

                    }
                    
                }
            }
        }
        
****************************************************************otros****************************************************************
        BalanzaService.getInstance().Impresoras.ImprimirEstandar(1,""); //imprime en la impresora 1



        Tipo a utilizar para la clase BalanzaService.Balanza es la interfaz "**Balanza**"
        ej: BalanzaService Service = BalanzaService.getInstance()
        ej: Balanza Balanzas = Service.Balanzas


        
