package net.tanozin.digiary.scheduler;

import android.database.Cursor;

import java.util.Date;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.DateHelper;

public class ScheduleItem {
    private String title;
    private String description;
    private String dateCreated;
    private String dateDue;
    private long itemId;


    public ScheduleItem(String title, String description, Long dateDue) {
        this.title = title;
        this.description = description;
        this.dateCreated = DateHelper.beautifulDateToString(new Date(System.currentTimeMillis()), Constants.PREFERRED_DATE_TIME);
        this.dateDue = DateHelper.beautifulDateToString(new Date(dateDue), Constants.PREFERRED_DATE_TIME);
    }

    public ScheduleItem(Cursor c) {
        int keyTitleId = c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_ROWID);
        int keyTitleIndex = c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_TITLE);
        int keyDateDueIndex = c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_DATE_TIME);
        int keyDescriptionIndex =  c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_BODY);
        int keyDateCreatedIndex = c.getColumnIndexOrThrow(ScheduleDbAdapter.KEY_DATE_ADDED);
        this.title = c.getString(keyTitleIndex);
        this.description = c.getString(keyDescriptionIndex);
        this.dateCreated = DateHelper.beautifulDateToString(new Date(c.getLong(keyDateCreatedIndex)),
                Constants.PREFERRED_DATE_TIME);
        this.dateDue = DateHelper.beautifulDateToString(new Date(c.getLong(keyDateDueIndex)),
                Constants.PREFERRED_DATE_TIME);
        this.itemId = c.getLong(keyTitleId);
    }

    public String getDateDue() {
        return this.dateDue;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public long getItemId() {
        return itemId;
    }
}
