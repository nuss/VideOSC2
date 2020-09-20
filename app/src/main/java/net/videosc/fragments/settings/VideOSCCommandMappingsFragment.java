package net.videosc.fragments.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cleveroad.adaptivetablelayout.AdaptiveTableLayout;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.CommandMappingsTableAdapter;
import net.videosc.fragments.VideOSCBaseFragment;

public class VideOSCCommandMappingsFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCCommandMappingsFragment.class.getSimpleName();

    private AdaptiveTableLayout mTableLayout;
    private VideOSCMainActivity mActivity;
    private CommandMappingsTableAdapter mTableAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "VideOSCCommandMappingsFragment onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.address_command_mappings_table, container, false);
        mTableLayout = view.findViewById(R.id.address_command_mappings_table);
        mActivity = (VideOSCMainActivity) getActivity();
        assert mActivity != null;
        mTableAdapter = new CommandMappingsTableAdapter(getContext(), mActivity);
        mTableLayout.setAdapter(mTableAdapter);
//        return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }
}
