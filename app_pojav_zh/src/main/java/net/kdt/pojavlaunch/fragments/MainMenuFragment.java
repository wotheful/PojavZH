package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.dpToPx;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ADVANCED_FEATURES;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CheckNewNotice;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.ShareLogDialog;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";
    private CheckNewNotice.NoticeInfo noticeInfo;
    private mcVersionSpinner mVersionSpinner;
    private View mLauncherNoticeView, mLauncherMenuView;
    private ImageButton mNoticeSummonButton;
    private Button mNoticeCloseButton;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindValues(view);

        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);
        Button mOpenInstanceDirButton = view.findViewById(R.id.zh_open_instance_dir_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mAboutButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), AboutFragment.class, AboutFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext(), new File(Tools.DIR_GAME_HOME + "/latestlog.txt"));
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, Tools.DIR_GAME_HOME);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenInstanceDirButton.setOnClickListener(v -> {
            String path = PojavZHTools.getGameDirPath(getCurrentProfile().gameDir).getAbsolutePath();
            File file = new File(path);
            if (!file.exists()) file.mkdirs(); //必须保证此路径存在

            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, path);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        initNotice(view);
        mOpenMainDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
        mOpenInstanceDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
    }

    private void initNotice(View view) {
        noticeInfo = CheckNewNotice.getNoticeInfo();

        mNoticeSummonButton.setOnClickListener(v -> {
            DEFAULT_PREF.edit().putBoolean("noticeDefault", true).apply();
            setNotice(true, view);
        });

        mNoticeCloseButton.setOnClickListener(v -> {
            DEFAULT_PREF.edit().putBoolean("noticeDefault", false).apply();
            setNotice(false, view);
        });

        //当偏好设置内是开启通知栏 或者 检测到通知编号不为偏好设置里保存的值时，显示通知栏
        if (DEFAULT_PREF.getBoolean("noticeDefault", false) ||
                (noticeInfo != null &&
                        noticeInfo.getNumbering() != DEFAULT_PREF.getInt("numbering", 0))) {
            setNotice(true, view);
            DEFAULT_PREF.edit().putBoolean("noticeDefault", true).apply();
            if (noticeInfo != null)
                DEFAULT_PREF.edit().putInt("numbering", noticeInfo.getNumbering()).apply();
        }
    }

    private void bindValues(View view) {
        mLauncherNoticeView = view.findViewById(R.id.zh_menu_notice);
        mLauncherMenuView = view.findViewById(R.id.launcher_menu);
        mNoticeSummonButton = view.findViewById(R.id.zh_menu_notice_summon_button);
        mNoticeCloseButton = view.findViewById(R.id.zh_menu_notice_close_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    private void setNotice(boolean show, View view) {
        mNoticeSummonButton.setClickable(false);
        mNoticeCloseButton.setClickable(false);

        if (PREF_ANIMATION) {
            //通知组件动画
            mLauncherNoticeView.setVisibility(View.VISIBLE);
            float extraPx = dpToPx(8);

            if (show) {
                mLauncherNoticeView.setTranslationX(-mLauncherNoticeView.getWidth());
            }

            mLauncherNoticeView.animate()
                    .translationX(show ? 0 : -mLauncherNoticeView.getWidth() - extraPx) //稍微滑出屏幕以确保完全不可见
                    .setDuration(200)
                    .start();

            //召唤按钮动画
            mNoticeSummonButton.setVisibility(View.VISIBLE);
            mNoticeSummonButton.animate()
                    .alpha(show ? 0 : 1)
                    .setDuration(200)
                    .start();

            //主菜单View
            int originalWidth = mLauncherMenuView.getWidth();
            int expandedWidth = originalWidth * 2;

            ValueAnimator animator;
            if (show) {
                animator = ValueAnimator.ofInt(originalWidth, originalWidth / 2);
            } else {
                animator = ValueAnimator.ofInt(originalWidth, expandedWidth);
            }
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                int newValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mLauncherMenuView.getLayoutParams();
                layoutParams.width = newValue;
                mLauncherMenuView.setLayoutParams(layoutParams);
                mLauncherMenuView.requestLayout(); // 触发重新布局
            });
            animator.start();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                mLauncherNoticeView.setVisibility(show ? View.VISIBLE : View.GONE);
                if (show) {
                    checkNewNotice(view);
                }
                mNoticeCloseButton.setClickable(show);
                mNoticeSummonButton.setClickable(!show);
                mNoticeSummonButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }, 250);
        } else {
            mLauncherNoticeView.setVisibility(show ? View.VISIBLE : View.GONE);
            mNoticeSummonButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mNoticeCloseButton.setClickable(show);
            mNoticeSummonButton.setClickable(!show);
            checkNewNotice(view);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkNewNotice(View view) {
        noticeInfo = CheckNewNotice.getNoticeInfo();

        if (noticeInfo == null) {
            return;
        }

        runOnUiThread(() -> {
            //初始化
            TextView noticeTitleView = view.findViewById(R.id.zh_menu_notice_title);
            TextView noticeDateView = view.findViewById(R.id.zh_menu_notice_date);
            TextView noticeSubstanceWebView = view.findViewById(R.id.zh_menu_notice_substance);

            if (!noticeInfo.getRawTitle().equals("NONE")) {
                noticeTitleView.setText(noticeInfo.getRawTitle());
            }

            noticeDateView.setText(noticeInfo.getRawDate());
            noticeSubstanceWebView.setText(noticeInfo.getSubstance());
        });
    }
}
