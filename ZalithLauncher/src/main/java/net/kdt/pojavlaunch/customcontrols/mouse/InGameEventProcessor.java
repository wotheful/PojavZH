package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.support.touch_controller.ContactHandler;

import org.lwjgl.glfw.CallbackBridge;

public class InGameEventProcessor implements TouchEventProcessor {
    private final Handler mGestureHandler = new Handler(Looper.getMainLooper());
    private final double mSensitivity;
    private boolean mEventTransitioned = true;
    private final PointerTracker mTracker = new PointerTracker();
    private final LeftClickGesture mLeftClickGesture = new LeftClickGesture(mGestureHandler);
    private final RightClickGesture mRightClickGesture = new RightClickGesture(mGestureHandler);
    private final ContactHandler mContactHandler;

    public InGameEventProcessor(double sensitivity) {
        mSensitivity = sensitivity;
        mContactHandler = AllSettings.getUseControllerProxy().getValue() ? ContactHandler.INSTANCE : null;
    }

    @Override
    public boolean processTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTracker.startTracking(motionEvent);
                if(AllSettings.getDisableGestures().getValue()) break;
                mEventTransitioned = false;
                checkGestures();
                break;
            case MotionEvent.ACTION_MOVE:
                mTracker.trackEvent(motionEvent);
                float[] motionVector = mTracker.getMotionVector();
                float deltaX = (float) (motionVector[0] * mSensitivity);
                float deltaY = (float) (motionVector[1] * mSensitivity);
                mLeftClickGesture.setMotion(deltaX, deltaY);
                mRightClickGesture.setMotion(deltaX, deltaY);
                CallbackBridge.mouseX += deltaX;
                CallbackBridge.mouseY += deltaY;
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                if(AllSettings.getDisableGestures().getValue()) break;
                checkGestures();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.cancelTracking();
                cancelGestures(false);
        }
        return true;
    }

    @Override
    public void cancelPendingActions() {
        cancelGestures(true);
    }

    @Override
    public void dispatchTouchEvent(MotionEvent event, View view) {
        if (mContactHandler != null) {
            //单独处理触摸事件，支持TouchController模组
            mContactHandler.progressEvent(event, view);
        }
    }

    private void checkGestures() {
        mLeftClickGesture.inputEvent();
        // Only register right click events if it's a fresh event stream, not one after a transition.
        // This is done to avoid problems when people hold the button for just a bit too long after
        // exiting a menu for example.
        if(!mEventTransitioned) mRightClickGesture.inputEvent();
    }

    private void cancelGestures(boolean isSwitching) {
        mEventTransitioned = true;
        mLeftClickGesture.cancel(isSwitching);
        mRightClickGesture.cancel(isSwitching);
    }
}
