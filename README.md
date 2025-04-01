# Broker-P3: Sistema Distribuido con Java RMI

## Descripción

Este proyecto implementa un sistema distribuido utilizando Java RMI (Remote Method Invocation) que actúa como un **Broker de objetos**, actuando como intermediario entre los clientes y los servicios remotos. El Broker permite la localización, registro y ejecución de servicios ofrecidos por servidores distribuidos.

## Componentes

1. **Broker**: El Broker es responsable de registrar los servidores, gestionar la ejecución de los servicios y responder a las peticiones de los clientes.
2. **Servidores**: Los servidores ofrecen servicios remotos específicos que pueden ser invocados por los clientes a través del Broker.
3. **Cliente**: El cliente interactúa con el Broker para invocar servicios y recibir resultados.

## Requisitos

- **Java JDK 11+**.
- **RMI Registry** (ejecutar `rmiregistry` en el directorio del proyecto).
- **Máquinas distribuidas**: Se recomienda utilizar al menos tres máquinas en la misma red local (L1.02).

##Ejecución

- Broker: rmiregistry -J-Djava.rmi.server.hostname=155.210.154.197 && java BrokerImpl
- Servidor A: rmiregistry 1100 -J-Djava.rmi.server.hostname=155.210.154.198 && java ServidorAImpl
- Servidor B: rmiregistry 1101 -J-Djava.rmi.server.hostname=155.210.154.198 && java ServidorBImpl
