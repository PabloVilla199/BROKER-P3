/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Remote;
import  java.rmi.RemoteException;
import java.util.Vector;

public interface Broker extends Remote {
    void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException;
    Respuesta ejecutar_servicio(String nom_servicio, Vector parametros_servicio) throws RemoteException;
}
