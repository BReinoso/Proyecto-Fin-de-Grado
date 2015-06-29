package com.example.bryan.imagen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import android.hardware.Camera;


public class Imagen extends ActionBarActivity implements TextToSpeech.OnInitListener{
    /**
     * Resource to use with the assetManager
     */
    private PropertyReader propertyReader;
    private Context context;
    private Properties properties;
    /**
     * Asset Manager to use with the properties
     */
    /**
     * TextToSpeech object
     */
    private TextToSpeech myTTS;
    /**
     *Code to take a picture with the camera
     */
    private int TAKE_PICTURE = 829;
    /**
     * To control the use of application
     */
    private int n_touchs = 0;
    /**
     * To put the label of the photo after the prediction
     */
    private EditText lblPhoto;
    /**
     * To show the photo
     */
    private ImageView imgPhoto;
    /**
     * To work with the entire application
     */
    private RelativeLayout world;
    /**
     * To handle the image
     */
    private Uri selectedImage;
    private Bitmap imagen;
    /**
     * To cast the uri for the post request
     */
    private File file_image;
    /**
     * In case the connection don't work properly
     */
    private String resultado;
    /**
     * URL of the server
     */
    private String url="Servidor";

    private Camera camera;

    private Camera.Parameters parameters;

    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;


    private File fileDir;

    private Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (bytes != null) {
                Log.w("TOMADA","Si que se tienen datos");
                camera.stopPreview();
                camera.release();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                int orientation;
                if(bitmap.getHeight() < bitmap.getWidth()){
                    orientation = 90;
                } else {
                    orientation = 0;
                }

                Bitmap bMapRotate;
                if (orientation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);
                }
                imagen=bitmap;
                imgPhoto.setImageBitmap(imagen);
                file_image=new File(Environment.getExternalStorageDirectory(), "procesarImagen.jpg");
                if(file_image.exists()){
                    file_image.delete();
                }
                OutputStream os;
                try{
                    os=new FileOutputStream(file_image.getPath());
                    os.write(bytes);
                    os.flush();
                    os.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                new ImageUploader().execute();
            }else{

                Log.w("NO TOMADA","No se tienen datos");
            }
        }
    };
    /**
     * Function onCreate where the main configuration of the activity is established
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen);
        lblPhoto = (EditText) findViewById(R.id.lblPhoto);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        world = (RelativeLayout) findViewById(R.id.world);
        fileDir=getApplicationContext().getFilesDir();

        context=this;
        propertyReader = new PropertyReader(context);
        properties = propertyReader.getMyProperties("Imagen.properties");
        url=properties.getProperty(url);
        lblPhoto.setText(url);

        myTTS = new TextToSpeech(Imagen.this,Imagen.this);

        camera=Camera.open(0);
        parameters= camera.getParameters();
        surfaceView=(SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder=surfaceView.getHolder();
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        world.setOnTouchListener(new RelativeLayout.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (n_touchs == 0) {
                    tomarFoto();
                    n_touchs++;
                }
                return true;
            }
        });
    }
    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_imagen, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void tomarFoto(){
        camera.startPreview();
        camera.takePicture(null, null, pictureCallback);
    }

    /**
     * Función que se encarga de extraer el path de un fichero a través
     * de un objeto de tipo Uri
     *
     * @param uri Objeto Uri referenciando el archivo
     * @return String con el path del archivo
     */
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Internal class to do the post request
     */
    class ImageUploader extends AsyncTask<Void, Void, String> {
        /**
         * Client to do the post request
         */
        private HttpClient client;
        /**
         * HttPost object to handle the post request
         */
        private HttpPost post = new HttpPost(url);
        /**
         * To handle the data into the post request
         */
        private MultipartEntityBuilder builder = MultipartEntityBuilder.create();
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
         * To wait for the main thread
         */
        private final ProgressDialog dialog = new ProgressDialog(Imagen.this);

        /**
         * This function is necessary for the post request execution because it cannot be execute in
         * the thread main
         * @param params
         * @return Return the label or "Did not work"
         */
        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
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
                    result = "Did not work!";
                resultado = result;
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                resultado="No se ha conectado con el servidor";
                return null;
            }
        }

        /**
         *To execute before the post request
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            speakWords("Procesando imagen, esto puede tardar uno o dos minutos");
            this.dialog.setMessage("Processing...");
            this.dialog.show();
        }

        /**
         * To execute after the post request
         * @param result The result of the execution
         */
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            this.dialog.cancel();
            Imagen.this.lblPhoto.setText(resultado);
            Imagen.this.speakWords(resultado);
            n_touchs=0;
        }

    }
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

    /**
     * Se lee una cadena en voz alta
     *
     * @param speech Cadena a leer
     */
    private void speakWords(String speech) {
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Configurando el Text2Speech object
     * @param initStatus Status
     */
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}