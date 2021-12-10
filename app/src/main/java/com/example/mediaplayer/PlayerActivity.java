package com.example.mediaplayer;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

import static com.example.mediaplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mediaplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mediaplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mediaplayer.MusicAdapter.audioMusicFiles;
import static java.lang.Thread.sleep;

public class PlayerActivity extends AppCompatActivity implements ButtonAction, ServiceConnection {

    // View Elements
    ImageView imageView2, shuffle, previous, rewind, playPause, fastForward, next, repeat, equilizerBtn;
    ToggleButton favoriteList;
    TextView displaySongName, artistName, songStartTime, songEndTime;
    SeekBar songSeekBar;
    Animation customAnimation;

    // Booleans for Suffel, Repeat and Favorite Songs
    static boolean shuffleBool = false, repeatBool = false, favSongBtn = false;

    //other importants items
    public static final String EXTRA_NAME = "song_name";
    int position = 0;
    static ArrayList<MusicFiles> mySongs = new ArrayList<>(); // original Song Array
    static Uri uri;
    Thread updateSeekBar;
    private Handler handler = new Handler();
    public static boolean MUSIC_PLAYING;
    MusicService musicService;
    //static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

    // init views
        viewElements();
    // Outside of init views

        defineAnimation();

    // Get Intent Method.
        position = getIntent().getIntExtra("audioListPosition", 0);

        mySongs = audioMusicFiles;

        if (mySongs != null) {
            playPause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(mySongs.get(position).getPath());
        } else {
            playPause.setImageResource(R.drawable.ic_play);
        }

        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("musicServicePosition", position);
        startService(intent);
    // outside get intent method.

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

        //songEndTime.setText(createTime(musicService.getDuration()));
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    songSeekBar.setProgress(musicService.getCurrentPosition());
                    songStartTime.setText(createTime(musicService.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void defineAnimation() {
        customAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotation);
        customAnimation.setInterpolator(new LinearInterpolator());
    }

    private void activatingAudioConsole() {
        activatePlayPauseButton();
        activateNextSongButton();
        // music end listener
        activatePreviousSongButton();
        activateFastForwardButton();
        activateRewindButton();
        activatingEqualizer();
        activatingSongSuffelButton();
        activatingRepeatButton();
    }

    private void viewElements() {
        imageView2 = findViewById(R.id.imageView2);
        shuffle = findViewById(R.id.shuffle);
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
        shuffle.setOnClickListener(new View.OnClickListener() {
            //same as repeat, it is created by video help
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (shuffleBool) {
                    shuffleBool = false;
                    shuffle.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.white));
                } else {
                    shuffleBool = true;
                    shuffle.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.RoyleBlue));
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

    // TODO: Manage This after complete project
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
            imageView2.clearAnimation();
            musicService.pause();
            playPause.setImageResource(R.drawable.ic_play);
            musicService.release();
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    public void changeMusicInMediaplayer() {
        songSeekBar.setProgress(0);
        Uri u = Uri.parse(mySongs.get(position).getPath());
        //mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        musicService.createMediaPlayer(position);
        setMusicAndArtistName();
        songSeekBar.setMax(musicService.getDuration());
        displaySongName.setSelected(true);
        musicService.start();
        playPause.setImageResource(R.drawable.ic_pause);
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
    /*
    public void showNotification(int playPauseSign) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Previous Intent
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0,
                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Play Intent
        Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Next Intent
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating Thumbnail of Audio File to show in notification.
        byte[] picture = null;
        picture = getAudioAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music);
        }

        //TODO: CONTINUE 17:--> 13:26

        // Creating a Notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                .addAction(playPauseSign, "Play", playPendingIntent)
                .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .build();

        startForeground(2,  notification);

        // NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // notificationManager.notify(0, notification);
    }
*/
    private byte[] getAudioAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }



    // Button Functions
    @Override
    public void nextButtonClicked() {

        resetMediaplayerSeekbarAndAnimation();
        musicService.showNotification(R.drawable.ic_play_2);
        if (shuffleBool && !repeatBool) {
            position = getRandom(mySongs.size() - 1);
        } else {
            if (!shuffleBool && !repeatBool) {
                position = ((position + 1) % mySongs.size());
            }
        }
        setMusicImage();
        changeMusicInMediaplayer();
        musicService.showNotification(R.drawable.ic_round_pause);
        musicService.onCompletionListener();

    }

    @Override
    public void previousButtonClicked() {
        musicService.showNotification(R.drawable.ic_play_2);
        resetMediaplayerSeekbarAndAnimation();

        if (shuffleBool && !repeatBool) {
            position = getRandom(mySongs.size() - 1);

        } else if (!shuffleBool && !repeatBool) {
            position = ((position - 1) % mySongs.size());
            setMusicImage();
        }

        setMusicImage();
        changeMusicInMediaplayer();
        musicService.showNotification(R.drawable.ic_round_pause);
        musicService.onCompletionListener();
    }

    @Override
    public void playPauseButtonClicked() {
        if (musicService.isPlaying()) {
            imageView2.clearAnimation();
            musicService.pause();
            playPause.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play_2);

            MUSIC_PLAYING = true;

        } else {
            imageView2.startAnimation(customAnimation);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_round_pause);

            MUSIC_PLAYING = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.buttonCallBack(this);
        Log.e("Service_Connection", "Connected: "+musicService);

        songSeekBar.setMax(musicService.getDuration());
        // metaData of uri: collect song image and set that on music image.


        byte[] audioArt = getAudioAlbumArt(uri.toString());

        Bitmap bitmap;
        if (audioArt != null) {
            Glide.with(this).asBitmap().load(audioArt).into(imageView2);
            bitmap = BitmapFactory.decodeByteArray(audioArt, 0, audioArt.length);
            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch = null;
                if (palette != null) {
                    swatch = palette.getDominantSwatch();
                }

                LinearLayout audioPlayerLinearLayout = findViewById(R.id.audioPlayerLinearLayout);
                if (swatch != null) {
                    audioPlayerLinearLayout.setBackgroundResource(R.drawable.gradient_background);

                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{swatch.getRgb(), swatch.getRgb()});
                    audioPlayerLinearLayout.setBackground(gradientDrawableBg);

                    displaySongName.setTextColor(swatch.getTitleTextColor());
                    artistName.setTextColor(swatch.getBodyTextColor());
                } else {
                    audioPlayerLinearLayout = findViewById(R.id.audioPlayerLinearLayout);
                    audioPlayerLinearLayout.setBackgroundResource(R.drawable.gradient_background);
                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{0xff000000, 0xff000000});
                    audioPlayerLinearLayout.setBackground(gradientDrawableBg);
                    displaySongName.setTextColor(Color.WHITE);
                    artistName.setTextColor(Color.DKGRAY);
                }
            });
        } else {
            Glide.with(this).asBitmap().load(R.drawable.cd_img).into(imageView2);
            LinearLayout audioPlayerLinearLayout = findViewById(R.id.audioPlayerLinearLayout);
            audioPlayerLinearLayout.setBackgroundResource(R.drawable.gradient_background);
            displaySongName.setTextColor(Color.WHITE);
            artistName.setTextColor(Color.DKGRAY);
        }

        imageAnimation();

        displaySongName.setText(mySongs.get(position).getTitle());
        artistName.setText(mySongs.get(position).getArtist());
        songEndTime.setText(createTime(musicService.getDuration()));

        activatingAudioConsole();

        musicService.onCompletionListener();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}