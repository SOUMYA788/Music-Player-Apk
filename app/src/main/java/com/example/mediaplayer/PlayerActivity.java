package com.example.mediaplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

import static com.example.mediaplayer.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity {

    // View Elements
    ImageView imageView2, shuffel,previous,rewind,playPause,fastForward,next,repeat,equilizerBtn;
    ToggleButton favoriteList;
    TextView displaySongName,artistName, songStartTime, songEndTime;
    SeekBar songSeekBar;
    Animation customAnimation;

    static boolean shuffelBool = false, repeatBool = false, favSongBtn = false;

    //other importants items
    String sname;
    byte[] sImg;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    static ArrayList<MusicFiles> mySongs = new ArrayList<>(); // original Song Array
    static Uri uri;
    ArrayList<File> favSongs; // Favorite Song Array
    File favsngnamestr;
    Thread updateSeekBar;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Define Elements
        viewElements();

        //Declare Rotation Animation
        customAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotation);
        customAnimation.setInterpolator(new LinearInterpolator());

        // Getting Position through Intent
        position = getIntent().getIntExtra("audioListPosition", 0);

        mySongs = musicFiles;

        // Setup URI
        if (mySongs!=null)
        {
            playPause.setImageResource(R.drawable.ic_pause);
            uri = uri.parse(mySongs.get(position).getPath());
        }

        // Reset Media Player
        if (mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        setMusicAndArtistName();
        setMusicImage();
        displaySongName.setSelected(true);

        setupNewMediaPlayer();
        imageView2.startAnimation(customAnimation);

        setseekbarMaxAndUpdateSeekbar();

        setSongTiming();

        activatingAudioConsole();


        // ON CREATE METHOD ends here
    }

    private void activatingAudioConsole() {
        activatePlayPauseButton();
        currentSongOver();
        activateNextSongButton();
        activatePreviousSongButton();
        activateFastForwardButton();
        activateRewindButton();
        activatingEqualizer();
        activatingSongSuffelButton();
        activatingRepeatButton();
    }

    // Define View Elements on XML file
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
        favSongs = new ArrayList<>();
    }

    // Activation of Media Console
    private void activatingRepeatButton() {
        repeat.setOnClickListener(v -> {
            // created by me with youtube video help
            if (repeatBool){
                repeatBool = false;
                repeat.setImageResource(R.drawable.ic_repeat);
            }else {
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
                if (shuffelBool){
                    shuffelBool = false;
                    shuffel.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.white));
                }else {
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
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mediaPlayer.getAudioSessionId());
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                startActivityForResult(eqIntent, 13);
                Toast.makeText(getApplicationContext(), "Presenting Equalizer", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Equilizer Not Found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activateRewindButton() {
        rewind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-3000);
                }
            }
        });
    }

    private void activateFastForwardButton() {
        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
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
        if (art!=null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            imageView2.setImageBitmap(bitmap);
        }
        retriever.release();
    }

    private void setupNewMediaPlayer() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
    }

    private void setseekbarMaxAndUpdateSeekbar() {
        songSeekBar.setMax(mediaPlayer.getDuration());

        updateSeekBar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition<totalDuration)
                {
                    try
                    {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        songSeekBar.setProgress(currentPosition);
                        sleep(500);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateSeekBar.start();

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer!=null && fromUser)
                {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(songSeekBar.getProgress());
            }
        });

    }

    public void setSongTiming(){
        String endTime = createTime(mediaPlayer.getDuration());
        songEndTime.setText(endTime);
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                songStartTime.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public String createTime(int duration){
        // Used in setSongTiming
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time+=min+":";
        if (sec<10){
            time+="0";
        }
        time+= sec;
        return time;
    }

    private void activatePlayPauseButton() {
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    imageView2.clearAnimation();
                    mediaPlayer.pause();
                    playPause.setImageResource(R.drawable.ic_play);

                }else
                {
                    imageView2.startAnimation(customAnimation);
                    mediaPlayer.start();
                    playPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    private void currentSongOver() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next.performClick();
            }
        });
    }

    private void activateNextSongButton() {
        next.setOnClickListener(v -> {
            resetMediaplayerSeekbarAndAnimation();
            if (shuffelBool && !repeatBool){
                position = getRandom(mySongs.size()-1);
                setMusicImage();
                changeMusicInMediaplayer();
            }else if (!shuffelBool && !repeatBool){
                position = ((position + 1) % mySongs.size());
                setMusicImage();
                changeMusicInMediaplayer();
            }else {
                if (repeatBool && !shuffelBool){
                    setMusicImage();
                    changeMusicInMediaplayer();
                }
            }
        });
    }

    private void activatePreviousSongButton() {
        previous.setOnClickListener(v -> {
            resetMediaplayerSeekbarAndAnimation();
            if (shuffelBool && !repeatBool){
                position = getRandom(mySongs.size()-1);
                setMusicImage();
                changeMusicInMediaplayer();

            }else if (!shuffelBool && !repeatBool){
                position = ((position - 1) % mySongs.size());
                setMusicImage();
                changeMusicInMediaplayer();

            }else {
                setMusicImage();
                changeMusicInMediaplayer();

            }
        });
    }

    public void resetMediaplayerSeekbarAndAnimation() {
        mediaPlayer.stop();
        playPause.setImageResource(R.drawable.ic_play);
        mediaPlayer.release();
        imageView2.clearAnimation();
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void changeMusicInMediaplayer(){
        Uri u = Uri.parse(mySongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        setMusicAndArtistName();
        songSeekBar.setMax(mediaPlayer.getDuration());
        displaySongName.setSelected(true);
        playPause.setImageResource(R.drawable.ic_pause);
        mediaPlayer.start();
        imageView2.startAnimation(customAnimation);
        setSongTiming();
        imageAnimation();
    }

    public void imageAnimation(){
        if (mediaPlayer.isPlaying()){
            imageView2.startAnimation(customAnimation);
        }else {
            imageView2.clearAnimation();
        }
    }
}