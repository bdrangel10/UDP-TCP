package com.example.usuario.redes_lab5.TraficoUDP;

/**
 * Created by Usuario on 13/03/2016.
 */
public class ThreadEnvioUDP extends Thread
{
    SocketUDP socket;
    String mensaje;

    public ThreadEnvioUDP(SocketUDP pSocket, String pMsj)
    {
        socket=pSocket;
        mensaje=pMsj;
    }



    @Override
    public void run()
    {
        socket.enviarMensaje(mensaje);
    }
}
