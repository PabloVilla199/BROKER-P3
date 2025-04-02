
/**
 *
 * @author Pablo Y Marcos 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServidorA extends Remote {
    double calcularFactorial(int n) throws RemoteException;
    boolean esNumeroPrimo(int numero) throws RemoteException;
    String convertirBinario(int numero) throws RemoteException;
}