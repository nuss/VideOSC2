package net.videosc2.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by stefan on 25.06.17, package net.videosc2.adapters, project VideOSC22.
 */
public class ToolsMenuAdapterTest {
	private ToolsMenuAdapter adapter;

	@Mock
	private Context mMockContext;
	@Mock
	private List<BitmapDrawable> mBitmaps;

	@Before
	public void setUp() {
		adapter = new ToolsMenuAdapter(mMockContext, 0, 0, mBitmaps);
	}

	@Test
	public void getToolsDrawerListState() {
		HashMap<Integer, Integer> map = adapter.getToolsDrawerListState();
		assertTrue(map.isEmpty());
	}

	@After
	public void tearDown() {
		adapter = null;
	}
}