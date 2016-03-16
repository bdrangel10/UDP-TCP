package com.example.usuario.redes_lab5.TraficoTCP;

/**
 * Created by Usuario on 15/03/2016.
 */
public class ThreadEnvioTCP extends Thread
    {
        SocketTCP socket;
        String mensaje;

        public ThreadEnvioTCP(SocketTCP pSocket, String pMsj)
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

