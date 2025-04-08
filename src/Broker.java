/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Remote;
import  java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface Broker extends Remote {
    void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException;
    void altaServicio(String nombreServidor, String nombreServicio, List<String> parametros, String tipoRetorno) throws RemoteException;
    void bajaServicio(String nombreServidor, String nombreServicio) throws RemoteException;

    Map<String, ServicioInfo> listarServicios() throws RemoteException;
    Object ejecutarServicio(String nombreServicio, List<Object> parametros) throws RemoteException;
    String ejecutarServicioAsinc(String clientId, String nombreServicio, List<Object> parametros) throws RemoteException;
    Object obtenerRespuestaAsinc(String clientId, String solicitudId) throws RemoteException;
}
