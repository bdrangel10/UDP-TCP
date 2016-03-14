package com.example.usuario.redes_lab5;import android.location.Location;import android.location.LocationListener;import android.os.Bundle;/** * Created by Usuario on 12/03/2016. */public class GeneradorPosiciones implements Runnable, LocationListener{    private MainActivity principal;    private Location coordenadas;    public boolean detener;    GeneradorPosiciones(MainActivity pal)    {        principal=pal;    }    public void run()    {        System.out.println("Inició el generador de posiciones");        detener=false;        while(!detener)        {            principal.anotarCoordenada(coordenadas, System.currentTimeMillis());            System.out.println("COORDENADA NULL:"+coordenadas==null);            try            {                Thread.sleep(1000);            }            catch(Exception e)            {                e.printStackTrace();            }        }    }    public Location darUbicacion()    {        return coordenadas;    }    @Override    public void onLocationChanged(Location location)    {        coordenadas=location;        System.out.println("*********************GPS Actualizado");    }    @Override    public void onStatusChanged(String provider, int status, Bundle extras) {    }    @Override    public void onProviderEnabled(String provider) {    }    @Override    public void onProviderDisabled(String provider) {    }}