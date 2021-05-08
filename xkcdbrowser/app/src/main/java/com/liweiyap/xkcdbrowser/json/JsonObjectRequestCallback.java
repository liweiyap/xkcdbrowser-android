package com.liweiyap.xkcdbrowser.json;

import androidx.annotation.NonNull;

public interface JsonObjectRequestCallback
{
    void onComplete(final JsonDataModel jsonDataModel, @NonNull final String urlString);
}