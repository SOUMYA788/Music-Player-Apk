package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import java.util.ArrayList;

import static com.example.mediaplayer.MainActivity.videoMusicFiles;

public class VideoPlayerActivity extends AppCompatActivity {
    int position, orientation;
    TextView songNameTextView, videoStartTime, videoEndTime;
    ImageView videoPlayPause, videoEquilizer, previousVideo,videoRewind, nextVideo,videoFastForward, videoRotation, videoLock, videoUnlock;
    SeekBar videoSeekBar;
    ArrayList<VideoMusicFiles>myVideoSongs;
    Uri VideoUri;
    VideoView player;

    Thread updateSeekbarPosition;
    RelativeLayout videoControlPannel, videoViewRelativeLayout;
    Boolean isOpen = true, isVideoLock = false;

    // Created Objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        // Dealers XML File View here.
        xmlFileView();
        videoUnlock.setVisibility(View.GONE);

        myVideoSongs = videoMusicFiles;

        videoPlayPause.setImageResource(R.drawable.ic_pause);

        position  = getIntent().getIntExtra("videoListPosition", 0);
        playInitialVideo();

        activatingControlPanel();

        // on create ends here.
    }

    private void xmlFileView() {
        songNameTextView = findViewById(R.id.songNameTextView);
        videoStartTime = findViewById(R.id.videoStartTime);
        videoEndTime = findViewById(R.id.videoEndTime);
        videoEquilizer = findViewById(R.id.videoEquilizer);
        previousVideo = findViewById(R.id.previousVideo);
        videoRewind = findViewById(R.id.videoRewind);
        nextVideo = findViewById(R.id.nextVideo);
        videoFastForward = findViewById(R.id.videoFastForward);
        videoRotation = findViewById(R.id.videoRotation);
        videoPlayPause = findViewById(R.id.videoPlayPause);
        videoSeekBar = findViewById(R.id.videoSeekBar);
        player = findViewById(R.id.videoPlayer);
        videoControlPannel = findViewById(R.id.videoControlPannel);
        videoViewRelativeLayout = findViewById(R.id.videoViewRelativeLayout);
        videoLock = findViewById(R.id.videoLock);
        videoUnlock = findViewById(R.id.videoUnlock);
    }

    private void activatingControlPanel() {
        // Video Control Panel
        videoPlayPause.setOnClickListener(v -> {
            if (player.isPlaying()){
                player.pause();
                videoPlayPause.setImageResource(R.drawable.ic_play);
            }else {
                player.start();
                videoPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });

        nextVideo.setOnClickListener(v -> {
            try {
                pausePlayer();
                position = ((position + 1) % myVideoSongs.size());

                videoSeekBar.setProgress(0);
                VideoUri = Uri.parse(myVideoSongs.get(position).getPath());
                player.setVideoURI(VideoUri);
                player.start();
                videoPlayPause.setImageResource(R.drawable.ic_pause);

            } catch (Exception e) {
                Toast.makeText(VideoPlayerActivity.this, "NOT AVAILABLE!", Toast.LENGTH_SHORT).show();
            }
        });

        videoFastForward.setOnClickListener(v -> {
            try {
                player.seekTo(player.getCurrentPosition()+5000);
            } catch (Exception e) {
                Toast.makeText(VideoPlayerActivity.this, "VIDEO COMPLETE", Toast.LENGTH_SHORT).show();
            }
        });

        previousVideo.setOnClickListener(v -> {
            pausePlayer();
            position = ((position - 1) % myVideoSongs.size());
            changeMusic();
        });

        videoRewind.setOnClickListener(v -> {
            try {
                player.seekTo(player.getCurrentPosition()-1000);
            } catch (Exception e) {
                Toast.makeText(VideoPlayerActivity.this, "REACHED ON STARTING POINT", Toast.LENGTH_SHORT).show();
            }
        });

        videoEquilizer.setOnClickListener(v -> {
            try {
                Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getPackageName());
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.getAudioSessionId());
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                startActivityForResult(eqIntent, 13);
                Toast.makeText(getApplicationContext(), "Presenting Equalizer", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(VideoPlayerActivity.this, "Equalizer Not Found", Toast.LENGTH_SHORT).show();
            }
        });

        videoRotation.setOnClickListener(v -> {

            try {
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception e) {
                Toast.makeText(VideoPlayerActivity.this, "ROTATION IS NOT POSSIBLE.", Toast.LENGTH_SHORT).show();
            }

        });

        videoViewRelativeLayout.setOnClickListener(v -> {
            if (!isVideoLock){
                try {
                    if (isOpen){
                        hideControlPanel();
                    }else {
                        showControlPanel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                hideControlPanel();
            }

        });

        videoUnlock.setOnClickListener(v -> {
            if (isVideoLock){
                if (videoUnlock.getVisibility() == View.VISIBLE){
                    videoUnlock.setVisibility(View.GONE);
                }
                isVideoLock = false;
                showControlPanel();
            }
        });

        videoLock.setOnClickListener(v -> {
            if (!isVideoLock){
                if (videoUnlock.getVisibility() == View.GONE){
                    videoUnlock.setVisibility(View.VISIBLE);
                }
                isVideoLock = true;
                hideControlPanel();
            }
        });
    }

    // Changing Music
    private void changeMusic() {
        videoSeekBar.setProgress(0);
        VideoUri = Uri.parse(myVideoSongs.get(position).getPath());
        player.setVideoURI(VideoUri);
        player.start();
        videoPlayPause.setImageResource(R.drawable.ic_pause);
    }

    // Pausing Player when change music on console
    private void pausePlayer() {
        player.pause();
        videoPlayPause.setImageResource(R.drawable.ic_play);
    }

    private void playInitialVideo(){
        // getting path as videoUri and set path on player.
        VideoUri = Uri.parse(myVideoSongs.get(position).getPath());
        player.setVideoURI(VideoUri);

        // On Prepared Listener
        player.setOnPreparedListener(mp -> {
            videoSeekBar.setProgress(0);
            videoSeekBar.setMax(player.getDuration());
            player.start();

            // SEEK BAR
            updateSeekbarPosition = new Thread() {
                @Override
                public void run() {
                    try {
                        while (player.getDuration()>0) {
                            videoSeekBar.setProgress(player.getCurrentPosition());
                            sleep(500);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            updateSeekbarPosition.start();

            // When User Change Seekbar Position
            videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && player!=null)
                    {
                        player.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.seekTo(videoSeekBar.getProgress());
                }
            });

            // Setting Video timing and video song name
            songNameTextView.setText(myVideoSongs.get(position).getTitle());
            videoEndTime.setText(createVideoTiming(player.getDuration()));
            setVideoSongTiming();
        });

        // On Completion Listener
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextVideo.performClick();
            }
        });
    }

    // Showing and hiding control panel when touch on control panel area
    private void showControlPanel() {
        videoControlPannel.setVisibility(View.VISIBLE);
        final Window window = getWindow();
        if (window == null)
        {
            return;
        }
        //window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        isOpen = true;
    }
    private void hideControlPanel() {

        videoControlPannel.setVisibility(View.GONE);
        final Window window = getWindow();
        if (window == null)
        {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        isOpen = false;

    }

    // Setup Video Song Timing
    public void setVideoSongTiming() {
        //videoEndTime.setText(createVideoTiming(player.getDuration()));
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createVideoTiming(player.getCurrentPosition());
                videoStartTime.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

    }
    public String createVideoTiming(int duration) {
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

    // Pause when minimise window
    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        //videoPlayPause.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        player.stopPlayback();
    }
}