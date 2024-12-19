package com.movtery.zalithlauncher.feature.update;

import static com.movtery.zalithlauncher.task.TaskExecutors.runInUIThread;
import static com.movtery.zalithlauncher.utils.file.FileTools.formatFileSize;

import android.content.Context;

import androidx.annotation.NonNull;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.ui.dialog.ProgressDialog;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.path.UrlManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public final class UpdateLauncher {
    private final Context context;
    private final LauncherVersion launcherVersion;
    private final String destinationFilePath;
    private final Call call;
    private ProgressDialog dialog;
    private Timer timer;

    public UpdateLauncher(Context context, LauncherVersion launcherVersion, UpdateSource updateSource) {
        this.context = context;
        this.launcherVersion = launcherVersion;

        this.destinationFilePath = UpdateUtils.sApkFile.getAbsolutePath();
        this.call = new OkHttpClient().newCall(
                UrlManager.createRequestBuilder(UpdateUtils.getDownloadUrl(launcherVersion, updateSource)).build()
        ); //获取请求对象
    }

    public void start() {
        this.call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                UpdateUtils.showFailToast(context, context.getString(R.string.update_fail));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    UpdateUtils.showFailToast(context, context.getString(R.string.update_fail_code, response.code()));
                    throw new IOException("Unexpected code " + response);
                } else {
                    File outputFile = new File(UpdateLauncher.this.destinationFilePath);
                    Objects.requireNonNull(response.body());
                    try (InputStream inputStream = response.body().byteStream();
                         OutputStream outputStream = Files.newOutputStream(outputFile.toPath())
                    ) {
                        byte[] buffer = new byte[1024 * 1024];
                        int bytesRead;

                        runInUIThread(() -> {
                            UpdateLauncher.this.dialog = new ProgressDialog(UpdateLauncher.this.context, () -> {
                                UpdateLauncher.this.stop();
                                return true;
                            });
                            UpdateLauncher.this.dialog.show();
                        });

                        final long[] downloadedSize = new long[1];
                        final long[] lastSize = {0};
                        final long[] lastTime = {ZHTools.getCurrentTimeMillis()};

                        //限制刷新速度
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                long size = downloadedSize[0];
                                long currentTime = ZHTools.getCurrentTimeMillis();
                                double timeElapsed = (currentTime - lastTime[0]) / 1000.0;
                                long sizeChange = size - lastSize[0];
                                long rate = (long) (sizeChange / timeElapsed);

                                lastSize[0] = size;
                                lastTime[0] = currentTime;

                                runInUIThread(() -> {
                                    String formattedDownloaded = formatFileSize(size);
                                    String totalSize = formatFileSize(UpdateUtils.getFileSize(launcherVersion.getFileSize()));
                                    UpdateLauncher.this.dialog.updateProgress(size, UpdateUtils.getFileSize(launcherVersion.getFileSize()));
                                    UpdateLauncher.this.dialog.updateRate(rate > 0 ? rate : 0L);
                                    UpdateLauncher.this.dialog.updateText(String.format(context.getString(R.string.update_downloading), formattedDownloaded, totalSize));
                                });
                            }
                        }, 0, 120);

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedSize[0] += bytesRead;
                        }
                        finish(outputFile);
                    }
                }
            }
        });
    }

    private void finish(File outputFile) {
        runInUIThread(UpdateLauncher.this.dialog::dismiss);
        timer.cancel();

        UpdateUtils.installApk(context, outputFile);
    }

    private void stop() {
        this.call.cancel();
        this.timer.cancel();
        FileUtils.deleteQuietly(UpdateUtils.sApkFile);
    }

    public enum UpdateSource {
        GITHUB_RELEASE, GHPROXY
    }
}
