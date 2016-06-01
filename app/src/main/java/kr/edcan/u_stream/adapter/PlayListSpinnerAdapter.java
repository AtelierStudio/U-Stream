package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kr.edcan.u_stream.R;

/**
 * Created by LNTCS on 2016-03-18.
 */

public class PlayListSpinnerAdapter extends BaseAdapter {
    private ArrayList<String> mItems = new ArrayList<>();
    Context mContext;
    LayoutInflater mInflater;

    public PlayListSpinnerAdapter(Context mContext){
        this.mContext = mContext;
        this.mInflater=(LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    public void clear() {
        mItems.clear();
    }
    public void addItem(String yourObject) {
        mItems.add(yourObject);
    }
    public void addItems(ArrayList<String> yourObjectList) {
        mItems.addAll(yourObjectList);
    }
    @Override
    public int getCount() {
        return mItems.size();
    }
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = mInflater.inflate(R.layout.spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getString(position));
        return view;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = mInflater.inflate(R.layout.
                    spinner_item_dropdown, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getString(position));
        return view;
    }
    private String getString(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : "";
    }
}