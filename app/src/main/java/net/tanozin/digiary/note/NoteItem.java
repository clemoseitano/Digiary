package net.tanozin.digiary.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import java.util.Date;

import net.tanozin.digiary.DateHelper;

/**
 * Created by Agbodeka on 4/4/2016.
 */
public class NoteItem {
    private String type;
    private String dateTaken;
    private String link;
    private String dateModified;
    private String name;

    public NoteItem(String type, String name, String link) {
        this.type = type;
        this.link = link;
        this.name = name;
        this.dateTaken = DateHelper.convertDateToString(new Date(System.currentTimeMillis()));
        this.dateModified = DateHelper.convertDateToString(new Date(System.currentTimeMillis()));
    }

    public NoteItem(String type, String name, String link, String dateTaken) {
        this.type = type;
        this.link = link;
        this.name = name;
        this.dateTaken = dateTaken;
        this.dateModified = DateHelper.convertDateToString(new Date(System.currentTimeMillis()));
    }

    public NoteItem(Cursor data) {
        this.type = data.getString(data.getColumnIndex(NoteContentProvider.KEY_TYPE));
        this.name = data.getString(data.getColumnIndex(NoteContentProvider.KEY_NAME));
        this.link = data.getString(data.getColumnIndex(NoteContentProvider.KEY_LINK));
        this.dateTaken = data.getString(data.getColumnIndex(NoteContentProvider.KEY_DATE_TAKEN));
        this.dateModified = data.getString(data.getColumnIndex(NoteContentProvider.KEY_DATE_MODIFIED));
    }

    public NoteItem(Context context, String name) {
        String[] args = {name};
        String sel = NoteContentProvider.KEY_NAME + " = ? ";
        String[] projection = new String[]{NoteContentProvider.KEY_TYPE, NoteContentProvider.KEY_NAME,
                NoteContentProvider.KEY_DATE_TAKEN, NoteContentProvider.KEY_LINK};
        Cursor data = context.getContentResolver().query(NoteContentProvider.CONTENT_URI, projection, sel, args, null);
        if (null == data) {
            Toast.makeText(context, "An error occurred while finding match for file with name " + "\' " +
                    name + " \'", Toast.LENGTH_LONG).show();
        } else if (data.getCount() < 1) {
            Toast.makeText(context, "No match for file with name " + "\' " +
                    name + " \'", Toast.LENGTH_LONG).show();
            data.close();
        } else {
            if (data.moveToFirst()) {
                this.type = data.getString(data.getColumnIndex(NoteContentProvider.KEY_TYPE));
                this.name = name;
                this.link = data.getString(data.getColumnIndex(NoteContentProvider.KEY_LINK));
                this.dateTaken = data.getString(data.getColumnIndex(NoteContentProvider.KEY_DATE_TAKEN));
                this.dateModified = DateHelper.convertDateToString(new Date(System.currentTimeMillis()));
                Toast.makeText(context, "File with name " + "\' " +
                        name + " \' was modified on "+this.dateModified , Toast.LENGTH_LONG).show();
                data.close();
            } else {
                Toast.makeText(context, "Something strange happened ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContentValues getContentValues() {
        ContentValues c = new ContentValues();
        c.put(NoteContentProvider.KEY_TYPE, this.type);
        c.put(NoteContentProvider.KEY_DATE_MODIFIED, this.dateModified);
        c.put(NoteContentProvider.KEY_DATE_TAKEN, this.dateTaken);
        c.put(NoteContentProvider.KEY_NAME, this.name);
        c.put(NoteContentProvider.KEY_LINK, this.link);
        return c;
    }
}
