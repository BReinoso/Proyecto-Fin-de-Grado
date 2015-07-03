package com.example.bryan.imagen;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Esta clase se encarga de ejecutar la peticion POST al servidor, extiende de AsyncTask porque las
 * conexiones de este tipo no pueden ser realizadas en el hilo principal.
 *
 * @author Bryan Reinoso Cevallos
 */
class ImageUploader extends AsyncTask<Void, Void, String> {
    /**
     * Imagen que se va a enviar
     */
    private File file_image;
    /**
     * Url para conectarse al servidor
     */
    private String url;
    /**
     * Client to do the post request
     */
    private HttpClient client;
    /**
     * HttPost object to handle the post request
     */
    private HttpPost post;
    /**
     * To handle the data into the post request
     */
    private MultipartEntityBuilder builder;
    /**
     * To handle the response after the post request, form here we will receive the prediction
     * label
     */
    private HttpResponse response;
    /**
     * Special object to send the image into the post request
     */
    private FileBody bin;
    /**
     * To handle the header of post request
     */
    private HttpEntity yourEntity;
    /**
     * Contstructor de la clase ImageUploader
     *
     * @param url Url al servidor
     */
    public ImageUploader(String url){
        this.url=url;
        post= new HttpPost(url);
        builder= MultipartEntityBuilder.create();
    }

    /**
     * Establece la imagen que se quiere enviar
     *
     * @param file Imagen a enviar
     */
    public void setFile_image(File file){
        this.file_image=file;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(Void... params) {
        /**
         * To save the result
         */
        String result = "";
        try {
            client = new DefaultHttpClient();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            bin = new FileBody(file_image);
            builder.addPart("file", bin);
            yourEntity = builder.build();
            post.setEntity(yourEntity);

            response = null;
            response = client.execute(post);

            InputStream inputStream = null;
            inputStream = response.getEntity().getContent();
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "No se ha obtenido resultado del servidor";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Hubo problemas al conectarse con el servidor, compruebe su conexión a Internet.";
        }
    }

    /**
     *{@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    /**
     * El metodo toma un objeto dee tipo InputStream y lo transforma en un objeto
     * de topo String usando los datos que el InputStream contiene.
     *
     * @param inputStream Objeto a convertir en string
     * @return  String resultate de la conversión
     * @throws IOException Input/Ouput Exception
     */
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        bufferedReader.close();
        inputStream.close();
        return result;

    }
}