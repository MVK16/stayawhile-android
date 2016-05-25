package se.kth.csc.stayawhile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends WearableActivity {
    protected static GridViewPager pager;

    private GoogleApiClient mGoogleApiClient;
    private final DataApi.DataListener onDataChangeListener;

    private static final String QUEUE_KEY = "se.kth.csc.stayawhile.queue";

    {
        onDataChangeListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                Log.i("DEV", "Wear: MainActivity.onDataChangeListener");
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        // DataItem changed
                        DataItem item = event.getDataItem();
                        if (item.getUri().getPath().compareTo("/stayawhile/queue") == 0) {
                            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                            queueUpdated(dataMap.getString(QUEUE_KEY));
                        }
                    } else if (event.getType() == DataEvent.TYPE_DELETED) {
                        // DataItem deleted
                    }
                }
            }
        };
    }

    private void readQueue() {
        //PendingResult<DataItem> = Wearable.DataApi.getDataItem(mGoogleApiClient, "/stayawhile/queue");
    }

    private void queueUpdated(String data) {
        try {
            JSONArray queue = new JSONArray(data);
            ArrayList<Bundle> queuees = new ArrayList<>();
            for (int i = 0; i < queue.length(); i++) {
                Bundle queueeBundle = new Bundle();
                JSONObject queueeJSON = queue.getJSONObject(i);
                queueeBundle.putString("ugid", queueeJSON.getString("ugKthid"));
                queueeBundle.putString("name", queueeJSON.getString("realname"));
                queueeBundle.putString("location", queueeJSON.getString("location"));
                queueeBundle.putString("comment", queueeJSON.getString("comment"));
                if (queueeJSON.getBoolean("help"))
                    queueeBundle.putString("type","Help");
                else
                    queueeBundle.putString("type","Present");
                queuees.add(queueeBundle);
            }
            MainGridPagerAdapter.getMainGridPagerAdapter().setQueue(queuees);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainGridPagerAdapter.getMainGridPagerAdapter().notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(MainGridPagerAdapter.getMainGridPagerAdapter(getFragmentManager()));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Wearable.DataApi.addListener(mGoogleApiClient, onDataChangeListener);
                        readQueue();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult r) {
                        //setText("Failed: " + r.getErrorMessage());
                        GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, r.getErrorCode(), 0).show();
                    }
                })
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, onDataChangeListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
