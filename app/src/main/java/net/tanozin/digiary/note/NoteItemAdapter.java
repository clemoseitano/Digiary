package net.tanozin.digiary.note;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.tanozin.digiary.R;

public class NoteItemAdapter extends BaseAdapter {
    private final List<NoteItem> items;
    private final Context context;

    public NoteItemAdapter(Context context, List litems) {
        this.items = litems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.note_item2, null);
            viewHolder = new ViewHolder();
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.iconView);
            //iconView can be set using type
            //example: if(getType()=="video")
            //setVideoIcon
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.nameView);
            viewHolder.modifiedView = (TextView) convertView.findViewById(R.id.modifiedView);
            viewHolder.typeView = (TextView) convertView.findViewById(R.id.typeView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameView.setText(items.get(position).getName());
        viewHolder.modifiedView.setText(items.get(position).getDateModified());
        viewHolder.setIcon(items.get(position).getType());
        viewHolder.setType(items.get(position).getType());
        return convertView;
    }

    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return mSelection.keySet();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView iconView;
        TextView nameView;
        TextView modifiedView;
        TextView typeView;

        public void setIcon(String type) {
            if (type.contains("text"))
                iconView.setImageResource(R.drawable.ic_file);
            else if (type.contains("audio"))
                iconView.setImageResource(R.drawable.ic_audio);
            else if (type.contains("image"))
                iconView.setImageResource(R.drawable.ic_image);
            else if (type.contains("video"))
                iconView.setImageResource(R.drawable.ic_video);
            else
                iconView.setImageResource(R.drawable.ic_unknown);

        }

        public void setType(String type) {
            String extension;
            if (type.contains("text"))
                extension = "txt";
            else if (type.contains("audio"))
                extension = "mp3";
            else if (type.contains("image"))
                extension = "jpg";
            else if (type.contains("video"))
                extension = "mp4";
            else
                extension = "unknown";
            typeView.setText(type.toUpperCase() + ":" + extension.toUpperCase());
        }
    }
}