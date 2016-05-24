package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


public class MainGridPagerAdapter extends FragmentGridPagerAdapter {

    private static MainGridPagerAdapter singleton;
    private List<Row> queue = new ArrayList<>();
    private boolean isAttending = false;
    private int currentQueuee = 0;
    private UpdateButtonFragment updateButton = new UpdateButtonFragment();

    public static void setQueue(List<Bundle> queue) {
        //MainGridPagerAdapter.queue = queue; TODO: Fix
    }

    public static MainGridPagerAdapter getMainGridPagerAdapter(FragmentManager fm) {
        if (singleton == null)
            singleton = new MainGridPagerAdapter(fm);
        return singleton;
    }

    public static MainGridPagerAdapter getMainGridPagerAdapter() {
        return singleton;
    }

    public MainGridPagerAdapter(FragmentManager fm) {
        super(fm);
        if (queue.size() == 0) {
            Bundle b1 = new Bundle();
            b1.putString("uid", "u1dalexskfHUMBUG");
            b1.putString("name", "Alexander Viklund");
            b1.putString("location", "Grey 06");
            b1.putString("type", "Help");
            b1.putString("comment", "What am i doing?");
            queue.add(new Row(b1));
            Bundle b2 = new Bundle();
            b2.putString("uid", "u1dwillekfHUMBUG");
            b2.putString("name", "Wille");
            b2.putString("location", "Grey 07");
            b2.putString("type", "Present");
            b2.putString("comment", "Long message is a long message");
            queue.add(new Row(b2));
        }
            //MainActivity.mMessageListener.requestUpdate();
    }

    public static void setAttending(boolean isAttending) {
        singleton.isAttending = isAttending;
        singleton.queue.get(singleton.currentQueuee).isAttending = isAttending;
        singleton.queue.get(singleton.currentQueuee).update();
        singleton.notifyDataSetChanged();
        if (isAttending)
            MainActivity.pager.setCurrentItem(0,0); //Only one row should be visible
        else
            MainActivity.pager.setCurrentItem(1,0); //Show row below updatebutton
        singleton.notifyDataSetChanged();
        MainActivity.pager.invalidate();
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (row == 0 && !isAttending)
            return updateButton;
        if (!isAttending)
            currentQueuee = row-1;
        return queue.get(currentQueuee).getFragment(col);
    }

    @Override
    public int getRowCount() {
        if (isAttending)
            return 1;
        else
            return queue.size()+1;
    }

    @Override
    public int getColumnCount(int rowNum) {
        if (rowNum == 0)
            return 1;
        else
            return 3;
    }

    class Row {
        Bundle user;
        QueueePageFragment userpage;
        AcceptActionFragment accept;
        DeclineActionFragment decline;
        KickActionFragment kick;
        boolean isAttending = false;

        Row(Bundle user) {
            this.user = user;
        }

        Fragment getFragment(int i) {
            switch (i) {
                case 0:
                    if (userpage == null) {
                        userpage = new QueueePageFragment();
                        userpage.setArguments(user);
                        userpage.setAttend(isAttending);
                    }
                    return userpage;
                case 1:
                    if (isAttending) {
                        if (decline == null) {
                            decline = new DeclineActionFragment();
                            decline.setArguments(user);
                        }
                        return decline;
                    } else {
                        if (accept == null) {
                            accept = new AcceptActionFragment();
                            accept.setArguments(user);
                        }
                        return accept;
                    }
                case 2:
                    if (kick == null) {
                        kick = new KickActionFragment();
                        kick.setArguments(user);
                        kick.setAttend(isAttending);
                    }
                    return kick;
            }
            throw new InvalidParameterException("Trying to get fragment " + i + " on user " + user.get("uid"));
        }

        void update() {
            if (userpage != null)
                userpage.setAttend(isAttending);
            if (kick != null)
                kick.setAttend(isAttending);
        }
    }
}
