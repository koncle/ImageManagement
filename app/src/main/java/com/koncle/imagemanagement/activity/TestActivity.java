package com.koncle.imagemanagement.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.view.TagAdapter;
import com.koncle.imagemanagement.view.TagViewLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Koncle on 2018/1/29.
 */

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_view);

        TagViewLayout tagViewLayout = findViewById(R.id.view_group);
        final String[] datax = {"hh", "asdfasdfsdaf", "asdfasdf", "23o94u2", "2-sdklf", "哈哈哈"};
        final List<String> data = new ArrayList<>();
        data.addAll(Arrays.asList(datax));

        tagViewLayout.setAdapter(new TagAdapter<String>(data) {
            @Override
            public View getView(ViewGroup parent, int position, String tag) {
                // attach parent to get its layoutparams
                View v = LayoutInflater.from(TestActivity.this).inflate(R.layout.test_item, parent, false);
                ((TextView) v.findViewById(R.id.test_id)).setText(tag);
                return v;
            }
        });
        tagViewLayout.setOnTagClickListener(new TagViewLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position) {
                Log.w("tag", "position " + data.get(position));
                return false;
            }
        });
        tagViewLayout.setOnSelectStateChangeListener(new TagViewLayout.OnSelectStateChangeListener() {
            @Override
            public void onSelectedStateChange(View view, int position, boolean checked) {
                Log.w("tag", "state changed position " + data.get(position));
            }
        });

    }
}
