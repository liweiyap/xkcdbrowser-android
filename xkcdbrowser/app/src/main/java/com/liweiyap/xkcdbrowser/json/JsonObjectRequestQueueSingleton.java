package com.liweiyap.xkcdbrowser.json;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * From the documentation (https://developer.android.com/training/volley/requestqueue#singleton):
 * If your application makes constant use of the network, it's probably most efficient to set up a single instance of RequestQueue
 * that will last the lifetime of your app. You can achieve this in various ways. The recommended approach is to implement a singleton class
 * that encapsulates RequestQueue and other Volley functionality. Another approach is to subclass Application and set up the RequestQueue in Application.onCreate().
 * But this approach is discouraged; a static singleton can provide the same functionality in a more modular way.
 */
public class JsonObjectRequestQueueSingleton
{
    private JsonObjectRequestQueueSingleton(@NonNull final Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    /**
     * @param context Use ApplicationContext to ensure that the RequestQueue will last for the lifetime of your app,
     *                instead of being recreated every time the Activity is recreated.
     *                In other words, this prevents leaking of the Activity.
     * @return single instance of JsonObjectRequestQueueSingleton
     */
    public static synchronized JsonObjectRequestQueueSingleton getInstance(@NonNull final Context context)
    {
        if (mInstance == null)
        {
            mInstance = new JsonObjectRequestQueueSingleton(context);
        }

        return mInstance;
    }

    public void enqueueJSONObjectRequest(final String urlString, final JsonObjectRequestCallback callback)
    {
        if (urlString == null)
        {
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET, urlString, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JsonDataModel jsonDataModel = parseJsonObject(response);

                    if (callback == null)
                    {
                        return;
                    }

                    callback.onComplete(jsonDataModel, urlString);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (callback == null)
                    {
                        return;
                    }

                    callback.onComplete(null, urlString);
                }
            });

        mRequestQueue.add(jsonObjectRequest);
    }

    private JsonDataModel parseJsonObject(final JSONObject jsonObject)
    {
        if (jsonObject == null)
        {
            return null;
        }

        JsonDataModel jsonDataModel = new JsonDataModel();

        try
        {
            if (!jsonObject.isNull("img"))
            {
                jsonDataModel.setComicImageUrl(jsonObject.getString("img"));
            }

            if (!jsonObject.isNull("num"))
            {
                jsonDataModel.setComicNum(jsonObject.getInt("num"));
            }

            if (!jsonObject.isNull("title"))
            {
                jsonDataModel.setComicTitle(jsonObject.getString("title"));
            }

            if (!jsonObject.isNull("day"))
            {
                jsonDataModel.setComicDay(jsonObject.getString("day"));
            }

            if (!jsonObject.isNull("month"))
            {
                jsonDataModel.setComicMonth(jsonObject.getString("month"));
            }

            if (!jsonObject.isNull("year"))
            {
                jsonDataModel.setComicYear(jsonObject.getString("year"));
            }

            if (!jsonObject.isNull("alt"))
            {
                jsonDataModel.setComicAltText(jsonObject.getString("alt"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return jsonDataModel;
    }

    private final RequestQueue mRequestQueue;
    private static JsonObjectRequestQueueSingleton mInstance;
}