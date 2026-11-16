package com.movtery.zalithlauncher.ui.dialog;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.movtery.zalithlauncher.ui.subassembly.view.DraggableViewWrapper;
import com.movtery.zalithlauncher.utils.logging.Logger;

public final class DraggableDialog {
    private DraggableDialog() {}

    public static void initDialog(DialogInitializationListener listener) {
        Window window = listener.onInit();
        if (window != null) {
            View contentView = window.findViewById(android.R.id.content);
            if (contentView != null) {
                DraggableViewWrapper draggableViewWrapper = new DraggableViewWrapper(contentView, new DraggableViewWrapper.AttributesFetcher() {
                    @NonNull
                    @Override
                    public DraggableViewWrapper.ScreenPixels getScreenPixels() {
                        int dmW = currentDisplayMetrics != null ? currentDisplayMetrics.widthPixels : contentView.getResources().getDisplayMetrics().widthPixels;
                        int dmH = currentDisplayMetrics != null ? currentDisplayMetrics.heightPixels : contentView.getResources().getDisplayMetrics().heightPixels;
                        int width = (dmW - contentView.getWidth()) / 2;
                        int height = (dmH - contentView.getHeight()) / 2;
                        return new DraggableViewWrapper.ScreenPixels(-width, -height, width, height);
                    }

                    @NonNull
                    @Override
                    public int[] get() {
                        WindowManager.LayoutParams attributes = window.getAttributes();
                        return new int[]{attributes.x, attributes.y};
                    }

                    @Override
                    public void set(int x, int y) {
                        WindowManager.LayoutParams attributes = window.getAttributes();
                        attributes.x = x;
                        attributes.y = y;
                        window.setAttributes(attributes);
                    }
                });

                draggableViewWrapper.init();
            } else {
                Logger.INSTANCE.warning("DraggableDialog", "The content view does not exist!", null);
            }
        }
    }

    public interface DialogInitializationListener {
        Window onInit();
    }
}

