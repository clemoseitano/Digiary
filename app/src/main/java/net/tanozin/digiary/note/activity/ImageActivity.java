package net.tanozin.digiary.note.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;

public class ImageActivity extends AppCompatActivity {
    private static final String EXTRA_FILENAME =
            "net.tanozin.digiary.note.EXTRA_FILENAME";
    private static final String FILENAME = "sample.jpg";
    private static final int CONTENT_REQUEST = 8462;
    private File output = null;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (savedInstanceState == null) {
            File dir = new File(Constants.NOTE_IMAGE_DIR);
            dir.mkdirs();
            output = new File(dir, FILENAME);
        } else {
            output = (File) savedInstanceState.getSerializable(EXTRA_FILENAME);
        }

        if (output.exists()) {
            output.delete();
        }
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(i, CONTENT_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_FILENAME, output);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void save() {
        final EditText noteName = new EditText(ImageActivity.this);
   noteName.setHint("imagename.jpg");
        new AlertDialog.Builder(ImageActivity.this)
                .setTitle("Save Image")
                .setMessage("Give a Name for this Image")
                .setView(noteName)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File dir = new File(Constants.NOTE_IMAGE_DIR);
                        if (!dir.exists())
                            if (!dir.mkdir()) {
                                Toast.makeText(ImageActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        String fn = noteName.getText().toString();
                        if (fn.split(".").length > 1) {

                            if (fn.split(".")[fn.split(".").length - 1] != "jpg")
                                fn = fn + ".jpg";
                        } else fn = fn + ".jpg";
                        File f = new File(dir, fn);
                        if (!output.renameTo(f)) {
                            Toast.makeText(ImageActivity.this, "File could not be saved to: " +
                                    f.getPath(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        NoteItem a = new NoteItem("image", fn, Uri.fromFile(f).toString());
                        ContentValues value = a.getContentValues();
                        Uri uri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);
                        getContentResolver().notifyChange(uri, null);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CONTENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(Uri.fromFile(output));
                Snackbar.make(imageView, "Touch image to save", Snackbar.LENGTH_LONG).show();
                }
        }

    }


}
