package net.kdt.pojavlaunch;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.zalithlauncher.databinding.ActivityZl1CustomControlsBinding;
import com.movtery.zalithlauncher.databinding.ViewZl1ControlMenuBinding;
import com.movtery.zalithlauncher.ui.subassembly.menu.ControlMenu;

import git.artdeell.mojo.BaseActivity;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;

import java.io.IOException;

public class CustomControlsActivity extends BaseActivity implements EditorExitable {
	public static final String BUNDLE_CONTROL_PATH = "control_path";
	private ActivityZl1CustomControlsBinding binding;
	private String mControlPath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.updateWindowSize(this);
		parseBundle();
		binding = ActivityZl1CustomControlsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ControlLayout controlLayout = binding.customctrlControllayout;
		DrawerLayout drawerLayout = binding.customctrlDrawerlayout;
		FrameLayout drawerNavigationView = binding.customctrlNavigationView;

		binding.customctrlMenuButton.setOnTouchListener(new android.view.View.OnTouchListener() {
			private float dX, dY;
			private float startX, startY;
			private static final int CLICK_THRESHOLD = 15;

			@Override
			public boolean onTouch(android.view.View v, android.view.MotionEvent event) {
				// Hide tip text on any menu button interaction
				if (binding.customctrlTipText != null && binding.customctrlTipText.getVisibility() == View.VISIBLE) {
					binding.customctrlTipText.setVisibility(View.GONE);
				}
				switch (event.getActionMasked()) {
					case android.view.MotionEvent.ACTION_DOWN:
						dX = v.getX() - event.getRawX();
						dY = v.getY() - event.getRawY();
						startX = event.getRawX();
						startY = event.getRawY();
						return true;
					case android.view.MotionEvent.ACTION_MOVE:
						float newX = event.getRawX() + dX;
						float newY = event.getRawY() + dY;
						android.view.View parent = (android.view.View) v.getParent();
						if (parent != null) {
							float maxX = parent.getWidth() - v.getWidth();
							float maxY = parent.getHeight() - v.getHeight();
							newX = Math.max(0, Math.min(newX, maxX));
							newY = Math.max(0, Math.min(newY, maxY));
						}
						v.setX(newX);
						v.setY(newY);
						return true;
					case android.view.MotionEvent.ACTION_UP:
						float diffX = Math.abs(event.getRawX() - startX);
						float diffY = Math.abs(event.getRawY() - startY);
						if (diffX < CLICK_THRESHOLD && diffY < CLICK_THRESHOLD) {
							boolean open = drawerLayout.isDrawerOpen(drawerNavigationView);
							if (open) drawerLayout.closeDrawer(drawerNavigationView);
							else drawerLayout.openDrawer(drawerNavigationView);
						}
						return true;
				}
				return false;
			}
		});

		// Hide tip text when drawer is toggled or when canvas is interacted
		drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerStateChanged(int newState) {
				if (binding.customctrlTipText != null) {
					binding.customctrlTipText.setVisibility(View.GONE);
				}
			}
		});

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		drawerLayout.setScrimColor(Color.TRANSPARENT);

		ViewZl1ControlMenuBinding controlMenuBinding = ViewZl1ControlMenuBinding.inflate(getLayoutInflater());
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
}
