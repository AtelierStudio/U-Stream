package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import io.karim.MaterialTabs;
import kr.edcan.u_stream.PlayListActivity;
import kr.edcan.u_stream.R;
import kr.edcan.u_stream.model.RM_PlayListData;
import kr.edcan.u_stream.util.DesignUtil;

/**
 * Created by LNTCS on 2016-03-11.
 */
public class MainAdapter extends PagerAdapter {

    Context mContext;
    private LayoutInflater mInflater;
    PlayListGridAdapter playListGridAdapter;
    AnalogPagerAdapter analogPagerAdapter;
    public MainAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        playListGridAdapter = new PlayListGridAdapter(mContext);
        analogPagerAdapter = new AnalogPagerAdapter(mContext);
    }
    public int getCount() {
        return 3;
    }
    public Object instantiateItem(View pager, int position) {
        View v = null;
        LinearLayout mLinear;
        switch (position) {
            case 0:
                v = mInflater.inflate(R.layout.content_main_space, null);
                break;
            case 1:
                v = mInflater.inflate(R.layout.content_main_playlist, null);
                GridView gridView = (GridView) v.findViewById(R.id.main_playlist_grid);
                gridView.setAdapter(playListGridAdapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(mContext, PlayListActivity.class);
                        RM_PlayListData pData = playListGridAdapter.getPlaylist(position);
                        i.putExtra("title",pData.getTitle());
                        i.putExtra("id",pData.getId());
                        mContext.startActivity(i);
                    }
                });
                break;
            case 2:
                v = mInflater.inflate(R.layout.content_main_analog, null);
                MaterialTabs analogTab = (MaterialTabs) v.findViewById(R.id.main_analog_tab);
                DesignUtil.changeTabsFont(mContext, analogTab);
                ViewPager analogPager = (ViewPager)v.findViewById(R.id.main_analog_pager);
                analogPager.setAdapter(analogPagerAdapter);
                analogTab.setViewPager(analogPager);
                break;
        }
        ((ViewPager) pager).addView(v, null);
        return v;
    }
    public void destroyItem(View pager, int position, Object view) {
        ((ViewPager) pager).removeView((View) view);
    }
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        playListGridAdapter.notifyDataSetChanged();
        analogPagerAdapter.notifyDataSetChanged();
    }
}