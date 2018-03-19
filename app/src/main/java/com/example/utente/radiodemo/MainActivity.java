package com.example.utente.radiodemo;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Button btn_Play;
    private MediaPlayer mediaPlayer;
    boolean connected = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    IcyStreamMeta streamMeta;
    MetadataTask2 metadataTask2;
    String title_artist;
    TextView textView;



    private boolean prepared = false;
    private boolean started = false;
    private String stream = "http://nr3.newradio.it:8358/stream";
    //private String stream = "http://rdo.fm/r/8fa3e";
    //private String stream = "http://wma02.fluidstream.net:5010/";
    //private String stream = "http://onair18.xdevel.com:8212/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_Play = (Button) findViewById(R.id.b_Play);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE);


        btn_Play.setVisibility(View.INVISIBLE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        textView = (TextView) findViewById(R.id.texts);


        streamMeta = new IcyStreamMeta();
        try {
            streamMeta.setStreamUrl(new URL(stream));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        metadataTask2 =new MetadataTask2();
        try {
            metadataTask2.execute(new URL(stream));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task,100, 10000);

        final ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //c'è connessione
            connected = true;
            new PlayerTask().execute(stream ,"true");
        }
        else {
            setContentView(R.layout.no_conn);
            connected = false;
            Button refresh = (Button) findViewById(R.id.btnR);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
                        connected = true;
                        new PlayerTask().execute(stream ,"true");
                        finish();
                        startActivity(getIntent());
                    }
                    else{
                        connected = false;
                        Toast.makeText(getApplicationContext(),"No Internet connection found",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                     //c'è connessione
                    connected = true;
                    swipeRefreshLayout.setRefreshing(false);
                    //new PlayerTask().execute(stream ,"true");
                }
                else{
                    connected = false;
                    setContentView(R.layout.no_conn);
                    Button refresh = (Button) findViewById(R.id.btnR);
                    refresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
                                connected = true;
                                new PlayerTask().execute(stream ,"true");
                                finish();
                                startActivity(getIntent());
                            }
                            else{
                                connected = false;
                                Toast.makeText(getApplicationContext(),"No Internet connection found",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    protected class MetadataTask2 extends AsyncTask<URL, Void, IcyStreamMeta>
    {
        @Override
        protected IcyStreamMeta doInBackground(URL... urls)
        {
            try
            {
                streamMeta.refreshMeta();
                Log.e("Retrieving MetaData","Refreshed Metadata");
            }
            catch (IOException e)
            {
                Log.e(MetadataTask2.class.toString(), e.getMessage());
            }
            return streamMeta;
        }

        @Override
        protected void onPostExecute(IcyStreamMeta result)
        {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!isFinishing()){
                        try {
                            Thread.sleep(1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        title_artist=streamMeta.getStreamTitle();
                                        if(title_artist.length()>0)
                                            textView.setText(title_artist);
                                        Log.i("TEXT", title_artist);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    catch (NetworkOnMainThreadException e){
                                        e.printStackTrace();
                                    }
                                    //(Log.e("Retrieved title_artist", title_artist);

                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }) ;t.start();


        }
    }

    class MyTimerTask extends TimerTask {
        public void run() {
            try {
                streamMeta.refreshMeta();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String title_artist=streamMeta.getStreamTitle();
                Log.i("ARTIST TITLE", title_artist);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /*public void showProgress(){
        //Set the progress status zero
        progressStatus = 0;
        //Visible the progressBar
        pb.setVisibility(View.VISIBLE);

        //Start the operation in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    //Update progressStaus
                    progressStatus ++;
                }
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        pb.setProgress(progressStatus);

                        if (progressStatus == 100) {
                            pb.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();
    }*/

     class PlayerTask extends AsyncTask<String,Void,Boolean> {
         @Override
         protected Boolean doInBackground(String... strings) {
             try {
                 mediaPlayer.setDataSource(strings[0]);
                 prepared = strings[1]!="false";
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return prepared;
         }

         @Override
         protected void onPostExecute(Boolean aBoolean) {
             super.onPostExecute(aBoolean);
             try {
                 btn_Play.setVisibility(View.VISIBLE);
                 mediaPlayer.prepare();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             if(aBoolean) {
                 mediaPlayer.start();
                 btn_Play.setText("Pause");
             }
             btn_Play.setEnabled(true);

             btn_Play.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if(started ){
                         started =false;
                         mediaPlayer.start();
                         btn_Play.setText("Pause");
                     }else{
                         btn_Play.setText("Play");
                         mediaPlayer.pause();
                         mediaPlayer.reset();
                         mediaPlayer= new MediaPlayer();
                         mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                         started =true;
                         new PlayerTask().execute(stream , "false");
                        /*
                         mediaPlayer.pause();
                         mediaPlayer.reset();
                         mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                         try {
                             mediaPlayer.setDataSource(stream);
                             mediaPlayer.prepare();
                             prepared = true;
                         } catch (IOException e) {
                             e.printStackTrace();
                         }*/
                     }
                 }
             });
         }

     }

    @Override
    protected void onPause() {
        super.onPause();
        if (started){
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(started){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(prepared){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}
