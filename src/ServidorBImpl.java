/**
 *
 * @author Pablo Y Marcos 
 */
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServidorBImpl extends UnicastRemoteObject implements ServidorB {
    private List<String> serviciosRegistrados = new ArrayList<>();

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

            servidor.serviciosRegistrados.add("servicioB");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (broker != null && !servidor.serviciosRegistrados.isEmpty()) {
                        for (String servicio : servidor.serviciosRegistrados) {
                            broker.bajaServicio(Config.SERVIDOR_A_NOMBRE, servicio);
                            System.out.println("Servicio eliminado: " + servicio);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al eliminar servicios: " + e.getMessage());
                }
                try {
                    if (rmiUrl != null) {
                        Naming.unbind(rmiUrl);
                        System.out.println("Servidor A desvinculado de RMI");
                    }
                } catch (Exception e) {
                    System.err.println("Error al desvincular RMI: " + e.getMessage());
                }
            }));
            
            System.out.println("Servidor B registrado en: " + rmiUrl);
            
        } catch (Exception e) {
            System.err.println("Error Servidor B: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
