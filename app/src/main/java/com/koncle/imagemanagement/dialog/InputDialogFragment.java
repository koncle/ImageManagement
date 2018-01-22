package com.koncle.imagemanagement.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/18.
 */

public class InputDialogFragment extends DialogFragment {
    OnInputFinished onInputFinished;
    String title;
    private EditText input;

    public static InputDialogFragment newInstance(OnInputFinished onInputFinished, String title) {

        Bundle args = new Bundle();

        InputDialogFragment fragment = new InputDialogFragment();
        fragment.setArguments(args);
        fragment.setData(onInputFinished);
        fragment.setTitle(title);
        return fragment;
    }

    public void setData(OnInputFinished onInputFinished) {
        this.onInputFinished = onInputFinished;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public interface OnInputFinished {
        void inputFinished(String eventName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_input_layout, null);

        input = root.findViewById(R.id.event_input);
        final TextView title = root.findViewById(R.id.dialog_title);
        title.setText(this.title);

        root.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        root.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInputFinished.inputFinished(input.getText().toString());
                dismiss();
            }
        });
        return root;
    }

}
