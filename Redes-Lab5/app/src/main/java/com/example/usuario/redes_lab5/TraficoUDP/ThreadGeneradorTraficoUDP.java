package com.example.usuario.redes_lab5.TraficoUDP;

import com.example.usuario.redes_lab5.MainActivity;

/**
 * Created by Usuario on 12/03/2016.
 */
public class ThreadGeneradorTraficoUDP extends Thread
{
    SocketUDP socketUDP;
    MainActivity principal;
    boolean generando;

    public ThreadGeneradorTraficoUDP(SocketUDP socket, MainActivity pal)
    {
        socketUDP = socket;
        principal=pal;
    }

    public void detenerTrafico()
    {
        generando=false;
        socketUDP.cerrarConexion();
    }


    @Override
    public void run()
    {
        generando=true;
        while(generando)
        {
            try
            {
                new ThreadEnvioUDP(socketUDP,principal.darUbicacionActual()).start();
                sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("UDP ERROR:"+e.getMessage());
                e.printStackTrace();
            }

        }


    }
}