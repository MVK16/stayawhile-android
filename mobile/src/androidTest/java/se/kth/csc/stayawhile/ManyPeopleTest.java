package se.kth.csc.stayawhile;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ManyPeopleTest extends ActivityInstrumentationTestCase2<QueueActivity> {

    private QueueActivity mActivity;

    public ManyPeopleTest() {
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
    }

    @Test
    public void addUsers() {
        for (int i = 0; i < 100; i++) {
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
            mActivity.getSocket().emit("putUser", "{\"realname\":\"Johan Sannemo\",\"username\":\"jsannemo\",\"ugKthid\":\"u1j5iiw6\"" + i + ",\"location\":\"asd\",\"time\":1464938116234,\"messages\":[],\"gettingHelp\":false,\"helper\":\"\",\"help\":true,\"comment\":\"asd\",\"completion\":false,\"badLocation\":false}\n");
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
