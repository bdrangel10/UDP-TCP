package com.example.usuario.redes_lab5;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{

    static final int SOLICITUD_GPS=123;

    LocationManager locManager;
    GeneradorPosiciones locListener;

    UdpTraffic udpTraffic;
    TcpTraffic tcpTraffic;
    Thread threadGPS;
    Thread threadUDP;
    Thread threadTCP;
    final static char SEPARADOR = ',';
    final static String nada = "000";

    long horaInicio;
    long horaFinal;

    int cantidadEnviado;
    int cantidadErrores;

    boolean enviandoUDP;
    boolean enviandoTCP;

    ArrayList<String> mensajes;

    String direccionIP;
    int puerto;

    //Componentes gráficos
    Button btnOk;
    EditText txtIP;
    EditText txtPuerto;
    Switch traficoUDP;
    Switch traficoTCP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOk = (Button)findViewById(R.id.btnOK);
        btnOk.setOnClickListener(this);

        txtIP =(EditText)findViewById(R.id.txtIP);
        txtPuerto=(EditText)findViewById(R.id.txtPuerto);
        traficoTCP=(Switch)findViewById(R.id.btnTCP);
        traficoTCP.setOnCheckedChangeListener(this);
        traficoTCP.setOnClickListener(this);
        traficoUDP=(Switch)findViewById(R.id.btnUDP);
        traficoUDP.setOnClickListener(this);
        traficoUDP.setOnCheckedChangeListener(this);
        System.out.println("Constructor completado con éxito");
    }

    public void inicializarEscucha()
    {
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mensajes = new ArrayList<String>();
        locListener = new GeneradorPosiciones(this);
        threadGPS = new Thread(locListener);
        System.out.println("Creado el trhead GPS");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
                System.out.println("No se tienen los permisos");


                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                    {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    }
                    else
                    {

                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},SOLICITUD_GPS);
                    }
                }

    }

    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case SOLICITUD_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    locManager.requestLocationUpdates(locManager.GPS_PROVIDER, 0, 0, locListener);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void iniciarEnvioUDP(String ip, int puerto)
    {
        horaInicio=System.currentTimeMillis();
        inicializarEscucha();
        iniciarGPS();
        cantidadEnviado=0;
        cantidadErrores=0;
        enviandoUDP=true;
        UdpTraffic udp = new UdpTraffic(ip,puerto,this);
        udpTraffic=udp;
        threadUDP=new Thread(udp);
        threadUDP.start();
    }

    public void detenerEnvioUPD()
    {
        if(enviandoUDP)
        {
            try
            {
                horaFinal=System.currentTimeMillis();
                enviandoUDP=false;
                threadUDP.stop();
                udpTraffic.cerrarConexion();
                detenerGPS();
            }
            catch( Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void iniciarEnvioTCP(String ip, int puerto)
    {
        horaInicio=System.currentTimeMillis();
        inicializarEscucha();
        iniciarGPS();
        cantidadEnviado=0;
        cantidadErrores=0;
        enviandoTCP=true;
        TcpTraffic tcp = new TcpTraffic(ip,puerto,this);
        tcpTraffic=tcp;
        threadTCP=new Thread(tcp);
        threadTCP.start();
    }

    public void detenerEnvioTCD()
    {
        if(enviandoUDP)
        {
            try
            {
                horaFinal=System.currentTimeMillis();
                enviandoUDP=false;
                threadTCP.stop();
                tcpTraffic.cerrarConexion();
                detenerGPS();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
    }


    public void iniciarGPS() {
        threadGPS.start();
    }

    public void detenerGPS() {
        threadGPS.stop();
    }


    public void anotarCoordenada(Location ubicacion, long hora)
    {
        cantidadEnviado++;
        Location coordenadas = ubicacion!=null?ubicacion:locManager.getLastKnownLocation(locManager.getAllProviders().get(0));
        double latitud;
        double longitud;
        float velocidad;
        String mensaje = "" + hora + SEPARADOR + nada + SEPARADOR + nada + SEPARADOR + nada + SEPARADOR + nada;
        if (coordenadas != null) {
            latitud = coordenadas.getLatitude();
            longitud = coordenadas.getLongitude();
            velocidad = coordenadas.getSpeed();
            mensaje = "" + hora + SEPARADOR + latitud + SEPARADOR + longitud + SEPARADOR + ubicacion.getAltitude() + SEPARADOR + velocidad;

        }
        mensajes.add(mensaje);
        System.out.println("COLA:"+mensaje);
    }

    public String leerMensaje() {
        String mensaje = null;
        if (mensajes.size() > 0) {
            mensaje = mensajes.get(0);
            mensajes.remove(0);
        }
        return mensaje;
    }

    public void DesactivarEscuchaGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locManager.removeUpdates(locListener);
    }



    @Override
    public void onClick(View v)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        if(v==btnOk)
        {
            direccionIP=(txtIP.getText()).toString();
            puerto=Integer.parseInt(txtPuerto.getText().toString());
            alertDialog.setTitle("Red ok");
            alertDialog.setMessage("Se han establecido con éxito los parámetros de la conexión");
            alertDialog.show();
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        if (buttonView == traficoTCP) {
            if (isChecked) {
                iniciarEnvioTCP(direccionIP, puerto);
                alertDialog.setTitle("Envío de datos");
                alertDialog.setMessage("Se ha iniciado el envío de datos a través de protocolo TCP");
                alertDialog.show();
            } else {
                detenerEnvioTCD();
                double tiempo = (horaFinal - horaInicio) / 1000;
                int total = (cantidadEnviado + cantidadErrores);
                double porc = total != 0 ? cantidadErrores * 100 / total : 0;
                String msj = "Envío de datos protocolo TCP \nEnviados:" + cantidadEnviado + "\nErrores:" + cantidadErrores + "\nTotal:" + total + "\nTiempo:" + tiempo + "s\n%Error:" + porc;
                alertDialog.setTitle("Estadísticas TCP");
                alertDialog.setMessage(msj);
                alertDialog.show();
            }
        }
        else if(buttonView== traficoUDP)
        {
            if(isChecked)
            {
                iniciarEnvioUDP(direccionIP,puerto);
                alertDialog.setTitle("Envío de datos");
                alertDialog.setMessage("Se ha iniciado el envío de datos a través de protocolo UDP");
                alertDialog.show();
            }
            else
            {
                detenerEnvioUPD();
                double tiempo = (horaFinal-horaInicio)/1000;
                int total=(cantidadEnviado+cantidadErrores);
                double porc = total!=0?cantidadErrores*100/total:0;
                String msj="Envío de datos protocolo UDP \nEnviados:"+cantidadEnviado+"\nErrores:"+cantidadErrores+"\nTotal:"+total+"\nTiempo:"+tiempo+"s\n%Error:"+porc;
                alertDialog.setTitle("Estadísticas");
                alertDialog.setMessage(msj);
                alertDialog.show();
            }
        }


    }
}
