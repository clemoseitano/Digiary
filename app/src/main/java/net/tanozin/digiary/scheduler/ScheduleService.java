package net.tanozin.digiary.scheduler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.tanozin.digiary.R;

public class ScheduleService extends WakeReminderIntentService {

    public ScheduleService() {
        super("ScheduleService");
    }

    @Override
    void doReminderWork(Intent intent) {
        Long rowId = intent.getExtras().getLong(ScheduleDbAdapter.KEY_ROWID);
        Cursor c = new ScheduleDbAdapter(ScheduleService.this).open().fetchReminder(rowId);
        c.moveToFirst();
        Long nextEvent = c.getLong(c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_REPEATING));
        newsNotifier(c.getString(c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_TITLE)),
                c.getString(c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_BODY)), rowId);
        if (nextEvent > 0L) {//set repeating
            new ScheduleDbAdapter(ScheduleService.this).open().updateReminder(rowId, System.currentTimeMillis() + nextEvent);
            Calendar cl = new GregorianCalendar();
            cl.setTime(new Date(System.currentTimeMillis() + nextEvent));
            new ScheduleManager(this).setReminder(rowId, cl);
        }

    }

    private void newsNotifier(String title, String body, long id) {
        Context context = this;

        Resources resources = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                R.mipmap.ic_launcher);
        title = context.getString(R.string.app_name) + ":" + title;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setColor(Color.BLUE)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(title)
                        .setLights(0xFF0000FF, 2000, 3000)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentText(body);


        Intent resultIntent = new Intent(context, ScheduleActivity.class);
        resultIntent.putExtra(ScheduleDbAdapter.KEY_ROWID, id);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(2376, mBuilder.build());
    }
}

