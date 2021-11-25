package com.example.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MusicService.class);
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    Toast.makeText(context, "Play", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;
                case ACTION_NEXT:
                    Toast.makeText(context, "Playing Next Song", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;
                case ACTION_PREVIOUS:
                    Toast.makeText(context, "Playing Previous Song", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;

            }
        }
    }
}
