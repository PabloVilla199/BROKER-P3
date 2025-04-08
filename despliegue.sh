#!/bin/bash

# Configuración de las IPs de las máquinas
BROKER_IP="155.210.154.197"
SERVERS_IP="155.210.154.198"
USER="a874773"

# Función para ejecutar un comando en una máquina remota dentro de tmux
execute_remote_tmux() {
    local IP=$1
    local SESSION=$2
    local CMD=$3
    
    ssh "$USER@$IP" "tmux new-session -d -s $SESSION \"$CMD\"" 
}

echo "Desplegando el Broker y los Servidores con logs visibles en tmux..."

# Iniciar el Broker en su propia sesión de tmux
echo "Iniciando Broker en $BROKER_IP..."
execute_remote_tmux "$BROKER_IP" "broker" "cd src && javac *.java && rmiregistry -J-Djava.rmi.server.hostname=$BROKER_IP && java BrokerImpl 2>&1 | tee broker.log"

# Iniciar los servidores en sesiones separadas
echo "Iniciando Servidor A en $SERVERS_IP..."
execute_remote_tmux "$SERVERS_IP" "serverA" "cd src && javac *.java && rmiregistry 1100 -J-Djava.rmi.server.hostname=$SERVERS_IP && java ServidorAImpl 2>&1 | tee serverA.log"

echo "Iniciando Servidor B en $SERVERS_IP..."
execute_remote_tmux "$SERVERS_IP" "serverB" "cd src && javac *.java && rmiregistry 1101 -J-Djava.rmi.server.hostname=$SERVERS_IP && java ServidorBImpl 2>&1 | tee serverB.log"

echo "Despliegue completado. Para ver los logs, usa:"
echo "  ssh $USER@$BROKER_IP 'tmux attach -t broker'"
echo "  ssh $USER@$SERVERS_IP 'tmux attach -t serverA'"
echo "  ssh $USER@$SERVERS_IP 'tmux attach -t serverB'"
