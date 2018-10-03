package net.tanozin.digiary.note.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;
import net.tanozin.digiary.DemoView;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;

public class AudioActivity extends Activity implements MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener {
    private static final String BASENAME = "recording.mp3";
    private static final int MAX_DURATION = 24 * 60 * 60 * 1000;//1 hour
    private MediaRecorder recorder = null;
    private File output;

    private CountDownTimer timer;
    TextView recorded;
    DemoView stopRecord;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        recorded = (TextView) findViewById(R.id.recordedTextView);
        final FloatingActionButton fabAudio = (FloatingActionButton) findViewById(R.id.record);
        stopRecord = (DemoView) findViewById(R.id.demo_view);
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    timer.onFinish();
                    stopRecord.setVisibility(View.GONE);
                    fabAudio.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.w(getClass().getSimpleName(),
                            "Exception in stopping recorder", e);
                }
            }
        });

        fabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    stopRecord.setVisibility(View.VISIBLE);
                    fabAudio.setVisibility(View.GONE);
                    if (!new File(Constants.NOTE_AUDIO_DIR).exists())
                        if (!new File(Constants.NOTE_AUDIO_DIR).mkdirs()) {
                            Toast.makeText(AudioActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    output =
                            new File(Constants.NOTE_AUDIO_DIR, BASENAME);
                    recorder.setMaxDuration(MAX_DURATION);
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setOutputFile(output.getAbsolutePath());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        recorder.setAudioEncodingBitRate(160 * 1024);
                    } else {
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    }
                    recorder.setAudioChannels(2);
                    try {
                        recorder.prepare();
                        recorder.start();
                        timer = new CountDownTimer((MAX_DURATION), 1000) {
                            public void onTick(long millisUntilFinished) {
                                int r = (int) (MAX_DURATION - millisUntilFinished);
                                recorded.setText(formatTime(r));
                            }

                            public void onFinish() {
                                try {
                                    recorder.stop();
                                    stopRecord.setVisibility(View.GONE);
                                    fabAudio.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    Log.w(getClass().getSimpleName(),
                                            "Exception in stopping recorder", e);
                                    // can fail if start() failed for some reason
                                }
                                timer.cancel();
                                recorder.reset();
                                recorder.release();
                                save(Uri.fromFile(output));
                            }
                        }.start();

                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(),
                                "Exception in preparing recorder", e);
                        Toast.makeText(AudioActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recorder = new MediaRecorder();
        recorder.setOnErrorListener(this);
        recorder.setOnInfoListener(this);
    }

    @Override
    public void onPause() {
        recorder.release();
        recorder = null;
        super.onPause();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        String msg = "";

        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                msg = getString(R.string.max_duration);
                break;

            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                msg = getString(R.string.max_size);
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                msg = getString(R.string.strange) + what;
        }
        if (!msg.trim().equals(""))
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.strange) + what, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Toast.makeText(this, R.string.strange, Toast.LENGTH_LONG).show();
    }

    public void save(Uri uri) {
        final EditText noteName = new EditText(AudioActivity.this);
        noteName.setHint("audioname.mp3");
        new AlertDialog.Builder(AudioActivity.this)
                .setTitle("Save Audio")
                .setMessage("Set file name")
                .setView(noteName)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File dir = new File(Constants.NOTE_AUDIO_DIR);

                        if (!dir.exists())
                            if (!dir.mkdirs()) {
                                Toast.makeText(AudioActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        String fn = noteName.getText().toString();
                        if (fn.split(".").length > 1) {

                            if (fn.split(".")[fn.split(".").length - 1] != "mp3")
                                fn = fn + ".mp3";
                        } else fn = fn + ".mp3";
                        File f = new File(dir, fn);


                        if (!output.renameTo(f)) {
                            Toast.makeText(AudioActivity.this, "File could not be saved to: " +
                                    f.getPath(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        NoteItem a = new NoteItem("audio", fn, Uri.fromFile(f).toString());
                        ContentValues value = a.getContentValues();
                        Uri uri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);
                        getContentResolver().notifyChange(uri, null);
                        AudioActivity.this.finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }




    private String formatTime(int time) {
        String hour = (Integer.toString(time / (1000 * 60 * 60))).length() == 1 ? "0" + (Integer.toString(time / (1000 * 60 * 60))) : (Integer.toString(time / (1000 * 60 * 60)));
        String minute = ((Integer.toString((time / (1000 * 60)) % 60)).length() == 1 ? "0" + (Integer.toString(time / (1000 * 60) % 60)) : (Integer.toString(time / (1000 * 60) % 60)));
        String second = ((Integer.toString((time / (1000)) % 60)).length() == 1 ? "0" + (Integer.toString(time / (1000) % 60)) : (Integer.toString(time / (1000) % 60)));
        return hour + ":" + minute + ":" + second;
    }

}
