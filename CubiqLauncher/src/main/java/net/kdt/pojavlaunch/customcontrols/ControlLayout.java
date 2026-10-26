package net.kdt.pojavlaunch.customcontrols;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static org.lwjgl.glfw.CallbackBridge.isGrabbing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.movtery.zalithlauncher.R;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.widget.AppCompatButton;

import android.util.Log;
import com.movtery.zalithlauncher.setting.AllSettings;





import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlInfoData;
import com.movtery.zalithlauncher.path.PathManager;



import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.LwjglGlfwKeycode;
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
	private View mGameSurface = null;
	private ControlActionListener mActionListener = null;

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
		File jsonFile = jsonPath != null ? new File(jsonPath) : new File(PathManager.DIR_CONTROL_LAYOUTS, "zalith_1_layout.json");

		CustomControls layout = null;
		if (jsonFile.exists()) {
			try {
				String content = Tools.read(jsonFile.getAbsolutePath());
				if (content != null && content.contains("mControlDataList")) {
					layout = LayoutConverter.loadAndConvertIfNecessary(getContext(), jsonFile.getAbsolutePath());
				}
			} catch (Throwable ignored) {}
		}
		if (layout == null) {
			layout = LayoutConverter.loadFromAssets(getContext(), "zalith_1_layout.json");
			if (layout != null && jsonFile != null) {
				try {
					layout.save(jsonFile.getAbsolutePath());
				} catch (Throwable ignored) {}
			}
		}
		if (layout != null) {
			loadLayout(layout);
			String n = jsonFile.getName();
			mLayoutFileName = n.endsWith(".json") ? n.substring(0, n.length() - 5) : n;
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
		for(ControlJoystickData joystick : mLayout.mJoystickDataList){
			addJoystickView(joystick);
		}

		//CONTROL BUTTON
		for (ControlData button : controlLayout.mControlDataList) {
			addControlView(button);
		}

		//CONTROL DRAWER
		for(ControlDrawerData drawerData : controlLayout.mDrawerDataList){
			ControlDrawer drawer = addDrawerView(drawerData);
			if(mModifiable) drawer.areButtonsVisible = true;
		}

		mLayout.scaledAt = 100f;

		setModified(sanitizedModified);
		mButtons = null;
		getButtonChildren(); // Force refresh
	} // loadLayout

	//CONTROL BUTTON
	public void addControlButton(ControlData controlButton) {
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
		mLayout.mDrawerDataList.add(drawerData);
		addDrawerView();
	}

	private void addDrawerView(){
		addDrawerView(null);
	}

	private ControlDrawer addDrawerView(ControlDrawerData drawerData){

		final ControlDrawer view = new ControlDrawer(this,drawerData == null ? mLayout.mDrawerDataList.get(mLayout.mDrawerDataList.size()-1) : drawerData);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);
		//CONTROL SUB BUTTON
		for (ControlData subButton : view.getDrawerData().buttonProperties) {
			addSubView(view, subButton);
		}

		setModified(true);
		return view;
	}

	//CONTROL SUB-BUTTON
	public void addSubButton(ControlDrawer drawer, ControlData controlButton){
		//Yep there isn't much here
		drawer.getDrawerData().buttonProperties.add(controlButton);
		addSubView(drawer, drawer.getDrawerData().buttonProperties.get(drawer.getDrawerData().buttonProperties.size()-1 ));
	}

	private void addSubView(ControlDrawer drawer, ControlData controlButton){
		final ControlSubButton view = new ControlSubButton(this, controlButton, drawer);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}else{
			view.setVisible(true);
		}

		addView(view);
		drawer.addButton(view);


		setModified(true);
	}

	// JOYSTICK BUTTON
	public void addJoystickButton(ControlJoystickData data){
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
		//i wanna be sure that all the removed Views will be removed after a reload
		//because if frames will slowly go down after many control changes it will be warm and bad
	}

	public void saveLayout(String path) throws Exception {
		mLayout.save(path);
		setModified(false);
	}

	public void toggleControlVisible(){
		mControlVisible = !mControlVisible;
		setControlVisible(mControlVisible);
	}

	public float getLayoutScale(){
		return mLayout.scaledAt;
	}

	public CustomControls getLayout(){
		return mLayout;
	}

	public void setControlVisible(boolean isVisible) {
		if (mModifiable) return; // Not using on custom controls activity

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
			// In edit mode, all controls have to be shown
			for(ControlInterface button : getButtonChildren()){
				button.setVisible(true);
			}
		}
	}

	public boolean getModifiable(){
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

	public void refreshControlButtonPositions(){
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

    /**
	 * Load the layout if needed, and pass down the burden of filling values
	 * to the button at hand.
	 */
	public void editControlButton(ControlInterface button){
		if(mControlPopup == null){
			// When the panel is null, it needs to inflate first.
			// So inflate it, then process it on the next frame
			mControlPopup = new EditControlPopup(getContext(), this);
			post(() -> editControlButton(button));
			return;
		}

		mControlPopup.internalChanges = true;
		mControlPopup.setCurrentlyEditedButton(button);

		mControlPopup.appear(button.getControlView().getX() + button.getControlView().getWidth()/2f < currentDisplayMetrics.widthPixels/2f);
		button.loadEditValues(mControlPopup);

		mControlPopup.internalChanges = false;
		mControlPopup.disappearColor();

		if(mHandleView == null){
			mHandleView = new ControlHandleView(getContext());
			addView(mHandleView);
		}
		mHandleView.setControlButton(button);

		//mHandleView.show();
	}

	/** Swap the panel if the button position requires it */
	public void adaptPanelPosition(){
		if(mControlPopup != null) mControlPopup.adaptPanelPosition();
	}


	final HashMap<View, ControlInterface> mapTable = new HashMap<>();

	//While this is called onTouch, this should only be called from a ControlButton.
	public void onTouch(View v, MotionEvent ev) {
		ControlInterface lastControlButton = mapTable.get(v);

		// Map location to screen coordinates
		ev.offsetLocation(v.getX(), v.getY());


		//Check if the action is cancelling, reset the lastControl button associated to the view
		if (ev.getActionMasked() == MotionEvent.ACTION_UP
				|| ev.getActionMasked() == MotionEvent.ACTION_CANCEL
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
			mapTable.put(v, null);
			return;
		}

		if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) return;


		//Optimization pass to avoid looking at all children again
		if (lastControlButton != null) {
			System.out.println("last control button check" + ev.getX() + "-" + ev.getY() + "-" + lastControlButton.getControlView().getX() + "-" + lastControlButton.getControlView().getY());
			if (ev.getX() > lastControlButton.getControlView().getX()
					&& ev.getX() < lastControlButton.getControlView().getX() + lastControlButton.getControlView().getWidth()
					&& ev.getY() > lastControlButton.getControlView().getY()
					&& ev.getY() < lastControlButton.getControlView().getY() + lastControlButton.getControlView().getHeight()) {
				return;
			}
		}

		//Release last keys
		if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
		mapTable.remove(v);

		// Update the state of all swipeable buttons
		for (ControlInterface button : getButtonChildren()) {
			if (!button.getProperties().isSwipeable) continue;

			if (ev.getX() > button.getControlView().getX()
					&& ev.getX() < button.getControlView().getX() + button.getControlView().getWidth()
					&& ev.getY() > button.getControlView().getY()
					&& ev.getY() < button.getControlView().getY() + button.getControlView().getHeight()) {

				//Press the new key
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
		if (event.getActionMasked() != MotionEvent.ACTION_UP || mControlPopup == null)
			return true;

		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		// When the input window cannot be hidden, it returns false
		if(!imm.hideSoftInputFromWindow(getWindowToken(), 0)){
			if(mControlPopup.disappearLayer()){
				mActionRow.setFollowedButton(null);
				mHandleView.hide();
			}
		}
		return true;
	}

	public void removeEditWindow() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		// When the input window cannot be hidden, it returns false
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
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
			Log.e("ControlLayout", "Failed to save the layout at:" + path);}
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

	/** Cached getter for perf purposes */
	public void setActionListener(ControlActionListener listener) {
		this.mActionListener = listener;
	}

	public ControlActionListener getActionListener() {
		return mActionListener;
	}

	public void setGameSurface(View surface) {
		this.mGameSurface = surface;
	}

	public View getGameSurface(){
		return mGameSurface;
	}

	public void askToExit(EditorExitable editorExitable) {
		if(mIsModified) {
			openSaveAndExitDialog(editorExitable);
		}else{
			openExitDialog(editorExitable);
		}
	}

	public String saveToDirectory(String name) throws Exception{
		String fileName = name.endsWith(".json") ? name : name + ".json";
		String jsonPath = new File(PathManager.DIR_CONTROL_LAYOUTS, fileName).getAbsolutePath();
		saveLayout(jsonPath);
		return jsonPath;
	}

	private void showMinecraftAlert(String title, String message, String cancelText, String confirmText, boolean isRedConfirm, Runnable onConfirm) {
		Dialog dialog = new Dialog(getContext());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_minecraft_alert, null);
		TextView titleTv = view.findViewById(R.id.dialog_title);
		TextView msgTv = view.findViewById(R.id.dialog_message);
		AppCompatButton btnCancel = view.findViewById(R.id.dialog_btn_cancel);
		AppCompatButton btnConfirm = view.findViewById(R.id.dialog_btn_confirm);

		if (title != null) titleTv.setText(title);
		if (message != null) msgTv.setText(message);
		if (cancelText != null) btnCancel.setText(cancelText);
		if (confirmText != null) btnConfirm.setText(confirmText);
		btnCancel.setSupportBackgroundTintList(null);
		btnConfirm.setSupportBackgroundTintList(null);
		if (isRedConfirm) {
			btnConfirm.setBackgroundResource(R.drawable.btn_minecraft_red);
		} else {
			btnConfirm.setBackgroundResource(R.drawable.btn_minecraft_green);
		}

		btnCancel.setOnClickListener(v -> dialog.dismiss());
		btnConfirm.setOnClickListener(v -> {
			dialog.dismiss();
			if (onConfirm != null) onConfirm.run();
		});

		dialog.setContentView(view);
		dialog.show();
	}

	public void saveDialog(String title, Runnable confirmTask) {
		Dialog dialog = new Dialog(getContext());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_minecraft_save, null);
		TextView titleTv = view.findViewById(R.id.dialog_title);
		EditText input = view.findViewById(R.id.dialog_input);
		AppCompatButton btnCancel = view.findViewById(R.id.dialog_btn_cancel);
		AppCompatButton btnSave = view.findViewById(R.id.dialog_btn_save);

		btnCancel.setSupportBackgroundTintList(null);
		btnSave.setSupportBackgroundTintList(null);
		if (title != null) titleTv.setText(title);
		input.setText(mLayoutFileName != null ? mLayoutFileName : "zalith_1_layout");

		btnCancel.setOnClickListener(v -> dialog.dismiss());
		btnSave.setOnClickListener(v -> {
			String name = input.getText().toString().trim();
			if (!name.isEmpty()) {
				try {
					mLayoutFileName = name;
					String jsonPath = saveToDirectory(name);
					Toast.makeText(getContext(), getContext().getString(R.string.generic_save) + ": " + jsonPath, Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					if (confirmTask != null) confirmTask.run();
				} catch (Throwable th) {
					Tools.showError(getContext(), th, true);
				}
			}
		});

		dialog.setContentView(view);
		dialog.show();
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
		File[] files = PathManager.DIR_CONTROL_LAYOUTS.listFiles((dir, name) -> name.endsWith(".json"));
		if (files == null || files.length == 0) {
			Toast.makeText(getContext(), R.string.controls_load_failed, Toast.LENGTH_SHORT).show();
			return;
		}

		List<File> validFiles = new ArrayList<>();
		for (File f : files) {
			try {
				String c = Tools.read(f.getAbsolutePath());
				if (c != null && c.contains("mControlDataList")) {
					validFiles.add(f);
				}
			} catch (Throwable ignored) {}
		}

		if (validFiles.isEmpty()) {
			Toast.makeText(getContext(), R.string.controls_load_failed, Toast.LENGTH_SHORT).show();
			return;
		}

		Dialog dialog = new Dialog(getContext());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_minecraft_load, null);
		LinearLayout listContainer = view.findViewById(R.id.dialog_load_list);
		AppCompatButton btnCancel = view.findViewById(R.id.dialog_btn_cancel);

		btnCancel.setSupportBackgroundTintList(null);
		for (File f : validFiles) {
			AppCompatButton btn = new AppCompatButton(getContext());
			btn.setSupportBackgroundTintList(null);
			btn.setBackgroundResource(R.drawable.btn_minecraft_stone);
			btn.setTextColor(Color.WHITE);
			btn.setTextSize(13);
			btn.setText(f.getName());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) Tools.dpToPx(38)
			);
			lp.topMargin = (int) Tools.dpToPx(6);
			btn.setLayoutParams(lp);
			btn.setOnClickListener(v -> {
				dialog.dismiss();
				try {
					loadLayout(f.getAbsolutePath());
				} catch (Throwable e) {
					Tools.showError(getContext(), e);
				}
			});
			listContainer.addView(btn);
		}

		btnCancel.setOnClickListener(v -> dialog.dismiss());
		dialog.setContentView(view);
		dialog.show();
	}

	public void openSetDefaultDialog() {
		openLoadDialog();
	}

	public void openExitDialog(EditorExitable exitListener) {
		showMinecraftAlert(
			getContext().getString(R.string.customctrl_editor_exit_title),
			getContext().getString(R.string.customctrl_editor_exit_msg),
			getContext().getString(R.string.generic_cancel),
			getContext().getString(android.R.string.ok),
			true,
			() -> {
				if (exitListener != null) exitListener.exitEditor();
			}
		);
	}

	public void releaseAllKeys() {
		for (ControlInterface button : getButtonChildren()) {
			button.sendKeyPresses(false);
		}
	}


	private boolean mPreviewMode = false;

	public void togglePreview() {
		mPreviewMode = !mPreviewMode;
		if (mPreviewMode) {
			if (mControlPopup != null) {
				mControlPopup.disappear();
			}
			if (mHandleView != null) {
				mHandleView.setVisibility(View.GONE);
			}
			if (mActionRow != null) {
				mActionRow.setVisibility(View.GONE);
			}
			Toast.makeText(getContext(), "Preview Mode: ON (tap menu to exit)", Toast.LENGTH_SHORT).show();
		} else {
			if (mHandleView != null) {
				mHandleView.setVisibility(View.VISIBLE);
			}
			if (mActionRow != null) {
				mActionRow.setVisibility(View.VISIBLE);
			}
			Toast.makeText(getContext(), "Preview Mode: OFF", Toast.LENGTH_SHORT).show();
		}
	}

	public boolean isPreviewMode() {
		return mPreviewMode;
	}

	public void mirrorLayout() {
		int screenWidth = getWidth();
		if (screenWidth <= 0) screenWidth = Tools.currentDisplayMetrics.widthPixels;

		for (ControlInterface control : getButtonChildren()) {
			View v = control.getControlView();
			ControlData data = control.getProperties();
			if (data == null || v == null) continue;

			float curX = v.getX();
			float width = v.getWidth();
			float newX = screenWidth - curX - width;
			if (newX < 0) newX = 0;

			v.setX(newX);
			control.regenerateDynamicCoordinates();
		}
		setModified(true);
		refreshControlButtonPositions();
		Toast.makeText(getContext(), R.string.customctrl_mirror_layout, Toast.LENGTH_SHORT).show();
	}

	public void switchMovement() {
		if (mLayout == null) return;
		if (mLayout.mJoystickDataList != null && !mLayout.mJoystickDataList.isEmpty()) {
			mLayout.mJoystickDataList.remove(0);
			for (ControlInterface ci : new ArrayList<>(getButtonChildren())) {
				if (ci instanceof ControlJoystick) {
					removeView(ci.getControlView());
					
				}
			}
			float cx = Tools.currentDisplayMetrics.widthPixels * 0.15f;
			float cy = Tools.currentDisplayMetrics.heightPixels * 0.65f;
			float btnSize = Tools.dpToPx(50);

			addControlButton(new ControlData("W", new int[]{LwjglGlfwKeycode.GLFW_KEY_W}, cx + " * 1.0", (cy - btnSize * 1.15f) + " * 1.0", btnSize, btnSize, false));
			addControlButton(new ControlData("S", new int[]{LwjglGlfwKeycode.GLFW_KEY_S}, cx + " * 1.0", (cy + btnSize * 1.15f) + " * 1.0", btnSize, btnSize, false));
			addControlButton(new ControlData("A", new int[]{LwjglGlfwKeycode.GLFW_KEY_A}, (cx - btnSize * 1.15f) + " * 1.0", cy + " * 1.0", btnSize, btnSize, false));
			addControlButton(new ControlData("D", new int[]{LwjglGlfwKeycode.GLFW_KEY_D}, (cx + btnSize * 1.15f) + " * 1.0", cy + " * 1.0", btnSize, btnSize, false));
		} else {
			for (ControlInterface ci : new ArrayList<>(getButtonChildren())) {
				if (ci instanceof ControlButton) {
					String name = ((ControlButton) ci).getProperties().name;
					if ("W".equalsIgnoreCase(name) || "S".equalsIgnoreCase(name) || "A".equalsIgnoreCase(name) || "D".equalsIgnoreCase(name)) {
						((ControlButton) ci).removeButton();
					}
				}
			}
			ControlJoystickData joy = new ControlJoystickData();
			joy.dynamicX = "0.15 * ${screen_width}";
			joy.dynamicY = "0.65 * ${screen_height}";
			addJoystickButton(joy);
		}
		setModified(true);
		refreshControlButtonPositions();
		Toast.makeText(getContext(), R.string.customctrl_switch_movement, Toast.LENGTH_SHORT).show();
	}

	public void resetToDefault() {
		try {
			loadLayout((String) null);
			setModified(true);
			Toast.makeText(getContext(), R.string.customctrl_reset_default, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Tools.showError(getContext(), e);
		}
	}

	public boolean areControlVisible(){
		return mControlVisible;
	}
}