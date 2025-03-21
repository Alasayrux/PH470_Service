**********************************************************Inicializacion BalanzaService**********************************************************

        BalanzaService.init(AppCompatActivity,OnFragmentChangeListener) ( Devuelve Service ) Llamar 1 Sola vez luego
        BalanzaService.getInstance() Devuelve Service

************************************************Inicializacion Dispositivos,Expansiones,Escaneres************************************************

        BalanzaService.getInstance().Devices.init(new DeviceManager.DeviceMessageListener() {
            @Override
            public void DeviceListener(int Num, String data) {
                
            }
        });( Devuelve DeviceManager ) Llamar 1 Sola vez luego
        BalanzaService.getInstance().getInstanceDevices() Devuelve DeviceManager

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

        Balanza bzaEstandar = BalanzaService.getInstance().Balanzas;
        Balanza bzaEspecifica = bzaEstandar.getBalanza(1);
        if(BalanzaService.ModelosClasesBzas.ITW410.compararInstancia(1)){
            Balanza.ITW410 bza = (Balanza.ITW410)bzaEspecifica;
            bza.Itw410FrmGetUltimoIndice(0);
        };
        List<String> x = PreferencesDevicesManager.obtenerAliasDeModelos(BalanzaService.ModelosClasesBzas.values());
        for (int i = 0; i <x.size() ; i++) {
            switch (x.get(i)){
                case "Optima": {
                    if( BalanzaService.ModelosClasesBzas.Optima.compararInstancia(1)) {
                        Balanza.Optima_Image bza = (Balanza.Optima_Image) bzaEspecifica;
                        bza.getEstadoBajaBateria(0);
                    }
                    break;
                }
                case "ITW410": {
                    if( BalanzaService.ModelosClasesBzas.ITW410.compararInstancia(1)) {
                        Balanza.ITW410 bza = (Balanza.ITW410) bzaEspecifica;
                        bza.Itw410FrmGetUltimoIndice(0);
                    }
                    break;
                }
            }
        }
        
****************************************************************otros****************************************************************
        BalanzaService.getInstance().Impresoras.ImprimirEstandar(1,""); //imprime en la impresora 1



        Tipo a utilizar para la clase BalanzaService.Balanza es la interfaz "**Balanza**"
        ej: BalanzaService Service = BalanzaService.getInstance()
        ej: Balanza Balanzas = Service.Balanzas
