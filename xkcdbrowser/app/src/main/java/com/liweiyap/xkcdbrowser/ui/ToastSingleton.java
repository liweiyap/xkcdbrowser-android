package com.liweiyap.xkcdbrowser.ui;

import android.content.Context;
import android.widget.Toast;

public class ToastSingleton
{
    private ToastSingleton(){}

    public static synchronized ToastSingleton getInstance()
    {
        if (instance == null)
        {
            instance = new ToastSingleton();
        }

        return instance;
    }

    public void showNewToast(final Context context, final String message, final int duration)
    {
        if ( !((duration == Toast.LENGTH_SHORT) || (duration == Toast.LENGTH_LONG)) )
        {
            throw new RuntimeException(
                "ToastSingleton::showNewToast(): " +
                "Programming Error. Value for duration (" + duration + ") not recognised.");
        }

        if ( (context == null) || (message == null) )
        {
            return;
        }

        if (mToast != null)
        {
            mToast.cancel();
        }

        if (message.equals(""))
        {
            return;
        }

        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    private Toast mToast;
    private static ToastSingleton instance;
}