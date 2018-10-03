package net.tanozin.digiary.note.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.tanozin.digiary.R;

public class AudioPlayer extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    TextView playedTextView;
    TextView endTextView;
    SeekBar playProgress;
    private ImageButton play;
    private ImageButton stop;
    private MediaPlayer mp;
    private CountDownTimer playTimer;

    boolean playing = false;
    int timePlayed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_player);
        setTitle(getIntent().getStringExtra("name"));
        endTextView = (TextView) findViewById(R.id.endTextView);
        playedTextView = (TextView) findViewById(R.id.playedTextView);
        play = (ImageButton) findViewById(R.id.play);
        playProgress = (SeekBar) findViewById(R.id.pageSlider);
        playProgress.setProgress(0);
        stop = (ImageButton) findViewById(R.id.stop);

        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (playing)
                    pause();
                else play();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                stop();
            }
        });

        setup();
    }

    public void onCompletion(MediaPlayer mp) {
        stop();
        playProgress.setProgress(0);
    }


    private String formatTime(int time) {
        String hour = (Integer.toString(time / (1000 * 60 * 60))).length() == 1 ? "0" + (Integer.toString(time / (1000 * 60 * 60))) : (Integer.toString(time / (1000 * 60 * 60)));
        String minute = ((Integer.toString((time / (1000 * 60)) % 60)).length() == 1 ? "0" + (Integer.toString(time / (1000 * 60) % 60)) : (Integer.toString(time / (1000 * 60) % 60)));
        String second = ((Integer.toString((time / (1000)) % 60)).length() == 1 ? "0" + (Integer.toString(time / (1000) % 60)) : (Integer.toString(time / (1000) % 60)));
        return hour + ":" + minute + ":" + second;
    }

    private void play() {
        mp.start();
        mp.seekTo(timePlayed);
        playing = true;

        final int dur = mp.getDuration();
        playProgress.setMax(dur);
        endTextView.setText(formatTime(dur));
        playTimer = new CountDownTimer((dur-timePlayed), 1000) {//maximum 1 second error for pause

            public void onTick(long millisUntilFinished) {
                int r = (int) (dur - millisUntilFinished);
                if (playing) {
                    timePlayed= (int) (dur - millisUntilFinished);
                    playProgress.setProgress(timePlayed);
                }
                playedTextView.setText(formatTime(r));
            }

            public void onFinish() {
                try {
                    stop();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(),
                            "Error occurred in stopping recorder", e);
                    // can fail if start() failed for some reason
                }
                playTimer.cancel();
                mp.reset();
            }
        }.start();
        play.setEnabled(true);
        play.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        stop.setEnabled(true);
    }

    private void stop() {
        mp.stop();
        stop.setEnabled(false);
        playing = false;
        try {
            mp.prepare();
            mp.seekTo(0);
            playTimer.cancel();
            playedTextView.setText("00:00:00");
            timePlayed = 0;
            playProgress.setProgress(0);
            play.setEnabled(true);
        } catch (Throwable t) {
            Toast.makeText(AudioPlayer.this, "Error occurred in handling media", Toast.LENGTH_SHORT).show();
        }
        play.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
    }

    private void pause() {
        playTimer.cancel();
        mp.pause();
        playing = false;
        play.setEnabled(true);
        play.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        stop.setEnabled(true);
    }

    private void loadClip() {
        try {
            mp = MediaPlayer.create(this, Uri.parse(getIntent().getStringExtra("uri")));
            mp.setOnCompletionListener(this);
        } catch (Throwable t) {
            Toast.makeText(AudioPlayer.this, "Error occurred in handling media", Toast.LENGTH_SHORT).show();
        }
    }

    private void setup() {
        loadClip();
        play.setEnabled(true);
        stop.setEnabled(false);
    }
}
