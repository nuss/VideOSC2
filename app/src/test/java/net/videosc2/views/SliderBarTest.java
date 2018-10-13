package net.videosc2.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SliderBarTest {
	private SliderBar bar;

	@Mock
	private Context mMockContext;
	private Typeface mTypeFace;

	@Before
	public void setUp() {
		bar = new SliderBar(mMockContext);
		bar.setTouchY(0);
		bar.setNum("5");
	}

	@Test
	public void onDraw() {
		assertEquals(bar.mArea.getClass(), Rect.class);
	}

	@Test
	public void getTouchY() {
		assertEquals(0, bar.getTouchY());
	}

	@Test
	public void setTouchY() {
		bar.setTouchY(40);
		assertEquals(40, bar.getTouchY());
	}

	@Test
	public void getNum() {
		assertEquals("5", bar.getNum());
	}

	@Test
	public void setNum() {
		bar.setNum("20");
		assertEquals("20", bar.getNum());
	}
}