/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class ServidorBImpl extends UnicastRemoteObject implements ServidorB {
    public ServidorBImpl() throws RemoteException {
        super();
    }

    @Override
    public String servicioB(String parametro) throws RemoteException {
        return "Resultado del servicio B con parámetro: " + parametro;
    }

    public static void main(String[] args) {
        try {
            ServidorBImpl servidor = new ServidorBImpl();
            
            String rmiUrl = String.format("rmi://%s:%d/%s",
                Config.SERVIDOR_B_IP,
                Config.SERVIDOR_B_PUERTO,
                Config.SERVIDOR_B_NOMBRE);
            
            Naming.rebind(rmiUrl, servidor);
            
            Broker broker = (Broker) Naming.lookup(
                String.format("rmi://%s:%d/%s",
                    Config.BROKER_IP,
                    Config.BROKER_PUERTO,
                    Config.BROKER_NOMBRE)
            );
            
            broker.registrar_servidor(
                Config.SERVIDOR_B_NOMBRE,
                Config.SERVIDOR_B_IP + ":" + Config.SERVIDOR_B_PUERTO
            );
            
            broker.altaServicio(
                Config.SERVIDOR_B_NOMBRE,
                "servicioB",
                Arrays.asList("String"),
                "String"
            );
            
            System.out.println("Servidor B registrado en: " + rmiUrl);
            
        } catch (Exception e) {
            System.err.println("Error Servidor B: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
