package se.kth.csc.stayawhile;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends WearableActivity {
    public static WearMessageListener mMessageListener;
    protected static GridViewPager pager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = (GridViewPager) findViewById(R.id.pager);

        pager.setAdapter(MainGridPagerAdapter.getMainGridPagerAdapter(getFragmentManager()));
        mMessageListener = new WearMessageListener(this);
        Wearable.MessageApi.addListener(mMessageListener.getApi(), mMessageListener);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mMessageListener.disconnectApi();
    }
}
