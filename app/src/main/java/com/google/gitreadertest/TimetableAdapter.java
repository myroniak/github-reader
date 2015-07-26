package com.google.gitreadertest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Roman on 22.05.2015.
 */
public class TimetableAdapter extends BaseAdapter {
    private List<Timetable> items;
    private Context context;
    private static final String TAG = "myLogs";

    public TimetableAdapter(Context context, List<Timetable> items) {
        super();
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Timetable getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Timetable timetable =  getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item, null);
        }

        ((TextView) convertView.findViewById(R.id.repName))
                .setText(timetable.getName());
        ((TextView) convertView.findViewById(R.id.repLang))
                .setText(timetable.getLanguage());
        ((TextView) convertView.findViewById(R.id.textFork))
                .setText(timetable.getCountFork());
        ((TextView) convertView.findViewById(R.id.textStar))
                .setText(timetable.getCountStar());

        return convertView;
    }
}