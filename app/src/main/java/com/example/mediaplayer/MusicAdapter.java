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
    private ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles> mFiles) {
        this.context = context;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_of_song, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        holder.indSngNme.setText(mFiles.get(position).getTitle());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mFiles.get(position).getPath());
        byte[] art = retriever.getEmbeddedPicture();
        if (art!=null)
        {
            Glide.with(context).asBitmap().load(art).into(holder.imageView);
            //Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            // holder.imageView.setImageBitmap(bitmap);
        }
        retriever.release();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Fragment Section (Not Required)
                //FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.add(R.id.activityMainXML, new PlayerFragment());
                //fragmentTransaction.addToBackStack(null);
                //fragmentTransaction.commit();

                //Bundle bundle = new Bundle();
                //bundle.putInt("position", position);
                //new PlayerFragment().setArguments(bundle);

                // Intent Section (Required)
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("audioListPosition", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
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
}
