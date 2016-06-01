package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import kr.edcan.u_stream.R;

/**
 * Created by LNTCS on 2016-03-11.
 */
public class AnalogPagerAdapter extends PagerAdapter {

    String[] titles = new String[]{"재생목록", "음악목록"};
    Context mContext;
    private LayoutInflater mInflater;
    public AnalogPagerAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }
    public int getCount() {
        return 2;
    }
    public Object instantiateItem(View pager, int position) {
        View v = mInflater.inflate(R.layout.content_analog_list, null);
        switch (position) {
            case 0:
                break;
            case 1:
                break;
        }
        ((ViewPager) pager).addView(v, null);
        return v;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public void destroyItem(View pager, int position, Object view) {
        ((ViewPager) pager).removeView((View) view);
    }
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }
}