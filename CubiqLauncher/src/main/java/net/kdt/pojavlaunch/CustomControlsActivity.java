package net.kdt.pojavlaunch;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.ActivityCustomControlsBinding;
import com.movtery.zalithlauncher.databinding.ViewControlMenuBinding;
import com.movtery.zalithlauncher.ui.base.FullScreenAppCompatActivity;
import com.movtery.zalithlauncher.ui.subassembly.menu.ControlMenu;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;

import java.io.IOException;

public class CustomControlsActivity extends FullScreenAppCompatActivity implements EditorExitable {
	public static final String BUNDLE_CONTROL_PATH = "control_path";
	private ActivityCustomControlsBinding binding;
	private String mControlPath = null;

	@Override
	public boolean isFullScreenActivity() {
		return true;
	}

	@Override
	public boolean isIgnoreNotch() {
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.currentDisplayMetrics = getResources().getDisplayMetrics();
		parseBundle();
		binding = ActivityCustomControlsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ControlLayout controlLayout = binding.customctrlControllayout;
		DrawerLayout drawerLayout = binding.customctrlDrawerlayout;
		FrameLayout drawerNavigationView = binding.customctrlNavigationView;

		binding.backgroundView.setBackgroundColor(0xFF1E1E1E);

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		drawerLayout.setScrimColor(Color.TRANSPARENT);

		binding.menuButton.setOnClickListener(v -> {
			boolean open = drawerLayout.isDrawerOpen(drawerNavigationView);
			if (open) {
				drawerLayout.closeDrawer(drawerNavigationView);
			} else {
				drawerLayout.openDrawer(drawerNavigationView);
			}
		});

		ViewControlMenuBinding controlMenuBinding = ViewControlMenuBinding.inflate(getLayoutInflater());
		new ControlMenu(this, this, controlMenuBinding, controlLayout, true);

		drawerNavigationView.addView(controlMenuBinding.getRoot());
		controlLayout.setModifiable(true);
		try {
			if (mControlPath == null) controlLayout.loadLayout((String) null);
			else controlLayout.loadLayout(mControlPath);
		} catch (IOException e) {
			Tools.showError(this, e);
		}

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				binding.customctrlControllayout.askToExit(CustomControlsActivity.this);
			}
		});
	}

	private void parseBundle() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mControlPath = bundle.getString(BUNDLE_CONTROL_PATH);
		}
	}

	@Override
	public void exitEditor() {
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		com.movtery.zalithlauncher.game.control.ControlManager.INSTANCE.refresh();
	}
}

