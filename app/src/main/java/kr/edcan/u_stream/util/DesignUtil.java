package kr.edcan.u_stream.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.karim.MaterialTabs;

/**
 * Created by LNTCS on 2016-03-15.
 */
public class DesignUtil {
    public static void changeTabsFont(Context mContext, MaterialTabs tabLayout) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/NanumBarunGothic.ttf"));
                }
            }
        }
    }

    public static void setFont(Context mContext, TextView view){
        view.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/NanumBarunGothic.ttf"));
    }
}
