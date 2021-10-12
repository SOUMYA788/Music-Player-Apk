package com.example.mediaplayer;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class VideoMusicAdapter extends RecyclerView.Adapter<VideoMusicAdapter.VideoHolder> {

    private Context context;
    private ArrayList<VideoMusicFiles> vFiles;

    public VideoMusicAdapter(Context context, ArrayList<VideoMusicFiles> vFiles) {
        this.context = context;
        this.vFiles = vFiles;
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_of_video_songs, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
        holder.txtVideoSongName.setText(vFiles.get(position).getTitle());

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(vFiles.get(position).getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        holder.videoImage.setImageBitmap(thumb);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vFiles.size();
    }

    public class VideoHolder extends RecyclerView.ViewHolder {
        ImageView videoImage;
        TextView txtVideoSongName;
        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            videoImage = itemView.findViewById(R.id.videoimg);
            txtVideoSongName = itemView.findViewById(R.id.txtVideoSongName);
        }
    }
}
