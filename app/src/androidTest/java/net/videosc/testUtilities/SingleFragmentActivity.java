package net.videosc.testUtilities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.videosc.R;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

@RestrictTo(RestrictTo.Scope.TESTS)
public class SingleFragmentActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout content = new FrameLayout(this);
		content.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		content.setId(R.id.camera_preview);
		setContentView(content);
	}

	public void setFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.add(R.id.camera_preview, fragment, "TEST")
				.commit();
	}

	public void replaceFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.camera_preview, fragment).commit();
	}
}
