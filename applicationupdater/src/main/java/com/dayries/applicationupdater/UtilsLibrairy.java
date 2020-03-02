package com.dayries.applicationupdater;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.android.volley.BuildConfig;

import java.io.File;
import java.util.Objects;

public class UtilsLibrairy {

    public static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context.getApplicationContext(),
                    "com.dayries.applicationupdater.FileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
