package net.videosc.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.utilities.enums.RGBModes;

public class SparseStringsAdapter extends SparseArrayAdapter<String> {
    private static final String TAG = SparseStringsAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final RGBModes mColor;

    public SparseStringsAdapter(Context context, SparseArray<String> data, RGBModes color) {
        this.mInflater = LayoutInflater.from(context);
        this.mColor = color;
        setData(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemLayout = (LinearLayout) convertView;
        if (itemLayout == null) {
            itemLayout = (LinearLayout) mInflater.inflate(R.layout.group_sliders_editor_item, parent, false);
        }

        final TextView pixelNrField = itemLayout.findViewById(R.id.pixel_number);
        switch (mColor) {
            case R:
                pixelNrField.setBackgroundColor(0xffff0000);
                break;
            case G:
                pixelNrField.setBackgroundColor(0xff00aa00);
                break;
            case B:
                pixelNrField.setBackgroundColor(0xff0000ff);
                break;
        }
        pixelNrField.setTextColor(0xffffffff);

        final TextView pixelText = itemLayout.findViewById(R.id.pixel_text);

        pixelNrField.setText(String.valueOf(getItemId(position)));
        pixelText.setText(getItem(position));

        return itemLayout;
    }
}
