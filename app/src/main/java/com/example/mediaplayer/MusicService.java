package com.example.mediaplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.example.mediaplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mediaplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mediaplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mediaplayer.MainActivity.musicFiles;
import static com.example.mediaplayer.MusicAdapter.audioMusicFiles;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;

    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String LAST_TRACK = "LAST_TRACK";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String TRACK = "TRACK";

    public static final boolean musicPlaying = false;

    ButtonAction buttonAction;
    int position = 0;
    MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "Audio_Player");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int myPosition = intent.getIntExtra("musicServicePosition", 0);
        String actionName = intent.getStringExtra("myActionName");

        if (myPosition != 0) {
            playMedia(myPosition);
        }

        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    activatePlayPauseBtn();
                    break;
                case "next":
                    activateNextBtn();
                    break;
                case "previous":
                    activatePreviousBtn();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = audioMusicFiles;
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                startMediaPlayer();
            }
        } else {
            startMediaPlayer();
        }
    }



    private void startMediaPlayer() {
        // Uri uri = Uri.parse(musicFiles.get(position).getPath());
        createMediaPlayer(position);
        mediaPlayer.start();
    }

    public void buttonCallBack(ButtonAction buttonAction) {
        this.buttonAction = buttonAction;
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void release() {
        mediaPlayer.release();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer( int myPosition) {
        position = myPosition;
        Uri u = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(LAST_TRACK, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, u.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(TRACK, musicFiles.get(position).getTitle());
        editor.apply();

        mediaPlayer = MediaPlayer.create(getBaseContext(), u);
    }

    void pause() {
        mediaPlayer.pause();
    }

    void onCompletionListener() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (buttonAction != null) {
            if (mediaPlayer != null) {
                buttonAction.nextButtonClicked();
            }
        }
    }

    // for equalizer
    int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

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

    private byte[] getAudioAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    void activatePlayPauseBtn(){
        if (buttonAction != null) {
            buttonAction.playPauseButtonClicked();
        }
    }
    void activateNextBtn(){
        if (buttonAction != null) {
            buttonAction.nextButtonClicked();
        }
    }

    void activatePreviousBtn(){
        if (buttonAction != null) {
            buttonAction.previousButtonClicked();

        }
    }
}
