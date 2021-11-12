package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<MusicFiles> musicFiles;// Use in runtime permission, for collecting all songs in one array list.
    static ArrayList<VideoMusicFiles> videoMusicFiles;
    //String[] items;
    public static final int REQUEST_PERMISSION_SETTING = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        runtimePermission();
    }




    public void runtimePermission()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        musicFiles = getAllAudio(MainActivity.this);
                        videoMusicFiles = getAllVideo(MainActivity.this);
                        initViewPager();
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


    private void initViewPager()
    {
        ViewPager musicListViewPager = findViewById(R.id.musicListViewPager);
        TabLayout musicListMenuTabLayout = findViewById(R.id.musicListMenuTabLayout);

        viewPagerAdapter viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsListFragment(), "AUDIO MUSIC");
        viewPagerAdapter.addFragments(new VideoFragment(), "VIDEO MUSIC");

        musicListViewPager.setAdapter(viewPagerAdapter);
        musicListMenuTabLayout.setupWithViewPager(musicListViewPager);
    }

    public static class viewPagerAdapter extends FragmentStatePagerAdapter
    {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public viewPagerAdapter(@NonNull FragmentManager fm)
        {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            return fragments.get(position);
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            return titles.get(position);
        }
    }

    public static ArrayList<MusicFiles> getAllAudio(Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri  = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, // For Path
                MediaStore.Audio.Media.ARTIST,
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor!=null){
            while (cursor.moveToNext()){
                String album  = cursor.getString(0);
                String title  = cursor.getString(1);
                String duration  = cursor.getString(2);
                String path  = cursor.getString(3);
                String artist  = cursor.getString(4);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;
    }

    public static ArrayList<VideoMusicFiles> getAllVideo(Context context) {
        ArrayList<VideoMusicFiles> tempVideoList = new ArrayList<>();
        Uri uri  = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, // For Path
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor!=null){
            while (cursor.moveToNext()){
                int id = cursor.getInt(0);
                String path  = cursor.getString(1);
                String title  = cursor.getString(2);
                int duration  = Integer.parseInt(cursor.getString(3));
                int size = cursor.getInt(4);

                VideoMusicFiles videoMusicFiles = new VideoMusicFiles(id, path, title, duration, size);
                tempVideoList.add(videoMusicFiles);
            }
            cursor.close();
        }
        return tempVideoList;
    }
}