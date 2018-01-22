package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/10.
 */

public class RemarkActivity extends AppCompatActivity {
    public static final int REMARK_CANCEL = 0;
    public static final int REMARK_OK = -1;
    public static final String REMARK_INPUT = "input";
    private ImageButton back;
    private Button complete;
    private EditText input;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_layout);

        initListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remark_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.remark_menu_item_complete) {
            String remark = input.getText().toString().trim();
            if (!TextUtils.isEmpty(remark)) {
                Intent intent = new Intent();
                intent.putExtra(REMARK_INPUT, remark);
                setResult(REMARK_OK, intent);
                RemarkActivity.this.finish();
            } else {
                cancelAction();
            }
        }
        return true;
    }

    private void initListeners() {
        input = findViewById(R.id.remark_input);

        toolbar = findViewById(R.id.mark_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAction();
            }
        });
        toolbar.setTitle("Remark");
    }

    private void cancelAction() {
        Intent intent = new Intent();
        setResult(REMARK_CANCEL, intent);
        RemarkActivity.this.finish();
    }
}
