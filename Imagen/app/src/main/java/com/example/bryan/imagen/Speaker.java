package com.example.bryan.imagen;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Clase Speaker, que se encarga de trabajar con la biblioteca TexttoSpeech
 *
 * @author Bryan Reinoso Cevallos
 */
class Speaker implements TextToSpeech.OnInitListener{
    /**
     * TextToSpeech object
     */
    private TextToSpeech myTTS;
    /**
     *Contexto de la actividad que instancia esta clase
     */
    private Context context;

    /**
     * Constructor de la clase Speaker
     *
     * @param context Contexto de la clase que instancia el objeto
     */
    public Speaker(Context context){
        myTTS=new TextToSpeech(context,this);
        this.context=context;
    }
    /**
     * Se lee una cadena en voz alta
     *
     * @param speech Cadena a leer
     */
    public void speakWords(String speech) {
        while(myTTS.isSpeaking());
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * {@inheritDoc}
     */
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE) {
                myTTS.setLanguage(Locale.US);
            }
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(context, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}
