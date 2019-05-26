package net.videosc.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseIntArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by stefan on 25.06.17, package net.videosc.adapters, project VideOSC22.
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
		SparseIntArray map = adapter.getToolsDrawerListState();
		assertEquals(0, map.size());
	}

	@After
	public void tearDown() {
		adapter = null;
	}
}