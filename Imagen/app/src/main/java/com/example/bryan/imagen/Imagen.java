package com.example.bryan.imagen;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import android.hardware.Camera;

/**
 * Esta es la clase para la actividad principal de la aplicación, en ella se controla toda su
 * ejecución.
 *
 * @version 2.7
 * @author Bryan Reinoso Cevallos
 */
public class Imagen extends ActionBarActivity {
    /**
     * Objeto con el que trabajaremos para ejecutar y usar la biblioteca Text2Speech
     */
    private ImageUploader img;
    /**
     * Clase que se encarga de extraer de un fichero .propeerties el objeto Properties para que
     * podamos acceder a las propiedades de manera muy sencilla
     */
    private PropertyReader propertyReader;
    /**
     * Objeto tipo Context para guardar el contexto de la aplicación
     */
    private Context context;
    /**
     * Objeto tipo propiedades donde guardaremos las propiedades del fichero properties de la
     * aplicacion.
     */
    private Properties properties;

    /**
     * Instancia de la clase Speaker, sirve para usar la biblioteca TextToSpeach
     */
    private Speaker speaker;
    /**
     * Para evitar que el usuario pueda hacer un mal uso de los toques de pantalla
     */
    private int n_touchs = 0;
    /**
     * Para presentar la descripcion de la imagen una vez se ha ejecutado la prediccion
     */
    private EditText lblPhoto;
    /**
     * Para mostrar la foto en la interefaz una vez se ha ejecutado la prediccion
     */
    private ImageView imgPhoto;
    /**
     * Objeto que hace referencia al contenedor principal de la applicacion
     */
    private RelativeLayout world;
    /**
     * Objeto en el que guardaremos la imagen para mostrarla a traves de la interfaz
     */
    private Bitmap imagen;
    /**
     *  Objeto de tipo File para construir el cuerpo de la peticion POST
     */
    private File file_image;
    /**
     * Objeto en el que se guardara el resultado de la prediccion
     */
    private String resultado;
    /**
     * URL para conectarse al servidor, extraido de un fichero de propiedades
     */
    private String url="Servidor";
    /**
     * Objeto camara para controlar la camara desde nuestra aplicacion. Ahora esta actualizado, pero
     * para uso con Android lolipop asi que se ha decidido usar este objeto.
     */
    private Camera camera;
    /**
     * Objeto referencia al surfaceView del xml de la actividad, donde se vera la previsualizacion
     * de la camara. Nosotros lo tenemos puesto en transparente.
     */
    private SurfaceView surfaceView;
    /**
     *Este objeto sirve como contenedor de la surfaceView y es con el que iniciamos la previsualizacion
     * de la camara, porque sin iniciarlo la camara no permite tomar fotos.
     */
    private SurfaceHolder surfaceHolder;
    /**
     * Boton que se autoactiva para dar las primeras instrucciones al usuario
     */
    private Button button;
    /**
     * ProgressDialog para asegurar el correcto uso de la aplicación
     */
    private ProgressDialog progressDialog;
    /**
     * Callback para recoger la imagen la foto tomada, después se procesa y se procede a mandar
     * la petición post.
     */
    private Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (bytes != null) {
                Log.w("TOMADA","Si que se tienen datos");
                camera.stopPreview();
                camera.release();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imagen=corregirOrientación(bitmap);
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
                imgPhoto.setImageBitmap(imagen);
                img.setFile_image(file_image);
                progressDialog=ProgressDialog.show(Imagen.this, "", "Procesando...");
                try {
                    speaker.speakWords("Procesando imagen, esto puede tardar uno o dos minutos");
                    resultado=img.execute().get();
                    lblPhoto.setText(resultado);
                    speaker.speakWords(resultado);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                button.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        button.performClick();
                    }

                }, 2000);
            }else{

                Log.w("NO TOMADA","No se tienen datos");
            }
        }
    };
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen);
        lblPhoto = (EditText) findViewById(R.id.lblPhoto);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        world = (RelativeLayout) findViewById(R.id.world);
        button= (Button) findViewById(R.id.initButton);;

        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                speaker.speakWords("Apunte con el móvil en la dirección deseada y toque la pantalla para tomar la foto.");
                n_touchs=0;
            }
        });

        context=this;
        propertyReader = new PropertyReader(context);
        properties = propertyReader.getMyProperties("Imagen.properties");
        url=properties.getProperty(url);
        lblPhoto.setText("A la espera de que se toque la pantalla");

        speaker = new Speaker(context);

        surfaceView=(SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder=surfaceView.getHolder();

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

        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.performClick();
            }

        }, 2000);
    }
    /**
     *{@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_imagen, menu);
        return true;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Funcion que se encarga de que la aplicacion tome la foto sin necesidad de la previsualizacion
     * y capture los datos en un Callback
     *
     * La camara se inicia aqu porque se libera los recursos de esta cada vez que se ha tomado la foto
     */
    public void tomarFoto() {
        img= new ImageUploader(url);
        camera=Camera.open(0);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        camera.takePicture(null, null, pictureCallback);
    }

    /**
     * Funcion que se encarga de extraer el path de un fichero a traves
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
     * Funcion que se encarga de corregir la orientacion de la imagen de cara a presentarla en la
     * interfaz grafica.
     *
     * @param bitmapEntrada Imagen que queremos corregir su orientacion
     * @return Imagen con orientacion correcta
     */
    private Bitmap corregirOrientación(Bitmap bitmapEntrada){
        Bitmap bitmap=bitmapEntrada;
        int orientation;
        if(bitmap.getHeight() < bitmap.getWidth()){
            orientation = 90;
        } else {
            orientation = 0;
        }

        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
        }

        return bitmap;
    }
}