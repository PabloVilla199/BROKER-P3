/**
 *
 * @author Pablo y Marcos
 */

 import java.rmi.Naming;
 import java.util.Scanner;
 import java.util.Vector;
 
 public class Cliente {
 
     static String brokerHost = "localhost";  
     static String brokerPuerto = "1099";     
 
     public static void main(String[] args) {
         try {
             // Conectar al Broker usando las variables globales configurables
             Broker broker = (Broker) Naming.lookup("rmi://" + brokerHost + ":" + brokerPuerto + "/Broker");
 
             Scanner scanner = new Scanner(System.in);
 
             // Solicitar el nombre del servicio al usuario
             System.out.print("Introduce el nombre del servicio que deseas ejecutar: ");
             String servicioSeleccionado = scanner.nextLine();
 
             // Solicitar parámetros al usuario
             Vector<Object> parametros = new Vector<>();
             System.out.print("¿Cuántos parámetros tiene el servicio? ");
             int numParametros = scanner.nextInt();
             scanner.nextLine(); // Consumir el salto de línea
 
             for (int i = 0; i < numParametros; i++) {
                 System.out.print("Introduce el parámetro " + (i + 1) + ": ");
                 String parametro = scanner.nextLine();
                 parametros.add(parametro);
             }
 
             // Ejecutar el servicio en el Broker
             Respuesta respuesta = broker.ejecutar_servicio(servicioSeleccionado, parametros);
 
             // Mostrar la respuesta del servicio
             System.out.println("Respuesta del servicio '" + servicioSeleccionado + "': " + respuesta.getMensaje());
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }