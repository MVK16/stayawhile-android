package se.kth.csc.stayawhile;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Willy on 2016-04-28.
 */
public class WearMessageHandler extends WearableListenerService{
    private static final String SENDING_QUEUE = "/saw_sendingqueue";
    private static final String SEND_UPDATE_REQUEST = "/saw_sendupdatepls";
    private static final String SEND_KICK_USER = "/saw_kickemlow";
    private static final String SEND_ATTEND_USER = "/saw_illhavealook";


    private GoogleApiClient mGoogleApiClient;
    private MainActivity mainActivity;
    private JSONObject mQueue;
    private NodeApi.GetConnectedNodesResult nodes;

    public WearMessageHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }




    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        if( messageEvent.getPath().equalsIgnoreCase( "/saw_repl" ) ) {
            //String msg = messageEvent.getData();
            Log.i("DEV", "Got queue sent confirmation!");
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_REQUEST)){
            try {
                sendQueueToWear(mQueue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_KICK_USER)){
            kickUser(messageEvent.getData());
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_ATTEND_USER)){
            attendUser(messageEvent.getData());
        } else {
            super.onMessageReceived( messageEvent );
        }
    }

    private void attendUser(byte[] data) {
        //TODO
    }

    private void kickUser(byte[] data) {
        //TODO
    }


    public void sendQueueToWear(JSONObject o) throws JSONException {
        //TODO: TEST!!
        if (o != null)
            this.mQueue = o;
        if (this.mQueue == null) return;
        Log.i("DEV", "Queue data: " + mQueue.getJSONArray("queue").toString());
        Log.i("DEV", "SIZE: " + mQueue.getJSONArray("queue").toString().getBytes().length);
        sendMessage(SENDING_QUEUE, this.mQueue.getJSONArray("queue").toString().getBytes());
    }

    public void sendQueueToWear(){
        //TODO: TEST!!
        try {
            sendQueueToWear(mQueue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }








    private void initGoogleApi(){
        mGoogleApiClient = new GoogleApiClient.Builder(mainActivity)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    private String getBestNodeId(){
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public void sendMessage( final String path, final byte[] data ) {

        new Thread( new Runnable() {
            @Override
            public void run() {
                while (mGoogleApiClient == null)
                    initGoogleApi();

                Log.i("DEVm", "Getting nodes!");

                if (nodes == null)
                    nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                Log.i("DEVm", "Got " + nodes.getNodes().size() + " nodes!");
                for(Node node : nodes.getNodes()) {
                    Log.i("DEVm", "Sent message! Path: " + path + " Data: " + new String(data));
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, data ).await();
                }
            }
        }).start();
        Log.i("DEVm","Should've sent message!");
    }
    public GoogleApiClient getApi(){
        while (mGoogleApiClient == null)
            initGoogleApi();
        return mGoogleApiClient;
    }
    public void disconnectApi() {
        mGoogleApiClient.disconnect();
    }
}
