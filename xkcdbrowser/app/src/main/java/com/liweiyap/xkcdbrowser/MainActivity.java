package com.liweiyap.xkcdbrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
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

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.liweiyap.xkcdbrowser.json.JsonDataModel;
import com.liweiyap.xkcdbrowser.json.JsonObjectRequestCallback;
import com.liweiyap.xkcdbrowser.json.JsonObjectRequestQueueSingleton;
import com.liweiyap.xkcdbrowser.ui.ViewGroupAccessibilityManager;
import com.liweiyap.xkcdbrowser.util.DateFormatter;
import com.liweiyap.xkcdbrowser.util.PermissionChecker;
import com.squareup.picasso.Picasso;

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

        mJsonObjectRequestQueueSingleton = JsonObjectRequestQueueSingleton.getInstance(getApplicationContext());

        // ====================================================================
        // Set onClickListeners
        // ====================================================================

        mRefreshMaterialButton.setOnClickListener(view -> {
            if (mNewestComicNum == null)
            {
                updateComicAndMetaData(mNewestComicURLString);
            }
            else
            {
                updateComicAndMetaData(String.format(Locale.ENGLISH, mGeneralComicURLStringPattern, mCurrentlyDisplayedComicNum));
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
            if (!PermissionChecker.hasPermissions(this, PERMISSIONS))
            {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 112);
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

        updateComicAndMetaData(mNewestComicURLString);
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

        updateComicAndMetaData(String.format(Locale.ENGLISH, mGeneralComicURLStringPattern, comicNum));
    }

    /**
     * Called from onCreate(), navigateToNewComic(), and refreshMaterialButton::onClick().
     * Although at the start of this function, we handle the edge case of the argument urlString being a NULL String,
     * do note that this edge case is already handled by the above functions, and hence, urlString should never be a NULL String.
     */
    private void updateComicAndMetaData(final String urlString)
    {
        if (urlString == null)
        {
            return;
        }

        mLastRequestedURLString = urlString;

        // display placeholder
        mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);

        // From the documentation (https://developer.android.com/training/volley/simple#send):
        // When you call add(), Volley runs one cache processing thread and a pool of network dispatch threads.
        // When you add a request to the queue, it is picked up by the cache thread and triaged:
        // if the request can be serviced from cache, the cached response is parsed on the cache thread
        // and the parsed response is delivered on the main thread. If the request cannot be serviced from cache,
        // it is placed on the network queue. The first available network thread takes the request from the queue,
        // performs the HTTP transaction, parses the response on the worker thread, writes the response to cache,
        // and posts the parsed response back to the main thread for delivery.
        // Note that expensive operations like blocking I/O and parsing/decoding are done on worker threads.
        // You can add a request from any thread, but responses are always delivered on the main thread.
        //
        // So Volley already uses background threads. Thus, perhaps no need to use Executor like in this example?
        // https://developer.android.com/guide/background/threading#java
        // But to handle race condition if user clicks too fast on leftArrowImageButton or rightArrowImageButton,
        // we declare a mLastRequestedURLString variable. (Note that this doesn't really solve the issue for newestComicImageButton, but unlike for leftArrowImageButton or rightArrowImageButton, the problem isn't noticeable.)
        // The race condition occurs in the first place because Volley network calls are asynchronous in nature,
        // (https://stackoverflow.com/questions/35362167/avoid-getting-race-condition-in-android-volley-in-android-app)
        // so if user clicks too fast and multiple requests are sent over a very short period of time,
        // then we have no control over which request will get completed first.
        mJsonObjectRequestQueueSingleton.enqueueJSONObjectRequest(urlString, new JsonObjectRequestCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(final JsonDataModel jsonDataModel, final String urlString) {
                if ( (mLastRequestedURLString != null) && (!urlString.equals(mLastRequestedURLString)) )
                {
                    return;
                }

                if (jsonDataModel == null)
                {
                    mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
                    mRefreshMaterialButton.setVisibility(View.VISIBLE);
                    mComicPhotoView.setImageResource(0);
                    mComicPhotoView.setOnLongClickListener(null);
                    mComicAltText = null;
                    mPhotoGalleryImageButton.setEnabled(false);
                    mPhotoGalleryImageButton.setAlpha(0.5f);
                    return;
                }

                if (jsonDataModel.getComicImageUrl() == null)
                {
                    mComicPhotoView.setImageResource(0);
                }
                else
                {
                    Picasso.get().load(jsonDataModel.getComicImageUrl()).into(mComicPhotoView);
                }

                if (jsonDataModel.getComicNum() == null)
                {
                    mComicNumberButton.setText("");
                }
                else
                {
                    mComicNumberButton.setText("xkcd #" + jsonDataModel.getComicNum());
                    mCurrentlyDisplayedComicNum = jsonDataModel.getComicNum();
                }

                mComicTitleTextView.setText(
                    jsonDataModel.getComicTitle() == null ?
                        "":
                        jsonDataModel.getComicTitle()
                );

                if ( (jsonDataModel.getComicDay() != null) &&
                     (jsonDataModel.getComicMonth() != null) &&
                     (jsonDataModel.getComicYear() != null) )
                {
                    mComicDateTextView.setText(
                        DateFormatter.formatDate(
                            jsonDataModel.getComicDay(),
                            jsonDataModel.getComicMonth(),
                            jsonDataModel.getComicYear()));
                }
                else
                {
                    mComicDateTextView.setText("");
                }

                if (jsonDataModel.getComicAltText() == null)
                {
                    mComicPhotoView.setOnLongClickListener(null);
                }
                else
                {
                    mComicAltText = jsonDataModel.getComicAltText();

                    mComicPhotoView.setOnLongClickListener(view -> {
                        showNewToast(mComicAltText);
                        return true;
                    });
                }

                // display comic
                mViewGroupAccessibilityManager.setChildVisibility(findViewById(R.id.mainDisplayRelativeLayout), View.GONE);
                mComicPhotoView.setVisibility(View.VISIBLE);

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
        });
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
    private JsonObjectRequestQueueSingleton mJsonObjectRequestQueueSingleton;
    private String mLastRequestedURLString;
}