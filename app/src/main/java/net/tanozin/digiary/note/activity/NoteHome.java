package net.tanozin.digiary.note.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;
import net.tanozin.digiary.note.fragment.AudioItemFragment;
import net.tanozin.digiary.note.fragment.ImageItemFragment;
import net.tanozin.digiary.note.fragment.NoteItemFragment;
import net.tanozin.digiary.note.fragment.TextItemFragment;
import net.tanozin.digiary.note.fragment.VideoItemFragment;
import net.tanozin.digiary.texttray.EditorActivity;

public class NoteHome extends AppCompatActivity
        implements
        NoteItemFragment.OnListFragmentInteractionListener, TextItemFragment.OnListFragmentInteractionListener,
        ImageItemFragment.OnListFragmentInteractionListener, VideoItemFragment.OnListFragmentInteractionListener,
        AudioItemFragment.OnListFragmentInteractionListener {
    private static final int REQUEST_ID = 8462;
    private Uri result = null;
    private File output;
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.ic_unknown,
            R.drawable.ic_file,
            R.drawable.ic_image,
            R.drawable.ic_video,
            R.drawable.ic_audio
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_note_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        findViewById(R.id.add_icons).setVisibility(View.INVISIBLE);
        final FloatingActionButton plus = (FloatingActionButton) findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plus.setVisibility(View.INVISIBLE);
                findViewById(R.id.add_icons).setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.add_icons).setVisibility(View.INVISIBLE);
                        plus.setVisibility(View.VISIBLE);
                    }
                }, (1000 * 4));

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteHome.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteHome.this, ImageActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton fabVideo = (FloatingActionButton) findViewById(R.id.fabVideo);
        fabVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!new File(Constants.NOTE_VIDEO_DIR).exists())
                    if (!new File(Constants.NOTE_VIDEO_DIR).mkdirs()) {
                        Toast.makeText(NoteHome.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                output =
                        new File(Constants.NOTE_VIDEO_DIR,
                                "sample.mp4");

                if (output.exists()) {
                    if (!output.delete())
                        Toast.makeText(NoteHome.this, "File storage permission denied", Toast.LENGTH_SHORT).show();
                }

                Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                result = Uri.fromFile(output);

                i.putExtra(MediaStore.EXTRA_OUTPUT, result);
                i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                startActivityForResult(i, REQUEST_ID);
            }
        });

        FloatingActionButton fabAudio = (FloatingActionButton) findViewById(R.id.fabAudio);
        fabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteHome.this, AudioActivity.class);
                startActivity(intent);
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_home, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_ID && resultCode == RESULT_OK) {
            save();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            final EditText noteName = new EditText(NoteHome.this);
            new AlertDialog.Builder(NoteHome.this)
                    .setTitle(getString(R.string.search_string))
                    .setMessage(getString(R.string.search_detail))
                    .setView(noteName)
                    .setPositiveButton(R.string.action_search, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String searchTerm = (noteName.getText().toString().trim().toLowerCase());
                            String[] projection = new String[]{NoteContentProvider.KEY_LINK,
                                    NoteContentProvider.KEY_DATE_TAKEN, NoteContentProvider.KEY_NAME,
                                    NoteContentProvider.KEY_TYPE, NoteContentProvider.KEY_DATE_MODIFIED};
                            Cursor c = getContentResolver().query(NoteContentProvider.CONTENT_URI, projection, null, null, null);
                            if (c == null)
                                return;
                            c.moveToFirst();
                            boolean empty = true;
                            while (!c.isAfterLast()) {
                                String fullCursor =
                                        c.getString(c.getColumnIndex(NoteContentProvider.KEY_NAME)) +
                                                " " + c.getString(c.getColumnIndex(NoteContentProvider.KEY_TYPE));

                                if (fullCursor.toLowerCase().contains(searchTerm.toLowerCase())) {
                                    //stop and open results on first find
                                    empty = false;
                                    startActivity(new Intent(NoteHome.this, SearchActivity.class)
                                            .putExtra("search", searchTerm));
                                } else
                                    c.moveToNext();
                            }
                            c.close();
                            if (empty)
                                Toast.makeText(NoteHome.this, "No content matches your search request",
                                        Toast.LENGTH_SHORT).show();


                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //set default name
                        }
                    })
                    .show();

        }
        return true;
    }


    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
        tabLayout.getTabAt(4).setIcon(tabIcons[4]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new NoteItemFragment(), ("All").toUpperCase());
        adapter.addFrag(new TextItemFragment(), ("Text").toUpperCase());
        adapter.addFrag(new ImageItemFragment(), ("Image").toUpperCase());
        adapter.addFrag(new VideoItemFragment(), ("Video").toUpperCase());
        adapter.addFrag(new AudioItemFragment(), ("Audio").toUpperCase());
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onListFragmentInteraction(NoteItem item) {

    }

    public void save() {
        final EditText noteName = new EditText(NoteHome.this);

//Hints are used to give user an idea of what to enter
        //well hints are hints, literally
        noteName.setHint("videoname.mp4");

        new AlertDialog.Builder(NoteHome.this)
                .setTitle("Save Video")
                .setMessage("Give a Name for this Video")
                .setView(noteName)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File dir = new File(Constants.NOTE_VIDEO_DIR);
                        if (!dir.exists())
                            if (!dir.mkdirs()) {
                                Toast.makeText(NoteHome.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                                return;
                            }

                        String fn = noteName.getText().toString();
                        if (fn.split(".").length > 1) {

                            if (!fn.split(".")[fn.split(".").length - 1].equals("mp4"))
                                fn = fn + ".mp4";
                        } else fn = fn + ".mp4";
                        File f = new File(dir, fn);

                        if (output.renameTo(f)) {
                            NoteItem a = new NoteItem("video", fn, Uri.fromFile(f).toString());
                            ContentValues value = a.getContentValues();
                            Uri uri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);
                            if (uri != null)
                                getContentResolver().notifyChange(uri, null);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //set default name
                    }
                })
                .show();
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
