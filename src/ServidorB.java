/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServidorB extends Remote {
    String servicioB(String parametro) throws RemoteException;
}
