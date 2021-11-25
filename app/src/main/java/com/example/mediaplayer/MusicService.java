package com.example.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MusicService extends Service {
    private IBinder mBinder = new myBinder();
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    ButtonAction buttonAction;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class myBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName = intent.getStringExtra("myActionName");

        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    if (buttonAction != null) {
                        buttonAction.playPauseButtonClicked();
                    }
                    break;
                case ACTION_NEXT:
                    if (buttonAction != null) {
                        buttonAction.nextButtonClicked();
                    }
                    break;
                case ACTION_PREVIOUS:
                    if (buttonAction != null) {
                        buttonAction.previousButtonClicked();
                    }
                    break;

            }
        }
        return START_STICKY;
    }

    public void buttonCallBack(ButtonAction buttonAction) {
        this.buttonAction = buttonAction;
    }
}
