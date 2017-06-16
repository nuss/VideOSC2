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

	public ToolsMenuAdapter(Context context, int resource, int bitmapResourceId, List<BitmapDrawable> tools) {
		super(context, resource, bitmapResourceId, tools);
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {

		BitmapDrawable tool;

		tool = getItem(position);
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
