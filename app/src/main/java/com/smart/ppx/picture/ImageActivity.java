package com.smart.ppx.picture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.example.base.download.FileUtil;
import com.smart.ppx.R;
import com.example.base.download.DownloadListener;

import java.util.ArrayList;

public class ImageActivity extends Activity {

    public static final String PICTURE = "picture";

    public static final String POSTFIX = "postfix";

    private String name;

    TextView textView;
    ViewPager viewPager;
    ImageView imageView;

    ArrayList<String> images;

    public static void start(Context context, String postfix, ArrayList<String> urls) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(POSTFIX, postfix);
        intent.putExtra(PICTURE, urls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        textView = findViewById(R.id.index);
        viewPager = findViewById(R.id.main);
        imageView = findViewById(R.id.down);

        images = getIntent().getStringArrayListExtra(PICTURE);
        String postfix = getIntent().getStringExtra(POSTFIX);
        name = postfix.replaceAll("/", "");
        viewPager.setAdapter(new ImagePagerAdapter(this, images));

        textView.setText(1 + "/" + images.size());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                textView.setText(i + 1 + "/" + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int position = viewPager.getCurrentItem();
                download(name + position, images.get(position));
            }
        });
    }

    private void updateAlbum(String fileName) {
        MediaScannerConnection.scanFile(this, new String[]{fileName},
                new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    private void download(String name, String url) {
        FileUtil.savePicture(url, name, new DownloadListener() {
            @Override
            public void start(long max) {
            }

            @Override
            public void loading(int progress) {
            }

            @Override
            public void complete(String path) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        toast("已保存到" + FileUtil.IMAGE + "目录");
                    }
                });
                updateAlbum(path);
            }

            @Override
            public void fail(int code, String message) {
            }

            @Override
            public void loadFail(String message) {
            }
        });
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
