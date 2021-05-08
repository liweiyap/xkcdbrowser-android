package com.liweiyap.xkcdbrowser.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class PermissionChecker
{
    public static boolean hasPermissions(@NonNull final Context context, final String... permissions)
    {
        if (permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }

        return true;
    }
}