package com.example.armando.reproductoraudio2;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
                // primera vez que se hace click en el boton de play
                if (mp == null) {
                    mp = MediaPlayer.create(this, R.raw.tiki_tiki);
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
                    posicion = 0;
                }
                break;

            case R.id.buttonSalir:
                if (mp != null) {
                    mp.stop();
                    mp.release();
                    posicion = 0;
                }
                finish();
                break;
        }
    }

    public void kakaPrueba () {
        // funcion para comprobar el funcionamiento de Git
    }

    public void listadoCancionesSD(final View view) {

        TableLayout tableLayout = (TableLayout) findViewById(R.id.TableLayoutPrincipal);
        String path = Environment.getExternalStorageDirectory().toString() + "/canciones";
        Log.d("Archivos: ", "Directorio: " + path);
        File f = new File(path);
        File archivos[] = f.listFiles();
        Log.d("Archivos: ", "**Tamanyo: " + archivos.length);

       for (int i = 0; i < archivos.length; i++) {
            // muestro en el log el listado de canciones del directorio 'canciones' de la SD
            Log.d("Archivos: ", "Nombre archivo: "+archivos[i].getName());
           String nombreArchivo = archivos[i].getName();
            // creo una fila para la tableRow
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT ,TableRow.LayoutParams.WRAP_CONTENT);
            tableRow.setLayoutParams(layoutParams);
            TextView tv = new TextView(this);
            tv.setText(nombreArchivo);
            // anyado el textview a la fila
            tableRow.addView(tv);
            tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

           // gestionar click en las filas de la lista de canciones
            tableRow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view1) {
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
                }
            });
        }
    }
}





