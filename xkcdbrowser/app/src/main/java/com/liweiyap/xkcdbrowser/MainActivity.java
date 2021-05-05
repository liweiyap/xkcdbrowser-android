package com.liweiyap.xkcdbrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.liweiyap.xkcdbrowser.ui.ViewGroupAccessibilityManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mComicPhotoView = findViewById(R.id.comicPhotoView);
        mComicNumberButton = findViewById(R.id.comicNumberButton);
        mComicTitleTextView = findViewById(R.id.comicTitleTextView);
        mComicDateTextView = findViewById(R.id.comicDateTextView);
        mLoadingProgressBar = findViewById(R.id.loadingProgressBar);
        mRefreshMaterialButton = findViewById(R.id.refreshMaterialButton);
        mPhotoGalleryImageButton = findViewById(R.id.photoGalleryImageButton);
        mNewestComicImageButton = findViewById(R.id.newestComicImageButton);

        mRequestQueue = Volley.newRequestQueue(this);

        // ====================================================================
        // Set onClickListeners
        // ====================================================================

        mRefreshMaterialButton.setOnClickListener(view -> {
            if (mNewestComicNum == null)
            {
                enqueueJSONObjectRequest(mNewestComicURLString);
            }
            else
            {
                enqueueJSONObjectRequest(String.format(Locale.ENGLISH, mGeneralComicURLStringPattern, mCurrentlyDisplayedComicNum));
            }
        });

        ImageButton leftArrowImageButton = findViewById(R.id.leftArrowImageButton);
        leftArrowImageButton.setOnClickListener(view -> {
            if (mCurrentlyDisplayedComicNum == null)
            {
                return;
            }

            mCurrentlyDisplayedComicNum -= 1;
            if (mCurrentlyDisplayedComicNum <= 0)
            {
                mCurrentlyDisplayedComicNum = mNewestComicNum;
            }
            navigateToNewComic(mCurrentlyDisplayedComicNum);
        });

        ImageButton rightArrowImageButton = findViewById(R.id.rightArrowImageButton);
        rightArrowImageButton.setOnClickListener(view -> {
            if (mCurrentlyDisplayedComicNum == null)
            {
                return;
            }

            mCurrentlyDisplayedComicNum += 1;
            if (mCurrentlyDisplayedComicNum > mNewestComicNum)
            {
                mCurrentlyDisplayedComicNum = 1;
            }
            navigateToNewComic(mCurrentlyDisplayedComicNum);
        });

        mComicNumberButton.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.comic_selector_dialog_linearlayout);

            EditText comicSelectorEditText = dialog.findViewById(R.id.comicSelectorEditText);
            Button comicSelectorOKButton = dialog.findViewById(R.id.comicSelectorOKButton);

            comicSelectorOKButton.setOnClickListener(view1 -> {
                String inputString = comicSelectorEditText.getText().toString();

                if (inputString.equals(""))
                {
                    dialog.dismiss();
                    return;
                }

                Integer inputInt = Integer.valueOf(inputString);
                if ((inputInt > mNewestComicNum) || (inputInt < 1))
                {
                    TextView comicSelectorEditTextHint = dialog.findViewById(R.id.comicSelectorEditTextHint);
                    comicSelectorEditTextHint.setText("The number should be between 1 and " + mNewestComicNum + " inclusive.");
                    return;
                }

                dialog.dismiss();
                mCurrentlyDisplayedComicNum = inputInt;
                navigateToNewComic(mCurrentlyDisplayedComicNum);
            });

            dialog.show();
        });

        mNewestComicImageButton.setOnClickListener(view -> {
            if (mNewestComicNum == null)
            {
                return;
            }

            navigateToNewComic(mNewestComicNum);
        });

        mPhotoGalleryImageButton.setOnClickListener(view -> {
            if ((mComicPhotoView == null) || (mComicPhotoView.getDrawable() == null))
            {
                return;
            }

            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(this, PERMISSIONS))
            {
                ActivityCompat.requestPermissions(this, PERMISSIONS,112);
            }

                String imageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    ((BitmapDrawable) mComicPhotoView.getDrawable()).getBitmap(),
                    mComicTitleTextView.getText().toString(),
                    mComicAltText);

                try
                {
                    if (imageURL == null)
                    {
                        throw new Exception("Failed to save image");
                    }
                    else
                    {
                        showNewToast("Image saved to Photo Gallery.");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
        });

        // ====================================================================
        // Disable after setting onClickListeners.
        // Elements will be automatically re-enabled as soon as the newest comic can be retrieved.
        // ====================================================================

        mViewGroupAccessibilityManager.setChildEnabledState(findViewById(R.id.comicNavigatorConstraintLayout), false);
        mViewGroupAccessibilityManager.setChildEnabledState(findViewById(R.id.comicMiscControlsConstraintLayout), false, 0.5f);

        enqueueJSONObjectRequest(mNewestComicURLString);
    }

    /**
     * Need to ask for permission at run-time
     */
    private static boolean hasPermissions(Context context, String... permissions)
    {
        if (context != null && permissions != null)
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

    /**
     * Called from: leftArrowImageButton, rightArrowImageButton, comicNumberButton, and newestComicImageButton.
     * Although at the start of this function, we handle the edge case of the argument comicNum being a NULL Integer,
     * do note that this edge case is already handled by the above UI elements, and hence, comicNum should never be a NULL Integer.
     */
    @SuppressLint("SetTextI18n")
    private void navigateToNewComic(Integer comicNum)
    {
        if (comicNum == null)
        {
            return;
        }

        mComicNumberButton.setText("xkcd #" + comicNum);
        mComicTitleTextView.setText("");
        mComicDateTextView.setText("");

        enqueueJSONObjectRequest(String.format(Locale.ENGLISH, mGeneralComicURLStringPattern, comicNum));
    }

    /**
     * Called from onCreate(), navigateToNewComic(), and refreshMaterialButton::onClick().
     * Although at the start of this function, we handle the edge case of the argument urlString being a NULL String,
     * do note that this edge case is already handled by the above functions, and hence, urlString should never be a NULL String.
     */
    private void enqueueJSONObjectRequest(final String urlString)
    {
        if (urlString == null)
        {
            return;
        }

        // display placeholder
        mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET, urlString, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    updateComicAndMetadata(response);

                    if (urlString.equals(mNewestComicURLString))
                    {
                        // this is the only time that newest comic number is updated
                        mNewestComicNum = mCurrentlyDisplayedComicNum;

                        // navigator buttons are initially disabled
                        // enable them only after the newest comic strip has been displayed for the first time
                        mViewGroupAccessibilityManager.setChildEnabledState(findViewById(R.id.comicNavigatorConstraintLayout), true);
                        mViewGroupAccessibilityManager.setChildEnabledState(findViewById(R.id.comicMiscControlsConstraintLayout), true, 1f);

                        return;
                    }

                    mPhotoGalleryImageButton.setEnabled(true);
                    mPhotoGalleryImageButton.setAlpha(1f);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
                    mRefreshMaterialButton.setVisibility(View.VISIBLE);
                    mComicPhotoView.setImageResource(0);
                    mComicPhotoView.setOnLongClickListener(null);
                    mComicAltText = null;
                    mPhotoGalleryImageButton.setEnabled(false);
                    mPhotoGalleryImageButton.setAlpha(0.5f);
                }
            });

        mRequestQueue.add(jsonObjectRequest);
    }

    /**
     * Called only from enqueueJSONObjectRequest().
     * Although at the start of this function, we handle the edge case of the argument jsonObject being a NULL JSONObject,
     * do note that due to the logic of enqueueJSONObjectRequest(), jsonObject should never be a NULL JSONObject.
     * In addition, just in case some comics have incomplete JSON data,
     * we do extensive checks for the existence of all relevant keys in jsonObject.
     */
    @SuppressLint("SetTextI18n")
    private void updateComicAndMetadata(final JSONObject jsonObject)
    {
        if (jsonObject == null)
        {
            return;
        }

        // update from JSON
        try
        {
            if (jsonObject.isNull("img"))
            {
                mComicPhotoView.setImageResource(0);
            }
            else
            {
                Picasso.get().load(jsonObject.getString("img")).into(mComicPhotoView);
            }

            if (jsonObject.isNull("num"))
            {
                mComicNumberButton.setText("");
            }
            else
            {
                String numString = jsonObject.getString("num");
                mComicNumberButton.setText("xkcd #" + numString);
                mCurrentlyDisplayedComicNum = Integer.valueOf(numString);
            }

            if (jsonObject.isNull("title"))
            {
                mComicTitleTextView.setText("");
            }
            else
            {
                mComicTitleTextView.setText(jsonObject.getString("title"));
            }

            if ((!jsonObject.isNull("day")) &&
                (!jsonObject.isNull("month")) &&
                (!jsonObject.isNull("year")))
            {
                mComicDateTextView.setText(
                    formatDate(
                        jsonObject.getString("day"),
                        jsonObject.getString("month"),
                        jsonObject.getString("year")));
            }
            else
            {
                mComicDateTextView.setText("");
            }

            if (jsonObject.isNull("alt"))
            {
                mComicPhotoView.setOnLongClickListener(null);
            }
            else
            {
                mComicAltText = jsonObject.getString("alt");

                mComicPhotoView.setOnLongClickListener(view -> {
                    showNewToast(mComicAltText);
                    return true;
                });
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        // display comic
        mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
        mComicPhotoView.setVisibility(View.VISIBLE);
    }

    /**
     * Called only from updateComicAndMetadata().
     * Although at the start of this function, we handle the edge case of the arguments being NULL Strings,
     * do note that due to the logic of updateComicAndMetadata(), these arguments should never be NULL Strings.
     */
    private String formatDate(final String day, final String month, final String year)
    {
        if ((day == null) || (month == null) || (year == null))
        {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH);

        String dateString = String.format(
            "%s/%s/%s",
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(day)),
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(month)),
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(year))
        );

        Date date = null;

        try
        {
            date = simpleDateFormat.parse(dateString);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (date == null)
        {
            return "";
        }

        simpleDateFormat.applyPattern("EEE, d MMM yyyy");
        return simpleDateFormat.format(date);
    }

    private void showNewToast(final String message)
    {
        if (message == null)
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

        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    private final String mNewestComicURLString = "https://xkcd.com/info.0.json";
    private final String mGeneralComicURLStringPattern = "https://xkcd.com/%d/info.0.json";
    private RequestQueue mRequestQueue;
    private Integer mCurrentlyDisplayedComicNum;
    private Integer mNewestComicNum;

    private String mComicAltText;

    private PhotoView mComicPhotoView;
    private Button mComicNumberButton;
    private TextView mComicTitleTextView;
    private TextView mComicDateTextView;
    private ProgressBar mLoadingProgressBar;
    private MaterialButton mRefreshMaterialButton;
    private ImageButton mPhotoGalleryImageButton;
    private ImageButton mNewestComicImageButton;
    private Toast mToast;

    private final ViewGroupAccessibilityManager mViewGroupAccessibilityManager = new ViewGroupAccessibilityManager();
}