package com.example.mediaplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.Inflater;

public class VideoMusicAdapter extends RecyclerView.Adapter<VideoMusicAdapter.VideoHolder> {

    BottomSheetDialog bottomSheetDialog;
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

        Glide.with(context).load(new File(vFiles.get(position).getPath())).into(holder.videoImage);

        holder.video_menu_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.video_bottom_sheet_layout,
                        v.findViewById(R.id.BottomSheet));

                // Option Play
                bottomSheetView.findViewById(R.id.moreitem_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.itemView.performClick();
                        bottomSheetDialog.dismiss();
                    }
                });

                // Option Rename: NOT WORKING
                bottomSheetView.findViewById(R.id.moreitem_rename).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background);
                        alertDialog.setTitle("Enter New Name...");
                        EditText editText = new EditText(context);
                        String path = vFiles.get(position).getPath();// full path of a file
                        final File file = new File(path);
                        String videoName = file.getName();

                        videoName = videoName.substring(0, videoName.lastIndexOf(".")); // 0 to before .ext
                        editText.setText(videoName);
                        alertDialog.setView(editText);
                        editText.requestFocus();

                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                                if (TextUtils.isEmpty(editText.getText().toString())){
                                    Toast.makeText(context, "Cannot Rename Empty File", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                
                                String onlyPath = Objects.requireNonNull(file.getParentFile()).getAbsolutePath();

                                String ext = file.getAbsolutePath();
                                String extension = ext.substring(ext.lastIndexOf("."));
                                String newPath = onlyPath + "/" + editText.getText().toString() + extension;

                                File newFile = new File(newPath);
                                boolean rename = file.renameTo(newFile);

                                if (rename) {
                                    ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                    resolver.delete(MediaStore.Files.getContentUri("external"),
                                            MediaStore.MediaColumns.DATA + "=?", new String[]
                                                    {file.getAbsolutePath()});
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(newFile));
                                    context.getApplicationContext().sendBroadcast(intent);

                                    notifyDataSetChanged();
                                    Toast.makeText(context, "DONE...", Toast.LENGTH_SHORT).show();
                                    SystemClock.sleep(200);
                                    ((Activity) context).recreate();
                                } else {
                                    Toast.makeText(context, "SORRY!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.create().show();
                        bottomSheetDialog.dismiss();
                    }
                });

                // Option Share
                bottomSheetView.findViewById(R.id.moreitem_share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(vFiles.get(position).getPath());
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("Video/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(shareIntent, "You Can Share Via"));
                        bottomSheetDialog.dismiss();
                    }

                });

                // Option Delete: NOT WORKING
                bottomSheetView.findViewById(R.id.moreitem_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background);
                        alertDialog.setTitle("DELETE");
                        alertDialog.setMessage("DO YOU WANT TO DELETE THIS VIDEO?");
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        vFiles.get(position).getID());

                                File file = new File(vFiles.get(position).getPath());
                                boolean delete = file.delete();
                                if (delete) {
                                    context.getContentResolver().delete(contentUri, null, null);
                                    vFiles.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, vFiles.size());
                                    Toast.makeText(context, "VIDEO DELETED", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "VIDEO CANNOT BE DELETED", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                        bottomSheetDialog.dismiss();
                    }
                });

                // Option Properties
                bottomSheetView.findViewById(R.id.moreitem_property).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background);
                        alertDialog.setTitle("PROPERTIES");

                        // Workings for File Data Element
                        String path = vFiles.get(position).getPath();
                        String namePlusFormat = vFiles.get(position).getTitle();

                        int indexOfPath = path.lastIndexOf("/");
                        int size = vFiles.get(position).getSize();

                        int formatIndex = path.lastIndexOf(".");
                        String onlyFormat = path.substring(formatIndex + 1);

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(path);
                        String videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        String videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);


                        // File Data Element
                        String fileName = "FILE: " + namePlusFormat;
                        String filePath = "PATH: " + path.substring(0, indexOfPath);
                        String fileSize = "SIZE: " + android.text.format.Formatter.formatFileSize(context, size);
                        String fileLength = "LENGTH: " + createVideoTiming(vFiles.get(position).getDuration());
                        String fileFormat = "FORMAT: " + onlyFormat;
                        String fileResolution = "RESOLUTION: " + videoWidth + " X " + videoHeight;

                        // File Data
                        String videoInfo = fileName+"\n\n"+filePath+"\n\n"+fileSize+"\n\n"+fileLength+"\n\n"+fileFormat+"\n\n"+fileResolution;

                        alertDialog.setMessage(videoInfo);
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        alertDialog.show();
                        bottomSheetDialog.dismiss();
                    }
                });


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("videoListPosition", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vFiles.size();
    }

    public class VideoHolder extends RecyclerView.ViewHolder {
        ImageView videoImage, video_menu_more;
        TextView txtVideoSongName, video_size;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            videoImage = itemView.findViewById(R.id.videoimg);
            txtVideoSongName = itemView.findViewById(R.id.txtVideoSongName);
            video_menu_more = itemView.findViewById(R.id.video_menu_more);
            video_size = itemView.findViewById(R.id.video_size);
        }
    }

    public String createVideoTiming(int duration) {
        // Used in setSongTiming
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        time += min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }

    void updateVideoFiles(ArrayList<VideoMusicFiles> files){
        vFiles = new ArrayList<>();
        vFiles.addAll(files);
        notifyDataSetChanged();
    }
}
