
/**
 *
 * @author Pablo Y Marcos 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServidorA extends Remote {
    String servicioA(String parametro) throws RemoteException;
}
