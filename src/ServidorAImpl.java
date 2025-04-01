
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

public class ServidorAImpl extends UnicastRemoteObject implements ServidorA {
    private List<String> serviciosRegistrados = new ArrayList<>();

    public ServidorAImpl() throws RemoteException {
        super();
    }

    @Override
    public String servicioA(String parametro) throws RemoteException {
        return "Resultado del servicio A con parámetro: " + parametro;
    }

    public static void main(String[] args) {
        try {
            ServidorAImpl servidor = new ServidorAImpl();
            
            String rmiUrl = String.format("rmi://%s:%d/%s",
                Config.SERVIDOR_A_IP,
                Config.SERVIDOR_A_PUERTO,
                Config.SERVIDOR_A_NOMBRE);
            
            Naming.rebind(rmiUrl, servidor);
            
            Broker broker = (Broker) Naming.lookup(
                String.format("rmi://%s:%d/%s",
                    Config.BROKER_IP,
                    Config.BROKER_PUERTO,
                    Config.BROKER_NOMBRE)
            );
            
            broker.registrar_servidor(
                Config.SERVIDOR_A_NOMBRE,
                Config.SERVIDOR_A_IP + ":" + Config.SERVIDOR_A_PUERTO
            );
            
            broker.altaServicio(
                Config.SERVIDOR_A_NOMBRE,
                "servicioA",
                Arrays.asList("String"),
                "String"
            );

            servidor.serviciosRegistrados.add("servicioA");

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

            
            System.out.println("Servidor A registrado en: " + rmiUrl);
            
        } catch (Exception e) {
            System.err.println("Error Servidor A: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
