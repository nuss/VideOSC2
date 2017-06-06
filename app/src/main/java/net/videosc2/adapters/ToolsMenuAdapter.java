package net.videosc2.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.utilities.enums.RGBToolbarStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static net.videosc2.utilities.enums.RGBToolbarStatus.RGB;
import static net.videosc2.utilities.enums.RGBToolbarStatus.RGB_INV;

/**
 * Created by stefan on 14.03.17.
 */

public class ToolsMenuAdapter extends ArrayAdapter<BitmapDrawable> {
	final static String TAG = "ToolsMenuAdapter";
	private int menuSize;
	private Method getMode;

	public ToolsMenuAdapter(Context context, int resource, int bitmapResourceId, List<BitmapDrawable> tools) {
		super(context, resource, bitmapResourceId, tools);
		menuSize = tools.size();
		try {
			getMode = context.getClass().getMethod("getColorModeToolsDrawer");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		int modePosition = 2;
		BitmapDrawable tool;
		Context context = getContext();
		Enum mode = null;

		try {
			mode = (Enum) getMode.invoke(getContext());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		// Get the data item for this position
		if (menuSize == 6)
			modePosition = 1;
		if (position != modePosition)
			tool = getItem(position);
		else {
			tool = getItem(position);
			if (mode != null) {
				if (mode.equals(RGBToolbarStatus.RGB_INV)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.rgb_inv);
				} else if (mode.equals(RGBToolbarStatus.R)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.r);
				} else if (mode.equals(RGBToolbarStatus.R_INV)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.r_inv);
				} else if (mode.equals(RGBToolbarStatus.G)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.g);
				} else if (mode.equals(RGBToolbarStatus.G_INV)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.g_inv);
				} else if (mode.equals(RGBToolbarStatus.B)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.b);
				} else if (mode.equals(RGBToolbarStatus.B_INV)) {
					tool = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.b_inv);
				}
			}
		}
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
		}
		// Lookup view for data population
		ImageView toolView = (ImageView) convertView.findViewById(R.id.tool);
		// Populate the data into the template view using the data object
		toolView.setImageDrawable(tool);
		// Return the completed view to render on screen
		return convertView;
	}
}
