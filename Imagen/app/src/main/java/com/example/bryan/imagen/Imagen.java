package com.example.bryan.imagen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;


public class Imagen extends ActionBarActivity implements TextToSpeech.OnInitListener{
    /**
     * Resource to use with the assetManager
     */
    Resources resources = this.getResources();
    /**
     * Asset Manager to use with the properties
     */
    AssetManager assetManager = resources.getAssets();
    /**
     * Properties to load the properties
     */
    Properties properties = new Properties();
    /**
     * TextToSpeech object
     */
    private TextToSpeech myTTS;
    /**
     * Code to check data for TTS
     */
    private int MY_DATA_CHECK_CODE = 0;
    /**
     *Code to select image from the galery
     */
    private int SELECT_IMAGE = 237;
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
    /**
     * To cast the uri for the post request
     */
    private File file_image;
    /**
     * In case the connection don't work properly
     */
    private String resultado = "No se ha conectado con el servidor";
    /**
     * URL of the server
     */
    private String url="Servidor";
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

        try {
            InputStream inputStream = assetManager.open("Imagen.properties");
            properties.load(inputStream);
            System.out.println("The properties are now loaded");
            System.out.println("properties: " + properties);
            url=properties.getProperty(url);
        } catch (IOException e) {
            System.err.println("Failed to open microlog property file");
            e.printStackTrace();
        }

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        world.setOnTouchListener(new RelativeLayout.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (n_touchs == 0) {
                    dialogPhoto();
                    n_touchs++;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void dialogPhoto() {
        try {
            final CharSequence[] items = {"Seleccionar de la galería", "Hacer una foto"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccionar una foto");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_IMAGE);
                            break;
                        case 1:
                            startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
                            break;
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_imagen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == SELECT_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    imgPhoto.setImageURI(selectedImage);
                    file_image = new File(getPath(selectedImage));
                    new ImageUploader().execute();
                }
            }
            if (requestCode == TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    imgPhoto.setImageURI(selectedImage);
                    file_image = new File(getPath(selectedImage));
                    new ImageUploader().execute();
                }
            }
            if (requestCode == MY_DATA_CHECK_CODE) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(Imagen.this,Imagen.this);
                }
                else {
                    //no data - install it now
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        }

    }
    private void leerTexto(){
        lblPhoto.setText(resultado);
        TextToSpeech ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        }
        );
        Locale loc=new Locale("es","","");
        ttobj.setLanguage(loc);
        ttobj.speak(resultado,TextToSpeech.QUEUE_FLUSH, null);
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
    //speak the user text
    private void speakWords(String speech) {

        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}