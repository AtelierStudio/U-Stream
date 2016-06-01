package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import kr.edcan.u_stream.model.SearchData;

/**
 * Created by LNTCS on 2016-03-11.
 */
public class SearchResultPagerAdapter extends PagerAdapter {

    String[] titles = new String[]{"음악", "재생목록"};
    Context mContext;
    ArrayList<SearchData> musicObject = new ArrayList<>();
    ArrayList<SearchData> listObject = new ArrayList<>();
    SearchResultListAdapter searchResultMusicAdapter;
    SearchResultListAdapter searchResultListAdapter;

    public SearchResultPagerAdapter(Context context, ArrayList<SearchData> musicData, ArrayList<SearchData> listData) {
        super();
        mContext = context;
        this.musicObject = musicData;
        this.listObject = listData;
        searchResultListAdapter = new SearchResultListAdapter(mContext, listData, 1);
        searchResultMusicAdapter = new SearchResultListAdapter(mContext, musicData, 0);
    }
    public int getCount() {
        return 2;
    }
    public Object instantiateItem(View pager, int position) {
        ListView listView = new ListView(mContext);
        switch (position){
            case 0:
                listView.setAdapter(searchResultMusicAdapter);
                break;
            case 1:
                listView.setAdapter(searchResultListAdapter);
                break;
        }
        ((ViewPager) pager).addView(listView, null);
        return listView;
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        searchResultListAdapter.notifyDataSetChanged();
        searchResultMusicAdapter.notifyDataSetChanged();
    }
}