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
    public static final int REQUEST_PERMISSION_SETTING = 12, STORAGE_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission();
        //runtimePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION){
            for (int i = 0; i<permissions.length; i++){
                String per = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    // we will check whether user check never ask again or not
                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if (!showRationale){
                        // User check never ask again
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background);
                        builder.setTitle("APP PERMISSION").
                                setMessage("For playing videos first of all i need to collect all music files in your storage. And access your storage I need device permission" +"\n\n\n"
                                        + "Now Follow this steps if you want to allow me" + "\n\n"
                                        + "01. Open Setting by bellow button" + "\n"
                                        + "02. Click on Permissions" + "\n"
                                        + "03. Allow Access for Storage"+ "\n\n"
                                        +"Or hit cancel to goes out")
                                .setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();
                    }else {
                        // User Denied Permission
                        continuePermissionRequest();
                    }

                }else {
                    // User Clicked on Allow Button
                    startCollectMusic();
                }
            }
        }
    }

    public void permission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            startCollectMusic();
        }else {
            continuePermissionRequest();
        }
    }

    private void continuePermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
    }

    private void startCollectMusic() {
        musicFiles = getAllAudio(MainActivity.this);
        videoMusicFiles = getAllVideo(MainActivity.this);
        initViewPager();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            startCollectMusic();
        }
    }
}