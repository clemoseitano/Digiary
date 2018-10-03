package net.tanozin.digiary.fileexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import java.io.File;

public class FileBrowser extends FragmentActivity implements FileDialog.FileListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FileDialog f = new FileDialog();
        Bundle arguments = new Bundle();
        arguments.putInt(FileDialog.ARG_REQUEST_CODE, 3456);
        f.setMode(FileDialog.Mode.PICK_FILE);
        f.setTitle("Pick File");
        f.setTempDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
        f.setArguments(arguments);
        f.show(getSupportFragmentManager(), "FileDialog");

    }

    @Override
    public void onFileReturned(int requestcode, File file) {
        setResult(1, (new Intent()).setAction(file.getAbsolutePath()));
    }
}