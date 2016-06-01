package kr.edcan.u_stream.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by LNTCS on 2016-01-26.
 */

public class YouTubeClient {
    public static final String BASE_URL = "https://www.googleapis.com/youtube/v3";
    public static final String key_search= "AIzaSyCGfe8nemQS_9webbrBUODZKtC1PXcpiDM";
    public static final String key_list= "AIzaSyBidYxusin-5L013M4NDWhmvMojYJMtylc";

    public static final String API_URL = "http://www.youtubeinmp4.com/youtube.php?video=";
    public static final String REDIRECT_URL = "http://www.youtubeinmp4.com/";
    public static final String DOWNLOAD_URL = "http://www.w6.youtubeinmp3.com/download/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void search(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(BASE_URL + "/search", params, responseHandler);
    }
}
