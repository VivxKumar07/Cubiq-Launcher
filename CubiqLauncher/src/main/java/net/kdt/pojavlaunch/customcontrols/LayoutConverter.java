package net.kdt.pojavlaunch.customcontrols;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.movtery.zalithlauncher.R;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.glfw.CallbackBridge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LayoutConverter {
    public static CustomControls loadAndConvertIfNecessary(Context ctx, String jsonPath) throws IOException, JsonSyntaxException {
        String jsonLayoutData = Tools.read(jsonPath);
        if (jsonLayoutData == null || !jsonLayoutData.contains("mControlDataList")) {
            return null;
        }
        try {
            JSONObject layoutJobj = new JSONObject(jsonLayoutData);
            return loadAndConvertIfNecessary(ctx, layoutJobj, jsonLayoutData, jsonPath, true);
        } catch (Exception e) {
            Tools.showError(ctx, ctx.getString(R.string.controls_load_failed), e);
            return null;
        }
    }

    public static CustomControls loadFromAssets(Context context, String jsonName) {
        String cleanName = jsonName.contains("/") ? jsonName.substring(jsonName.lastIndexOf('/') + 1) : jsonName;
        InputStream is = null;
        try {
            try {
                is = context.getAssets().open(jsonName);
            } catch (Exception e) {
                is = context.getAssets().open(cleanName);
            }
            String string = IOUtils.toString(is, StandardCharsets.UTF_8);
            JSONObject layoutJobj = new JSONObject(string);
            return loadAndConvertIfNecessary(context, layoutJobj, string, null, false);
        } catch (Exception e) {
            try {
                if (is != null) is.close();
                is = context.getAssets().open("zalith_1_layout.json");
                String string = IOUtils.toString(is, StandardCharsets.UTF_8);
                JSONObject layoutJobj = new JSONObject(string);
                return loadAndConvertIfNecessary(context, layoutJobj, string, null, false);
            } catch (Exception e2) {
                try {
                    if (is != null) is.close();
                    is = context.getAssets().open("default.json");
                    String string = IOUtils.toString(is, StandardCharsets.UTF_8);
                    JSONObject layoutJobj = new JSONObject(string);
                    return loadAndConvertIfNecessary(context, layoutJobj, string, null, false);
                } catch (Exception e3) {
                    Tools.showError(context, context.getString(R.string.controls_load_failed), e3);
                    return null;
                }
            }
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
        }
    }

    public static CustomControls loadFromJsonObject(Context ctx, JSONObject layoutJobj, String jsonString, String jsonPath, boolean showError) {
        try {
            return loadAndConvertIfNecessary(ctx, layoutJobj, jsonString, jsonPath, showError);
        } catch (Exception e) {
            if (showError) Tools.showError(ctx, ctx.getString(R.string.controls_load_failed), e);
            return null;
        }
    }

    private static CustomControls loadAndConvertIfNecessary(Context ctx, JSONObject layoutJobj, String jsonLayoutData, String jsonPath, boolean showError) throws Exception {
        if (!layoutJobj.has("version")) { // v1 layout
            if (!layoutJobj.has("mControlDataList")) {
                try {
                    CustomControls direct = Tools.GLOBAL_GSON.fromJson(jsonLayoutData, CustomControls.class);
                    if (direct != null && direct.mControlDataList != null) return direct;
                } catch (Throwable ignored) {}
                return null;
            }
            CustomControls layout = LayoutConverter.convertV1Layout(layoutJobj);
            if (jsonPath != null) layout.save(jsonPath);
            return layout;
        } else {
            int version = layoutJobj.optInt("version", -1);
            if (version == 1) {
                if (!layoutJobj.has("mControlDataList")) return null;
                CustomControls layout = LayoutConverter.convertV1Layout(layoutJobj);
                if (jsonPath != null) layout.save(jsonPath);
                return layout;
            }
            if (version == 2) {
                if (!layoutJobj.has("mControlDataList")) return null;
                CustomControls layout = LayoutConverter.convertV2Layout(layoutJobj);
                if (jsonPath != null) layout.save(jsonPath);
                return layout;
            }
            if (version == 3 || version == 4 || version == 5) {
                return LayoutConverter.convertV3_4Layout(layoutJobj);
            }
            if (version == 6 || version == 7) {
                return convertV6_7Layout(layoutJobj);
            }
            if (version == 8) {
                return Tools.GLOBAL_GSON.fromJson(jsonLayoutData, CustomControls.class);
            }
            try {
                CustomControls direct = Tools.GLOBAL_GSON.fromJson(jsonLayoutData, CustomControls.class);
                if (direct != null && direct.mControlDataList != null) return direct;
            } catch (Throwable ignored) {}

            if (showError) {
                IOException ioException = new IOException();
                Tools.showError(ctx, ctx.getString(R.string.controls_unsupported_layout_version), ioException);
            }
            return null;
        }
    }

    /**
     * Normalize the layout to v8 from v6/7. An issue from the joystick height and position has to be fixed.
     * @param oldLayoutJson The old layout
     * @return The new layout with the fixed joystick height
     */
    public static CustomControls convertV6_7Layout(JSONObject oldLayoutJson) {
        CustomControls layout = Tools.GLOBAL_GSON.fromJson(oldLayoutJson.toString(), CustomControls.class);
        if (layout != null && layout.mJoystickDataList != null) {
            for (ControlJoystickData data : layout.mJoystickDataList) {
                if (data.getHeight() > data.getWidth()) {
                    float ratio = data.getHeight() / data.getWidth();
                    data.dynamicX = data.dynamicX.replace("${height}", "(" + ratio + " * ${height})");
                    data.dynamicY = data.dynamicY.replace("${height}", "(" + ratio + " * ${height})") +  " + (" + (ratio-1) + " * ${height})";
                    data.setHeight(data.getWidth());
                }
            }
            layout.version = 8;
        }
        return layout;
    }

    /**
     * Normalize the layout to v6 from v3/4: The stroke width is no longer dependant on the button size
     */
    private static CustomControls convertV3_4Layout(JSONObject oldLayoutJson) {
        CustomControls layout = Tools.GLOBAL_GSON.fromJson(oldLayoutJson.toString(), CustomControls.class);
        if (layout != null) {
            convertStrokeWidth(layout);
            layout.version = 6;
        }
        return layout;
    }

    private static CustomControls convertV2Layout(JSONObject oldLayoutJson) throws JSONException {
        CustomControls layout = null;
        try {
            layout = Tools.GLOBAL_GSON.fromJson(oldLayoutJson.toString(), CustomControls.class);
        } catch (Throwable ignored) {}
        if (layout == null) layout = new CustomControls();

        JSONArray layoutMainArray = oldLayoutJson.optJSONArray("mControlDataList");
        if (layoutMainArray != null) {
            layout.mControlDataList = new ArrayList<>(layoutMainArray.length());
            for (int i = 0; i < layoutMainArray.length(); i++) {
                JSONObject button = layoutMainArray.optJSONObject(i);
                if (button == null) continue;
                ControlData n_button = null;
                try {
                    n_button = Tools.GLOBAL_GSON.fromJson(button.toString(), ControlData.class);
                } catch (Throwable ignored) {}
                if (n_button == null) n_button = new ControlData();
                if (!Tools.isValidString(n_button.dynamicX) && button.has("x")) {
                    double buttonC = button.optDouble("x", 0);
                    double ratio = buttonC / (CallbackBridge.physicalWidth > 0 ? CallbackBridge.physicalWidth : 1);
                    n_button.dynamicX = ratio + " * ${screen_width}";
                }
                if (!Tools.isValidString(n_button.dynamicY) && button.has("y")) {
                    double buttonC = button.optDouble("y", 0);
                    double ratio = buttonC / (CallbackBridge.physicalHeight > 0 ? CallbackBridge.physicalHeight : 1);
                    n_button.dynamicY = ratio + " * ${screen_height}";
                }
                layout.mControlDataList.add(n_button);
            }
        }
        JSONArray layoutDrawerArray = oldLayoutJson.optJSONArray("mDrawerDataList");
        if (layoutDrawerArray != null) {
            layout.mDrawerDataList = new ArrayList<>();
            for (int i = 0; i < layoutDrawerArray.length(); i++) {
                JSONObject button = layoutDrawerArray.optJSONObject(i);
                if (button == null) continue;
                JSONObject buttonProperties = button.optJSONObject("properties");
                ControlDrawerData n_button = null;
                try {
                    n_button = Tools.GLOBAL_GSON.fromJson(button.toString(), ControlDrawerData.class);
                } catch (Throwable ignored) {}
                if (n_button != null) {
                    if (buttonProperties != null && n_button.properties != null) {
                        if (!Tools.isValidString(n_button.properties.dynamicX) && buttonProperties.has("x")) {
                            double buttonC = buttonProperties.optDouble("x", 0);
                            double ratio = buttonC / (CallbackBridge.physicalWidth > 0 ? CallbackBridge.physicalWidth : 1);
                            n_button.properties.dynamicX = ratio + " * ${screen_width}";
                        }
                        if (!Tools.isValidString(n_button.properties.dynamicY) && buttonProperties.has("y")) {
                            double buttonC = buttonProperties.optDouble("y", 0);
                            double ratio = buttonC / (CallbackBridge.physicalHeight > 0 ? CallbackBridge.physicalHeight : 1);
                            n_button.properties.dynamicY = ratio + " * ${screen_height}";
                        }
                    }
                    layout.mDrawerDataList.add(n_button);
                }
            }
        }
        convertStrokeWidth(layout);
        layout.version = 3;
        return layout;
    }

    private static CustomControls convertV1Layout(JSONObject oldLayoutJson) throws JSONException {
        CustomControls empty = new CustomControls();
        JSONArray layoutMainArray = oldLayoutJson.optJSONArray("mControlDataList");
        if (layoutMainArray == null) return empty;
        empty.mControlDataList = new ArrayList<>(layoutMainArray.length());
        for (int i = 0; i < layoutMainArray.length(); i++) {
            JSONObject button = layoutMainArray.optJSONObject(i);
            if (button == null) continue;
            ControlData n_button = new ControlData();
            int[] keycodes = new int[]{LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN};
            n_button.dynamicX = button.optString("dynamicX", "0");
            n_button.dynamicY = button.optString("dynamicY", "0");
            if (!Tools.isValidString(n_button.dynamicX) && button.has("x")) {
                double buttonC = button.optDouble("x", 0);
                double ratio = buttonC / (CallbackBridge.physicalWidth > 0 ? CallbackBridge.physicalWidth : 1);
                n_button.dynamicX = ratio + " * ${screen_width}";
            }
            if (!Tools.isValidString(n_button.dynamicY) && button.has("y")) {
                double buttonC = button.optDouble("y", 0);
                double ratio = buttonC / (CallbackBridge.physicalHeight > 0 ? CallbackBridge.physicalHeight : 1);
                n_button.dynamicY = ratio + " * ${screen_height}";
            }
            n_button.name = button.optString("name", "Button");
            n_button.opacity = ((float) ((button.optInt("transparency", 0) - 100) * -1)) / 100f;
            n_button.passThruEnabled = button.optBoolean("passThruEnabled", false);
            n_button.isToggle = button.optBoolean("isToggle", false);
            n_button.setHeight(button.optInt("height", 50));
            n_button.setWidth(button.optInt("width", 50));
            n_button.bgColor = 0x4d000000;
            n_button.strokeWidth = 0;
            if (button.optBoolean("isRound", false)) {
                n_button.cornerRadius = 35f;
            }
            int next_idx = 0;
            if (button.optBoolean("holdShift", false)) {
                keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
                next_idx++;
            }
            if (button.optBoolean("holdCtrl", false)) {
                keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
                next_idx++;
            }
            if (button.optBoolean("holdAlt", false)) {
                keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
                next_idx++;
            }
            keycodes[next_idx] = button.optInt("keycode", 0);
            n_button.keycodes = keycodes;
            empty.mControlDataList.add(n_button);
        }
        empty.scaledAt = (float) oldLayoutJson.optDouble("scaledAt", 100.0);
        empty.version = 3;
        return empty;
    }

    /**
     * Convert the layout stroke width to the V5 form
     */
    private static void convertStrokeWidth(CustomControls layout) {
        for (ControlData data : layout.mControlDataList) {
            data.strokeWidth = Tools.pxToDp(computeStrokeWidth(data.strokeWidth, data.getWidth(), data.getHeight()));
        }

        for (ControlDrawerData data : layout.mDrawerDataList) {
            data.properties.strokeWidth = Tools.pxToDp(computeStrokeWidth(data.properties.strokeWidth, data.properties.getWidth(), data.properties.getHeight()));
            for (ControlData subButtonData : data.buttonProperties) {
                subButtonData.strokeWidth = Tools.pxToDp(computeStrokeWidth(subButtonData.strokeWidth, data.properties.getWidth(), data.properties.getWidth()));
            }
        }
    }

    /**
     * Convert a size percentage into a px size, used by older layout versions
     */
    static int computeStrokeWidth(float widthInPercent, float width, float height) {
        float maxSize = Math.max(width, height);
        return (int) ((maxSize / 2) * (widthInPercent / 100));
    }
}
