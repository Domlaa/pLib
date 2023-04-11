package com.example.base.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileUtil {

    private static final String TAG = "FileUtil--->>>";
    public static final String VIDEO = "/PPX/video";
    public static final String IMAGE = "/PPX/images";

    //第一次下载
    public static Call download(String prefix, String url, DownloadListener downloadListener) {
        return download(downloadListener, 0, url, prefix);//第一次下载，默认为0
    }

    //续传
    /*public static Call reDownload(final DownloadListener downloadListener,Float version){
        return download(downloadListener,getFileStart(version),version);//获取文件已下载进度
    }*/

    private static Call download(final DownloadListener downloadListener, long startsPoint, String url, String prefix) {
        return download(downloadListener, url, startsPoint, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadListener.fail(201, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long length = response.body().contentLength();

                if (length == 0) {
                    downloadListener.complete(getVideo(prefix).getPath());
                    return;
                }
                downloadListener.start(length + startsPoint);
                // 保存文件到本地
                InputStream is = null;
                RandomAccessFile randomAccessFile = null;
                BufferedInputStream bis = null;

                byte[] buff = new byte[2048];
                int len = 0;
                try {
                    is = response.body().byteStream();
                    bis = new BufferedInputStream(is);

                    File file = getVideo(prefix);
                    // 随机访问文件，可以指定断点续传的起始位置
                    randomAccessFile = new RandomAccessFile(file, "rwd");
                    randomAccessFile.seek(startsPoint);
                    while ((len = bis.read(buff)) != -1) {
                        randomAccessFile.write(buff, 0, len);
                    }
                    // 下载完成
                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                } catch (Exception e) {
                    e.printStackTrace();
                    downloadListener.loadFail(e.getMessage());
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static Call savePicture(String url, String prefix, final DownloadListener downloadListener) {
        return savePicture(downloadListener, url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadListener.fail(201, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long length = response.body().contentLength();

                if (length == 0) {
                    downloadListener.complete(getImage(prefix).getPath());
                    return;
                }
                downloadListener.start(length);
                // 保存文件到本地
                InputStream is = null;
                RandomAccessFile randomAccessFile = null;
                BufferedInputStream bis = null;

                byte[] buff = new byte[2048];
                int len = 0;
                try {
                    is = response.body().byteStream();
                    bis = new BufferedInputStream(is);

                    File file = getImage(prefix);
                    // 随机访问文件，可以指定断点续传的起始位置
                    randomAccessFile = new RandomAccessFile(file, "rwd");
                    randomAccessFile.seek(0);
                    while ((len = bis.read(buff)) != -1) {
                        randomAccessFile.write(buff, 0, len);
                    }
                    // 下载完成
                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                } catch (Exception e) {
                    e.printStackTrace();
                    downloadListener.loadFail(e.getMessage());
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private static Call savePicture(final DownloadListener downloadListener, String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();

        // 重写ResponseBody监听请求
        Interceptor interceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new DownloadRspBody(originalResponse, 0, downloadListener))
                    .build();
        };

        OkHttpClient.Builder dlOkhttp = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor);

        // 发起请求
        Call call = dlOkhttp.build().newCall(request);
        call.enqueue(callback);
        return call;
    }

    private static Call download(final DownloadListener downloadListener,
                                 String url, final long startsPoint, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startsPoint + "-")//断点续传
                .build();

        // 重写ResponseBody监听请求
        Interceptor interceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new DownloadRspBody(originalResponse, startsPoint, downloadListener))
                    .build();
        };

        OkHttpClient.Builder dlOkhttp = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor);

        // 发起请求
        Call call = dlOkhttp.build().newCall(request);
        call.enqueue(callback);
        return call;
    }


    private static File getImage(String name) {
        String root = getExternalStoragePath() + IMAGE;
        File folder = new File(root);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = name + ".jpg";
        return new File(root, fileName);
    }

    private static File getVideo(String name) {
        String root = getExternalStoragePath() + VIDEO;
        File folder = new File(root);
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            Log.d(TAG, "getVideo: folder = " + root + ", result = " + result);
        }
        String fileName = name + ".mp4";
        return new File(root, fileName);
    }

    public static boolean videoExist(String name) {
        String root = getExternalStoragePath() + VIDEO;
        String fileName = name + ".mp4";
        File file = new File(root, fileName);
        return file.exists();
    }

//    public static String getPath(String prefix) {
//        String root = getExternalStoragePath() + VIDEO;
//        String fileName = prefix + ".mp4";
//        return root + fileName;
//    }

    private static String getExternalStoragePath() {
        File rootDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//有SD卡
            rootDir = Environment.getExternalStorageDirectory();
        } else {
            rootDir = Environment.getDataDirectory();
        }
        Log.d(TAG, "getExternalStoragePath: root = " + rootDir + ", state: " + Environment.getExternalStorageState());
        return rootDir.getPath();
    }

    // 保存图片到手机
    @SuppressLint("StaticFieldLeak")
    public static void download(Context context, final String url) {
        new AsyncTask<Void, Integer, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                File file;
                try {
                    file = Glide.with(context)
                            .load(url)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    // 首先保存图片
                    File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();

                    File appDir = new File(pictureFolder, "Beauty");
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + ".jpg";
                    File destFile = new File(appDir, fileName);
                    copy(file, destFile);
                } catch (Exception e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
                if (e == null)
                    Toast.makeText(context, "图片已保存到文件夹/Pictures/Beauty/下", Toast.LENGTH_SHORT).show();
                else Toast.makeText(context, "保存失败," + e, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }
        }.execute();
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    private static void copy(File source, File target) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}