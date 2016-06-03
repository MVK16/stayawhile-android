package se.kth.csc.stayawhile;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Queue;

import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WsApiTest extends ActivityInstrumentationTestCase2<QueueActivity> {

    private QueueActivity mActivity;

    public WsApiTest() {
        super(QueueActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        Intent intent = new Intent();
        intent.putExtra("queue", "Test Queue Please Ignore");
        setActivityIntent(intent);
        mActivity = getActivity();
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(mActivity.getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    @Test
    public void joinQueue() throws Exception {
        assertTrue(mActivity.getSocket().connected());
        mActivity.getSocket().emit("putUser",
            new JSONObject(
                    "{ " +
                        "\"queueName\": \"Test Queue Please Ignore\", " +
                        "\"user\": {" +
                            "\"realname\":\"Johan Sannemo\"," +
                            "\"username\":\"jsannemo\"," +
                            "\"ugKthid\":\"u1j5iiw6\"," +
                            "\"location\":\"asd\"," +
                            "\"time\":1464938116234," +
                            "\"messages\":[]," +
                            "\"gettingHelp\":false," +
                            "\"helper\":\"\"," +
                            "\"help\":true," +
                            "\"comment\":\"asd\"," +
                            "\"completion\":false," +
                            "\"badLocation\":false" +
                        "}" +
                    "}"));
    }


    @Test
    public void markHelped() throws Exception {
        mActivity.sendHelp(new JSONObject("{" +
            "\"realname\":\"Johan Sannemo\"," +
            "\"username\":\"jsannemo\"," +
            "\"ugKthid\":\"u1j5iiw6\"," +
            "\"location\":\"asd\"," +
            "\"time\":1464938116234," +
            "\"messages\":[]," +
            "\"gettingHelp\":false," +
            "\"helper\":\"\"," +
            "\"help\":true," +
            "\"comment\":\"asd\"," +
            "\"completion\":false," +
            "\"badLocation\":false" +
            "}"));
        Thread.sleep(200);
        QueueAdapter ad = mActivity.getAdapter();
        assertTrue(ad.onPosition(ad.positionOf("u1j5iiw6")).getString("helper").length() > 0);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
