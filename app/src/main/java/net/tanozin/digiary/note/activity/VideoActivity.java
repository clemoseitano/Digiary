package net.tanozin.digiary.note.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;

public class VideoActivity extends AppCompatActivity {

    private static final int REQUEST_ID = 8462;
    private Uri result = null;
    private VideoView videoView;
    private MediaController ctlr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        String uri = intent.getStringExtra("uri");
        if (uri.trim().isEmpty()) {
            File video =
                    new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                            "sample.mp4");

            if (video.exists()) {
                video.delete();
            }

            Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            result = Uri.fromFile(video);
       /* videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(result);
            }
        });*/
            i.putExtra(MediaStore.EXTRA_OUTPUT, result);
            i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            startActivityForResult(i, REQUEST_ID);
        } else {
            setContentView(R.layout.activity_video);
            videoView = (VideoView) findViewById(R.id.videoView);
            final File clip = new File(uri);

            if (clip.exists()) {
                videoView.setVideoPath(clip.getAbsolutePath());

                ctlr = new MediaController(this);
                ctlr.setMediaPlayer(videoView);
                videoView.setMediaController(ctlr);
                videoView.requestFocus();
                videoView.start();
                setTitle(intent.getStringExtra("name"));
                videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save(Uri.fromFile(clip));
                    }
                });
            }
        }
    }

    public void save(Uri uri) {
        final EditText noteName = new EditText(VideoActivity.this);
        final Uri iru = uri;
//Hints are used to give user an idea of what to enter
        //well hints are hints, literally
        noteName.setHint("videoname.mp4");

        new AlertDialog.Builder(VideoActivity.this)
                .setTitle("Save Note")
                .setMessage("Give a Name for this Note")
                .setView(noteName)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File dir =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

                        dir.mkdirs();
                        String fn = noteName.getText().toString();
                        if (fn.split(".").length > 1) {

                            if (fn.split(".")[fn.split(".").length - 1] != "mp4")
                                fn = fn + ".mp4";
                        } else fn = fn + ".mp4";
                        File f = new File(dir, fn);
                        File output = new File(String.valueOf(iru));
                        output.renameTo(f);
                        NoteItem a = new NoteItem("video", fn, Uri.fromFile(output).toString());
                        ContentValues value = a.getContentValues();
                        getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //set default name
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_ID && resultCode == RESULT_OK) {
            /*Intent view=
                    new Intent(Intent.ACTION_VIEW).setDataAndType(result,
                            "video/mp4");

            startActivity(view);*/
            //videoView.setVideoURI(result);
            //videoView.start();
            save(result);
            Toast.makeText(VideoActivity.this, "Touch image to save", Toast.LENGTH_SHORT).show();
        }

    }
}
