package com.koncle.imagemanagement.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Koncle on 2018/1/28.
 */

public class InfoDialog extends DialogFragment {
    private Image image;
    private Map<String, String> infoMap;
    private String[] keys = {"path", "name", "time"};

    public static InfoDialog newInstance(Image image) {

        Bundle args = new Bundle();

        InfoDialog fragment = new InfoDialog();
        fragment.setArguments(args);
        fragment.setImage(image);
        return fragment;
    }

    public void setImage(Image image) {
        this.image = image;
        this.infoMap = new HashMap<>();
        infoMap.put(keys[0], image.getPath());
        infoMap.put(keys[1], image.getName());
        infoMap.put(keys[2], ImageUtils.getFormatedTime(image.getTime()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("image", image);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Image image = savedInstanceState.getParcelable("image");
            setImage(image);
        }
        View root = inflater.inflate(R.layout.image_info, null, false);

        RecyclerView recyclerView = root.findViewById(R.id.info_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        InfoAdapter adapter = new InfoAdapter();
        recyclerView.setAdapter(adapter);
        return root;
    }

    private class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoHolder> {
        @Override
        public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.image_info_item, null);
            return new InfoHolder(view);
        }

        @Override
        public void onBindViewHolder(InfoHolder holder, int position) {
            String key = keys[position];
            String value = infoMap.get(key);
            holder.value.setText(value);
            holder.key.setText(key + ":");
        }

        @Override
        public int getItemCount() {
            return keys.length;
        }

        public class InfoHolder extends RecyclerView.ViewHolder {
            TextView key;
            TextView value;

            public InfoHolder(View itemView) {
                super(itemView);
                key = itemView.findViewById(R.id.info_key);
                value = itemView.findViewById(R.id.info_value);
            }
        }
    }
}
