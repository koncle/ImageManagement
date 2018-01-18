package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by 10976 on 2018/1/18.
 */

public class FolderSpinnerAdapter implements SpinnerAdapter {

    private List<String> folderList;
    private Map<String, List<String>> folderMap;
    private Context context;

    public FolderSpinnerAdapter(Context context, Map<String, List<String>> folderMap) {
        this.folderMap = folderMap;
        this.folderList = new ArrayList<>();
        folderList.addAll(folderMap.keySet());
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.image_select_spinner_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String folder = folderList.get(position);

        Glide.with(context)
                .load(folderMap.get(folder).get(0))
                .into(viewHolder.imageView);
        viewHolder.textView.setText(String.format(new Locale("zh"), "%s (%d) ", folder, folderMap.get(folder).size()));
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return folderList.size();
    }

    @Override
    public Object getItem(int position) {
        return folderMap.get(folderList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SelectedViewHolder viewHolder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.image_select_spinner_selected_laytou, null);
            viewHolder = new SelectedViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SelectedViewHolder) convertView.getTag();
        }

        String folder = folderList.get(position);
        viewHolder.textView.setText(String.format(new Locale("zh"), "%s (%d) ", folder, folderMap.get(folder).size()));
        return convertView;
    }

    public List<String> getFloderList() {
        return folderList;
    }

    private class SelectedViewHolder {
        TextView textView;

        SelectedViewHolder(View v) {
            textView = v.findViewById(R.id.select_folder_name);
        }
    }


    private class ViewHolder {
        ImageView imageView;
        TextView textView;

        ViewHolder(View v) {
            imageView = v.findViewById(R.id.select_folder_image);
            textView = v.findViewById(R.id.select_folder_name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return folderList.size() == 0;
    }
}
