package com.example.mediaplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
    ListView listViewOne;
    String[]items;
    String SDCard;
    File TargetSdCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listViewOne = findViewById(R.id.listViewOne);


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

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
        customAdapter customAdapter = new customAdapter();
        listViewOne.setAdapter(customAdapter);


        listViewOne.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String songName = (String) listViewOne.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("songs", mySongs);
                intent.putExtra("songName", songName);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

    }
    class customAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
           View myView = getLayoutInflater().inflate(R.layout.list_of_song, null);
           TextView textSong = myView.findViewById(R.id.txtSongName);
           textSong.setSelected(true);
           textSong.setText(items[position]);
           return myView;
        }
    }
}