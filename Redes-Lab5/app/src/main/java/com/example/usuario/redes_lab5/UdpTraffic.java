package com.example.usuario.redes_lab5;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Usuario on 12/03/2016.
 */
public class UdpTraffic implements Runnable
{
    private String ip;
    private int puerto;
    private MainActivity principal;
    DatagramSocket socketUDP;
    public boolean detener;

    public UdpTraffic(String ip, int puerto, MainActivity pal)
    {
        this.ip=ip;
        principal=pal;
        this.puerto=puerto;
        try
        {
            socketUDP= new DatagramSocket();
            System.out.println("Socket UDP creado con éxito");
        }
        catch(Exception e)
        {
            System.out.println("Socket UPD: No creado-"+e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje)
    {
        byte[] msj = mensaje.getBytes();
        try
        {
            // Construimos un datagrama para enviar el mensaje al servidor
            DatagramPacket peticion = new DatagramPacket(msj, mensaje.length(), InetAddress.getByName(ip),puerto);

            // Enviamos el datagrama
            socketUDP.send(peticion);
            principal.cantidadEnviado++;
            System.out.println("UDP-Envío:"+mensaje);
            // Construimos el DatagramPacket que contendrá la respuesta
            byte[] bufer = new byte[1000];
            DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length);
            socketUDP.receive(respuesta);
            String rta = new String(respuesta.getData());
            System.out.println("UDP-Rta:"+rta);
            if(!rta.contains("200 OK"));
            {
                // Se produjo un error, informar a la principal
                principal.cantidadErrores++;
            }
        }
        catch(Exception e)
        {
            principal.cantidadErrores++;
            System.out.println("Socket UDP: Error al enviar o recibir un mensaje-"+e.getMessage());
            e.printStackTrace();
        }
    }

    public void cerrarConexion()
    {
        try
        {
            socketUDP.close();
            System.out.println("Socket UDP cerrado");
        }
        catch(Exception e)
        {
            System.out.println("ERROR CERRANDO EL SOCKET");
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        detener=false;
        while(principal.seguirLeyendo())
        {
            String mensaje = principal.leerMensaje();
            if(mensaje!=null)
            {
                enviarMensaje(mensaje);
                System.out.println("Socket UDP: Mensaje Enviado");

            }

        }

        cerrarConexion();

    }
}
