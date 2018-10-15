package net.videosc2.utilities;

import android.content.Context;
import android.view.ContextThemeWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

import static org.junit.Assert.assertEquals;

public class VideOSCOscHandlerTest {
	private VideOSCOscHandler mHandler;

	@Mock
	private Context mMockContext;

	@Before
	public void setUp() {
		mMockContext = new ContextThemeWrapper();
		mHandler = new VideOSCOscHandler(mMockContext);
	}

	@After
	public void tearDown() {
		mHandler = null;
	}

	@Test
	public void makeMessage() {
		OscMessage oscMsg = mHandler.makeMessage(null, "/test");
		assertEquals(OscMessage.class, oscMsg.getClass());
		assertEquals("/test", oscMsg.addrPattern());
		oscMsg = mHandler.makeMessage(oscMsg, "/test2");
		assertEquals("/test2", oscMsg.addrPattern());
	}

	@Test
	public void getOscP5() {
		OscP5 oscP5 = mHandler.getOscP5();
		assertEquals(OscP5.class, oscP5.getClass());
	}

	@Test
	public void getBroadcastAddr() {
		mHandler.setBroadcastAddr("123.1.1.0", 12345);
		NetAddress mBroadcastAddr = mHandler.getBroadcastAddr();
		assertEquals("123.1.1.0", mBroadcastAddr.address());
		assertEquals(12345, mBroadcastAddr.port());
	}

	@Test
	public void getBroadcastIP() {
		mHandler.setBroadcastIP("111.25.0.1");
		assertEquals("111.25.0.1", mHandler.getBroadcastIP());
	}

	@Test
	public void getBroadcastPort() {
		mHandler.setBroadcastPort(54321);
		assertEquals(54321, mHandler.getBroadcastPort());
	}
}
