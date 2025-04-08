/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServidorB extends Remote {
    String obtenerHora(String parametro) throws RemoteException;
    List<String> listarZonasHorarias() throws RemoteException;

}
