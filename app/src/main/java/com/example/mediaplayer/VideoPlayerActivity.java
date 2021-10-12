package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.halilibo.bvpkotlin.BetterVideoPlayer;

import java.util.ArrayList;

import static com.example.mediaplayer.MainActivity.videoMusicFiles;

public class VideoPlayerActivity extends AppCompatActivity {


    int position;
    ArrayList<VideoMusicFiles>myVideoSongs;
    Uri VideoUri;
    BetterVideoPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        player = findViewById(R.id.videoPlayer);
        position  = getIntent().getIntExtra("position", 0);
        myVideoSongs = videoMusicFiles;

        if (myVideoSongs!=null)
        {
            VideoUri = Uri.parse(myVideoSongs.get(position).getPath());
        }
        player.setSource(VideoUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }
}