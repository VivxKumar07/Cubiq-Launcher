package git.artdeell.mojo.game.platform;

import git.artdeell.mojo.game.platform.backend.PlatformBackend;

public class Platform {
    public static double cursorX;
    public static double cursorY;

    public static PlatformBackend PLATFORM = new PlatformBackend() {
        @Override public void surfaceCreated(android.view.Surface surface) {}
        @Override public void surfaceUpdated() {}
        @Override public void surfaceDestroyed() {}

        @Override
        public void sendMousePosition(double x, double y, boolean relative) {
            cursorX = x;
            cursorY = y;
            if (relative) {
                org.lwjgl.glfw.CallbackBridge.sendCursorDelta((float) x, (float) y);
            } else {
                org.lwjgl.glfw.CallbackBridge.sendCursorPos((float) x, (float) y);
            }
        }

        @Override
        public void sendMouseButtonEvent(int button, int state, int mods) {
            org.lwjgl.glfw.CallbackBridge.sendMouseKeycode(button, mods, state == 1);
        }

        @Override
        public boolean sendKeyEvent(int key, int state, int mods, char codepoint) {
            org.lwjgl.glfw.CallbackBridge.sendKeyPress(key, codepoint, 0, mods, state == 1);
            return true;
        }

        @Override
        public boolean sendKeyEvent(int key, int state, int mods) {
            org.lwjgl.glfw.CallbackBridge.sendKeyPress(key, mods, state == 1);
            return true;
        }

        @Override
        public boolean sendKeyEvent(int key, boolean state, int mods) {
            org.lwjgl.glfw.CallbackBridge.sendKeyPress(key, mods, state);
            return true;
        }

        @Override
        public void sendScrollEvent(double x, double y) {
            org.lwjgl.glfw.CallbackBridge.sendScroll(x, y);
        }

        @Override
        public void sendBulkUnicodeEvent(String text, int mods) {
            if (text != null) {
                for (char c : text.toCharArray()) {
                    org.lwjgl.glfw.CallbackBridge.sendChar(c, mods);
                }
            }
        }

        @Override public String backendName() { return "CubiqBridge"; }
        @Override public void setHovered(boolean hovered) {}
        @Override public void setVisible(boolean visible) {}
    };

    public static void sendMouseButtonEvent(int button, int state, int mods) {
        PLATFORM.sendMouseButtonEvent(button, state, mods);
    }

    public static void sendCursorPosition(double x, double y) {
        PLATFORM.sendMousePosition(x, y, false);
    }

    public static void resetCursorPosition() {}

        public static void addGrabListener(git.artdeell.mojo.game.platform.input.PlatformGrabListener pgl) {
        org.lwjgl.glfw.CallbackBridge.addGrabListener(pgl::onGrabState);
    }

    public static boolean isGrabbing() {
        return org.lwjgl.glfw.CallbackBridge.isGrabbing();
    }
}
