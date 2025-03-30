
/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServidorAImpl extends UnicastRemoteObject implements ServidorA {
    public ServidorAImpl() throws RemoteException {
        super();
    }

    @Override
    public String servicioA(String parametro) throws RemoteException {
        return "Resultado del servicio A con parámetro: " + parametro;
    }
}
