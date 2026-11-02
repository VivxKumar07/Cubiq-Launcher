package git.artdeell.mojo;

public class CallbackBridge {
    public static void setModifiers(int keyCode, boolean isDown) {
        org.lwjgl.glfw.CallbackBridge.setModifiers(keyCode, isDown);
    }

    public static int getCurrentMods() {
        return org.lwjgl.glfw.CallbackBridge.getCurrentMods();
    }

    public static void sendKeyPress(int keyCode) {
        org.lwjgl.glfw.CallbackBridge.sendKeyPress(keyCode);
    }

    public static void sendMouseButton(int button, boolean status) {
        org.lwjgl.glfw.CallbackBridge.sendMouseButton(button, status);
    }

    public static void sendMouseKeycode(int button, int modifiers, boolean isDown) {
        org.lwjgl.glfw.CallbackBridge.sendMouseKeycode(button, modifiers, isDown);
    }

    public static void sendScroll(double xoffset, double yoffset) {
        org.lwjgl.glfw.CallbackBridge.sendScroll(xoffset, yoffset);
    }
}
