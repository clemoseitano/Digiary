package net.tanozin.digiary.note.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;

public class NoteActivity extends AppCompatActivity {
    public EditText textContent;
    public Button saveButton;
    public Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        textContent = (EditText) findViewById(R.id.noteContent);
        saveButton = (Button) findViewById(R.id.save);
        clearButton = (Button) findViewById(R.id.clear);

        if (getIntent() != null)
            if (getIntent().getStringExtra("uri") != null) {
                textContent.setText(readFile(getIntent().getStringExtra("uri")));
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getIntent() != null)
                            if (getIntent().getStringExtra("uri") != null)
                                save(getIntent().getStringExtra("name"), getIntent().getStringExtra("uri"));
                    }
                });
            } else {
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getIntent() != null)
                            if (getIntent().getStringExtra("uri") != null)
                                save(getIntent().getStringExtra("name"), getIntent().getStringExtra("uri"));
                        final EditText noteName = new EditText(NoteActivity.this);

//Hints are used to give user an idea of what to enter
                        //well hints are hints, literally
                        noteName.setHint("mynote.txt");

                        new AlertDialog.Builder(NoteActivity.this)
                                .setTitle("Save Note")
                                .setMessage("Give a Name for this Note")
                                .setView(noteName)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String fn = noteName.getText().toString();
                                        if (fn.split(".").length > 1) {

                                            if (!(fn.split(".")[fn.split(".").length - 1]).equals("txt"))
                                                fn = fn + ".txt";
                                        } else fn = fn + ".txt";
                                        save(fn);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                });
            }
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContent.setText("");
            }
        });
    }

    private void save(String name, String uri) {
        File dir = new File(Constants.NOTE_TEXT_DIR);
        if (!dir.exists())
            if (!dir.mkdirs()) {
                Toast.makeText(NoteActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                return;
            }
        File fileToEdit = new File(Constants.NOTE_TEXT_DIR, uri);
        new SaveThread(textContent.getText().toString(),
                fileToEdit).start();
        NoteItem a = new NoteItem(this, name);

        ContentValues value = a.getContentValues();//values to insert
        String where = NoteContentProvider.KEY_NAME + " = ?;";//where to do selection
        String[] array = new String[]{a.getName()};//values to match selection
        getContentResolver().update(NoteContentProvider.CONTENT_URI, value, where, array);
        getContentResolver().notify();
    }

    public String readFile(String filename) {
        String content = null;
        File file = new File(filename); //for ex foo.txt
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }


    public void save(String filename) {
        File dir = new File(Constants.NOTE_TEXT_DIR);
        if (!dir.exists())
            if (!dir.mkdirs()) {
                Toast.makeText(NoteActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                return;
            }
        File fileToEdit = new File(Constants.NOTE_TEXT_DIR, filename);
        new SaveThread(textContent.getText().toString(),
                fileToEdit).start();
        NoteItem a = new NoteItem("text", filename, fileToEdit.getAbsolutePath());
        ContentValues value = a.getContentValues();
        Uri uri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);
        getContentResolver().notifyChange(uri != null ? uri : null, null);
    }

    private class SaveThread extends Thread {
        private final String text;
        private final File fileToEdit;

        SaveThread(String text, File fileToEdit) {
            this.text = text;
            this.fileToEdit = fileToEdit;
        }

        @Override
        public void run() {
            try {
                fileToEdit.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(fileToEdit);

                Writer w = new BufferedWriter(new OutputStreamWriter(fos));

                try {
                    w.write(text);
                    w.flush();
                    fos.getFD().sync();
                } finally {
                    w.close();
                    NoteActivity.this.finish();
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Exception writing file", e);
            }
        }
    }
}
