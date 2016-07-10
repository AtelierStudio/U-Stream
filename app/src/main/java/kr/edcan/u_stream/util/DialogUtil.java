package kr.edcan.u_stream.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import kr.edcan.u_stream.R;
import kr.edcan.u_stream.adapter.PlayListSpinnerAdapter;
import kr.edcan.u_stream.adapter.PlaylistListAdapter;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.model.RM_MusicData;
import kr.edcan.u_stream.model.RM_PlayListData;
import kr.edcan.u_stream.model.SearchData;

/**
 * Created by LNTCS on 2016-03-15.
 */
public class DialogUtil {
    public static MaterialDialog mProgressDialog;
    static int selectListPos;
    public static Realm realm;
    public static RealmConfiguration realmConfig;
    public static void showProgressDialog(Context mContext) {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(mContext)
                    .canceledOnTouchOutside(false)
                    .content("로딩중...")
                    .widgetColorRes(R.color.colorPrimary)
                    .backgroundColorRes(R.color.background)
                    .progress(true, 0)
                    .build();
            DesignUtil.setFont(mContext, mProgressDialog.getContentView());
        }
        mProgressDialog.show();
    }
    public static void showProgressDialog(Context mContext, String text) {
        mProgressDialog = new MaterialDialog.Builder(mContext)
                .canceledOnTouchOutside(false)
                .content(text)
                .widgetColorRes(R.color.colorPrimary)
                .backgroundColorRes(R.color.background)
                .progress(true, 0)
                .build();
        DesignUtil.setFont(mContext, mProgressDialog.getContentView());
        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public enum Type{
        POS,
        BOTH
    }
    public static void showDialog(Context mContext, String title, String text, Type type){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
                .title(title)
                .titleColorRes(R.color.colorPrimary)
                .backgroundColorRes(R.color.lgt_background)
                .content(text)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("확인");
        switch (type){
            case BOTH:
                builder.negativeText("취소")
                        .negativeColorRes(R.color.text_lgt_gray);
                break;
        }
        MaterialDialog mDlg = builder.build();
        DesignUtil.setFont(mContext, mDlg.getTitleView());
        DesignUtil.setFont(mContext, mDlg.getContentView());
        mDlg.show();
    }

    static MaterialDialog addDlg;

    public static void selectPlayListDialog(final Context mContext, final SearchData data, final int type) {
        realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        selectListPos = 0;
        boolean wrapInScrollView = true;
        addDlg = new MaterialDialog.Builder(mContext)
                .title("재생목록에 추가")
                .titleColorRes(R.color.colorPrimary)
                .customView(R.layout.content_dialog_add_playlist, wrapInScrollView)
                .backgroundColorRes(R.color.lgt_background)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("확인")
                .negativeColorRes(R.color.text_gray)
                .negativeText("취소")
                .build();
        DesignUtil.setFont(mContext, addDlg.getTitleView());
        View view = addDlg.getCustomView();

        TextView content = (TextView) view.findViewById(R.id.search_dialog_content);
        content.setText("'"+data.getTitle()+"'을(를) 재생목록에 추가합니다.");

        Spinner spinner = (Spinner) view.findViewById(R.id.search_dialog_spinner);
        PlayListSpinnerAdapter spinnerAdapter = new PlayListSpinnerAdapter(mContext);

        final RealmResults<RM_PlayListData> pList = realm.where(RM_PlayListData.class).findAll();
        for(RM_PlayListData pData : pList) {
            spinnerAdapter.addItem(pData.getTitle());
        }
        spinnerAdapter.addItem("새 재생목록 추가...");
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getCount() - 1 == position){
                    selectListPos = -1;
                }else {
                    selectListPos = position;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addDlg.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if(selectListPos == -1){
                    addPlayListDialog(mContext, data, type);
                    return;
                }
                RM_PlayListData pData = pList.get(selectListPos);
                int musicId = getNumberInt(realm.where(RM_MusicData.class).max("id")) + 1;

                if(type == 0) {
                    final RM_MusicData mData = new RM_MusicData();
                    mData.setId(musicId);
                    mData.setTitle(data.getTitle());
                    mData.setPlayListId(pData.getId());
                    mData.setThumbnail(data.getThumbnail());
                    mData.setUploader(data.getUploader());
                    mData.setDescription(data.getDescription());
                    mData.setVideoId(data.getId());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealm(mData);
                        }
                    });
                    Toast.makeText(mContext, pData.getTitle() + "에 1곡이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    new getMusicsTask(mContext, data.getId(), pData.getId(), musicId, pData.getTitle()).execute();
                }
            }
        }).build().show();
    }

    public static void addPlayListDialog(final Context mContext, final SearchData data, final int type) {
        realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        MaterialDialog mDlg = new MaterialDialog.Builder(mContext)
                .title("재생목록 추가")
                .titleColorRes(R.color.colorPrimary)
                .content("'"+data.getTitle()+"'을(를) 새로운 재생목록에 추가합니다.")
                .backgroundColorRes(R.color.lgt_background)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("추가")
                .negativeColorRes(R.color.text_gray)
                .negativeText("취소")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("제목을 입력해주세요.", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String input = dialog.getInputEditText().getText().toString();
                        if (input.trim().equals("")) {
                            dialog.dismiss();
                            addPlayListDialog(mContext, data, type);
                        } else {
                            if (type == 0) {
                                int listId = getNumberInt(realm.where(RM_PlayListData.class).max("id")) + 1;
                                int musicId = getNumberInt(realm.where(RM_MusicData.class).max("id")) + 1;

                                final RM_PlayListData pData = new RM_PlayListData();
                                pData.setId(listId);
                                pData.setTitle(input);

                                final RM_MusicData mData = new RM_MusicData();
                                mData.setId(musicId);
                                mData.setTitle(data.getTitle());
                                mData.setPlayListId(listId);
                                mData.setThumbnail(data.getThumbnail());
                                mData.setUploader(data.getUploader());
                                mData.setDescription(data.getDescription());
                                mData.setVideoId(data.getId());
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(pData);
                                        realm.copyToRealm(mData);
                                    }
                                });
                                Toast.makeText(mContext, input + "에 1곡이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                int listId = getNumberInt(realm.where(RM_PlayListData.class).max("id")) + 1;
                                int musicId = getNumberInt(realm.where(RM_MusicData.class).max("id")) + 1;
                                final RM_PlayListData pData = new RM_PlayListData();
                                pData.setId(listId);
                                pData.setTitle(input);
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(pData);
                                    }
                                });
                                new getMusicsTask(mContext, data.getId(), listId, musicId, input).execute();
                            }
                        }
                    }
                })
                .build();
        DesignUtil.setFont(mContext, mDlg.getTitleView());
        DesignUtil.setFont(mContext, mDlg.getContentView());
        DesignUtil.setFont(mContext, mDlg.getInputEditText());
        mDlg.show();
    }

    private static int getNumberInt(Number num) {
        return (num != null)? num.intValue() : 0;
    }

    public static void deletePlayListDialog(final Context mContext, final MusicData data, String playlistTitle, final PlaylistListAdapter adapter) {
        realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        MaterialDialog mDlg = new MaterialDialog.Builder(mContext)
                .title("곡 제거")
                .titleColorRes(R.color.colorPrimary)
                .content("'" + data.getTitle()+"'을(를) '" + playlistTitle + "'에서 제거합니다.")
                .backgroundColorRes(R.color.lgt_background)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("확인")
                .negativeColorRes(R.color.text_gray)
                .negativeText("취소")
                .widgetColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.where(RM_MusicData.class).equalTo("id", data.getId()).findFirst().deleteFromRealm();
                            }
                        });
                        adapter.remove(data);
                        //TODO 삭제후 현재곡일경우 다음곡으로
                    }
                })
                .build();
        DesignUtil.setFont(mContext, mDlg.getTitleView());
        DesignUtil.setFont(mContext, mDlg.getContentView());
        mDlg.show();
    }

    public static void editPlayListDialog(final Context mContext, final RM_PlayListData data){
        realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        MaterialDialog mDlg = new MaterialDialog.Builder(mContext)
                .title("재생목록 편집")
                .titleColorRes(R.color.colorPrimary)
                .backgroundColorRes(R.color.lgt_background)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("저장")
                .negativeColorRes(R.color.text_gray)
                .negativeText("취소")
                .neutralColorRes(R.color.colorPrimary)
                .neutralText("삭제")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("제목을 입력해주세요.", data.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final String input = dialog.getInputEditText().getText().toString();
                        if (input.trim().equals("")) {
                            dialog.dismiss();
                            editPlayListDialog(mContext, data);
                        } else {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    data.setTitle(input);
                                }
                            });
                        }
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                data.deleteFromRealm();
                            }
                        });
                    }
                })
                .build();
        DesignUtil.setFont(mContext, mDlg.getTitleView());
        DesignUtil.setFont(mContext, mDlg.getContentView());
        DesignUtil.setFont(mContext, mDlg.getInputEditText());
        mDlg.show();
    }
    static class getMusicsTask extends AsyncTask<String, String, ArrayList<RM_MusicData>>{
        String id;
        Context mContext;
        int totResults = 0;
        int playlistId = 0;
        int musicId = 0;
        String input;
        public getMusicsTask(Context mContext, String id, int playlistId, int musicId, String input){
            this.id = id;
            this.mContext = mContext;
            this.playlistId = playlistId;
            this.musicId = musicId;
            this.input = input;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(mContext, "재생목록 분석중...");
        }

        @Override
        protected ArrayList<RM_MusicData> doInBackground(String... params) {
            ArrayList<RM_MusicData> mList = new ArrayList<>();
            try {
                String doc = Jsoup.connect(
                        YouTubeClient.BASE_URL + "/playlistItems?" +
                                "part=snippet&maxResults=50&key="+YouTubeClient.key_list+"&playlistId="+id
                ).ignoreContentType(true).execute().body();
                JSONObject jsonObject = new JSONObject(doc);
                totResults = jsonObject.getJSONObject("pageInfo").getInt("totalResults");
                JSONArray items = jsonObject.getJSONArray("items");
                for(int i = 0 ; i < items.length() ; ++i){
                    JSONObject snippet = items.getJSONObject(i).getJSONObject("snippet");
                    RM_MusicData mData = new RM_MusicData();
                    mData.setId(musicId);
                    mData.setTitle(snippet.getString("title"));
                    if(mData.getTitle().equals("Deleted video") || mData.getTitle().equals("")){
                        continue;
                    }
                    mData.setPlayListId(playlistId);
                    if(snippet.has("thumbnails") && snippet.getJSONObject("thumbnails").has("medium")) {
                        mData.setThumbnail(snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url"));
                    }else{
                        mData.setThumbnail("");
                    }
                    mData.setUploader(snippet.getString("channelTitle"));
                    mData.setDescription(snippet.getString("description"));
                    mData.setVideoId(snippet.getJSONObject("resourceId").getString("videoId"));
                    mList.add(mData);
                    musicId++;
                }
                if(jsonObject.has("nextPageToken")){
                    getNext(jsonObject.getString("nextPageToken"), mList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mList;
        }

        @Override
        protected void onPostExecute(final ArrayList<RM_MusicData> mDatas) {
            super.onPostExecute(mDatas);
            realmConfig = new RealmConfiguration.Builder(mContext).build();
            realm = Realm.getInstance(realmConfig);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for(RM_MusicData mData : mDatas){
                        realm.copyToRealm(mData);
                    }
                }
            });
            hideProgressDialog();
            Toast.makeText(mContext, input + "에 "+ mDatas.size() +"곡이 추가되었습니다.", Toast.LENGTH_SHORT).show();
        }

        public void getNext(String nextPageToken, ArrayList<RM_MusicData> mList) {
            try {
                String doc = Jsoup.connect(
                        YouTubeClient.BASE_URL + "/playlistItems?" +
                                "part=snippet&maxResults=50&key="+YouTubeClient.key_list+"&playlistId="+id+"&pageToken="+nextPageToken
                ).ignoreContentType(true).execute().body();
                JSONObject jsonObject = new JSONObject(doc);
                totResults = jsonObject.getJSONObject("pageInfo").getInt("totalResults");
                JSONArray items = jsonObject.getJSONArray("items");
                for(int i = 0 ; i < items.length() ; ++i){
                    JSONObject snippet = items.getJSONObject(i).getJSONObject("snippet");
                    RM_MusicData mData = new RM_MusicData();
                    mData.setId(musicId);
                    mData.setTitle(snippet.getString("title"));
                    if(mData.getTitle().equals("Deleted video") || mData.getTitle().equals("")){
                        continue;
                    }
                    mData.setPlayListId(playlistId);
                    if(snippet.has("thumbnails") && snippet.getJSONObject("thumbnails").has("medium")) {
                        mData.setThumbnail(snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url"));
                    }else{
                        mData.setThumbnail("");
                    }
                    mData.setUploader(snippet.getString("channelTitle"));
                    mData.setDescription(snippet.getString("description"));
                    mData.setVideoId(snippet.getJSONObject("resourceId").getString("videoId"));
                    mList.add(mData);
                    musicId++;
                }
                if(jsonObject.has("nextPageToken")){
                    getNext(jsonObject.getString("nextPageToken"), mList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}