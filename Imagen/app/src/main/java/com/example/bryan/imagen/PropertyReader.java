package com.example.bryan.imagen;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

/**
 * Clase para controlar la extraccion de propiedades desde el fichero properties.
 *
 * @version 1.0
 * @author Bryan Reinoso Cevallos
 */
public class PropertyReader {
    /**
     * Contexto de la actividad que llama al objeto
     */
    private Context context;
    /**
     * Conjunto de propiedades ya listas para trabajar con ellas
     */
    private Properties properties;

    /**
     * Contructor de ProppertyReader
     *
     * @param context contexto de la actividad que instancia
     */
    public PropertyReader(Context context){
        this.context=context;
        properties = new Properties();
    }

    /**
     * Recibe un fichero que es el que tiene las propiedades que queremos extraer y devuelve las
     * propiedades para ser usadas.
     *
     * @param file Nombre del fichero .properties con el que se quiere trabajar
     * @return Objeto properties del que se pueden extraer faacilmente las propiedades.
     */
    public Properties getMyProperties(String file){
        try{
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        }catch (Exception e){
            System.out.print(e.getMessage());
        }

        return properties;
    }
}