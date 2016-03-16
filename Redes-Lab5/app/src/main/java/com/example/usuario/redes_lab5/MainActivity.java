package com.example.usuario.redes_lab5;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.example.usuario.redes_lab5.TraficoTCP.SocketTCP;
import com.example.usuario.redes_lab5.TraficoTCP.SocketTCPEscenario;
import com.example.usuario.redes_lab5.TraficoTCP.ThreadGeneradorTraficoTCP;
import com.example.usuario.redes_lab5.TraficoUDP.SocketUDP;
import com.example.usuario.redes_lab5.TraficoUDP.ThreadGeneradorTraficoUDP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    //USADOS
    public boolean socketTCPCreado;
    public boolean probando;
    private ThreadGeneradorTraficoUDP generadorTraficoUDP;
    private ThreadGeneradorTraficoTCP generadorTraficoTCP;
    LocationManager locManager;
    GeneradorPosiciones locListener;

    String direccionIP="192.168.40.254";
    int puerto=12000;


    //ESTADISTICAS
    long horaInicio;
    long horaFinal;
    public int intentos;
    public int enviados_OK;
    public int enviados_error;
    public int respuestas_OK;
    public int respuestas_error;

    final static char SEPARADOR = ',';
    final static String nada = "000";
    int tiempo_defecto_envío=1000;
    int tiempo_duracion_prueba_sec=60;

    static final int SOLICITUD_GPS=123;

    //Componentes gráficos
    Button btnOk;
    EditText txtIP;
    EditText txtPuerto;
    EditText txtHilos;
    EditText txtTotalPrueba;
    EditText txtPerEnvio;
    Switch traficoUDP;
    Switch traficoTCP;
    Button btnu100;
    Button btnu200;
    Button btnu300;
    Button btnt100;
    Button btnt200;
    Button btnt300;
    Button btnTCP;
    Button btnUDP;

    public String darUbicacionActual()
    {
        intentos++;
        Location coordenadas=locListener.darUbicacion();
        double latitud;
        double longitud;
        float velocidad;
        long hora =System.currentTimeMillis();
        String mensaje = "" + hora + SEPARADOR + nada + SEPARADOR + nada + SEPARADOR + nada + SEPARADOR + nada;
        if (coordenadas != null) {
            latitud = coordenadas.getLatitude();
            longitud = coordenadas.getLongitude();
            velocidad = coordenadas.getSpeed();
            mensaje = "" + hora + SEPARADOR + latitud + SEPARADOR + longitud + SEPARADOR + coordenadas.getAltitude() + SEPARADOR + velocidad;
        }
        return mensaje;
    }

    public void generarTraficoUDP_continuo()
    {
        inicializarGeneradorPosiciones();
        horaInicio=System.currentTimeMillis();
        reiniciarEstadisticas();
        SocketUDP socket = new SocketUDP(direccionIP,puerto,this);
        generadorTraficoUDP=new ThreadGeneradorTraficoUDP(socket,this);
        generadorTraficoUDP.start();
        System.out.println("TRAFICO UDP INICIADO...");
    }

    public void detenerTraficoUDP_continuo()
    {
        horaFinal=System.currentTimeMillis();
        generadorTraficoUDP.detenerTrafico();
        desactivarEscuchaGPS();
    }

    public void generarTraficoTCP_continuo()
    {
        socketTCPCreado =false;
        inicializarGeneradorPosiciones();
        horaInicio=System.currentTimeMillis();
        reiniciarEstadisticas();
        //Crea el socket TCP en un hilo de ejecución diferente
        SocketTCP socket = new SocketTCP(direccionIP,puerto,this);
        socket.start();
        int i=0;
        while (socketTCPCreado ==false && i<20)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        if(i<20)
        {
            generadorTraficoTCP=new ThreadGeneradorTraficoTCP(socket,this);
            generadorTraficoTCP.start();
            System.out.println("TRAFICO TCP INICIADO...");
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            traficoTCP.setChecked(false);
            alertDialog.setTitle("Error TCP");
            alertDialog.setMessage("No se ha podido crear el Socket TCP. Verifique que el servidor se encuentra activo en el puerto " + puerto);
            alertDialog.show();

        }
    }

    public void detenerTraficoTCP_continuo()
    {
        horaFinal=System.currentTimeMillis();
        generadorTraficoTCP.detenerTrafico();
        desactivarEscuchaGPS();
    }


    public void iniciarEscenarioUDP(int n, int segundos, int envio_datos)
    {
        probando=true;
        reiniciarEstadisticas();
        inicializarGeneradorPosiciones();
        horaInicio=System.currentTimeMillis();
        for(int i =0; i<n;i++)
        {
            SocketUDP socket = new SocketUDP(direccionIP,puerto,this);
            socket.cambiarPeriodoEnvio(envio_datos);
            socket.start();
        }
        DetenerPruebas detener= new DetenerPruebas(segundos*1000,DetenerPruebas.UDP,this);
        new Thread(detener).start();

    }


    public void iniciarEscenarioTCP(int n, int segundos, int envio_datos)
    {
        inicializarGeneradorPosiciones();
        horaInicio=System.currentTimeMillis();
        reiniciarEstadisticas();
        System.out.println("Escenario TCP: Lanzando threads");
        probando=true;
        for(int i =0; i<n;i++)
        {
            //Crea el socket TCP en un hilo de ejecución diferente
            SocketTCPEscenario socketActual = new SocketTCPEscenario(direccionIP,puerto,this,i,envio_datos);
            socketActual.start();
        }
        DetenerPruebas detener= new DetenerPruebas(segundos*1000,DetenerPruebas.TCP,this);
        new Thread(detener).start();
    }




    @Override
    public void onClick(View v)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        int cant=0;
        String tit="Prueba de concurrencia TCP";
        boolean UDP=false;
        boolean ok=false;
        boolean personalizada=false;
        if(v==btnOk)
        {
            ok=true;
            direccionIP=(txtIP.getText()).toString();
            puerto=Integer.parseInt(txtPuerto.getText().toString());
            alertDialog.setTitle("Red ok");
            alertDialog.setMessage("Se han establecido con éxito los parámetros de la conexión");
            alertDialog.show();

        }
        else if(v==btnu100)
        {
            UDP=true;
            cant=100;
        }
        else if(v==btnu200)
        {
            UDP=true;
            cant=200;
        }
        else if(v==btnu300)
        {
            UDP=true;
            cant=300;
        }

        else if(v==btnt100)
        {
            cant=100;
            UDP=false;
        }
        else if(v==btnt200)
        {
            cant=200;
            UDP=false;
        }
        else if(v==btnt300)
        {
            cant=300;
            UDP=false;
        }
        else if(v==btnTCP)
        {
            UDP=false;
            personalizada=true;
        }
        else if(v==btnUDP)
        {
            UDP=true;
            personalizada=true;
        }

        if(UDP)
        {
            tit="Prueba de concurrencia UDP";
            if(!personalizada)
            {
                iniciarEscenarioUDP(cant,tiempo_duracion_prueba_sec,tiempo_defecto_envío);
            }
            else
            {
                int hilos=Integer.parseInt(txtHilos.getText().toString());
                cant=hilos;
                int tiempo_prueba=Integer.parseInt(txtTotalPrueba.getText().toString());
                int per_envio=Integer.parseInt(txtPerEnvio.getText().toString());
                iniciarEscenarioUDP(hilos, tiempo_prueba, per_envio);
            }

        }
        else
        {
            if(personalizada)
            {
                int hilos=Integer.parseInt(txtHilos.getText().toString());
                cant=hilos;
                int tiempo_prueba=Integer.parseInt(txtTotalPrueba.getText().toString());
                int per_envio=Integer.parseInt(txtPerEnvio.getText().toString());
                iniciarEscenarioTCP(hilos,tiempo_prueba,per_envio);
            }
            else
            {
                iniciarEscenarioTCP(cant, tiempo_duracion_prueba_sec, tiempo_defecto_envío);
            }

        }
        if(!ok)
        {
            alertDialog.setTitle(tit);
            alertDialog.setMessage("Se está lanzando el pool de Threads con los hilos específicados: "+cant);
            alertDialog.show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == traficoTCP)
        {
            if (isChecked) {
                generarTraficoTCP_continuo();
                mostrarMsjInicioFlujoConstante("TCP");
            } else {
                detenerTraficoTCP_continuo();
                mostrarEstadisticasTrafico("TCP");
            }
        }
        else if(buttonView== traficoUDP)
        {
            if(isChecked)
            {
                generarTraficoUDP_continuo();
                mostrarMsjInicioFlujoConstante("UDP");
            }
            else
            {
                detenerTraficoUDP_continuo();
                mostrarEstadisticasTrafico("UDP");
            }
        }
    }

    //ALERTAS Y ESTADÍSTICAS

    public void mostrarMsjInicioFlujoConstante(String tipoTrafico)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Tráfico "+tipoTrafico);
        alertDialog.setMessage("Ha iniciado el flujo constante de trafico " + tipoTrafico);
        alertDialog.show();
    }

    public void mostrarEstadisticasTrafico(String tipoTrafico)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        horaFinal=System.currentTimeMillis();
        double tiempo = (horaFinal-horaInicio)/1000;

        double errorEnvio=intentos!=0?enviados_error*100/intentos:0;
        double errorRta=(respuestas_error+respuestas_OK)!=0?respuestas_error*100/(respuestas_OK+respuestas_error):0;
        String separador="\n------------------------------";
        String msj="Envío de datos protocolo "+tipoTrafico+separador+
                "\nEnviados OK:"+enviados_OK+
                "\nEnviados Error:"+enviados_error+
                "\nIntentos Envío:"+ intentos +separador+
                "\nRespuestas OK:"+respuestas_OK+
                "\nRespuestas Error:"+respuestas_error+
                "\nTotal respuestas:"+respuestas_error+respuestas_OK+separador+
                "\nError de envíos:"+errorEnvio+
                "\nError de rtas:"+errorRta+separador+
                "\nTiempo de envío(s):"+tiempo;
        alertDialog.setTitle("Estadísticas "+tipoTrafico);
        alertDialog.setMessage(msj);
        alertDialog.show();
    }

    public void reiniciarEstadisticas()
    {
        enviados_OK=0;
        enviados_error=0;
        respuestas_OK=0;
        respuestas_error=0;
        intentos=0;
        horaInicio=System.currentTimeMillis();
        horaFinal=horaInicio;
    }

    //MÉTODOS ASOCIADOS A LA INICIALIZACIÓN Y PETICIÓN DEL GPS

    public void inicializarGeneradorPosiciones()
    {
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locListener = new GeneradorPosiciones(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            System.out.println("No se tienen los permisos");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {    // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},SOLICITUD_GPS);
            }
        }
        System.out.println("GPS:Activo");

    }

    public void desactivarEscuchaGPS() {
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

    //ONCREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOk = (Button)findViewById(R.id.btnOK);
        btnOk.setOnClickListener(this);

        btnu100=(Button)findViewById(R.id.u_100);
        btnu200=(Button)findViewById(R.id.u_200);
        btnu300=(Button)findViewById(R.id.u_300);
        btnt300=(Button)findViewById(R.id.t_300);
        btnt200=(Button)findViewById(R.id.t_200);
        btnt100=(Button)findViewById(R.id.t_100);
        btnUDP=(Button)findViewById(R.id.udp_test);
        btnTCP=(Button)findViewById(R.id.tcp_test);
        btnu100.setOnClickListener(this);
        btnu200.setOnClickListener(this);
        btnu300.setOnClickListener(this);
        btnt100.setOnClickListener(this);
        btnt200.setOnClickListener(this);
        btnt300.setOnClickListener(this);
        btnUDP.setOnClickListener(this);
        btnTCP.setOnClickListener(this);

        txtHilos=(EditText)findViewById(R.id.hilos);
        txtTotalPrueba=(EditText)findViewById(R.id.tiempo_prueba);
        txtPerEnvio=(EditText)findViewById(R.id.tiempo_envío);

        txtIP =(EditText)findViewById(R.id.txtIP);
        txtPuerto=(EditText)findViewById(R.id.txtPuerto);
        txtIP.setText(direccionIP);
        txtPuerto.setText(""+puerto);

        traficoTCP=(Switch)findViewById(R.id.btnTCP);
        traficoTCP.setOnCheckedChangeListener(this);
        traficoUDP=(Switch) findViewById(R.id.btnUDP);
        traficoUDP.setOnCheckedChangeListener(this);
    }


}
