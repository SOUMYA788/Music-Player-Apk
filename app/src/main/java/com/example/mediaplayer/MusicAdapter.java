package com.example.mediaplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.holder> {

    private final Context context;
    static ArrayList<MusicFiles> audioMusicFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles> audioMusicFiles) {
        this.context = context;
        MusicAdapter.audioMusicFiles = audioMusicFiles;
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("audioListPosition", position);
            context.startActivity(intent);
        });

        holder.audio_more_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.individual_audio_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteAudio:
                                deleteFile(position, v);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioMusicFiles.size();
    }

    class holder extends RecyclerView.ViewHolder {
        ImageView imageView, audio_more_item;
        TextView indSngNme;

        public holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSong);
            indSngNme = itemView.findViewById(R.id.txtSongName);
            audio_more_item = itemView.findViewById(R.id.audio_more_item);
        }
    }

    void updateAudioList(ArrayList<MusicFiles> audioMusicFilesArrayList) {
        audioMusicFiles = new ArrayList<>();
        audioMusicFiles.addAll(audioMusicFilesArrayList);
        notifyDataSetChanged();
    }

    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(audioMusicFiles.get(position).getId()));

        File file = new File(audioMusicFiles.get(position).getPath());

        boolean deleted = file.delete();
        String filePath = file.getAbsolutePath();


        Log.e("Deleted", filePath);
        if (deleted) {
            context.getContentResolver().delete(contentUri, null, null);
            audioMusicFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, audioMusicFiles.size());
            Snackbar.make(view, "DELETE: " + file, Snackbar.LENGTH_LONG).show();
            Toast.makeText(context, "DONE", Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, "DELETE UNSUCCESSFUL", Snackbar.LENGTH_LONG).show();
        }
    }
}
