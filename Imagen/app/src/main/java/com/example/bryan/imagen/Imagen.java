package com.example.bryan.imagen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Imagen extends ActionBarActivity {
    private int SELECT_IMAGE = 237;
    private int TAKE_PICTURE = 829;
    private int n_touchs=0;
    private EditText lblPhoto;
    private ImageView imgPhoto;
    private RelativeLayout world;
    private HttpClient client;
    private HttpPost post=new HttpPost("http://192.168.1.138");
    private MultipartEntityBuilder builder=MultipartEntityBuilder.create();
    private HttpResponse response;
    private Uri selectedImage;
    private File file_image;
    private String resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen);
        lblPhoto=(EditText)findViewById(R.id.lblPhoto);
        imgPhoto=(ImageView)findViewById(R.id.imgPhoto);
        world=(RelativeLayout)findViewById(R.id.world);
        client=new DefaultHttpClient();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
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

    private void dialogPhoto(){
        try{
            final CharSequence[] items = {"Seleccionar de la galería", "Hacer una foto"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccionar una foto");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch(item){
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
        } catch(Exception e){
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

        try{
            if (requestCode == SELECT_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    imgPhoto.setImageURI(selectedImage);
                    file_image=new File(selectedImage.toString());
                    new ImageUploader().execute();
                    lblPhoto.setText(resultado);
                }
            }
            if(requestCode == TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    imgPhoto.setImageURI(selectedImage);
                    file_image=new File(selectedImage.toString());
                    lblPhoto.setText(resultado);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    class ImageUploader extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String result = "";
            try {

                // creating a file body consisting of the file that we want to
                // send to the server
                FileBody bin;
                bin = new FileBody(file_image);

                /**
                 * An HTTP entity is the majority of an HTTP request or
                 * response, consisting of some of the headers and the body, if
                 * present. It seems to be the entire request or response
                 * without the request or status line (although only certain
                 * header fields are considered part of the entity).
                 *
                 * */
                builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("file",file_image, ContentType.create("image/jpeg"), file_image.getName());
                post.setHeader("enctype", "multipart/form-data");
                post.setEntity(builder.build());

                // Execute POST request to the given URL
                response= null;
                response = client.execute(post);

                // receive response as inputStream
                InputStream inputStream = null;
                inputStream = response.getEntity().getContent();

                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";
                resultado =result;
                return result;
            } catch (Exception e) {

                return null;
            }

            // return result;
        }
        private String convertInputStreamToString(InputStream inputStream)
                throws IOException {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }
    }
}
