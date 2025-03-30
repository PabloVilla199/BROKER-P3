/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServidorBImpl extends UnicastRemoteObject implements ServidorB {
    public ServidorBImpl() throws RemoteException {
        super();
    }

    @Override
    public String servicioB(String parametro) throws RemoteException {
        return "Resultado del servicio B con parámetro: " + parametro;
    }
}
