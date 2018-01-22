package com.koncle.imagemanagement.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/18.
 */

public class InputDialogFragment extends DialogFragment {
    OnInputFinished onInputFinished;
    String title;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.event_dialog_layout, null);

        final EditText input = view.findViewById(R.id.event_input);
        final TextView title = view.findViewById(R.id.dialog_title);
        title.setText(this.title);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onInputFinished.inputFinished(input.getText().toString().trim());
                    }
                }).setNegativeButton("Cancel", null);
        return builder.create();
    }
}
