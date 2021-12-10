package com.example.mediaplayer;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import static com.example.mediaplayer.MainActivity.musicFiles;

public class SongsListFragment extends Fragment {
    RecyclerView musicListRecyclerView;
    static MusicAdapter musicAdapter;
    NotificationManager notificationManager;

    public SongsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_songs_list, container, false);
        musicListRecyclerView = view.findViewById(R.id.musicListRecyclerView);

        musicListRecyclerView.setHasFixedSize(true);

        if (!(musicFiles.size()<1))
        {
            musicAdapter = new MusicAdapter(getContext(), musicFiles);
            musicListRecyclerView.setAdapter(musicAdapter);
            musicListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
        return view;
    }

}