package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.pojavzh.ui.subassembly.view.DraggableView;

public abstract class DraggableDialog extends Dialog {
    public DraggableDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public DraggableDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    public DraggableDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        Window window = getWindow();
        if (window != null) {
            View contentView = window.findViewById(android.R.id.content);
            if (contentView != null) {
                DraggableView draggableView = new DraggableView(contentView, new DraggableView.AttributesFetcher() {
                    @Override
                    public int[] getScreenPixels() {
                        int width = (currentDisplayMetrics.widthPixels - contentView.getWidth()) / 2;
                        int height = (currentDisplayMetrics.heightPixels - contentView.getHeight()) / 2;
                        return new int[]{width, height};
                    }

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

                draggableView.init();
            } else {
                Log.w("DraggableDialog", "The content view does not exist!");
            }
        }
    }
}
