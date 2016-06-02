package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by viklu on 2016-05-20.
 */
public class UpdateButtonFragment extends Fragment{
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.update_button_fragment, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.update);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.getInstance().sendMessageToHost("/stayawhile/queue/update", new byte[0]);
                return false;
            }
        });
        return view;
    }
}
