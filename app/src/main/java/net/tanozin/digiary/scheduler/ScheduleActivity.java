
package net.tanozin.digiary.scheduler;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import java.util.ArrayList;

import net.tanozin.digiary.R;

public class ScheduleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private ArrayList<ScheduleItem> ITEMS = new ArrayList<>();
    private ScheduleDbAdapter mDbHelper;
    private AbsListView listView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        }
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        listView = (AbsListView) findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        findViewById(R.id.placeholder).setVisibility(View.GONE);

        FloatingActionButton fabAddSchedule = (FloatingActionButton) findViewById(R.id.add_schedule);
        fabAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReminder();
            }
        });

        mDbHelper = new ScheduleDbAdapter(this);
        mDbHelper.open();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
            fillData();
        super.onWindowFocusChanged(hasFocus);
    }

    private void fillData() {
        Cursor remindersCursor = mDbHelper.fetchAllReminders();
        //check if its empty
        if (remindersCursor.getCount() <= 0) {
            findViewById(R.id.container).setVisibility(View.INVISIBLE);
            findViewById(R.id.placeholder).setVisibility(View.VISIBLE);
            return;
        } else {
            findViewById(R.id.container).setVisibility(View.VISIBLE);
            findViewById(R.id.placeholder).setVisibility(View.GONE);
        }
        remindersCursor.moveToFirst();
        ITEMS.clear();
        do {
            ScheduleItem newItem = new ScheduleItem(remindersCursor);
            ITEMS.add(newItem);
        } while (remindersCursor.moveToNext());
        final ScheduleAdapter reminders = new ScheduleAdapter(ScheduleActivity.this, ITEMS);
        ((AdapterView<ListAdapter>) listView).setAdapter(reminders);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                final ScheduleItem item = (ScheduleItem) parent.getAdapter().getItem(position);
                CharSequence[] items;
                items = new CharSequence[1];
                items[0] = "Delete Item?";

                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                builder.setTitle("Reminder");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mDbHelper.deleteReminder(item.getItemId());
                                reminders.notifyDataSetChanged();
                                fillData();
                                break;
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });
        remindersCursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_insert:
                createReminder();
                return true;
            case R.id.menu_settings:
                Intent i = new Intent(this, TaskPreferences.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createReminder() {
        Intent i = new Intent(this, EditScheduleActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScheduleItem item = (ScheduleItem) parent.getAdapter().getItem(position);
        Intent i = new Intent(ScheduleActivity.this, EditScheduleActivity.class);
        i.putExtra(ScheduleDbAdapter.KEY_ROWID, item.getItemId());
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }


}
