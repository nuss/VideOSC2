package net.videosc2.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import net.videosc2.R;
import java.util.List;

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
		// Get the data item for this position
		BitmapDrawable tool = getItem(position);
		Log.d(TAG, "tool: " + tool);
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
