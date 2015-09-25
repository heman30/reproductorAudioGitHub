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

/**
 * Creates and displays an audio player with common/basic functionality.
 * @author Armando Quiles
 */

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar seekBar;
    private Thread updateSeekbarThread;
    private volatile boolean seekbarThreadEnd;
    private String songName;
    private ListView songsListview;
    private List<String> songsArray;
    private static MediaPlayer mediaplayer;
    private int songProgressPosition = 0;
    private Button startSongButton, exitAppButton;
    /* Variable used to know when to change text from button startSongButton from START, TO PAUSE/CONTINUE.
    Values:
    0 --> first click on the button. Button says START.
    1 --> second click on the button. Button says PAUSE.
    2 --> third click on the button. Button says CONTINUE. */
    private int pauseOrContinueFlag;


    /**
     * Sets song position to the one speficied by the touch of the user.
     */
    private void setManualProgressSeekbar() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int currentPosition = seekBar.getProgress();
                mediaplayer.seekTo(currentPosition);
            }
        });
    } // setManualProgressSeekbar

    /**
     * A new thread moves forward the seekbar cursor as the song progresses.
     */
    private void updateSeekbarPosition() {
        updateSeekbarThread = new Thread() {
            @Override
            public synchronized void run() {
                int totalDuration = mediaplayer.getDuration();
                seekBar.setMax(totalDuration);
                int currentPosition = mediaplayer.getCurrentPosition();
                seekbarThreadEnd = false;

                while (!seekbarThreadEnd && (currentPosition < totalDuration))
                {
                    try {
                        sleep(50);
                        currentPosition = mediaplayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);

                    } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                }
            } // run
        };
        updateSeekbarThread.start();
    } // updateSeekbarPosition

    /**
     * Initializes all the app elements.
     *
     * @param savedInstanceState
     * @see fillListviewSongs()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pauseOrContinueFlag = 0;
        songName = null;
        seekbarThreadEnd = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSongButton = (Button) findViewById(R.id.buttonIniciar);
        exitAppButton = (Button) findViewById(R.id.buttonSalir);
        songsListview = (ListView) findViewById(R.id.listViewCanciones);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        setManualProgressSeekbar();
        fillListviewSongs();
        Log.d("******** INICIO: ", "ACTIVITY");
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

    /**
     * Handles user touch events for the application.
     */

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonIniciar:
                if (songName == null) {
                    break;
                }
                // user clicked to pause the song
                if (pauseOrContinueFlag == 1) {
                    mediaplayer.pause();
                    songProgressPosition = mediaplayer.getCurrentPosition();
                    startSongButton.setText("CONTINUAR");
                }
                // user clicked to continue the song
                if (pauseOrContinueFlag > 1) {
                    mediaplayer.seekTo(songProgressPosition);
                    mediaplayer.start();
                    startSongButton.setText("PAUSAR");
                    pauseOrContinueFlag = 1;
                    break;
                }
                pauseOrContinueFlag++;
                break;

            case R.id.buttonSalir:
                // song status is paused or playing
                if (mediaplayer != null) {
                    seekbarThreadEnd = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mediaplayer.stop();
                    mediaplayer.release();
                 } // if
                songProgressPosition = 0;
                finish();
                break;

            // user clicks on any song from the list
            default:
                /* if player was playing, stop playing the current song (if any)
                   and start playing the selected one */
                if (mediaplayer != null) {
                    seekbarThreadEnd = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mediaplayer.stop();
                    mediaplayer.release();
                } // if
                // emulator path to songs folder
                mediaplayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/canciones/" + songName));
                // smart-phone path to songs folder
                // mediaplayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() +"/Music/"+ songName));
                mediaplayer.start();
                startSongButton.setText("PAUSAR");
                pauseOrContinueFlag = 1;
                updateSeekbarPosition();
                break;
        } // switch
    } // onClick

    /**
     * Reads a folder containing songs in either the smart-phone or the emulator, and populates
     * a Listview with the content.
     */
    private void fillListviewSongs() {
        songsListview = (ListView) findViewById(R.id.listViewCanciones);
        songsArray = new ArrayList<String>();
        // emulator path to folder containing songs
         String pathToMusicFolder = Environment.getExternalStorageDirectory().toString() + "/canciones";
        // smart-phone path to folder containing songs
        // String pathToMusicFolder = Environment.getExternalStorageDirectory().toString() + "/Music/";
        File musicFolderFile = new File(pathToMusicFolder);
        File[] songs = musicFolderFile.listFiles();
        /* Log.d("**** Archivos: ", "Tamanyo: " + songs.length);
        Log.d("**** Ruta sd mi movil.", "Ruta: " + pathToMusicFolder); */

        for (int songPosition = 0; songPosition < songs.length; songPosition++) {
            String fileName = songs[songPosition].getName();
            songsArray.add(fileName);
            /* This is the array adapter, it takes the context of the activity as a
             first parameter, the type of list view as a second parameter and your
             array as a third parameter. */
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songsArray);
            songsListview.setAdapter(arrayAdapter);

            songsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    String item = (String) adapter.getItemAtPosition(position).toString();
                    songName = item;
                    onClick(view);
                }
            });
        } // for
    } // fillListviewSongs
}






