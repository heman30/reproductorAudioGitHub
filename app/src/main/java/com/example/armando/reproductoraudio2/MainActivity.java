package com.example.armando.reproductoraudio2;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar sb;
    private Thread hiloActualizaSeekBar;
    private volatile boolean finHiloSeekBar;
    private String nombreCancion;
    private ListView lv;
    private List<String> arrayCanciones;
    private static MediaPlayer mp;
    private int posicion = 0;
    private Button botonIniciar, botonSalir;
    private int flag; /* Variable para saber cuando cambiar el texto del boton PAUSAR/CONTINUAR,
                         y para pausar/continuar la cancion.
                         Sirve para controlar en que estado estamos:
                         flag = 0 --> primer click en el boton de play
                         flag = 1 --> segundo click para pausar la cancion
                         flag = 2 --> tercer click para continuar la cancion */


    /* Funcion para que cuando el usuario mueva la seekbar con el dedo, la cancion avance
     hasta donde este indica */

    private void progresoManualSeekBar() {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int posicionActual = seekBar.getProgress();
                mp.seekTo(posicionActual);
            }
        });
    }

    /* Funcion para que la posicion de la seekbar vaya progresando al mismo tiempo que la cancion */

    private void actualizaPosicionSeekBar() {
        // creo un hilo de ejecucion nuevo para que vaya actualizando el progreso
        // de la seekbar:
        hiloActualizaSeekBar = new Thread() {
            @Override
            public synchronized void run() {
                finHiloSeekBar = false;
                int duracionTotal = mp.getDuration();
                sb.setMax(duracionTotal);
                int posicionActual = mp.getCurrentPosition();

                while (!finHiloSeekBar && (posicionActual < duracionTotal))
                {
                    try {
                        sleep(50);
                        posicionActual = mp.getCurrentPosition();
                        sb.setProgress(mp.getCurrentPosition());
                        } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                }
            } // del run
        };
        hiloActualizaSeekBar.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        flag = 0;
        nombreCancion = null;
        finHiloSeekBar = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        botonIniciar = (Button) findViewById(R.id.buttonIniciar);
        botonSalir = (Button) findViewById(R.id.buttonSalir);
        lv = (ListView) findViewById(R.id.listViewCanciones);
        sb = (SeekBar) findViewById(R.id.seekBar);
        // manejador de la seekbar para cuando el usuario la mueva con el dedo
        progresoManualSeekBar();

        Log.d("******** INICIO: ", "ACTIVITY");
        rellenaListViewCanciones();
        // actualizaSeekBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Manejador para cuando se hace click en la aplicacion */

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonIniciar:
                if (nombreCancion == null)
                {
                    break;
                }

                // se hace click en el boton para pausar la cancion
                if (flag == 1)
                {
                    mp.pause();
                    posicion = mp.getCurrentPosition();
                    botonIniciar.setText("CONTINUAR");
                }
                // se hace click para continuar la cancion
                if (flag > 1)
                {
                    mp.seekTo(posicion);
                    mp.start();
                    botonIniciar.setText("PAUSAR");
                    flag = 1;
                    break;
                }
                flag++;
                break;

            case R.id.buttonSalir:
                // si estaba en pause o reproduciendo
                if (mp != null)
                {
                    finHiloSeekBar = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mp.stop();
                    mp.release();
               }
                posicion = 0;
                finish();
                break;

            // para cuando el usuario hace click en alguna de las canciones de la lista
            default:
                // ruta para el emulador
                //si ya estaba reproduciendo, lo paro para que no haya mas de una reproduccion a la vez
                if (mp != null) {
                    finHiloSeekBar = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mp.stop();
                    mp.release();
                }
                // inicia la cancion clickada
                // ruta para el emulador:
                //mp = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/canciones/" + nombreCancion));
                // ruta para MI movil
                mp = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() +"/Music/"+nombreCancion));
                mp.start();
                botonIniciar.setText("PAUSAR");
                flag = 1;
                actualizaPosicionSeekBar();
                break;
        } // del switch
    }

    // funcion para rellenar la lista con las canciones de una carpeta en la memoria interna del movil

    private void rellenaListViewCanciones() {
        lv = (ListView) findViewById(R.id.listViewCanciones);
        arrayCanciones = new ArrayList<String>();
        // ruta en el emulador:
         //String path = Environment.getExternalStorageDirectory().toString() + "/canciones";
        // ruta en MI movil:
        String path = Environment.getExternalStorageDirectory().toString() + "/Music/";
        File f = new File(path);
        File archivos[] = f.listFiles();
        Log.d("**** Archivos: ", "Tamanyo: " + archivos.length);
        Log.d("**** Ruta sd mi movil.", "Ruta: " + path);

        for (int i = 0; i < archivos.length; i++)
        {
            // muestro en el log el listado de canciones del directorio 'canciones' de la SD
            Log.d("Archivos: ", "Nombre archivo: " + archivos[i].getName());
            String nombreArchivo = archivos[i].getName();
            arrayCanciones.add(nombreArchivo);
            // This is the array adapter, it takes the context of the activity as a
            // first parameter, the type of list view as a second parameter and your
            // array as a third parameter.
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayCanciones);
            lv.setAdapter(arrayAdapter);

            // manejar click en la lista de canciones:
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    String item = (String) adapter.getItemAtPosition(position).toString();
                    nombreCancion = item;
                    onClick(view);
                }
            });
        }
    } // de rellenaListaCanciones
}






