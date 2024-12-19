package com.movtery.zalithlauncher.utils.skin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.movtery.zalithlauncher.utils.path.PathManager;

import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SkinLoader {
    public static Drawable getAvatarDrawable(Context context, MinecraftAccount account, int size) throws IOException {
        File skin = new File(PathManager.DIR_USER_SKIN, account.getUniqueUUID() + ".png");
        if (skin.exists()) {
            try (InputStream is = Files.newInputStream(skin.toPath())) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return new BitmapDrawable(context.getResources(), getAvatar(bitmap, size));
            }
        }
        return getDefaultAvatar(context, size);
    }

    private static Drawable getDefaultAvatar(Context context, int size) throws IOException {
        InputStream is = context.getAssets().open("steve.png");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return new BitmapDrawable(context.getResources(), getAvatar(bitmap, size));
    }

    public static Bitmap getAvatar(Bitmap skin, int size) {
        float faceOffset = Math.round(size / 18.0);
        float scaleFactor = skin.getWidth() / 64.0f;
        int faceSize = Math.round(8 * scaleFactor);
        Bitmap faceBitmap = Bitmap.createBitmap(skin, faceSize, faceSize, faceSize, faceSize, (Matrix) null, false);
        Bitmap hatBitmap = Bitmap.createBitmap(skin, Math.round(40 * scaleFactor), faceSize, faceSize, faceSize, (Matrix) null, false);
        Bitmap avatar = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(avatar);
        Matrix matrix;
        float faceScale = ((size - 2 * faceOffset) / faceSize);
        float hatScale = ((float) size / faceSize);
        matrix = new Matrix();
        matrix.postScale(faceScale, faceScale);
        Bitmap newFaceBitmap = Bitmap.createBitmap(faceBitmap, 0, 0 , faceSize, faceSize, matrix, false);
        matrix = new Matrix();
        matrix.postScale(hatScale, hatScale);
        Bitmap newHatBitmap = Bitmap.createBitmap(hatBitmap, 0, 0, faceSize, faceSize, matrix, false);
        canvas.drawBitmap(newFaceBitmap, faceOffset, faceOffset, new Paint(Paint.ANTI_ALIAS_FLAG));
        canvas.drawBitmap(newHatBitmap, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));
        return avatar;
    }
}
