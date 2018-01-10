package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_layout);

        findViews();
        initListeners();
    }

    private void initListeners() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(REMARK_CANCEL, intent);
                RemarkActivity.this.finish();
            }
        });

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remark = input.getText().toString().trim();
                if (TextUtils.isEmpty(remark)) {
                    show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(REMARK_INPUT, remark);
                    setResult(REMARK_OK, intent);
                    RemarkActivity.this.finish();
                }
            }

            private void show() {

            }
        });
    }

    private void findViews() {
        back = findViewById(R.id.remark_back);
        complete = findViewById(R.id.remark_complete);
        input = findViewById(R.id.remark_input);
    }
}
