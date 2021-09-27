package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static com.example.mediaplayer.R.drawable.ic_repeat_one;

public class PlayerActivity extends AppCompatActivity {

    // List of items in music player app
    ImageView imageView2,shuffel,previous,rewind,playPause,fastForward,next,repeat,equilizerBtn,favoriteList;
    TextView displaySongName, songStartTime, songEndTime;
    SeekBar songSeekBar;
    Animation customAnimation;

    static boolean shuffelBool = false, repeatBool = false;

    //other importants items
    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Define Elements
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
        favoriteList = findViewById(R.id.favoriteList);
        songSeekBar = findViewById(R.id.songSeekBar);
        songStartTime = findViewById(R.id.songStartTime);
        songEndTime = findViewById(R.id.songEndTime);


        //Declear Rotation Animation
        customAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);

        // stopping mediaplayer, if it null
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = intent.getStringExtra("songName");
        position = bundle.getInt("position",0);
        displaySongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        displaySongName.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        //startAnimation(imageView2);
        imageView2.startAnimation(customAnimation);



        updateSeekBar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        songSeekBar.setProgress(currentPosition);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        songSeekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() == mediaPlayer.getDuration()){
                    next.performClick();
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

        // Setting Start Timing and End Time of Songs
        setSongTiming();


        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    playPause.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                    imageView2.clearAnimation();
                }else {
                    playPause.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                    // startAnimation(imageView2);
                    imageView2.startAnimation(customAnimation);

                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMediaplayerAndSeekbar();
                if (shuffelBool && !repeatBool){
                    position = getRandom(mySongs.size()-1);
                    changeMusicInMediaplayer();
                }else if (!shuffelBool && !repeatBool){
                    position = ((position + 1) % mySongs.size());
                    changeMusicInMediaplayer();
                }else {
                    changeMusicInMediaplayer();
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMediaplayerAndSeekbar();
                if (shuffelBool && !repeatBool){
                    position = getRandom(mySongs.size()-1);
                    changeMusicInMediaplayer();
                }else if (!shuffelBool && !repeatBool){
                    position = ((position + 1) % mySongs.size());
                    changeMusicInMediaplayer();
                }else {
                    changeMusicInMediaplayer();
                }
            }
        });

        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

        equilizerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                    eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getPackageName());
                    eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mediaPlayer.getAudioSessionId());
                    eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                    startActivityForResult(eqIntent, 13);
                    Toast.makeText(PlayerActivity.this, "Presenting Equalizer", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(PlayerActivity.this, "Equilizer Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        });


        shuffel.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (shuffelBool){
                    shuffelBool = false;
                    shuffel.setBackgroundTintList(ContextCompat.getColorStateList(PlayerActivity.this, R.color.white));

                }else {
                    shuffelBool = true;
                    shuffel.setBackgroundTintList(ContextCompat.getColorStateList(PlayerActivity.this, R.color.RoyleBlue));
                }
            }
        });


        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBool){
                    repeatBool = false;
                    repeat.setImageResource(R.drawable.ic_repeat);
                }else {
                    repeatBool = true;
                    repeat.setImageResource(R.drawable.ic_repeat_one);
                }
            }
        });
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
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

    public void imageAnimation(){
        if (mediaPlayer.isPlaying()){
            imageView2.startAnimation(customAnimation);
        }else {
            imageView2.clearAnimation();
        }
    }

    public void resetMediaplayerAndSeekbar(){
        mediaPlayer.stop();
        mediaPlayer.release();
    }
    public void changeMusicInMediaplayer(){
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        sname = mySongs.get(position).getName();
        displaySongName.setText(sname);
        mediaPlayer.start();
        playPause.setBackgroundResource(R.drawable.ic_pause);
        //startAnimation(imageView2);
        setSongTiming();
        imageAnimation();
    }

    /*
    // We can use this also, but in this case infinite loop is not possible. so we remove this technique

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView2, "rotation", 0f, 360f);
        // animator.setCurrentPlayTime(mediaPlayer.getDuration());
        animator.setDuration(2000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(mediaPlayer.getDuration());
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    */

    public String createTime(int duration){
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
    /*
    public void metaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null){

        }
    }
    */


}