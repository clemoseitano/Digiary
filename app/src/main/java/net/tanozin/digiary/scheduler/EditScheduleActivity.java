
package net.tanozin.digiary.scheduler;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.tanozin.digiary.R;

public class EditScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String DATE_PICKER_DIALOG = "date_picker";
    private static final String TIME_PICKER_DIALOG = "time_picker";

    private static final String DATE_FORMAT = "yyyy MMM dd";
    private static final String TIME_FORMAT = "kk:mm";

    private EditText mTitleText;
    private EditText mBodyText;
    private TextView mDateView;
    private TextView mTimeView;
    private Long mRowId;
    private ScheduleDbAdapter mDbHelper;
    private Calendar mCalendar;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private long timeValue = 0L;
    private Spinner spinner;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ScheduleDbAdapter(this);

        setContentView(R.layout.reminder_edit);
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

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(new Date(System.currentTimeMillis()));
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateView = (TextView) findViewById(R.id.reminder_date);
        mTimeView = (TextView) findViewById(R.id.reminder_time);
        ImageButton mDateButton = (ImageButton) findViewById(R.id.date_button);
        ImageButton mTimeButton = (ImageButton) findViewById(R.id.time_button);
        Button mConfirmButton = (Button) findViewById(R.id.confirm);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDateDialog();
            }
        });
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTimeDialog();
            }
        });
        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDateDialog();
            }
        });
        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTimeDialog();
            }
        });
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveState();
                setResult(RESULT_OK);
                Toast.makeText(EditScheduleActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(ScheduleDbAdapter.KEY_ROWID)
                : null;


        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> courseAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_values, android.R.layout.simple_spinner_item);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(courseAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                time = (String) adapterView.getItemAtPosition(i);
                if (time.trim().equals("30 minutes"))
                    timeValue = 60 * 30 * 1000L;
                else if (time.trim().equals("1 hour"))
                    timeValue = 60 * 60 * 1000L;
                else if (time.trim().equals("3 hours"))
                    timeValue = 60 * 60 * 1000 * 3L;
                else if (time.trim().equals("6 hours"))
                    timeValue = 60 * 60 * 1000 * 6L;
                else if (time.trim().equals("12 hours"))
                    timeValue = 60 * 60 * 1000 * 12L;
                else if (time.trim().equals("Daily"))
                    timeValue = 60 * 60 * 1000 * 24L;
                else if (time.trim().equals("Weekly"))
                    timeValue = 60 * 60 * 1000 * 24 * 7L;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        CheckBox repeat = (CheckBox) findViewById(R.id.repeating);
        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                spinner.setVisibility(b ? View.VISIBLE : View.GONE);
                if (!b)
                    timeValue = 0L;
            }
        });
        datePickerDialog = DatePickerDialog.newInstance(this,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH), false/*no vibration*/);
        timePickerDialog = TimePickerDialog.newInstance(this,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false, false);

        updateDateButtonText();
        updateTimeButtonText();
    }

    private void setRowIdFromIntent() {
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(ScheduleDbAdapter.KEY_ROWID)
                    : null;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open();
        setRowIdFromIntent();
        populateFields();
    }


    private void createTimeDialog() {
        timePickerDialog.setVibrate(false);
        timePickerDialog.setCloseOnSingleTapMinute(false);
        timePickerDialog.show(getSupportFragmentManager(), TIME_PICKER_DIALOG);
    }

    private void createDateDialog() {
        datePickerDialog.setVibrate(false);
        datePickerDialog.setYearRange(mCalendar.get(Calendar.YEAR), 2037);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), DATE_PICKER_DIALOG);
    }

    private void populateFields() {


        // Only populate the text boxes and change the calendar date
        // if the row is not null from the database.
        if (mRowId != null) {
            Cursor reminder = mDbHelper.fetchReminder(mRowId);
            reminder.moveToFirst();
            mTitleText.setText(reminder.getString(
                    reminder.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_TITLE)));
            mBodyText.setText(reminder.getString(
                    reminder.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_BODY)));


            // Get the date from the database and format it for our use. 
            Date date;
            Long dateString = reminder.getLong(reminder.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_DATE_TIME));
            date = new Date(dateString);
            timeValue = reminder.getLong(reminder.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_REPEATING));
            mCalendar.setTime(date);
            reminder.close();
        } else {
            // This is a new task - add defaults from preferences if set.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultTitleKey = getString(R.string.pref_task_title_key);
            String defaultTimeKey = getString(R.string.pref_default_time_from_now_key);

            String defaultTitle = prefs.getString(defaultTitleKey, null);
            String defaultTime = prefs.getString(defaultTimeKey, null);

            if (defaultTitle != null)
                mTitleText.setText(defaultTitle);

            if (defaultTime != null)
                mCalendar.add(Calendar.MINUTE, Integer.parseInt(defaultTime));

        }

        updateDateButtonText();
        updateTimeButtonText();

    }

    private void updateTimeButtonText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        String timeForButton = timeFormat.format(mCalendar.getTime());
        mTimeView.setText(timeForButton);
    }

    private void updateDateButtonText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String dateForButton = dateFormat.format(mCalendar.getTime());
        mDateView.setText(dateForButton);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ScheduleDbAdapter.KEY_ROWID, mRowId);
    }


    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        long reminderDateTime = mCalendar.getTimeInMillis();

        if (mRowId == null) {

            long id = mDbHelper.createReminder(title, body, reminderDateTime, timeValue);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateReminder(mRowId, title, body, reminderDateTime, timeValue);
        }

        new ScheduleManager(this).setReminder(mRowId, mCalendar);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updateDateButtonText();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        updateTimeButtonText();
    }
}
