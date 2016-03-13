package com.example.usuario.redes_lab5;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Usuario on 12/03/2016.
 */
public class GeneradorPosiciones implements Runnable, LocationListener
{
    private MainActivity principal;
    private Location coordenadas;
    private boolean estado;


    GeneradorPosiciones(MainActivity pal)
    {
        principal=pal;
        boolean estado=true;
    }


    public void run()
    {

        while(estado)
        {
            principal.anotarCoordenada(coordenadas, System.currentTimeMillis());
            System.out.println("COORDENADA NULL:"+coordenadas==null);
            try
            {
                Thread.sleep(1000);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }

    }


    @Override
    public void onLocationChanged(Location location)
    {
        coordenadas=location;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
