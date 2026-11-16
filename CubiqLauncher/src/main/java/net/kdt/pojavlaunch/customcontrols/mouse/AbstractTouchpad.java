package net.kdt.pojavlaunch.customcontrols.mouse;

public interface AbstractTouchpad {
    boolean getDisplayState();

    default void applyMotionVector(float[] vector) {
        applyMotionVector(vector[0], vector[1]);
    }

    void applyMotionVector(float x, float y);

    void enable(boolean supposed);

    void disable();
}

