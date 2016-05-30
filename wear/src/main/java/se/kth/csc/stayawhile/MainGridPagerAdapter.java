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
    private List<Row> mQueue = new ArrayList<>();
    private boolean isAttending = false;
    private UpdateButtonFragment updateButton = new UpdateButtonFragment();

    public void setQueue(List<Bundle> queue,boolean isAttending) {
        this.isAttending = isAttending;
        mQueue = new ArrayList<>();

        for (Bundle queuee : queue) {
            mQueue.add(new Row(queuee));
        }
        if (isAttending) {
            mQueue.get(0).isAttending = true;
            mQueue.get(0).update();
        }

        singleton.notifyDataSetChanged();
        MainActivity.pager.setCurrentItem(0,0); //Only one row should be visible
        singleton.notifyDataSetChanged();
        MainActivity.pager.invalidate();
    }

    public static MainGridPagerAdapter getMainGridPagerAdapter(FragmentManager fm) {
        if (singleton == null)
            singleton = new MainGridPagerAdapter(fm);

        MainActivity.getInstance().sendMessageToHost("/stayawhile/queue/update", new byte[0]);
        return singleton;
    }

    public static MainGridPagerAdapter getMainGridPagerAdapter() {
        return singleton;
    }

    public MainGridPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (mQueue.size() == 0)
            return updateButton;
        return mQueue.get(row).getFragment(col);
    }

    @Override
    public int getRowCount() {
        if (isAttending || mQueue.size() == 0)
            return 1;
        else
            return mQueue.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        if (mQueue.size() == 0)
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
