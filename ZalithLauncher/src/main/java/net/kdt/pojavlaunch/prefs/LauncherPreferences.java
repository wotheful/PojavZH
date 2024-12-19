package net.kdt.pojavlaunch.prefs;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;
import static net.kdt.pojavlaunch.Architecture.is32BitsDevice;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;

import com.movtery.zalithlauncher.feature.log.Logging;
import com.movtery.zalithlauncher.feature.unpack.Jre;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.setting.Settings;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;
import com.movtery.zalithlauncher.utils.path.PathManager;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.utils.JREUtils;

public class LauncherPreferences {
    public static void loadPreferences(Context ctx) {
        //Required for the data folder.
        PathManager.initContextConstants(ctx);

        String argLwjglLibname = "-Dorg.lwjgl.opengl.libname=";
        String javaArgs = AllSettings.getJavaArgs().getValue();
        if (javaArgs != null) {
            for (String arg : JREUtils.parseJavaArguments(javaArgs)) {
                if (arg.startsWith(argLwjglLibname)) {
                    // purge arg
                    AllSettings.getJavaArgs().put(javaArgs.replace(arg, "")).save();
                }
            }
        }

        reloadRuntime();
    }

    public static void reloadRuntime() {
        if (!Settings.Manager.contains("defaultRuntime") && !MultiRTUtils.getRuntimes().isEmpty()) {
            //设置默认运行环境
            AllSettings.getDefaultRuntime().put(Jre.JRE_8.getJreName()).save();
        }
    }

    /**
     * This functions aims at finding the best default RAM amount,
     * according to the RAM amount of the physical device.
     * Put not enough RAM ? Minecraft will lag and crash.
     * Put too much RAM ?
     * The GC will lag, android won't be able to breathe properly.
     * @param ctx Context needed to get the total memory of the device.
     * @return The best default value found.
     */
    public static int findBestRAMAllocation(Context ctx){
        int deviceRam = Tools.getTotalDeviceMemory(ctx);
        if (deviceRam < 1024) return 300;
        if (deviceRam < 1536) return 450;
        if (deviceRam < 2048) return 600;
        // Limit the max for 32 bits devices more harshly
        if (is32BitsDevice()) return 700;

        if (deviceRam < 3064) return 936;
        if (deviceRam < 4096) return 1148;
        if (deviceRam < 6144) return 1536;
        return 2048; //Default RAM allocation for 64 bits
    }

    /** Compute the notch size to avoid being out of bounds */
    public static void computeNotchSize(BaseActivity activity) {
        if (Build.VERSION.SDK_INT < P) return;
        try {
            final Rect cutout;
            if(SDK_INT >= Build.VERSION_CODES.S){
                cutout = activity.getWindowManager().getCurrentWindowMetrics().getWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            } else {
                cutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            }

            // Notch values are rotation sensitive, handle all cases
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) AllStaticSettings.notchSize = cutout.height();
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) AllStaticSettings.notchSize = cutout.width();
            else AllStaticSettings.notchSize = Math.min(cutout.width(), cutout.height());

        }catch (Exception e){
            Logging.i("NOTCH DETECTION", "No notch detected, or the device if in split screen mode");
            AllStaticSettings.notchSize = -1;
        }
        Tools.updateWindowSize(activity);
    }
}
