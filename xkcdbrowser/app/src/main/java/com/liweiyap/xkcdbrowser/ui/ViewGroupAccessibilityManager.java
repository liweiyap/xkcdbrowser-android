package com.liweiyap.xkcdbrowser.ui;

import android.view.View;
import android.view.ViewGroup;

public class ViewGroupAccessibilityManager
{
    public ViewGroupAccessibilityManager(){}

    public void setChildVisibility(ViewGroup viewGroup, int visibility) throws RuntimeException
    {
        if ( !((visibility == View.VISIBLE) ||
               (visibility == View.INVISIBLE) ||
               (visibility == View.GONE)) )
        {
            throw new RuntimeException(
                "ViewGroupAccessibilityManager::setChildVisibility(): " +
                "Programming Error. Value for visibility (" + visibility + ") not recognised.");
        }

        if (viewGroup == null)
        {
            return;
        }

        for (int childIdx = 0; childIdx < viewGroup.getChildCount(); ++childIdx)
        {
            View child = viewGroup.getChildAt(childIdx);
            child.setVisibility(visibility);
        }
    }

    public void setChildEnabledState(ViewGroup viewGroup, boolean enabledState)
    {
        if (viewGroup == null)
        {
            return;
        }

        for (int childIdx = 0; childIdx < viewGroup.getChildCount(); ++childIdx)
        {
            View child = viewGroup.getChildAt(childIdx);
            child.setEnabled(enabledState);
        }
    }

    public void setChildEnabledState(ViewGroup viewGroup, boolean enabledState, float alpha) throws RuntimeException
    {
        if ( (alpha < -Math.ulp(0f)) || (alpha > 1f+Math.ulp(1f)) )
        {
            throw new RuntimeException(
                "ViewGroupAccessibilityManager::setChildEnabledState(): " +
                "Programming Error. Value for enabled state (" + alpha + ") not recognised.");
        }

        if (viewGroup == null)
        {
            return;
        }

        for (int childIdx = 0; childIdx < viewGroup.getChildCount(); ++childIdx)
        {
            View child = viewGroup.getChildAt(childIdx);
            child.setEnabled(enabledState);
            child.setAlpha(alpha);
        }
    }
}