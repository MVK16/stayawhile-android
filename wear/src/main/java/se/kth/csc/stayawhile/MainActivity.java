package se.kth.csc.stayawhile;

import android.net.Uri;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends WearableActivity {
    protected static GridViewPager pager;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.instance = this;
        setContentView(R.layout.activity_main);
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(MainGridPagerAdapter.getMainGridPagerAdapter(getFragmentManager()));

        // Setup DataItem API

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Wearable.DataApi.addListener(mGoogleApiClient, onDataChangeListener);

                        Uri uri = new Uri.Builder()
                                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                                .path("/stayawhile/queue")
                                .build();

                        Wearable.DataApi.getDataItems(mGoogleApiClient, uri)
                                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                                                       @Override
                                                       public void onResult(DataItemBuffer items) {
                                                           for(int i=0;i<items.getCount();i++) {
                                                               DataItem item = items.get(i);
                                                               DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                                                               queueUpdated(dataMap.getString(QUEUE_KEY));
                                                           }
                                                       }
                                                   }
                                );
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

        // Setup Message API

        PendingResult<CapabilityApi.GetCapabilityResult> capabilityResult =
                Wearable.CapabilityApi.getCapability(
                        mGoogleApiClient, HOST_CAPABILITY_NAME,
                        CapabilityApi.FILTER_REACHABLE);

        capabilityResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult result) {
                updateTranscriptionCapability(result.getCapability());
            }
        });

        CapabilityApi.CapabilityListener capabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateTranscriptionCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                HOST_CAPABILITY_NAME);
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
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void queueUpdated(String data) {
        try {
            JSONArray queue = new JSONArray(data);
            ArrayList<Bundle> queuees = new ArrayList<>();
            boolean attending = false;
            if (queue.length() >= 1) attending = queue.getJSONObject(0).getBoolean("gettingHelp");
            for (int i = 0; i < queue.length(); i++) {
                Bundle queueeBundle = new Bundle();
                JSONObject queueeJSON = queue.getJSONObject(i);
                // TODO: is it really a good idea to change these names?
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
            MainGridPagerAdapter.getMainGridPagerAdapter().setQueue(queuees, attending);

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

    private static final String
            HOST_CAPABILITY_NAME = "host";

    private String hostNodeId = null;

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        hostNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            System.out.println("Got possible node: " + node.getId());
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public void sendMessageToHost(String path, byte[] bytes) {
        if (hostNodeId != null) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, hostNodeId,
                    path, bytes).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        System.err.println("failed to send message");
                    }
                }
            });
        } else {
            System.err.println("no capable node connected");
            /*
            final String pathTemp = path; final byte[] bytesTemp = bytes;
            PendingResult<NodeApi.GetConnectedNodesResult> result = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
            result.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetConnectedNodesResult result) {
                    System.out.println("Connected nodes:");
                    for (Node n : result.getNodes()) {
                        System.out.println(n.getDisplayName() + ", " + n.getId());

                        Wearable.MessageApi.sendMessage(mGoogleApiClient, n.getId(),
                                pathTemp, bytesTemp).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    System.out.println("failed to send message");
                                }
                            }
                        });
                    }
                }
            });
            */
        }
    }
}
