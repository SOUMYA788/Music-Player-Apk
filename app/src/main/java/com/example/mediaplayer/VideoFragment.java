package com.example.mediaplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.mediaplayer.MainActivity.videoMusicFiles;


public class VideoFragment extends Fragment {
    RecyclerView videoListRecyclerView;
    VideoMusicAdapter videoMusicAdapter;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        videoListRecyclerView = view.findViewById(R.id.videoListRecyclerView);
        videoListRecyclerView.setHasFixedSize(true);

        if (!(videoMusicFiles.size()<1))
        {
            videoMusicAdapter = new VideoMusicAdapter(getContext(), videoMusicFiles);
            videoListRecyclerView.setAdapter(videoMusicAdapter);
            videoListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
        return view;
    }
}