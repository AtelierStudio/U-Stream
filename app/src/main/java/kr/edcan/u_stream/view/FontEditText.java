package kr.edcan.u_stream.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by LNTCS on 2015-09-01.
 */

public class FontEditText extends android.widget.EditText {

    public FontEditText(Context context) {
        super(context);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NanumBarunGothicLight.ttf"));
    }

    public FontEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NanumBarunGothicLight.ttf"));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FontEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/NanumBarunGothicLight.ttf"));
    }
}
