package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.holder> {

    private Context context;
    static ArrayList<MusicFiles> audioMusicFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles> audioMusicFiles) {
        this.context = context;
        this.audioMusicFiles = audioMusicFiles;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_of_song, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        holder.indSngNme.setText(audioMusicFiles.get(position).getTitle());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(audioMusicFiles.get(position).getPath());
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(context).asBitmap().load(art).into(holder.imageView);
        }
        retriever.release();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("audioListPosition", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioMusicFiles.size();
    }

    class holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView indSngNme;

        public holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSong);
            indSngNme = itemView.findViewById(R.id.txtSongName);
        }
    }

    void updateAudioList(ArrayList<MusicFiles> audioMusicFilesArrayList) {
        audioMusicFiles = new ArrayList<>();
        audioMusicFiles.addAll(audioMusicFilesArrayList);
        notifyDataSetChanged();
    }
}
