package com.example.bryan.imagen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class Imagen extends ActionBarActivity {
    private int SELECT_IMAGE = 237;
    private int TAKE_PICTURE = 829;
    private EditText lblPhoto;
    private Button btnPhoto;
    private ImageView imgPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen);
        lblPhoto=(EditText)findViewById(R.id.lblPhoto);
        btnPhoto=(Button)findViewById(R.id.btnPhoto);
        imgPhoto=(ImageView)findViewById(R.id.imgPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogPhoto();
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
        } catch(Exception e){}
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
                    Uri selectedImage = data.getData();
                    lblPhoto.setText(getPath(selectedImage));
                    imgPhoto.setImageURI(selectedImage);
                }
            }
            if(requestCode == TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    lblPhoto.setText(getPath(selectedImage));
                    imgPhoto.setImageURI(selectedImage);
                }
            }
        } catch(Exception e){}
    }
    private String getPath(Uri uri) {
        /*String[] projection = { android.provider.MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);*/
        return new String("Ok");
    }
}
