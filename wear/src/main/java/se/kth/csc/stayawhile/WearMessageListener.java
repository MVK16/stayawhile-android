package se.kth.csc.stayawhile;


import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by willy on 27/04/16.
 */
public class WearMessageListener extends WearableListenerService {
    private static final String SENDING_QUEUE = "/saw_sendingqueue";
    private static final String SEND_UPDATE_REQUEST = "/saw_sendupdatepls";
    private static final String SEND_KICK_USER = "/saw_kickemlow";
    private static final String SEND_ATTEND_USER = "/saw_illhavealook";

    private GoogleApiClient mGoogleApiClient;
    private MainActivity mainActivity;


    public WearMessageListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void requestUpdate(){
        sendMessage(SEND_UPDATE_REQUEST, new byte[0]);
    }

    public void sendKickUser(String user){
        sendMessage(SEND_KICK_USER, user.getBytes());
    }

    public void sendToggleAttendUser(String user){
        sendMessage(SEND_ATTEND_USER, user.getBytes());
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        if( messageEvent.getPath().equalsIgnoreCase( SENDING_QUEUE ) ) {
            try {
                JSONArray queue = new JSONArray(new String(messageEvent.getData()));
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
                MainGridPagerAdapter.setQueue(queuees);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Log.i("DEV", "Got queue sent to us! Sending reply! Queue data: " + new String(messageEvent.getData()));
            sendMessage("/saw_repl", new byte[0]);


        } else {
            super.onMessageReceived( messageEvent );
        }
    }

    private void initGoogleApi(){
        mGoogleApiClient = new GoogleApiClient.Builder( this.mainActivity )
                .addApi( Wearable.API )
                .build();

        mGoogleApiClient.connect();
    }
    public void sendMessage( final String path, final byte[] data ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mGoogleApiClient == null)
                    initGoogleApi();

                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, data).await();
                    Log.i("DEV", "Sent message! To: " + node.getId() + ", Path: " + path + ", Data: " + new String(data));
                }
            }
        }).start();
        Log.i("DEV", "Sending message");
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
