package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<MusicFiles> musicFiles; // Use in runtime permission, for collecting all songs in one array list.
    static ArrayList<VideoMusicFiles> videoMusicFiles;
    //String[] items;
    public static final int REQUEST_PERMISSION_SETTING = 12, STORAGE_PERMISSION = 1;

    Toolbar customToolbarMusicList;
    private String SORT_PREF = "Sort_Order";
    FrameLayout bottomPlayer;

    // Getting Share Preference Keys Value in a static string.
    public static final String LAST_TRACK = "LAST_TRACK";
    public static final String MUSIC_FILE_PATH = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String TRACK = "TRACK";

    // Copy value of keys in another static string and use them desired area.
    public static boolean SHOW_NOW_PLAYING = false;
    public static String PATH = null;
    public static String ARTISTNAME = null;
    public static String TRACKNAME  = null;
    File internalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customToolbarMusicList = findViewById(R.id.customToolbarMusicList);
        bottomPlayer = findViewById(R.id.now_playing);
        setSupportActionBar(customToolbarMusicList);

        permission();
    }

    public void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startCollectMusic();
        } else {
            continuePermissionRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                String per = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // we will check whether user check never ask again or not
                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if (!showRationale) {
                        // User check never ask again
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background);
                        builder.setTitle("APP PERMISSION")
                                .setMessage("For playing videos first of all i need to collect all music files in your storage. And access your storage I need device permission" + "\n\n\n"
                                        + "Now Follow this steps if you want to allow me" + "\n\n"
                                        + "01. Open Setting by bellow button" + "\n"
                                        + "02. Click on Permissions" + "\n"
                                        + "03. Allow Access for Storage" + "\n\n"
                                        + "Or hit cancel to goes out")
                                .setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();
                    } else {
                        // User Denied Permission
                        continuePermissionRequest();
                    }

                } else {
                    // User Clicked on Allow Button
                    startCollectMusic();
                }
            }
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

    private void initViewPager() {
        ViewPager musicListViewPager = findViewById(R.id.musicListViewPager);
        TabLayout musicListMenuTabLayout = findViewById(R.id.musicListMenuTabLayout);

        viewPagerAdapter viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsListFragment(), "AUDIO MUSIC");
        viewPagerAdapter.addFragments(new VideoFragment(), "VIDEO MUSIC");

        musicListViewPager.setAdapter(viewPagerAdapter);
        musicListMenuTabLayout.setupWithViewPager(musicListViewPager);
    }

    public static class viewPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public viewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public ArrayList<MusicFiles> getAllAudio(Context context) {

        SharedPreferences sharedPreferences = getSharedPreferences(SORT_PREF, MODE_PRIVATE);
        String Sort_Order = sharedPreferences.getString("sorting", "sortByName");

        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        String order = null;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch (Sort_Order) {

            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
            case "sortByDate":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, // For Path
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;
    }

    public static ArrayList<VideoMusicFiles> getAllVideo(Context context) {
        ArrayList<VideoMusicFiles> tempVideoList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, // For Path
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                int duration = Integer.parseInt(cursor.getString(3));
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

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startCollectMusic();
        }
        
        SharedPreferences preferences = getSharedPreferences(LAST_TRACK, MODE_PRIVATE);

        /*
            * String items are temporary items, they are goes into psfs Global Variables, which is used into required area.
            * Keys comes from Genuine Preference Key. Value copy to String Item and they goes into psfs Global Variable.
         */

        String path = preferences.getString(MUSIC_FILE_PATH, null);
        String artist = preferences.getString(ARTIST_NAME, null);
        String songName = preferences.getString(TRACK, null);

        if (path!=null){
            SHOW_NOW_PLAYING = true;
            PATH = path;
            ARTISTNAME = artist;
            TRACKNAME = songName;
        }else {
            SHOW_NOW_PLAYING = false;
            PATH = null;
            ARTISTNAME = null;
            TRACKNAME = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_music, menu);
        MenuItem menuItem = menu.findItem(R.id.searchMusic);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userInput = newText.toLowerCase();
                ArrayList<MusicFiles> myFiles = new ArrayList<>();
                for (MusicFiles song : musicFiles) {
                    if (song.getTitle().toLowerCase().contains(userInput)) {
                        myFiles.add(song);
                    }
                }
                SongsListFragment.musicAdapter.updateAudioList(myFiles);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(SORT_PREF, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.byName:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.byDate:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.bySize:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}