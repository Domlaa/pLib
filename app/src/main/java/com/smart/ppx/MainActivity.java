package com.smart.ppx;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.example.base.BaseActivity;
import com.example.base.download.FileUtil;
import com.example.base.net.HttpClient;
import com.example.base.net.IClient;
import com.smart.ppx.bean.Result;
import com.example.base.download.DownloadListener;
import com.smart.ppx.okhttp.HttpManager;
import com.smart.ppx.okhttp.IResponseListener;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import org.json.JSONObject;

import static com.smart.ppx.Config.IMAGE;
import static com.smart.ppx.Config.KEY;
import static com.smart.ppx.Config.PLAY;
import static com.smart.ppx.Config.PREFIX;
import static com.smart.ppx.Config.SHORT;
import static com.smart.ppx.Config.TAG;


public class MainActivity extends BaseActivity {

    private String prefix;
    private String downloadUrl;
    ClipboardManager clipboard;
    private TextView textView;
    private final StringBuilder builder = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                clipboard.setText(downloadUrl);
                toast("复制成功");
            }
        });
    }

    private boolean linkLegal(String url) {
        return url.contains(PREFIX) && url.length() == 31;
    }

    private String pasteLegal() {
        String pasteString = clipboard.getText().toString();
        if (linkLegal(pasteString)) {
            return pasteString;
        } else {
            toast("请在粘贴板复制正确链接,当前粘贴板内容：" + pasteString + ",长度：" + pasteString.length());
        }
        return null;
    }

    private void appendText(String text) {
        builder.append(text);
        builder.append("\n");
        textView.setText(builder.toString());
    }

    private void clearText() {
        builder.setLength(0);
        textView.setText("");
    }

    /**
     * 1.获取粘贴板，检测合理
     * 2.根据分享连接获取id
     * 3.直接打开
     */
    public void open(View view) {
        String pasteString = pasteLegal();
        if (pasteString != null) {
            try {
                getId(pasteString, new HttpClient.Callback() {
                    @Override public void complete(String result) {
                        clipboard.setText(downloadUrl);
                        toast("复制成功");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private static void getId(final String path, final HttpClient.Callback callback) {
        new AsyncTask<Void, Void, Exception>() {
            String result;

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(path)
                            .openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(5000);
                    String url = conn.getHeaderField("Location");
                    result = url.substring(26, url.indexOf("?"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                callback.complete(result);
            }
        }.execute();
    }


    public void down(View view) {
        clearText();
        String url = pasteLegal();
        if (url == null) {
            return;
        }
        prefix = url.substring(23, 29);
        if (FileUtil.videoExist(prefix)) {
            toast("文件已存在！");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions("请授予以下权限", permissions, callback);
        }
    }


    /**
     * @param id 视频id
     */
    private void openPPX(String id) {
        Log.i(TAG, "openPPX: " + id);
        //com.sup.android.detail.ui.DetailActivity
        // bds://cell_detail?item_id=6800586962215377163
        Intent intent = new Intent("android.intent.action.VIEW",
                android.net.Uri.parse("bds://cell_detail?item_id=" + id));
        intent.setPackage(Config.PACKAGE);
        startActivity(intent);
    }


    private final PermissionCallback callback = new PermissionCallback() {
        @Override
        public void hasPermission() {
            final String url = pasteLegal();
            if (url != null) {
                appendText("share url： " + url);
                getId(url, new HttpClient.Callback() {
                    @Override public void complete(String result) {
                        getDownJson(result);
                    }
                });
                //getPlaySource(url.substring(23, 30));
            }
        }

        @Override
        public void noPermission() {
            toast("下载失败，请授予存储权限");
        }
    };


    /**
     * @param id  视频id
     */
    public void getDownJson(String id) {
        appendText("video id： " + id);
        Log.i(TAG, "getDownJson: id " + id);
        // https://h5.pipix.com/bds/webapi/item/detail/?item_id=6940091573933709604&source=share
        //String apiUrl = "https://h5.pipix.com/bds/webapi/item/detail/?item_id=" + id + "&source=share";
        HttpManager.getInstance().getWithId(id,
                new IResponseListener() {
                    @Override
                    public void onSuccessful(Result data) {
                        String path = getPlayUrl(data.getData());
                        appendText("download url： " + path);
                        downloadUrl = path;
                        Log.i(TAG, "--->>> path " + path);
                        downVideo(prefix, path);
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        toast("访问错误:" + errorMsg);
                    }

                    @Override
                    public void finished() {
                    }
                });
    }


    private String getPlayUrl(String data) {
        // ['data']['item']['video']['video_high']['url_list'][0]['url']
        try {
            JSONObject json = new JSONObject(data);
            JSONObject cell = json.getJSONObject("data")
                    .getJSONArray("cell_comments")
                    .getJSONObject(0);

            JSONObject item = cell.getJSONObject("comment_info").getJSONObject("item");
            JSONObject video = item.getJSONObject("video");
            String name = video.getString("text");
            appendText("video name: " + name);
           // Log.i(TAG, "--->>> video " + video);
            print(TAG, video.toString());
            Log.i(TAG, "--->>> tt " + video.has("video_high"));

            JSONObject high = video.getJSONObject("video_high");
            JSONObject urlList = high.getJSONArray("url_list").getJSONObject(0);
            return urlList.getString("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    private void downVideo(String prefix, String url) {
        if (url == null) return;
        //实例化进度条对话框（ProgressDialog）
        final ProgressDialog pd = new ProgressDialog(this);
        //设置对话进度条样式为水平
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置提示信息
        pd.setMessage("正在下载....");
        //设置对话进度条显示在屏幕顶部（方便截图）
        pd.getWindow().setGravity(Gravity.CENTER);
        pd.setMax(100);
        FileUtil.download(prefix, url, new DownloadListener() {
            @Override
            public void start(long max) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.show();
                        toastOnUI("开始下载。。。");
                    }
                });
            }

            @Override
            public void loading(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.setProgress(progress);
                    }
                });
            }

            @Override
            public void complete(final String path) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.dismiss();
                        toastOnUI(path + "已下载到PPX目录下");
                    }
                });
                updateAlbum(path);
            }

            @Override
            public void fail(int code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.dismiss();
                        toastOnUI("下载失败" + message);
                    }
                });
            }

            @Override
            public void loadFail(final String message) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.dismiss();
                        toastOnUI("下载失败" + message);
                    }
                });
            }
        });
    }

    private void updateAlbum(String fileName) {
        MediaScannerConnection.scanFile(this, new String[] { fileName },
                new String[] { "video/mp4" }, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }


    public void link(View view) {
        String pasteString = pasteLegal();
        if (pasteString != null) {
            String url = PLAY + pasteString.substring(23, 30);
            shorter(url, new HttpClient.Callback() {
                @Override public void complete(String url1) {
                    clipboard.setText(url1);
                    toast(url1);
                }
            });
        }
    }


    @SuppressLint("StaticFieldLeak")
    public static void shorter(final String url, final HttpClient.Callback callback) {
        new AsyncTask<Void, Void, Exception>() {
            String result;

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    String formatUrl = URLEncoder.encode(url, "UTF-8");
                    IClient client = new HttpClient(SHORT);
                    client.addParam("url", formatUrl);
                    client.addParam("key", KEY);
                    result = client.get();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                super.onPostExecute(exception);
                System.out.println("result::" + result);
                callback.complete(result);
            }
        }.execute();
    }

    public void pic(View view) {
        String pasteString = pasteLegal();
        if (pasteString != null) {
            try {
                image(pasteString.substring(23, 30));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void image(String postfix) {
        HttpManager.getInstance().get(IMAGE + postfix,
                new IResponseListener() {
                    @Override
                    public void onSuccessful(Result data) {
//                        JSONArray list = JSONArray.parseArray(data.getData());
//                        if (list != null && list.size() > 0) {
//                            ArrayList<String> images = new ArrayList<>();
//                            for (int a = 0; a < list.size(); a++) {
//                                images.add(list.getString(a));
//                            }
//                            ImageActivity.start(MainActivity.this, postfix, images);
//                        } else {
//                            toast(data.getMsg());
//                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        toast("访问错误" + errorMsg);
                    }

                    @Override
                    public void finished() {
                    }
                });
    }

    public static void print(String tag, String msg) {
        if (tag == null || tag.length() == 0
                || msg == null || msg.length() == 0)
            return;

        int segmentSize = 3 * 1024;
        long length = msg.length();
        if (length <= segmentSize ) {// 长度小于等于限制直接打印
            Log.e(tag, msg);
        }else {
            while (msg.length() > segmentSize ) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize );
                msg = msg.replace(logContent, "");
                Log.e(tag, logContent);
            }
            Log.e(tag, msg);// 打印剩余日志
        }
    }

    public void file(View view) {
        String pasteString = clipboard.getText().toString();
        if (pasteString.contains("http")) {
            downVideo(UUID.randomUUID().toString(), pasteString);
        } else {
            toast("请在粘贴板复制正确链接,当前粘贴板内容：" + pasteString + ",长度：" + pasteString.length());
        }
    }


}
