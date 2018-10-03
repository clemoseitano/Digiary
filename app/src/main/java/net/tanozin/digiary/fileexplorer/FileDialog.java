package net.tanozin.digiary.fileexplorer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import net.tanozin.digiary.R;
import net.tanozin.digiary.fileexplorer.widget.IndexableListView;

/**
 * Created by user on 9/9/2016.
 */
public class FileDialog extends DialogFragment {
    FileListener mListener;
    File[] fileList;
    String tempDirectory;
    String wildcard = "";
    String title = "File Picker";
    Mode mode = Mode.PICK_DIRECTORY;
    EditText search;
    private boolean folder = false;
    private IndexableListView list;
    public static final String ARG_REQUEST_CODE = "requestcode";
    private TextView titletv;


    /**
     * Mode.PICK_FILE used to open files, the returned result of the dialog is a file.
     * Mode.PICK_DIRECTORY used to select a directory, returned result is a directory
     * Mode.SAVE_FILE a hybrid of Mode.PICK_FILE and Mode.PICK_DIRECTORY, this allows creation of
     * both files and folders
     */
    public enum Mode {
        PICK_FILE, PICK_DIRECTORY, SAVE_FILE
    }

    public void setWildCard(String wildcard) {
        this.wildcard = wildcard;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTempDirectory(String dir) {
        if (!(new File(dir).exists()))
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        this.tempDirectory = dir;
    }

    private int requestCode = 0;

    public FileDialog() {
        tempDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FileListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FileListener");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_REQUEST_CODE))
            requestCode = getArguments().getInt(ARG_REQUEST_CODE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.file_explorer, null);
        builder.setView(view);
        search = (EditText) view.findViewById(R.id.search);
        titletv = (TextView) view.findViewById(R.id.title);
        titletv.setText(title);

        final EditText fn = (EditText) view.findViewById(R.id.file_name);
        final EditText foldern = (EditText) view.findViewById(R.id.folder_name);
        final ImageButton newButton = (ImageButton) view.findViewById(R.id.new_file);
        if (!((mode == Mode.PICK_FILE) || (mode == Mode.SAVE_FILE))) {
            fn.setEnabled(false);
            fn.setVisibility(View.INVISIBLE);
            newButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);
        }

        list = (IndexableListView) view.findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File item = (File) parent.getAdapter().getItem(position);
                tempDirectory = item.getAbsolutePath();
                if (item.isDirectory()) {
                    fileList = item.listFiles();
                    //list.removeAllViews();
                    list.setAdapter(loadFiles());
                    updateCD();
                } else {
                    mListener.onFileReturned(requestCode, item);
                    list.setSelection(position);
                    dismiss();
                }
            }
        });
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fn.getText() != null && fn.getText().toString().trim().length() > 0) {
                    String filen = fn.getText().toString().trim();
                    filen = filen.replace("\\", "/");
                    File f = new File(tempDirectory, filen);
                    if (f.exists()) {
                        final File finalF = f;
                        new AlertDialog.Builder(getContext())
                                .setMessage("File exists, do you want to overwrite?")
                                .setTitle("Overwrite")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        newFile(finalF);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else if (filen.contains("/") || !f.exists()) {
                        f = new File(tempDirectory, filen);
                        newFile(f);
                    }
                } else {
                    fn.setHint("Enter a file name to create");
                    //implement a flashing widget
                    new CountDownTimer((2000), 100) {
                        boolean odd = false;

                        public void onTick(long millisUntilFinished) {
                            fn.setBackgroundColor(odd ? 0xFFFFFFFF : 0xFFFF0000);
                            odd = !odd;
                        }

                        public void onFinish() {
                            fn.setBackgroundColor(0x00000000);
                        }
                    }.start();
                }
            }
        });
        fileList = new File(tempDirectory).listFiles();
        ImageButton accept = (ImageButton) view.findViewById(R.id.button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fn.getText() != null && fn.getText().toString().trim().length() > 0) {
                    String filen = fn.getText().toString().trim();
                    filen = filen.replace("\\", "/");
                    File f = new File(tempDirectory, filen);
                    if (filen.contains("/") || (filen.contains("/") && !f.exists())) {
                        f = new File(tempDirectory, filen.substring(0, filen.lastIndexOf("/")));
                        if (mode.equals(Mode.PICK_DIRECTORY))
                            f.mkdirs();
                        else f.getParentFile().mkdirs();
                        mListener.onFileReturned(requestCode, f);
                    } else mListener.onFileReturned(requestCode, f);
                } else mListener.onFileReturned(requestCode, new File(tempDirectory));
                dismiss();
            }
        });
        final ImageButton fab = (ImageButton) view.findViewById(R.id.fab);
        ImageButton up = (ImageButton) view.findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tempDirectory == Environment.getRootDirectory().getAbsolutePath())
                    return;
                tempDirectory = new File(tempDirectory).exists()?(new File(tempDirectory).getParent()):tempDirectory;
                fileList = (new File(tempDirectory).exists()?new File(tempDirectory).listFiles():fileList);
                if (fileList != null && fileList.length > 0) {
                    list.setAdapter(loadFiles());
                    updateCD();
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (folder) {
                    folder = false;
                    //fab.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_create_new_folder));
                    if (foldern.getText() != null && foldern.getText().toString().trim().length() > 0) {
                        String filen = foldern.getText().toString().trim();
                        filen = filen.replace("\\", "/");
                        newFolder(filen);
                        titletv.setText(title);
                        titletv.setVisibility(View.VISIBLE);
                        foldern.setVisibility(View.GONE);
                    } else {
                        foldern.setHint("Enter a file name to create");
                        //implement a flashing widget
                        new CountDownTimer((2000), 100) {
                            boolean odd = false;

                            public void onTick(long millisUntilFinished) {
                                foldern.setBackgroundColor(odd ? 0xFFFFFFFF : 0xFFFF0000);
                                odd = !odd;
                            }

                            public void onFinish() {
                                foldern.setBackgroundColor(0x00000000);
                            }
                        }.start();
                    }
                } else {
                    folder = true;
                    //fab.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_check));
                    foldern.setHint("New Folder");
                    foldern.setText("");
                    foldern.setVisibility(View.VISIBLE);
                    titletv.setVisibility(View.GONE);
                }
            }
        });
        list.setAdapter(loadFiles());

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                fileList = Environment.getExternalStorageDirectory().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        s = s.toLowerCase();
                        return s.contains(charSequence) && s.contains(wildcard);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {
                list.setAdapter(loadFiles());
            }
        });
        return builder.create();
    }

    private void newFolder(String fn) {
        if (fn.trim().length() > 0) {
            File f = new File(tempDirectory, fn);
            f.mkdirs();
            if (f.exists()) {
                tempDirectory = f.getAbsolutePath();
            }
        }
        fileList = new File(tempDirectory).listFiles();
        //list.removeAllViews();
        list.setAdapter(loadFiles());
        updateCD();
    }

    private void newFile(File f) {
        f.getParentFile().mkdirs();
        FileWriter fstream;
        try {
            fstream = new FileWriter(f.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("");
            out.close();
            tempDirectory = f.getParent();
            fileList = f.getParentFile().listFiles();
            list.setAdapter(loadFiles());
            updateCD();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCD() {
        search.setHint(tempDirectory);
    }

    private FileAdapter loadFiles() {
        if (fileList == null) {//workaround for when you go beyond root
            tempDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
            fileList = new File(tempDirectory).listFiles();
            updateCD();
            Toast.makeText(getContext(), "Access denied", Toast.LENGTH_LONG).show();
        }
        Arrays.sort(fileList, new Comparator<File>() {
            public int compare(File file1, File file2) {
                if (file1 != null && file2 != null) {
                    if (file1.isDirectory() && (!file2.isDirectory())) return -1;
                    if (file2.isDirectory() && (!file1.isDirectory())) return 1;
                    return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
                }
                return 0;
            }
        });

        return new FileAdapter(this.getContext(), fileList);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public interface FileListener {
        void onFileReturned(int requestcode, File file);
    }
}
