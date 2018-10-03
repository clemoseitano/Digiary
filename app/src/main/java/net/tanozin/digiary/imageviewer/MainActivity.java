package net.tanozin.digiary.imageviewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.tanozin.digiary.R;


public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private String currentFile = "";
    private static final int PERMISSION = 2435;
    private boolean stretch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasStoragePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> permissions = new ArrayList<String>();
            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!permissions.isEmpty())
                requestPermissions(permissions.toArray(new String[permissions.size()]), PERMISSION);
        }
        onCreate();
    }

    public void onCreate() {
        Intent intent = getIntent();
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.image_view);
        newIntent(intent);
    }

    public void newIntent(Intent intent) {
        setIntent(intent);
        Uri mUri;
        {
            mUri = intent.getData();
            if (mUri != null) {
                 if (!mUri.getPath().equals(currentFile))
                    currentFile = mUri.getPath();
                openFile(mUri);

            }
        }
    }

    public void openFile(Uri mUri) {
        final File ft = new File(mUri.getPath());
        if (ft.isFile() && ft.exists()) {
            loadImage(ft.getAbsolutePath());
        }
    }


    private void loadImage(final String file) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file, options);
                // Calculate inSampleSize
                int width = options.outWidth;
                int height = options.outHeight;
                float ratio = 1;
                if ((width > 4096) || (height > 4096)) {
                    ratio = (float) (width > height ? (4096 * 1.0) / (1.0 * width) : (1.0 * 4096) / (1.0 * height));
                }
                float a = ratio * width;
                float c = ratio * height;

                width = (int) (a);
                height = (int) (c);

                if (stretch) {
                    Window window = getWindow();
                    width = window.getWindowManager().getDefaultDisplay().getWidth();
                } else {

                }
                Bitmap b = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(file), width, height, true);
                getIntent().setData(Uri.parse(file));
                imageView.setImageBitmap(b);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                        Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_stretch).setTitle(stretch ? "Default" : "Fit Width");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_stretch) {
            stretch = !stretch;
            onCreate();
        }
        return super.onOptionsItemSelected(item);
    }
}
