package com.example.usuario.redes_lab5;

/**
 * Created by Usuario on 13/03/2016.
 */
public class DetenerPruebas  implements Runnable
{
    int tiempo;
    public final static boolean UDP =true;
    public final static boolean TCP = false;
    MainActivity principal;
    boolean tipo_prueba;
    public DetenerPruebas(int n, boolean tipo, MainActivity pal)
    {
        tiempo = n;
        principal=pal;
        tipo_prueba=tipo;
    }

   @Override
    public void run()
   {
       try
       {
           Thread.sleep(tiempo);
           principal.probando=false;
       }
       catch(Exception e)
       {
           principal.probando=false;
           System.out.println("Prueba Forzada a terminar");
       }


    }
}
