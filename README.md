**********************************************************Inicializacion BalanzaService**********************************************************

        BalanzaService.init(AppCompatActivity,OnFragmentChangeListener) ( Devuelve Service ) Llamar 1 Sola vez luego
        BalanzaService.getInstance() Devuelve Service

************************************************Inicializacion Dispositivos,Expansiones,Escaneres************************************************

        BalanzaService.getInstance().initDevices(DeviceManager.DeviceMessageListener);( Devuelve DeviceManager ) Llamar 1 Sola vez luego
        BalanzaService.getInstance().getInstanceDevices() Devuelve DeviceManager

        BalanzaService.getInstance().initEscanner(EscannerManager.ScannerMessageListener);( Devuelve EscaneresManager ) Llamar 1 Sola vez luego
        BalanzaService.getInstance().getInstanceEscaneres()  Devuelve EscaneresManager

        BalanzaService.getInstance().initDevices(ExpansionManager.ExpansionesMessageListener); ( Devuelve ExpansionesManager ) Llamar 1 Sola vez luego
        BalanzaService.getInstance().getInstanceExpansiones()  Devuelve ExpansionesManager

****************************************************************Poliformismo****************************************************************

        Balanza bzaEstandar = BalanzaService.getInstance().Balanzas;

        Balanza bzaEspecifica = BalanzaService.getInstance().Balanzas.getbalanzas(1);
        if(bzaEspecifica instanceof Balanza.ITW410){
            Balanza.ITW410 bza410 = (Balanza.ITW410) bzaEspecifica;
           int a= bza410.Itw410FrmGetEstado(1);
        }
        InstanceOf Con enum :
        int N_BZA  = 1 //Numero Bza
        BalanzaService.ModelosClasesBzas.values().get(n) //PARA FOR
        BalanzaService.ModelosClasesBzas.Optima.getClase().isInstance(BalanzaService.getInstance().Balanzas.getBalanza(N_BZA))
        
****************************************************************otros****************************************************************

Tipo a utilizar para la clase BalanzaService.Balanza es la interfaz "**Balanza**"
ej: BalanzaService Service = BalanzaService.getInstance()
ej: Balanza Balanzas = Service.Balanzas
