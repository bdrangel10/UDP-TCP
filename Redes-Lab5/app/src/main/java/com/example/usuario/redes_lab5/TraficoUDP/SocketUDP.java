package com.example.usuario.redes_lab5.TraficoUDP;

import com.example.usuario.redes_lab5.MainActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Usuario on 13/03/2016.
 */
public class SocketUDP extends Thread
{

    private String ip;
    private int puerto;
    private MainActivity principal;
    DatagramSocket socketUDP;
    private int tiempo_escenario;

    public SocketUDP(String ip, int puerto, MainActivity pal)
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
        System.out.println("UDP-Peticion Envío:"+mensaje);
        if(socketUDP.isConnected())
        {
            byte[] msj = mensaje.getBytes();
            try
            {
                // Construimos un datagrama para enviar el mensaje al servidor
                DatagramPacket peticion = new DatagramPacket(msj, mensaje.length(), InetAddress.getByName(ip),puerto);
                // Enviamos el datagrama
                socketUDP.send(peticion);
                principal.enviados_OK++;
                System.out.println("UDP-Envío:"+mensaje);
            }
            catch(Exception e)
            {
                principal.enviados_error++;
                System.out.println("UDP-Envío: ERROR AL ENVIAR MENSAJE: "+e.getMessage());
                e.printStackTrace();
            }

            try
            {
                // Construimos el DatagramPacket que contendrá la respuesta
                byte[] bufer = new byte[1000];
                DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length);
                socketUDP.receive(respuesta);
                String rta = new String(respuesta.getData());
                System.out.println("UDP-Rta:"+rta);
                if(rta!=null && rta.contains("200 OK"))
                {
                    principal.respuestas_OK++;
                }
                else
                {
                    principal.respuestas_error++;
                }
            }
            catch(Exception e)
            {
                principal.respuestas_error++;
                System.out.println("UDP-Rta: ERROR AL LEER UN MENSAJE:"+e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("UDP-Socket: EL socket está cerrado");
            principal.enviados_error++;
        }
    }

    public void cerrarConexion()
    {
        if(socketUDP!=null & socketUDP.isConnected())
        {
            try
            {
                socketUDP.close();
                System.out.println("Socket UDP: **CERRADO");
            }
            catch(Exception e)
            {
                System.out.println("Socket UDP: ERROR CERRANDO EL SOCKET-"+e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Socket UDP: NO SE PUEDE CERRAR: No existe o ya está cerrado");
        }
    }

    public void cambiarPeriodoEnvio(int nTiempo)
    {
        tiempo_escenario=nTiempo;
    }

    @Override
    public void run()
    {
        System.out.println("ESCENARIO UDP: Envío de datos iniciado, se envía ubicación cada "+tiempo_escenario+" milisegundos");
        while(principal.probando)
        {
            try
            {
                String mensaje = principal.darUbicacionActual();
                if(mensaje!=null)
                {
                    enviarMensaje(mensaje);
                }
                sleep(tiempo_escenario);
            }
            catch(Exception e)
            {
                System.out.println("ESCENARIO UDP: ERROR AL DORMIR EL THREAD-"+e.getMessage());
                e.printStackTrace();
            }

        }
        cerrarConexion();

    }
}
