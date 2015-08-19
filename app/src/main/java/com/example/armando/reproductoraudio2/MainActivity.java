package com.example.armando.reproductoraudio2;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String nombreCancion;
    private ListView lv;
    private List<String> arrayCanciones;
    private static MediaPlayer mp;
    private int posicion = 0;
    private Button botonIniciar;
    private int flag; /* Variable para saber cuando cambiar el texto del boton PAUSAR/CONTINUAR,
                         y para pausar/continuar la cancion.
                         Sirve para controlar en que estado estamos:
                         flag = 0 --> primer click en el boton de play
                         flag = 1 --> segundo click para pausar la cancion
                         flag = 2 --> tercer click para continuar la cancion */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        botonIniciar = (Button) findViewById(R.id.buttonIniciar);
        flag = 0;
        nombreCancion = null;
        Log.d("******** INICIO: ", "ACTIVITY");
        rellenaListViewCanciones();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.buttonIniciar:

                if (nombreCancion == null) break;
                // primera vez que se hace click en el boton de play
                if (mp == null) {
                    //mp = MediaPlayer.create(this, R.raw.tiki_tiki);
                    mp = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() +"/canciones/"+nombreCancion));
                    mp.start();
                    botonIniciar.setText("PAUSAR");
                }
                // se hace click en el boton para pausar la cancion
                //if (mp != null && mp.isPlaying() && flag == 1) {
                if (flag == 1) {
                    mp.pause();
                    posicion = mp.getCurrentPosition();
                    botonIniciar.setText("CONTINUAR");
                }
                // se hace click para continuar la cancion
                //if (!mp.isPlaying() && flag == 2) {
                if (flag == 2) {
                    mp.seekTo(posicion);
                    mp.start();
                    botonIniciar.setText("PAUSAR");
                    flag = 0;
                }
                flag++;
                break;

            case R.id.buttonDetener:
                if (mp != null) {
                    mp.stop();
                    mp = null;
                    posicion = 0;
                    flag = 0;
                    nombreCancion = null;
                    botonIniciar.setText("PLAY");
                }
                break;

            case R.id.buttonSalir:
                // si estaba en pause o reproduciendo
                if (mp != null) {
                    mp.stop();
                    mp.release();
               }
                posicion = 0;
                finish();
                break;
        }
    }

    public void kakaPrueba() {
        // funcion para comprobar el funcionamiento de Git
    }

    public void rellenaListViewCanciones() {

        lv = (ListView) findViewById(R.id.listViewCanciones);
        arrayCanciones = new ArrayList<String>();
        String path = Environment.getExternalStorageDirectory().toString() + "/canciones";
        //Log.d("Archivos: ", "Directorio: " + path);
        File f = new File(path);
        File archivos[] = f.listFiles();
        Log.d("******** Archivos: ", "Tamanyo: " + archivos.length);

        for (int i = 0; i < archivos.length; i++) {
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
                    //view.setSelected(true);
                    //view.setPressed(true);
                    String item = (String) adapter.getItemAtPosition(position).toString();
                    nombreCancion = item;
                    //Log.d("Linea", "Nombre archivo: "+nombreCancion);
                }
            });
        }
    }
}






