package com.liweiyap.xkcdbrowser.json;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonObjectRequestQueue
{
    public JsonObjectRequestQueue(final Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
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
                    callback.onComplete(jsonDataModel, urlString);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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
}