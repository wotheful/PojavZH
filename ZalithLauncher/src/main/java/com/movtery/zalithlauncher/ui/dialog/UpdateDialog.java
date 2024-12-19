package com.movtery.zalithlauncher.ui.dialog;

import static com.movtery.zalithlauncher.utils.stringutils.StringUtils.markdownToHtml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.DialogUpdateBinding;
import com.movtery.zalithlauncher.feature.update.LauncherVersion;
import com.movtery.zalithlauncher.feature.update.UpdateLauncher;
import com.movtery.zalithlauncher.feature.update.UpdateUtils;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.task.TaskExecutors;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.file.FileTools;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;

public class UpdateDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogUpdateBinding binding = DialogUpdateBinding.inflate(getLayoutInflater());
    private final LauncherVersion launcherVersion;

    public UpdateDialog(@NonNull Context context, LauncherVersion launcherVersion) {
        super(context);
        this.launcherVersion = launcherVersion;

        this.setCancelable(false);
        this.setContentView(binding.getRoot());
        init();
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        String versionString = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_version), launcherVersion.getVersionName());
        String timeString = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_time), StringUtils.formattingTime(launcherVersion.getPublishedAt()));
        String sizeString = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_file_size), FileTools.formatFileSize(UpdateUtils.getFileSize(launcherVersion.getFileSize())));
        String versionType = StringUtils.insertSpace(getContext().getString(R.string.about_version_status), getVersionType());

        binding.versionName.setText(versionString);
        binding.updateTime.setText(timeString);
        binding.fileSize.setText(sizeString);
        binding.versionType.setText(versionType);

        String descriptionHtml = markdownToHtml(getLanguageText(launcherVersion.getDescription()));

        ZHTools.getWebViewAfterProcessing(binding.description);

        binding.description.getSettings().setJavaScriptEnabled(true);
        binding.description.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        binding.updateButton.setOnClickListener(view -> {
            this.dismiss();
            if (ZHTools.areaChecks("zh")) {
                TaskExecutors.runInUIThread(() -> {
                    UpdateSourceDialog updateSourceDialog = new UpdateSourceDialog(getContext(), launcherVersion);
                    updateSourceDialog.show();
                });
            } else {
                TaskExecutors.runInUIThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show());
                UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), launcherVersion, UpdateLauncher.UpdateSource.GITHUB_RELEASE);
                updateLauncher.start();
            }
        });
        binding.cancelButton.setOnClickListener(view -> this.dismiss());
        binding.ignoreButton.setOnClickListener(view -> {
            AllSettings.getIgnoreUpdate().put(launcherVersion.getVersionName()).save();
            this.dismiss();
        });
    }

    private String getVersionType() {
        return getContext().getString(launcherVersion.isPreRelease() ? R.string.generic_pre_release : R.string.generic_release);
    }

    private String getLanguageText(LauncherVersion.WhatsNew whatsNew) {
        String text;
        switch (ZHTools.getSystemLanguage()) {
            case "zh_cn":
                text = whatsNew.getZhCN();
                break;
            case "zh_tw":
                text = whatsNew.getZhTW();
                break;
            default:
                text = whatsNew.getEnUS();
        }
        return text;
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
