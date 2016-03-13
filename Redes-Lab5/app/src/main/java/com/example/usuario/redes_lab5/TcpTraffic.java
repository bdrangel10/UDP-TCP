package com.example.usuario.redes_lab5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Usuario on 12/03/2016.
 */
public class TcpTraffic implements Runnable
{
    private String ip;
    private int puerto;
    private MainActivity principal;
    private Socket socketTCP;
    private PrintWriter out;
    private BufferedReader in;

    public TcpTraffic(String ip, int puerto, MainActivity pal)
    {
        this.ip=ip;
        principal=pal;
        this.puerto=puerto;
        try
        {
            socketTCP=new Socket(ip,puerto);
            out = new PrintWriter(socketTCP.getOutputStream(),true);
            in = new BufferedReader( new InputStreamReader(socketTCP.getInputStream()));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje)
    {

        try
        {
            out.println(mensaje);
            System.out.println("TCP-Env�o:"+mensaje);
            String rta = in.readLine();
            System.out.println("TCP-Rta:"+rta);
            if(!rta.equals("200 OK"));
            {
                // Se produjo un error, informar a la principal
                principal.cantidadErrores++;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void cerrarConexion()
    {
        try
        {
            out.close();
            in.close();
            socketTCP.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            String mensaje = principal.leerMensaje();
            if(mensaje!=null)
            {
                enviarMensaje(mensaje);
            }
            try
            {
                wait(200);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}