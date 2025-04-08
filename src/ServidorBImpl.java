/**
  * Implementación del Servidor B.
  * Este servidor ofrece un servicio para obtener la hora en una zona horaria específica.
  * 
  * @author Pablo y Marcos
  */
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServidorBImpl extends UnicastRemoteObject implements ServidorB {

    private static final String SERVICIO_NOMBRE = "servicioB"; 
    private final List<String> serviciosRegistrados = new ArrayList<>();

    public ServidorBImpl() throws RemoteException {
        super();
    }

    /**
     * Servicio que devuelve la hora actual en una zona horaria específica.
     * 
     * @param zonaHoraria Zona horaria en formato "Continent/Country" 
     * @return Hora actual en la zona horaria especificada.
     * @throws RemoteException Si la zona horaria no es válida.
     */
    @Override
    public String obtenerHora(String zonaHoraria) throws RemoteException {
        try {
            ZoneId zoneId = ZoneId.of(zonaHoraria);
            LocalDateTime ahora = LocalDateTime.now(zoneId);
            return ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new RemoteException("Zona horaria no válida: " + zonaHoraria, e);
        }
    }

    /**
     * Método para listar todas las zonas horarias disponibles.
     * 
     * @return Lista de zonas horarias.
     * @throws RemoteException Si ocurre un error al obtener las zonas horarias.
     */
    @Override
    public List<String> listarZonasHorarias() throws RemoteException {
      List<String> zonasHorarias = new ArrayList<>(ZoneId.getAvailableZoneIds());
      List<String> zonasLimitadas = new ArrayList<>();
    
      for (int i = 0; i < 20 && i < zonasHorarias.size(); i++) {
            zonasLimitadas.add(zonasHorarias.get(i));
      }
      return zonasLimitadas;

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
                SERVICIO_NOMBRE,
                Arrays.asList("String"), 
                "String" 
            );
            broker.altaServicio(
                Config.SERVIDOR_B_NOMBRE,
                "listarZonasHorarias",
                new ArrayList<>(), 
                "List<String>"
            );
            
            servidor.serviciosRegistrados.add(SERVICIO_NOMBRE);
            servidor.serviciosRegistrados.add("listarZonasHorarias");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (broker != null && !servidor.serviciosRegistrados.isEmpty()) {
                        for (String servicio : servidor.serviciosRegistrados) {
                            broker.bajaServicio(Config.SERVIDOR_B_NOMBRE, servicio);
                            System.out.println("Servicio eliminado: " + servicio);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al eliminar servicios: " + e.getMessage());
                }
                try {
                    Naming.unbind(rmiUrl);
                    System.out.println("Servidor B desvinculado de RMI");
                } catch (Exception e) {
                    System.err.println("Error al desvincular RMI: " + e.getMessage());
                }
            }));

            System.out.println("Servidor B registrado en: " + rmiUrl);

        } catch (Exception e) {
            System.err.println("Error en el Servidor B: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
