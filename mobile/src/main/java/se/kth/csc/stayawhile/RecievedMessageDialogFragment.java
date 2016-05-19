package se.kth.csc.stayawhile;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by NAN on 2016-04-26.
 * Dialog message for recieved messages
 */
public class RecievedMessageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        String title = args.getString("title");
        builder.setTitle(title)
                .setNeutralButton("OK", null);
        String message = args.getString("message");
        if(args.getString("sender") != null) {
            message = message + "\n\n" + args.getString("sender");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        builder.setMessage(sb.toString());
        android.support.v7.app.AlertDialog dialog = builder.create();
        return dialog;
    }

}
