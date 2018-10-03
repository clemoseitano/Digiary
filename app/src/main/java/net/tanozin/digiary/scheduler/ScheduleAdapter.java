package net.tanozin.digiary.scheduler;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.tanozin.digiary.R;

/**
 * Created by user on 2/24/2016.
 */
public class ScheduleAdapter extends BaseAdapter {


    private final List<ScheduleItem> items;
    private final Context context;

    public ScheduleAdapter(Context context, List<ScheduleItem> items) {
        this.items = items;
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
    public long getItemId(int id) {
        return items.get(id).getItemId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.schedule_item, null);
            holder = new ViewHolder();
            holder.itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.itemDescription = (TextView) convertView.findViewById(R.id.list_item_description);
            holder.itemDateDue = (TextView) convertView.findViewById(R.id.list_item_date_due);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.itemTitle.setText(items.get(position).getTitle());
        holder.itemDescription.setText(items.get(position).getDescription());
        holder.itemDateDue.setText(items.get(position).getDateDue());

        return convertView;
    }

    static class ViewHolder {
        TextView itemTitle;
        TextView itemDescription;
        TextView itemDateDue;
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

}
