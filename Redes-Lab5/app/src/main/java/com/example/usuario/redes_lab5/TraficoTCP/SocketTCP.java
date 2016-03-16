package com.example.usuario.redes_lab5.TraficoTCP;

import com.example.usuario.redes_lab5.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Usuario on 15/03/2016.
 */
public class SocketTCP extends Thread
{
    private MainActivity principal;
    private Socket socketTCP;
    private PrintWriter out;
    private BufferedReader in;
    String ip;
    int puerto;


    public SocketTCP(String ip, int puerto, MainActivity pal)
    {
        principal=pal;
        this.ip=ip;
        this.puerto=puerto;

    }

    public void run()
    {
        try
        {
            socketTCP=new Socket(ip,puerto);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketTCP.getOutputStream())),true);
            in = new BufferedReader( new InputStreamReader(socketTCP.getInputStream()));
            principal.socketTCPCreado =true;
        }
        catch(Exception e)
        {
            System.out.println("Socket TCP: Error al crear: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje)
    {
        if(socketTCP!=null && socketTCP.isConnected())
        {
            try
            {
                out.println(mensaje);
                principal.enviados_OK++;
                System.out.println("TCP-Envío: Enviado"+mensaje);
            }
            catch(Exception e)
            {
                principal.enviados_error++;
                e.printStackTrace();
                System.out.println("TCP-Envío: ERROR al enviar");
            }

            try
            {
                String rta = in.readLine();
                System.out.println("TCP-Rta:"+rta);
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
                e.printStackTrace();
                principal.respuestas_error++;
                System.out.println("TCP-Rta:ERROR AL LEER"+e.getMessage());
            }
        }
        else
        {
            principal.enviados_error++;
            System.out.println("TCP-Socket:El socket está cerrado");
        }

    }

    public void cerrarConexion()
    {
        if(socketTCP!=null && socketTCP.isConnected())
        {
            try
            {
                out.close();
                in.close();
                socketTCP.close();
                System.out.println("Socket cerrado");
            }
            catch(Exception e)
            {
                System.out.println("ERROR CERRANDO EL SOCKET:"+e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("SOCKET TCP NO EXISTE O YA ESTÁ CERRADO");
        }
    }
}
