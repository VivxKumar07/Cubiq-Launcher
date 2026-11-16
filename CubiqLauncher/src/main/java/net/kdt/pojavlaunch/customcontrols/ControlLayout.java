package net.kdt.pojavlaunch.customcontrols;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static org.lwjgl.glfw.CallbackBridge.isGrabbing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.game.control.ControlManager;
import com.movtery.zalithlauncher.path.PathManager;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.ui.dialog.EditControlInfoDialog;
import com.movtery.zalithlauncher.ui.dialog.SelectControlsDialog;
import com.movtery.zalithlauncher.ui.dialog.TipDialog;
import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlButton;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlJoystick;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlSubButton;
import net.kdt.pojavlaunch.customcontrols.handleview.ActionRow;
import net.kdt.pojavlaunch.customcontrols.handleview.ControlHandleView;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlPopup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControlLayout extends FrameLayout {
	protected CustomControls mLayout;
	private ControlInfoData mInfoData;
	/* Accessible when inside the game by ControlInterface implementations, cached for perf. */
	private MinecraftGLSurface mGameSurface = null;

	/* Cache to buttons for performance purposes */
	private List<ControlInterface> mButtons;
	private boolean mModifiable = false;
	private boolean mIsModified;
	private boolean mControlVisible = false;

	private EditControlPopup mControlPopup = null;
	private ControlHandleView mHandleView;
	private ControlButtonMenuListener mMenuListener;
	public ActionRow mActionRow = null;
	public String mLayoutFileName;

	public ControlLayout(Context ctx) {
		super(ctx);
	}

	public ControlLayout(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}

	public void loadLayout(String jsonPath) throws IOException, JsonSyntaxException {
		File jsonFile;
		if (jsonPath != null) {
			jsonFile = new File(jsonPath);
		} else {
			String defVal = AllSettings.getDefaultCtrl().getValue();
			File checkFile = new File(defVal);
			if (checkFile.isAbsolute() && checkFile.exists()) {
				jsonFile = checkFile;
			} else {
				jsonFile = new File(com.movtery.zalithlauncher.path.PathManager.DIR_CONTROL_LAYOUTS, defVal);
			}
		}

		CustomControls layout = null;
		if (jsonFile.exists()) {
			try {
				layout = LayoutConverter.loadAndConvertIfNecessary(getContext(), jsonFile.getAbsolutePath());
				if (layout != null && layout.mControlDataList != null) {
					for (ControlData cd : layout.mControlDataList) {
						if ("DEBUG".equalsIgnoreCase(cd.name) || "CHAT".equalsIgnoreCase(cd.name) || "KEYBOARD".equalsIgnoreCase(cd.name)) {
							layout = LayoutConverter.loadFromAssets(getContext(), "zalith_1_layout.json");
							break;
						}
					}
				}
			} catch (Exception e) {
				layout = null;
			}
		}
		if (layout == null) {
			try {
				layout = LayoutConverter.loadFromAssets(getContext(), "zalith_1_layout.json");
			} catch (Exception e) {
				try {
					layout = LayoutConverter.loadFromAssets(getContext(), "default.json");
				} catch (Exception e2) {
					layout = null;
				}
			}
		}
		if (layout != null) {
			loadLayout(layout);
			if (jsonFile.exists()) {
				mLayoutFileName = jsonFile.getName().replace(".json", "");
			} else {
				mLayoutFileName = "zalith_1_layout";
			}
		}
	}

	public void loadLayout(CustomControls controlLayout) {
		boolean sanitizedModified = false;
		if(controlLayout != null) {
			sanitizedModified = LayoutSanitizer.sanitizeLayout(controlLayout);
		}
		mInfoData = controlLayout == null ? null : controlLayout.mControlInfoDataList;
		if (mInfoData == null) {
			mInfoData = new ControlInfoData();
		}

		if(mActionRow == null){
			mActionRow = new ActionRow(getContext());
			addView(mActionRow);
		}

		removeAllButtons();
		if(mLayout != null) {
			mLayout.mControlDataList = null;
			mLayout = null;
		}

		System.gc();
		mapTable.clear();

		// Cleanup buttons only when input layout is null
		if (controlLayout == null) return;

		mLayout = controlLayout;

		// Joystick(s) first, to workaround the touch dispatch
		if(mLayout.mJoystickDataList != null){
			for(ControlJoystickData joystick : mLayout.mJoystickDataList){
				addJoystickView(joystick);
			}
		}

		//CONTROL BUTTON
		if(mLayout.mControlDataList != null){
			for (ControlData button : controlLayout.mControlDataList) {
				addControlView(button);
			}
		}

		//CONTROL DRAWER
		if(mLayout.mDrawerDataList != null){
			for(ControlDrawerData drawerData : controlLayout.mDrawerDataList){
				ControlDrawer drawer = addDrawerView(drawerData);
				if(mModifiable) drawer.areButtonsVisible = true;
			}
		}

		mLayout.scaledAt = AllSettings.getButtonScale().getValue();

		setModified(sanitizedModified);
		mButtons = null;
		getButtonChildren(); // Force refresh
		post(() -> {
			refreshControlButtonPositions();
			if(mModifiable){
				for(ControlInterface button : getButtonChildren()){
					button.setVisible(true);
					button.getControlView().setVisibility(VISIBLE);
				}
			} else {
				setControlVisible(true);
			}
		});
	}

	//CONTROL BUTTON
	public void addControlButton(ControlData controlButton) {
		if(mLayout == null) mLayout = new CustomControls();
		if(mLayout.mControlDataList == null) mLayout.mControlDataList = new ArrayList<>();
		mLayout.mControlDataList.add(controlButton);
		addControlView(controlButton);
	}

	private void addControlView(ControlData controlButton) {
		final ControlButton view = new ControlButton(this, controlButton);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);

		setModified(true);
	}

	// CONTROL DRAWER
	public void addDrawer(ControlDrawerData drawerData){
		if(mLayout == null) mLayout = new CustomControls();
		if(mLayout.mDrawerDataList == null) mLayout.mDrawerDataList = new ArrayList<>();
		mLayout.mDrawerDataList.add(drawerData);
		addDrawerView();
	}

	private void addDrawerView(){
		addDrawerView(null);
	}

	private ControlDrawer addDrawerView(ControlDrawerData drawerData){
		final ControlDrawer view = new ControlDrawer(this, drawerData == null ? mLayout.mDrawerDataList.get(mLayout.mDrawerDataList.size()-1) : drawerData);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);
		//CONTROL SUB BUTTON
		if(view.getDrawerData() != null && view.getDrawerData().buttonProperties != null){
			for (ControlData subButton : view.getDrawerData().buttonProperties) {
				addSubView(view, subButton);
			}
		}

		setModified(true);
		return view;
	}

	//CONTROL SUB-BUTTON
	public void addSubButton(ControlDrawer drawer, ControlData controlButton){
		if(drawer.getDrawerData() != null && drawer.getDrawerData().buttonProperties != null){
			drawer.getDrawerData().buttonProperties.add(controlButton);
			addSubView(drawer, drawer.getDrawerData().buttonProperties.get(drawer.getDrawerData().buttonProperties.size()-1));
		}
	}

	private void addSubView(ControlDrawer drawer, ControlData controlButton){
		final ControlSubButton view = new ControlSubButton(this, controlButton, drawer);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		} else {
			view.setVisible(true);
		}

		addView(view);
		drawer.addButton(view);

		setModified(true);
	}

	// JOYSTICK BUTTON
	public void addJoystickButton(ControlJoystickData data){
		if(mLayout == null) mLayout = new CustomControls();
		if(mLayout.mJoystickDataList == null) mLayout.mJoystickDataList = new ArrayList<>();
		mLayout.mJoystickDataList.add(data);
		addJoystickView(data);
	}

	private void addJoystickView(ControlJoystickData data){
		ControlJoystick view = new ControlJoystick(this, data);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);
	}

	private void removeAllButtons() {
		for(ControlInterface button : getButtonChildren()){
			removeView(button.getControlView());
		}
		System.gc();
	}

	public void saveLayout(String path) throws Exception {
		if(mLayout != null) {
			mLayout.save(path);
		}
		setModified(false);
	}

	public void toggleControlVisible(){
		mControlVisible = !mControlVisible;
		setControlVisible(mControlVisible);
	}

	public float getLayoutScale(){
		return mLayout != null ? mLayout.scaledAt : 100f;
	}

	public CustomControls getLayout(){
		return mLayout;
	}

	public void setControlVisible(boolean isVisible) {
		if (mModifiable) return;

		mControlVisible = isVisible;
		for(ControlInterface button : getButtonChildren()){
			button.setVisible(((button.getProperties().displayInGame && isGrabbing()) || (button.getProperties().displayInMenu && !isGrabbing())) && isVisible);
		}
	}

	public void setModifiable(boolean isModifiable) {
		if(!isModifiable && mModifiable){
			removeEditWindow();
		}
		mModifiable = isModifiable;
		if(isModifiable){
			for(ControlInterface button : getButtonChildren()){
				button.setVisible(true);
			}
		}
	}

	public boolean getModifiable(){
		return mModifiable;
	}

	public boolean isModifiable(){
		return mModifiable;
	}

	public void setModified(boolean isModified) {
		mIsModified = isModified;
	}

	public List<ControlInterface> getButtonChildren(){
		if(mModifiable || mButtons == null){
			mButtons = new ArrayList<>();
			for(int i=0; i<getChildCount(); ++i){
				View v = getChildAt(i);
				if(v instanceof ControlInterface)
					mButtons.add(((ControlInterface) v));
			}
		}
		return mButtons;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0) {
			org.lwjgl.glfw.CallbackBridge.physicalWidth = w;
			org.lwjgl.glfw.CallbackBridge.physicalHeight = h;
			post(this::refreshControlButtonPositions);
		}
	}

	public void refreshControlButtonPositions(){
		if (getWidth() > 0 && getHeight() > 0) {
			org.lwjgl.glfw.CallbackBridge.physicalWidth = getWidth();
			org.lwjgl.glfw.CallbackBridge.physicalHeight = getHeight();
		}
		for(ControlInterface button : getButtonChildren()){
			button.setDynamicX(button.getProperties().dynamicX);
			button.setDynamicY(button.getProperties().dynamicY);
		}
	}

	@Override
	public void onViewRemoved(View child) {
		super.onViewRemoved(child);
		if(child instanceof ControlInterface && mControlPopup != null){
			mControlPopup.disappearColor();
			mControlPopup.disappear();
		}
	}

	public void editControlButton(ControlInterface button){
		if(mControlPopup == null){
			mControlPopup = new EditControlPopup(getContext(), this);
			post(() -> editControlButton(button));
			return;
		}

		mControlPopup.internalChanges = true;
		mControlPopup.setCurrentlyEditedButton(button);

		int screenWidth = (currentDisplayMetrics != null ? currentDisplayMetrics.widthPixels : getResources().getDisplayMetrics().widthPixels);
		mControlPopup.appear(button.getControlView().getX() + button.getControlView().getWidth()/2f < screenWidth/2f);
		button.loadEditValues(mControlPopup);

		mControlPopup.internalChanges = false;
		mControlPopup.disappearColor();

		if(mHandleView == null){
			mHandleView = new ControlHandleView(getContext());
			addView(mHandleView);
		}
		mHandleView.setControlButton(button);
	}

	public void adaptPanelPosition(){
		if(mControlPopup != null) mControlPopup.adaptPanelPosition();
	}

	final HashMap<View, ControlInterface> mapTable = new HashMap<>();

	public void onTouch(View v, MotionEvent ev) {
		ControlInterface lastControlButton = mapTable.get(v);

		ev.offsetLocation(v.getX(), v.getY());

		if (ev.getActionMasked() == MotionEvent.ACTION_UP
				|| ev.getActionMasked() == MotionEvent.ACTION_CANCEL
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
			mapTable.put(v, null);
			return;
		}

		if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) return;

		if (lastControlButton != null) {
			if (ev.getX() > lastControlButton.getControlView().getX()
					&& ev.getX() < lastControlButton.getControlView().getX() + lastControlButton.getControlView().getWidth()
					&& ev.getY() > lastControlButton.getControlView().getY()
					&& ev.getY() < lastControlButton.getControlView().getY() + lastControlButton.getControlView().getHeight()) {
				return;
			}
		}

		if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
		mapTable.remove(v);

		for (ControlInterface button : getButtonChildren()) {
			if (!button.getProperties().isSwipeable) continue;

			if (ev.getX() > button.getControlView().getX()
					&& ev.getX() < button.getControlView().getX() + button.getControlView().getWidth()
					&& ev.getY() > button.getControlView().getY()
					&& ev.getY() < button.getControlView().getY() + button.getControlView().getHeight()) {

				if (!button.equals(lastControlButton)) {
					button.sendKeyPresses(true);
					mapTable.put(v, button);
					return;
				}
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mModifiable) {
			return false;
		}
		if (mModifiable && event.getActionMasked() != MotionEvent.ACTION_UP || mControlPopup == null)
			return true;

		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		if(!imm.hideSoftInputFromWindow(getWindowToken(), 0)){
			if(mControlPopup.disappearLayer()){
				if(mActionRow != null) mActionRow.setFollowedButton(null);
				if(mHandleView != null) mHandleView.hide();
			}
		}
		return true;
	}

	public void removeEditWindow() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		if(imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
		if(mControlPopup != null) {
			mControlPopup.disappearColor();
			mControlPopup.disappear();
		}

		if(mActionRow != null) mActionRow.setFollowedButton(null);
		if(mHandleView != null) mHandleView.hide();
	}

	public void save(String path){
		try {
			mLayout.save(path);
		} catch (IOException e) {
			Log.e("ControlLayout", "Failed to save the layout at:" + path);
		}
	}

	public boolean hasMenuButton() {
		for(ControlInterface controlInterface : getButtonChildren()){
			for (int keycode : controlInterface.getProperties().keycodes) {
				if (keycode == ControlData.SPECIALBTN_MENU) return true;
			}
		}
		return false;
	}

	public void setMenuListener(ControlButtonMenuListener menuListener) {
		this.mMenuListener = menuListener;
	}

	public void notifyAppMenu() {
		if(mMenuListener != null) mMenuListener.onClickedMenu();
	}

	public MinecraftGLSurface getGameSurface() {
		if (mGameSurface == null) {
			View root = getRootView();
			if (root != null) {
				View found = root.findViewById(R.id.main_game_render_view);
				if (found instanceof MinecraftGLSurface) {
					mGameSurface = (MinecraftGLSurface) found;
				}
			}
		}
		return mGameSurface;
	}

	public void askToExit(EditorExitable editorExitable) {
		if(mIsModified) {
			openSaveAndExitDialog(editorExitable);
		}else{
			openExitDialog(editorExitable);
		}
	}

	public String saveToDirectory(String name) throws Exception {
		File dir = PathManager.Companion.getDIR_CONTROL_LAYOUTS();
		if (!dir.exists()) dir.mkdirs();
		String cleanName = name.endsWith(".json") ? name : (name + ".json");
		File targetFile = new File(dir, cleanName);
		saveLayout(targetFile.getAbsolutePath());
		mLayoutFileName = targetFile.getName().replace(".json", "");
		return targetFile.getAbsolutePath();
	}

	private void saveDialog(String title, Runnable confirmRunnable) {
		EditControlInfoDialog infoDialog = new EditControlInfoDialog(
				getContext(),
				true,
				mLayoutFileName != null ? mLayoutFileName : "zalith_1_layout",
				mInfoData != null ? mInfoData : new ControlInfoData()
		);
		if (title != null && !title.isEmpty()) infoDialog.setTitle(title);

		infoDialog.setOnConfirmClickListener((fileName, controlInfoData) -> {
			mInfoData = controlInfoData;
			if (mLayout != null) {
				mLayout.mControlInfoDataList = controlInfoData;
			}
			try {
				String jsonPath = saveToDirectory(fileName);
				Toast.makeText(getContext(), getContext().getString(R.string.generic_save) + ": " + jsonPath, Toast.LENGTH_SHORT).show();
				if (confirmRunnable != null) confirmRunnable.run();
			} catch (Throwable th) {
				Tools.showError(getContext(), th, true);
			}
			infoDialog.dismiss();
		});
		infoDialog.show();
	}

	public void openSaveDialog() {
		saveDialog(getContext().getString(R.string.generic_save), null);
	}

	public void openSaveAndExitDialog(EditorExitable editorExitable) {
		saveDialog(getContext().getString(R.string.global_save_and_exit), () -> {
			if (editorExitable != null) editorExitable.exitEditor();
		});
	}

	public void openLoadDialog() {
		SelectControlsDialog dialog = new SelectControlsDialog(getContext(), file -> {
			try {
				loadLayout(file.getAbsolutePath());
			} catch (IOException e) {
				Tools.showError(getContext(), e);
			}
		});
		dialog.show();
	}

	public void openSetDefaultDialog() {
		SelectControlsDialog dialog = new SelectControlsDialog(getContext(), file -> {
			String absolutePath = file.getAbsolutePath();
			try {
				AllSettings.getDefaultCtrl().save(absolutePath);
				ControlManager.INSTANCE.selectLayoutByFileName(file.getName());
				loadLayout(absolutePath);
			} catch (IOException | JsonSyntaxException e) {
				Tools.showError(getContext(), e);
			}
		});
		dialog.setTitleText(R.string.customctrl_selectdefault);
		dialog.show();
	}

	public void openExitDialog(EditorExitable exitListener) {
		new TipDialog.Builder(getContext())
				.setTitle(R.string.customctrl_editor_exit_title)
				.setMessage(R.string.customctrl_editor_exit_msg)
				.setConfirmClickListener(checked -> {
					if (exitListener != null) exitListener.exitEditor();
				})
				.showDialog();
	}

	public boolean areControlVisible(){
		return mControlVisible;
	}
}
