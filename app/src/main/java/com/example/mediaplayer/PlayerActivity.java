package com.example.mediaplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static com.example.mediaplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mediaplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mediaplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_1;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mediaplayer.MainActivity.musicFiles;
import static com.example.mediaplayer.MusicAdapter.audioMusicFiles;
import static java.lang.Thread.sleep;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection, ButtonAction {

    // View Elements
    ImageView imageView2, shuffel, previous, rewind, playPause, fastForward, next, repeat, equilizerBtn;
    ToggleButton favoriteList;
    TextView displaySongName, artistName, songStartTime, songEndTime;
    SeekBar songSeekBar;
    Animation customAnimation;

    static boolean shuffelBool = false, repeatBool = false, favSongBtn = false;

    //other importants items
    public static final String EXTRA_NAME = "song_name";
    int position;
    static ArrayList<MusicFiles> mySongs = new ArrayList<>(); // original Song Array
    static Uri uri;
    Thread updateSeekBar;
    private Handler handler = new Handler();
    public static boolean MUSIC_PLAYING;

    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        // Define Elements
        viewElements();

        // for notification of music
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        //Declare Rotation Animation
        customAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotation);
        customAnimation.setInterpolator(new LinearInterpolator());

        // Getting Position through Intent
        position = getIntent().getIntExtra("audioListPosition", 0);

        mySongs = audioMusicFiles;

        // Setup URI
        if (mySongs != null) {
            playPause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(mySongs.get(position).getPath());
        }

        // Reset Media Player
        /*
        if (musicService != null) {
            musicService.stop();
            musicService.release();
        }
         */

        //setMusicAndArtistName();
        //setMusicImage();
        //displaySongName.setSelected(true);

        //setupNewMediaPlayer
        /*
        musicService.createMediaPlayer(uri);
        musicService.start();
         */

        //imageView2.startAnimation(customAnimation);

        //songSeekBar.setMax(musicService.getDuration());
        Intent musicServiceIntent = new Intent(this, MusicService.class);
        musicServiceIntent.putExtra("musicServicePosition", position);
        startService(musicServiceIntent);

        //setSeekbarMaxAndUpdateSeekbar();

        //setSongTiming();

        //activatingAudioConsole();

        //createNotification();


        // ON CREATE METHOD ends here
    }

    private void activatingAudioConsole() {
        activatePlayPauseButton();
        activateNextSongButton();
        currentSongOver();
        activatePreviousSongButton();
        activateFastForwardButton();
        activateRewindButton();
        activatingEqualizer();
        activatingSongSuffelButton();
        activatingRepeatButton();
    }

    private void viewElements() {
        imageView2 = findViewById(R.id.imageView2);
        shuffel = findViewById(R.id.shuffel);
        previous = findViewById(R.id.previous);
        rewind = findViewById(R.id.rewind);
        playPause = findViewById(R.id.playPause);
        fastForward = findViewById(R.id.fastForward);
        next = findViewById(R.id.next);
        repeat = findViewById(R.id.repeat);
        equilizerBtn = findViewById(R.id.equilizerBtn);
        displaySongName = findViewById(R.id.displaySongName);
        artistName = findViewById(R.id.artistName);
        favoriteList = findViewById(R.id.favoriteList);
        songSeekBar = findViewById(R.id.songSeekBar);
        songStartTime = findViewById(R.id.songStartTime);
        songEndTime = findViewById(R.id.songEndTime);
    }

    // Activation of Media Console
    private void activatingRepeatButton() {
        repeat.setOnClickListener(v -> {
            if (repeatBool) {
                repeatBool = false;
                repeat.setImageResource(R.drawable.ic_repeat);
            } else {
                repeatBool = true;
                repeat.setImageResource(R.drawable.ic_repeat_one);
            }
        });
    }

    private void activatingSongSuffelButton() {
        shuffel.setOnClickListener(new View.OnClickListener() {
            //same as repeat, it is created by video help
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (shuffelBool) {
                    shuffelBool = false;
                    shuffel.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.white));
                } else {
                    shuffelBool = true;
                    shuffel.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.RoyleBlue));
                }
            }
        });
    }

    private void activatingEqualizer() {
        equilizerBtn.setOnClickListener(v -> {
            try {
                Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getApplicationContext().getPackageName());
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService.getAudioSessionId());
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                startActivityForResult(eqIntent, 13);
                Toast.makeText(getApplicationContext(), "Presenting Equalizer", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Equilizer Not Found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activateRewindButton() {
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.isPlaying()) {
                    musicService.seekTo(musicService.getCurrentPosition() - 3000);
                }
            }
        });
    }

    private void activateFastForwardButton() {
        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.isPlaying()) {
                    musicService.seekTo(musicService.getCurrentPosition() + 3000);
                }
            }
        });
    }

    private void setMusicAndArtistName() {
        displaySongName.setText(mySongs.get(position).getTitle());
        artistName.setText(mySongs.get(position).getArtist());
    }

    private void setMusicImage() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Uri u = Uri.parse(mySongs.get(position).getPath());
        retriever.setDataSource(u.toString());
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            imageView2.setImageBitmap(bitmap);
        }
        retriever.release();
    }

    private void setSeekbarMaxAndUpdateSeekbar() {
        songSeekBar.setMax(musicService.getDuration());
        updateSeekBar = new Thread() {
            @Override
            public void run() {
                try {
                    while (0 < musicService.getDuration()) {
                        songSeekBar.setProgress(musicService.getCurrentPosition());
                        sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeekBar.start();

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(songSeekBar.getProgress());
            }
        });
    }

    public void setSongTiming() {
        String endTime = createTime(musicService.getDuration());
        songEndTime.setText(endTime);
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(musicService.getCurrentPosition());
                songStartTime.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public String createTime(int duration) {
        // Used in setSongTiming
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        time += min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }

    private void activatePlayPauseButton() {
        playPause.setOnClickListener(v -> {
            playPauseButtonClicked();
        });
    }

    private void currentSongOver() {
        musicService.onCompletionListener();
    }

    private void activateNextSongButton() {
        next.setOnClickListener(v -> {
            nextButtonClicked();
        });
    }

    private void activatePreviousSongButton() {
        previous.setOnClickListener(v -> previousButtonClicked());
    }

    public void resetMediaplayerSeekbarAndAnimation() {
        if (musicService.isPlaying()) {
            musicService.stop();
            playPause.setImageResource(R.drawable.ic_play);
            musicService.release();
            imageView2.clearAnimation();
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    public void changeMusicInMediaplayer() {
        songSeekBar.setProgress(0);
        //Uri u = Uri.parse(musicFiles.get(position).getPath());
        musicService.createMediaPlayer(position);
        setMusicAndArtistName();
        songSeekBar.setMax(musicService.getDuration());
        displaySongName.setSelected(true);
        playPause.setImageResource(R.drawable.ic_pause);

        /*
        PlayerActivity.this.runOnUiThread(() -> {
            if (musicService != null) {
                try {
                    int currentPosition = musicService.getCurrentPosition();
                    songSeekBar.setProgress(currentPosition);
                    sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        */
        musicService.start();
        imageView2.startAnimation(customAnimation);
        setSongTiming();
        imageAnimation();

    }

    public void imageAnimation() {
        if (musicService.isPlaying()) {
            imageView2.startAnimation(customAnimation);
        } else {
            imageView2.clearAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();

        musicService.buttonCallBack(PlayerActivity.this);

        setMusicAndArtistName();
        setMusicImage();
        displaySongName.setSelected(true);
        imageView2.startAnimation(customAnimation);
        setSeekbarMaxAndUpdateSeekbar();
        setSongTiming();
        activatingAudioConsole();
        createNotification();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    // Button Functions
    @Override
    public void nextButtonClicked() {
        resetMediaplayerSeekbarAndAnimation();
        if (shuffelBool && !repeatBool) {
            position = getRandom(mySongs.size() - 1);
        } else {
            if (!shuffelBool && !repeatBool) {
                position = ((position + 1) % mySongs.size());
            }
        }
        setMusicImage();
        changeMusicInMediaplayer();
        currentSongOver();
        createNotification();
    }

    @Override
    public void previousButtonClicked() {
        resetMediaplayerSeekbarAndAnimation();

        if (shuffelBool && !repeatBool) {
            position = getRandom(mySongs.size() - 1);

        } else if (!shuffelBool && !repeatBool) {
            position = ((position - 1) % mySongs.size());
            setMusicImage();
        }

        setMusicImage();
        changeMusicInMediaplayer();
        currentSongOver();
        createNotification();
    }

    void createNotification() {
        if (musicService.isPlaying()) {
            musicService.showNotification(R.drawable.ic_pause);
        } else {
            musicService.showNotification(R.drawable.ic_play);
        }
    }

    @Override
    public void playPauseButtonClicked() {
        if (musicService.isPlaying()) {
            imageView2.clearAnimation();
            musicService.pause();
            playPause.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            MUSIC_PLAYING = true;

        } else {
            imageView2.startAnimation(customAnimation);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
            MUSIC_PLAYING = false;
        }
    }
}