package com.koncle.imagemanagement.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/18.
 */

class EventDialogFragment extends DialogFragment {
    OnInputFinished onInputFinished;

    public static EventDialogFragment newInstance(OnInputFinished onInputFinished) {

        Bundle args = new Bundle();

        EventDialogFragment fragment = new EventDialogFragment();
        fragment.setArguments(args);
        fragment.setData(onInputFinished);
        return fragment;
    }

    public void setData(OnInputFinished onInputFinished) {
        this.onInputFinished = onInputFinished;
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
