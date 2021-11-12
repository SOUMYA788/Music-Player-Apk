package com.example.mediaplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.mediaplayer.MainActivity.videoMusicFiles;


public class VideoFragment extends Fragment {
    SwipeRefreshLayout swipe_refresh_videos_list;
    RecyclerView videoListRecyclerView;
    VideoMusicAdapter videoMusicAdapter;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        videoListRecyclerView = view.findViewById(R.id.videoListRecyclerView);
        swipe_refresh_videos_list = view.findViewById(R.id.swipe_refresh_videos_list);
        videoListRecyclerView.setHasFixedSize(true);

        showVideoFiles();


        swipe_refresh_videos_list.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipe_refresh_videos_list.setRefreshing(false);
            }
        });

        return view;
    }

    private void showVideoFiles() {
        if (!(videoMusicFiles.size()<1))
        {
            videoMusicAdapter = new VideoMusicAdapter(getContext(), videoMusicFiles);
            videoListRecyclerView.setAdapter(videoMusicAdapter);
            videoListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
    }
}