package net.kdt.pojavlaunch;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.zalithlauncher.databinding.ActivityCustomControlsBinding;
import com.movtery.zalithlauncher.databinding.ViewControlMenuBinding;
import com.movtery.zalithlauncher.feature.background.BackgroundManager;
import com.movtery.zalithlauncher.feature.background.BackgroundType;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;
import com.movtery.zalithlauncher.ui.subassembly.menu.ControlMenu;
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;

import java.io.IOException;

public class CustomControlsActivity extends BaseActivity implements EditorExitable {
	public static final String BUNDLE_CONTROL_PATH = "control_path";
	private ActivityCustomControlsBinding binding;
	private String mControlPath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parseBundle();
		binding = ActivityCustomControlsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ControlLayout controlLayout = binding.customctrlControllayout;
		DrawerLayout drawerLayout = binding.customctrlDrawerlayout;
		FrameLayout drawerNavigationView = binding.customctrlNavigationView;

		new GameMenuViewWrapper(this, v -> {
			boolean open = drawerLayout.isDrawerOpen(drawerNavigationView);

			if (open) drawerLayout.closeDrawer(drawerNavigationView);
			else drawerLayout.openDrawer(drawerNavigationView);
		}).setVisibility(true);

		BackgroundManager.setBackgroundImage(this, BackgroundType.CUSTOM_CONTROLS, binding.backgroundView);

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		drawerLayout.setScrimColor(Color.TRANSPARENT);

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
	public boolean shouldIgnoreNotch() {
		return AllSettings.getIgnoreNotch().getValue();
	}

	@Override
	public void exitEditor() {
		finish();
	}
}
