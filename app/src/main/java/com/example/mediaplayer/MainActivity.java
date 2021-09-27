package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView musicListView;
    String[]items;
    String SDCard;
    File TargetSdCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicListView = findViewById(R.id.musicListView);

        runtimePermission();
    }

    public void runtimePermission()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        displayInteranlSong();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file)
    {

        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File singleFile: files)
        {
            if (singleFile.isDirectory() && !singleFile.isHidden())
            {
                arrayList.addAll(findSong(singleFile));
            }else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith("wav"))
                {
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void displayInteranlSong()
    {
        File[] externalCacheDirs = getExternalCacheDirs();
        for (File singleFie: externalCacheDirs)
        {
            if (Environment.isExternalStorageRemovable(singleFie))
            {
                //path is in format /storage.../Android...
                //Get everything before /Android
                SDCard = singleFie.getPath().split("/Android")[0];
                TargetSdCard = new File(SDCard);
                break;
            }
        }

        final ArrayList<File> mySongs= findSong(TargetSdCard);
        final ArrayList<File> myInternalSongs = findSong(Environment.getExternalStorageDirectory());

        items = new String[mySongs.size() + myInternalSongs.size()];

        for (int i=0; i<mySongs.size();i++)
        {
            items[i]=mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }

        musicListView.setLayoutManager(new LinearLayoutManager(this));
        customAdapter customAdapter = new customAdapter(items,getApplicationContext(), mySongs);
        musicListView.setAdapter(customAdapter);
    }

    class customAdapter extends RecyclerView.Adapter<customAdapter.holder>
    {
        String[] songData;
        Context context;
        ArrayList<File> mySongs;

        public customAdapter(String[] songData, Context context, ArrayList<File> mySongs) {
            this.songData = songData;
            this.context = context;
            this.mySongs = mySongs;
        }

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_song, parent,false);
            return new holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull holder holder, int position) {
            holder.indSngNme.setText(songData[position]);
            final String songName = songData[position];
            holder.indSngNme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("songs", mySongs);
                    intent.putExtra("songName", songName);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return songData.length;
        }

        class holder extends RecyclerView.ViewHolder
        {
            ImageView imageView;
            TextView indSngNme;
            public holder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imgSong);
                indSngNme = itemView.findViewById(R.id.txtSongName);
            }
        }
    }
}