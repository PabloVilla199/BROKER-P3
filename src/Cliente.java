/**
 *
 * @author Pablo y Marcos
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
 
 public class Cliente {
 
    static String brokerHost = "localhost";  
 
    public static void main(String[] args) {
        try {
            Broker broker = (Broker) Naming.lookup("rmi://" + brokerHost + ":" + Config.BROKER_PUERTO + "/" + Config.BROKER_NOMBRE);
 
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.println("\n=== MENÚ PRINCIPAL ===");
                System.out.println("Servicios disponibles:");
                Map<String, ServicioInfo> servicios = broker.listarServicios();
                servicios.forEach((nombre, info) -> 
                    System.out.println("- " + nombre + " (" + info.getParametros() + ") → " + info.getTipoRetorno())
                );
                
                System.out.println("\nOpciones:");
                System.out.println("1. Ejecutar servicio (síncrono/asíncrono)");
                System.out.println("2. Consultar respuesta asíncrona");
                System.out.println("3. Salir");
                System.out.print("Seleccione opción: ");
                
                String opcion = scanner.nextLine().trim();
                
                switch (opcion) {
                    case "1": 
                        System.out.print("Nombre del servicio: ");
                        String nombreServicio = scanner.nextLine().trim();
                        
                        if (!servicios.containsKey(nombreServicio)) {
                            System.out.println("¡Servicio no existe!");
                            break;
                        }
                        
                        ServicioInfo servicio = servicios.get(nombreServicio);
                        List<Object> parametros = new ArrayList<>();
                        
                        for (String tipoParam : servicio.getParametros()) {
                            System.out.print("Parámetro (" + tipoParam + "): ");
                            String valor = scanner.nextLine().trim();
                            parametros.add(parseParametro(valor, tipoParam));
                        }
                        
                        System.out.print("¿Ejecutar asíncrono? (s/n): ");
                        boolean esAsincrono = scanner.nextLine().equalsIgnoreCase("s");
                        
                        if (esAsincrono) {
                            String idSolicitud = broker.ejecutarServicioAsinc(nombreServicio, parametros);
                            System.out.println("Solicitud asíncrona registrada. ID: " + idSolicitud);
                        } else {
                            Object resultado = broker.ejecutarServicio(nombreServicio, parametros);
                            System.out.println("Resultado: " + resultado);
                        }
                        break;
                        
                    case "2":  
                        System.out.print("ID de solicitud: ");
                        String id = scanner.nextLine().trim();
                        try {
                            Object respuesta = broker.obtenerRespuestaAsinc(id);
                            System.out.println("Respuesta: " + respuesta);
                        } catch (RemoteException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                        
                    case "3": 
                        scanner.close();
                        System.exit(0);
                        
                    default:
                        System.out.println("Opción no válida");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object parseParametro(String valor, String tipo) throws IllegalArgumentException {
        try {
            return switch (tipo.toLowerCase()) {
                case "int" -> Integer.parseInt(valor);
                case "double" -> Double.parseDouble(valor);
                case "boolean" -> Boolean.parseBoolean(valor);
                case "string" -> valor;
                default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido para tipo " + tipo + ": " + valor);
        }
    }
}