package com.dayries.applicationupdater;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.android.volley.BuildConfig;

import java.io.File;

public class UtilsLibrairy {

    public static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, "com.dayries.applicationupdater.provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
