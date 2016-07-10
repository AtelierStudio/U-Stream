package kr.edcan.u_stream;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import io.karim.MaterialTabs;
import kr.edcan.u_stream.adapter.SearchResultPagerAdapter;
import kr.edcan.u_stream.model.SearchData;
import kr.edcan.u_stream.util.DesignUtil;
import kr.edcan.u_stream.util.DialogUtil;
import kr.edcan.u_stream.util.YouTubeClient;

/**
 * Created by LNTCS on 2016-03-15.
 */
public class SearchActivity extends AppCompatActivity {

    @Bind(R.id.toolbar_search_keyword)
    EditText search;
    @Bind(R.id.search_tab)
    MaterialTabs searchTab;
    @Bind(R.id.search_pager)
    ViewPager searchPager;

    SearchResultPagerAdapter searchResultAdapter;
    Context mContext;

    ArrayList<SearchData> searchMusics = new ArrayList<>();
    ArrayList<SearchData> searchLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        mContext = this;
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    performSearch(v);
                    return true;
                }
                return false;
            }
        });
        searchResultAdapter = new SearchResultPagerAdapter(mContext, searchMusics, searchLists);
        DesignUtil.changeTabsFont(mContext, searchTab);
        searchPager.setAdapter(searchResultAdapter);
        searchTab.setViewPager(searchPager);
    }

    @OnClick(R.id.toolbar_search_btn)
    void performSearch(View v) {
        searchMusics.clear();
        searchLists.clear();
        final RequestParams params = new RequestParams();
        params.put("part", "snippet");
        params.put("key", YouTubeClient.key_search);
        params.put("q", search.getText().toString());
        params.put("maxResults", "50");
        params.put("type", "video");
        DialogUtil.showProgressDialog(mContext);
        YouTubeClient.search(params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                params.remove("type");
                params.put("type", "playlist");
                try {
                    final JSONArray musicItems = new JSONObject(new String(responseBody)).getJSONArray("items");
                    for(int i = 0 ; i < musicItems.length() ; ++i){
                        JSONObject musicItem = musicItems.getJSONObject(i);
                        JSONObject snippet = musicItem.getJSONObject("snippet");
                        final SearchData data = new SearchData(
                                musicItem.getJSONObject("id").getString("videoId"),
                                snippet.getString("title"),
                                snippet.getString("description"),
                                getThumb(snippet),
                                snippet.getString("channelTitle")
                        );
                        searchMusics.add(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                YouTubeClient.search(params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            final JSONArray listItems = new JSONObject(new String(responseBody)).getJSONArray("items");
                            for(int i = 0 ; i < listItems.length() ; ++i){
                                JSONObject listItem = listItems.getJSONObject(i);
                                JSONObject snippet = listItem.getJSONObject("snippet");
                                final SearchData data = new SearchData(
                                        listItem.getJSONObject("id").getString("playlistId"),
                                        snippet.getString("title"),
                                        snippet.getString("description"),
                                        getThumb(snippet),
                                        snippet.getString("channelTitle")
                                );
                                searchLists.add(data);
                            }
                            searchResultAdapter.notifyDataSetChanged();
                            DialogUtil.hideProgressDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        DialogUtil.hideProgressDialog();
                        DialogUtil.showDialog(mContext, "검색 실패", "검색 중 오류가 발생하였습니다.\n다시 시도해 주세요.", DialogUtil.Type.POS);
                    }
                });
            }

            private String getThumb(JSONObject snippet) throws JSONException {
                String result = "";
                if(!snippet.has("thumbnails"))
                    return result;
                JSONObject jsonObject = snippet.getJSONObject("thumbnails");
                if(jsonObject.has("high") && !jsonObject.isNull("high")){
                    result = jsonObject.getJSONObject("high").getString("url");
                }else if(jsonObject.has("medium") && !jsonObject.isNull("medium")){
                    result = jsonObject.getJSONObject("medium").getString("url");
                }else if(jsonObject.has("default") && !jsonObject.isNull("high")){
                    result = jsonObject.getJSONObject("default").getString("url");
                }
                return result;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                DialogUtil.hideProgressDialog();
                DialogUtil.showDialog(mContext, "검색 실패", "검색 중 오류가 발생하였습니다.\n다시 시도해 주세요.", DialogUtil.Type.POS);
            }
        });
    }
}
