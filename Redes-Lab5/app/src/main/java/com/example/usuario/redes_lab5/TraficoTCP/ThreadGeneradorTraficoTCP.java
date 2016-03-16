package com.example.usuario.redes_lab5.TraficoTCP;

import com.example.usuario.redes_lab5.MainActivity;

/**
 * Created by Usuario on 15/03/2016.
 */
public class ThreadGeneradorTraficoTCP extends Thread
{
    SocketTCP socketTCP;
    MainActivity principal;
    boolean generando;

    public ThreadGeneradorTraficoTCP(SocketTCP socket, MainActivity pal)
    {
        socketTCP = socket;
        principal=pal;
    }

    public void detenerTrafico()
    {
        generando=false;
        socketTCP.cerrarConexion();
    }


    @Override
    public void run()
    {
        generando=true;
        while(generando)
        {
            try
            {
                new ThreadEnvioTCP(socketTCP,principal.darUbicacionActual()).start();
                sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("TCP ERROR:"+e.getMessage());
                e.printStackTrace();
            }


        }


    }
}
