package se.kth.csc.stayawhile;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import se.kth.csc.stayawhile.api.API;
import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HttpApiTest extends ActivityInstrumentationTestCase2<QueueListActivity> {

    private QueueListActivity mActivity;

    public HttpApiTest() {
        super(QueueListActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        Intent intent = new Intent();
        setActivityIntent(intent);
        mActivity = getActivity();
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(mActivity.getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    @Test
    public void queueList() throws IOException, JSONException {
        JSONArray queueList = new JSONArray(API.sendAPICall("queueList"));
        assertTrue(queueList.length() > 0);
        assertTrue(queueList.getJSONObject(0).getString("name").equals("Dtek"));
    }

    @Test
    public void queue() throws IOException, JSONException {
        JSONObject queue = new JSONObject(API.sendAPICall("queue/Dtek"));
        assertEquals("Dtek", queue.getString("name"));
    }

    @Test
    public void adminList() throws IOException, JSONException {
        JSONArray adminList = new JSONArray(API.sendAPICall("adminList"));
        assertEquals("robertwb", adminList.getJSONObject(0).getString("username"));
    }

    @Test
    public void serverMessage() throws IOException, JSONException {
        JSONObject adminList = new JSONObject(API.sendAPICall("serverMessage"));
        assertTrue(adminList.has("serverMessage"));
    }

    @Test
    public void userData() throws IOException, JSONException {
        JSONObject userData = new JSONObject(API.sendAPICall("userData"));
        assertTrue(userData.has("ugKthid"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
