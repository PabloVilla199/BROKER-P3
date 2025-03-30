/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BrokerImpl extends UnicastRemoteObject implements Broker {
    private Map<String, String> servidores = new HashMap<>();

    public BrokerImpl() throws RemoteException {
        super();
    }

    @Override
    public void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException {
        servidores.put(nombre_servidor, host_remoto_IP_puerto);
    }

    @Override
    public Respuesta ejecutar_servicio(String nom_servicio, Vector parametros_servicio) throws RemoteException {
        return new Respuesta("Servicio ejecutado: " + nom_servicio, 200);
}
}
