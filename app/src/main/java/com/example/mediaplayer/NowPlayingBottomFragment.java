package com.example.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.IllegalFormatCodePointException;

import static android.content.Context.MODE_PRIVATE;
import static android.os.SystemClock.sleep;
import static com.example.mediaplayer.MainActivity.ARTISTNAME;
import static com.example.mediaplayer.MainActivity.PATH;
import static com.example.mediaplayer.MainActivity.SHOW_NOW_PLAYING;
import static com.example.mediaplayer.MainActivity.TRACKNAME;
import static com.example.mediaplayer.PlayerActivity.MUSIC_PLAYING;
//import static com.example.mediaplayer.PlayerActivity.MUSIC_PLAYING;

public class NowPlayingBottomFragment extends Fragment implements ServiceConnection {
    ImageView albumImage, playPrevious, playPause, playNext;
    TextView songName, songArtistName;
    View view;
    ButtonAction buttonAction;
    MusicService musicService;

    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String LAST_TRACK = "LAST_TRACK";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String TRACK = "TRACK";

    public NowPlayingBottomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);

        getAllItemViews();

        if (MUSIC_PLAYING) {
            playPause.setImageResource(R.drawable.ic_play_2);
        } else {
            playPause.setImageResource(R.drawable.ic_round_pause);
        }

        playPause.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.activatePlayPauseBtn();
                if (musicService.isPlaying()) {
                    playPause.setImageResource(R.drawable.ic_round_pause);
                } else {
                    playPause.setImageResource(R.drawable.ic_play_2);
                }
            }
        });

        playNext.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                }
                playPause.setImageResource(R.drawable.ic_play);
                musicService.activateNextBtn();
                if (musicService.isPlaying()) {
                    playPause.setImageResource(R.drawable.ic_pause);
                }
                // Update music name, music title and music image.
                if (getActivity() != null) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(LAST_TRACK, MODE_PRIVATE).edit();
                    editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                    editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                    editor.putString(TRACK, musicService.musicFiles.get(musicService.position).getTitle());
                    editor.apply();


                    SharedPreferences preferences = getActivity().getSharedPreferences(LAST_TRACK, MODE_PRIVATE);
                    String path = preferences.getString(MUSIC_FILE, null);
                    String artist = preferences.getString(ARTIST_NAME, null);
                    String musicName = preferences.getString(TRACK, null);

                    if (path != null) {
                        SHOW_NOW_PLAYING = true;
                        PATH = path;
                        ARTISTNAME = artist;
                        TRACKNAME = musicName;
                    } else {
                        SHOW_NOW_PLAYING = false;
                        PATH = null;
                        ARTISTNAME = null;
                        TRACKNAME = null;
                    }

                    if (SHOW_NOW_PLAYING) {
                        if (PATH != null) {
                            byte[] art = getAudioAlbumArt(PATH);
                            if (getContext() != null) {
                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(albumImage);
                                } else {
                                    Glide.with(getContext()).load(R.drawable.cd_img).into(albumImage);
                                }
                            }
                            songName.setText(TRACKNAME);
                            songArtistName.setText(ARTISTNAME);
                        }
                    }
                }
            }
        });

        playPrevious.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.activatePreviousBtn();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Adding Details in Now Playing Mini View.
        Log.e("ON_RESUME_PAUSE", "ON RESUME STARTED");
        if (SHOW_NOW_PLAYING) {
            if (PATH != null) {
                // Adding Image in Mini View.
                byte[] art = getAudioAlbumArt(PATH);
                if (getContext() != null) {
                    if (art != null) {
                        Glide.with(getContext()).load(art).into(albumImage);
                    } else {
                        Glide.with(getContext()).load(R.drawable.cd_img).into(albumImage);
                    }
                }
                // Adding Song Name and Artist Name.
                songName.setText(TRACKNAME);
                songArtistName.setText(ARTISTNAME);

                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

/*
    @Override
    public void onPause() {
        Log.e("ON_RESUME_PAUSE", "ON PAUSE STARTED");
        super.onPause();
        if (getContext()!=null){
            getContext().unbindService(this);
        }
    }
    */


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null) {
            getContext().unbindService(this);
        }
    }

    private void getAllItemViews() {
        albumImage = view.findViewById(R.id.now_playing_bottom_album_art);
        songName = view.findViewById(R.id.now_playing_bottom_song_name);
        songArtistName = view.findViewById(R.id.now_playing_bottom_song_artist_name);
        playPrevious = view.findViewById(R.id.now_playing_previous);
        playPause = view.findViewById(R.id.now_playing_playPause);
        playNext = view.findViewById(R.id.now_playing_next);
    }

    private byte[] getAudioAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}