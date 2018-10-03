package net.tanozin.digiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.util.Calendar;
import java.util.Date;

import net.tanozin.digiary.scheduler.ScheduleDbAdapter;
import net.tanozin.digiary.scheduler.ScheduleManager;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ScheduleManager reminderMgr = new ScheduleManager(context);

        ScheduleDbAdapter dbHelper = new ScheduleDbAdapter(context);
        dbHelper.open();

        Cursor cursor = dbHelper.fetchAllReminders();

        if (cursor != null) {
            cursor.moveToFirst();

            int rowIdColumnIndex = cursor.getColumnIndex(ScheduleDbAdapter.KEY_ROWID);
            int dateTimeColumnIndex = cursor.getColumnIndex(ScheduleDbAdapter.KEY_DATE_TIME);

            while (!cursor.isAfterLast()) {
                Long rowId = cursor.getLong(rowIdColumnIndex);
                Long dateTime = cursor.getLong(dateTimeColumnIndex);
                Calendar cal = Calendar.getInstance();
                Date date = new Date(dateTime);
                cal.setTime(date);
                reminderMgr.setReminder(rowId, cal);
                cursor.moveToNext();
            }
            cursor.close();
        }

        dbHelper.close();
    }
}

