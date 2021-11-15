package com.example.mediaplayer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.ArrayList;

import static com.example.mediaplayer.MainActivity.videoMusicFiles;


public class VideoFragment extends Fragment implements SearchView.OnQueryTextListener {
    SwipeRefreshLayout swipe_refresh_videos_list;
    RecyclerView videoListRecyclerView;
    static VideoMusicAdapter videoMusicAdapter;
    ArrayList<VideoMusicFiles> vMusicFiles;

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

        vMusicFiles = videoMusicFiles;

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_music, menu);
        MenuItem menuItem = menu.findItem(R.id.searchMusic);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String inputs = newText.toLowerCase();
        ArrayList<VideoMusicFiles> videoMusicFiles = new ArrayList<>();
        for (VideoMusicFiles media:  vMusicFiles){
            if (media.getTitle().toLowerCase().contains(inputs)){
                videoMusicFiles.add(media);
            }
        }
        VideoFragment.videoMusicAdapter.updateVideoFiles(videoMusicFiles);
        return true;
    }
}