package net.tanozin.digiary.fileexplorer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

import net.tanozin.digiary.DateHelper;
import net.tanozin.digiary.R;
import net.tanozin.digiary.fileexplorer.util.StringMatcher;

/**
 * Created by user on 9/9/2016.
 */
public class FileAdapter extends BaseAdapter implements SectionIndexer {

    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(getFileName(getItem(j)).charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(getFileName(getItem(j)).charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    private File[] fileList;
    private Context context;

    public FileAdapter(Context c, File[] fs) {
        context = c;
        fileList = fs;
    }

    @Override
    public int getCount() {
        return fileList!=null?fileList.length:0;
    }

    @Override
    public File getItem(int i) {
        if (i < fileList.length)
            return fileList[i];
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.file_explorer_item, null);
            holder = new ViewHolder();
            holder.itemName = (TextView) convertView.findViewById(R.id.itemtext);
            holder.itemIcon = (ImageView) convertView.findViewById(R.id.itemimage);
            holder.itemSize = (TextView) convertView.findViewById(R.id.itemsize);
            holder.itemCreated = (TextView) convertView.findViewById(R.id.itemcreated);
            holder.itemModified = (TextView) convertView.findViewById(R.id.itemmodified);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Drawable folder = context.getResources().getDrawable(R.drawable.ic_folder);
        Drawable file = context.getResources().getDrawable(R.drawable.ic_file);
        holder.itemName.setText(fileList[position].getName());
        holder.itemSize.setText(formatFileSize(fileList[position]).trim());
        holder.itemModified.setText(DateHelper.beautifulDateToString(new
                Date(fileList[position].lastModified()), "dd MMM yyyy hh:mm:ss"));
        holder.itemIcon.setImageDrawable(fileList[position].isDirectory() ? folder : file);
        
        return convertView;
    }

    public static String formatFileSize(File file) {
        long size = file.length();

        if (file.isDirectory())
            return "";

        if (size < 0)
            return "\n0";
        else if (size == 1)
            return "\n1 Byte";
        else if (size < 1024)
            return "\n" + size + " Bytes";
        else if (size < 1024 * 1024)
            return "\n" + ((int) (size / 1024)) + " KiB";
        else
            return "\n" + Math.round(100.0 * size / (1024 * 1024)) / 100.0 + " MiB";
    }

    public static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 && i < f.length() - 1)
            ext = f.substring(i + 1).toLowerCase();

        return ext;
    }public static String getFileName(File f) {
        String ext = "";
        int i = f.getAbsolutePath().lastIndexOf('/');
        if (i > 0 && i < f.getAbsolutePath().length() - 1)
            ext = f.getAbsolutePath().substring(i + 1);

        return ext;
    }

    static class ViewHolder {
        TextView itemName;
        TextView itemSize;
        TextView itemCreated;
        TextView itemModified;
        ImageView itemIcon;
        public View layout;
    }


}
