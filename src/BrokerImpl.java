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
    private final Map<String, Future<Object>> respuestasAsinc = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public BrokerImpl() throws RemoteException {
        super();
    }

    // Api para los servidores

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
                tiposParametros[i] = parametros.get(i).getClass();
            }
            
            Method metodo = servidorRemoto.getClass().getMethod(nomServicio, tiposParametros);
            return metodo.invoke(servidorRemoto, parametros.toArray());
            
        } catch (Exception e) {
            throw new RemoteException("Error ejecutando servicio: " + e.getMessage());
        }
    }

    @Override
    public String ejecutarServicioAsinc(String nomServicio, List<Object> parametros) throws RemoteException {
        String solicitudId = UUID.randomUUID().toString();
        
        Future<Object> future = executor.submit(() -> ejecutarServicio(nomServicio, parametros));
        respuestasAsinc.put(solicitudId, future);
        
        System.out.println("Solicitud asíncrona ID: " + solicitudId);
        return solicitudId;
    }

    @Override
    public Object obtenerRespuestaAsinc(String solicitudId) throws RemoteException {
        try {
            if (!respuestasAsinc.containsKey(solicitudId)) {
                throw new RemoteException("Solicitud no encontrada o ya fue entregada");
            }
            
            Future<Object> future = respuestasAsinc.get(solicitudId);
            Object resultado = future.get(10, TimeUnit.SECONDS); 
            
            respuestasAsinc.remove(solicitudId); 
            return resultado;
            
        } catch (TimeoutException e) {
            throw new RemoteException("Tiempo de espera agotado");
        } catch (Exception e) {
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
