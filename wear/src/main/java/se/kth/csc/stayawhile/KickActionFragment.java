package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by viklu on 2016-04-22.
 */
public class KickActionFragment extends Fragment {
    private String userid;
    private View view;
    private boolean status;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userid = getArguments().getString("uid");
        view = inflater.inflate(R.layout.kick_action_fragment, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.kick);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //MainActivity.mMessageListener.sendKickUser( userid );
                return false;
            }
        });
        return view;
    }

    public void setAttend(boolean status) {
        this.status = status;
        if (isAdded()) {
            if (status)
                view.setBackgroundColor(getResources().getColor(R.color.green));
            else
                view.setBackgroundColor(getResources().getColor(R.color.dark_grey));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (status)
            view.setBackgroundColor(getResources().getColor(R.color.green));
        else
            view.setBackgroundColor(getResources().getColor(R.color.dark_grey));
    }
}
