/**
 *
 * @author Pablo Y Marcos 
 */

import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BrokerImpl extends UnicastRemoteObject implements Broker {

    private final Map<String, ServicioInfo> servicios = new ConcurrentHashMap<>();
    private final Map<String, String> servidores = new ConcurrentHashMap<>();
    private final Map<String, PeticionAsincrona> respuestasAsinc = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Map<ClientServiceKey, String> pendingClientServices = new ConcurrentHashMap<>();
    private final Set<String> solicitudesEntregadas = ConcurrentHashMap.newKeySet();

    public BrokerImpl() throws RemoteException {
        super();
    }

    // Clases para la gestión de peticiones asíncronas

    private static class ClientServiceKey {
        private final String clientId;
        private final String nomServicio;

        public ClientServiceKey(String clientId, String nomServicio) {
            this.clientId = clientId;
            this.nomServicio = nomServicio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientServiceKey that = (ClientServiceKey) o;
            return clientId.equals(that.clientId) && nomServicio.equals(that.nomServicio);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, nomServicio);
        }
    }

    private static class PeticionAsincrona {
        Future<Object> future;
        String clientId;
        String nomServicio;

        PeticionAsincrona(Future<Object> future, String clientId, String nomServicio) {
            this.future = future;
            this.clientId = clientId;
            this.nomServicio = nomServicio;
        }
    }
    

    // Api para los servidores

    private static Class<?> toPrimitive(Class<?> clazz) {
        if(clazz == Integer.class) return int.class;
        else if(clazz == Double.class) return double.class;
        else if(clazz == Boolean.class) return boolean.class;

        return clazz;
    }

    @Override
    public void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException {
        servidores.put(nombre_servidor, host_remoto_IP_puerto);
        System.out.println("Servidor registrado: " + nombre_servidor + " en " + host_remoto_IP_puerto);
    }

    @Override
    public void altaServicio(String nombreServidor, String nomServicio, List<String> parametros, String tipoRetorno) 
            throws RemoteException {
        servicios.put(nomServicio, new ServicioInfo(nombreServidor, parametros, tipoRetorno));
        System.out.println("Servicio registrado: " + nomServicio);
    }

    @Override
    public void bajaServicio(String nombreServidor, String nomServicio) throws RemoteException {
        servicios.remove(nomServicio);
        System.out.println("Servicio eliminado: " + nomServicio);
    }

    // Api para los clientes

    @Override
    public Map<String, ServicioInfo> listarServicios() throws RemoteException {
        return new HashMap<>(servicios);
    }

    @Override
    public Object ejecutarServicio(String nomServicio, List<Object> parametros) throws RemoteException {
        try {
            ServicioInfo servicio = servicios.get(nomServicio);
            if (servicio == null) throw new RemoteException("Servicio no encontrado");
            
            String hostServidor = servidores.get(servicio.getNombreServidor());
            Object servidorRemoto = Naming.lookup("rmi://" + hostServidor + "/" + servicio.getNombreServidor());
            
            Class<?>[] tiposParametros = new Class<?>[parametros.size()];
            for (int i = 0; i < parametros.size(); i++) {
                Class<?> clazz = parametros.get(i).getClass();
                tiposParametros[i] = toPrimitive(clazz);
            }
            
            Method metodo = servidorRemoto.getClass().getMethod(nomServicio, tiposParametros);
            return metodo.invoke(servidorRemoto, parametros.toArray());
            
        } catch (Exception e) {
            throw new RemoteException("Error ejecutando servicio: " + e.getMessage());
        }
    }

    @Override
    public String ejecutarServicioAsinc(String clientId, String nomServicio, List<Object> parametros) throws RemoteException {
        ClientServiceKey key = new ClientServiceKey(clientId, nomServicio);
        if (pendingClientServices.containsKey(key)) {
            throw new RemoteException("El cliente ya tiene una solicitud pendiente para este servicio");
        }

        String solicitudId = UUID.randomUUID().toString();
        
        Future<Object> future = executor.submit(() -> ejecutarServicio(nomServicio, parametros));
        pendingClientServices.put(key, solicitudId);
        respuestasAsinc.put(solicitudId, new PeticionAsincrona(future, clientId, nomServicio));
        
        System.out.println("Solicitud asíncrona ID: " + solicitudId);
        return solicitudId;
    }

    @Override
    public Object obtenerRespuestaAsinc(String currentClientId, String solicitudId) throws RemoteException {
        try {
            PeticionAsincrona peticion = respuestasAsinc.get(solicitudId);
            if (peticion == null) {
                if (solicitudesEntregadas.contains(solicitudId)) {
                    throw new RemoteException("La respuesta ya fue entregada previamente");
                } else {
                    throw new RemoteException("El cliente no había realizado previamente la solicitud");
                }
            }

            if (!respuestasAsinc.containsKey(solicitudId)) {
                throw new RemoteException("Solicitud no encontrada o ya fue entregada");
            }

            if (!currentClientId.equals(peticion.clientId)) {
                throw new RemoteException("El cliente que pide la respuesta no es el mismo que hizo la petición");
            }

            Object resultado = peticion.future.get(10, TimeUnit.SECONDS);

            ClientServiceKey key = new ClientServiceKey(peticion.clientId, peticion.nomServicio);
            pendingClientServices.remove(key);
            respuestasAsinc.remove(solicitudId);
            solicitudesEntregadas.add(solicitudId);
            
            respuestasAsinc.remove(solicitudId); 
            return resultado;
            
        } catch (TimeoutException e) {
            throw new RemoteException("Tiempo de espera agotado");
        } catch (Exception e) {
            PeticionAsincrona peticion = respuestasAsinc.get(solicitudId);
            if (peticion != null) {
                ClientServiceKey key = new ClientServiceKey(peticion.clientId, peticion.nomServicio);
                pendingClientServices.remove(key);
                respuestasAsinc.remove(solicitudId);
            }
            throw new RemoteException("Error recuperando respuesta: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Broker broker = new BrokerImpl();
            Naming.rebind("rmi://" + Config.BROKER_IP + ":" + Config.BROKER_PUERTO + "/" + Config.BROKER_NOMBRE, broker);
            System.out.println("Broker registrado como Broker_MP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
