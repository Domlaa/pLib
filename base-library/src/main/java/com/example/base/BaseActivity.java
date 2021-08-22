package com.example.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//**************** Android M Permission (Android 6.0权限控制代码封装)
//底层权限封装  任何需要使用到权限的activity都应该继承自此基类
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseA--->>>";
    private final int permissionRequestCode = 88;
    private PermissionCallback permissionRunnable;
    public Context context;

    protected NotificationManager notifyManager;

    protected Vibrator vibrator;

    protected View view = null;

    private static boolean inBack;

    /**
     *  @description 该方法仅在fragment中的onStop方法中调用
     *  如果需要判断activity是否后台，请调用application里的方法

     *  情景1：跳到ChatActivity。此时的ChatActivity先调用
     *        {@link BaseActivity#onStart()}方法，设置结果为false
     *        然后才是fragment的onStop方法执行
     *
     *  情景2：直接后台。得到的值是上一个activity的
     *        {@link BaseActivity#onStop()}带来的结果，结果是true
     */
    public boolean isInBack(){
        return inBack;
    }
    @Override
    protected void onStart() {
        inBack = false;
        super.onStart();
    }

    @Override
    protected void onStop() {
        inBack = true;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public interface PermissionCallback {
        void hasPermission();
        void noPermission();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
       /* if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "battery_channel", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setShowBadge(true);
            mNotifyManager.createNotificationChannel(channel);
        }*/

        view = getLayoutInflater().inflate(layoutResID,null);

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllGranted = true;
        switch (requestCode) {
            case permissionRequestCode: {
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {//有一个没通过
                        hasAllGranted = false;
                        //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                        // 可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            //第一次询问后拒绝不会经过这里, 第一次拒绝是没办法选择不再提醒的 此时这个值为true
                            //3.永久拒绝经过这里
                            //添加返回按钮
                            new AlertDialog.Builder(context).setTitle("提示")
                                    .setMessage("请到设置界面打开相关权限，否则应用将无法正常运行")
                                    .setPositiveButton("去授权", (dialog, which) -> {//去设置页面开启权限
                                        //TODO Auto-generated method stub
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }).setNegativeButton("取消", (dialog, which) -> {//响应事件
                                        dialog.dismiss();
                                    }).setOnCancelListener(dialog -> permissionRunnable.noPermission()).show();
                        } else {
                            //用户拒绝权限请求，但未选中“不再提示”选项(2-1.第一次询问后拒绝,无法勾选"不再提醒"，变成情况4)
                            //4.拒绝过了但未勾选“下次不再提醒”
                            permissionRunnable.noPermission();
                        }
                        break;
                    }
                }
                if (hasAllGranted) {//2-2.第一次询问时全都通过授权
                    permissionRunnable.hasPermission();
                }
            }
        }
    }

    /**
     * 发起权限请求
     * @param permissions
     * @param callback
     *
     */
    public void requestPermissions(String desc, final String[] permissions, PermissionCallback callback) {
        this.permissionRunnable = callback;
        //如果所有权限都已授权，则直接返回授权成功,只要有一项未授权，则发起权限请求
        boolean isAllGranted = true;
        for (String permission : permissions) {  //逐一遍历
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) { //存在未授权的：（第一次 或 永久拒绝 或 拒绝但未勾选“下次不再提醒”）
                isAllGranted = false; //存在未授权的 置为false
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) { // 4.拒绝过了但未勾选“下次不再提醒”
                    new AlertDialog.Builder(context).setTitle("提示").setMessage(desc)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {//使用自构对话框询问 此时的对话框是有"不再提醒"的
                                  //  ActivityCompat.requestPermissions(((Activity) context), permissions, permissionRequestCode);
                                }
                            }).show();//在按键响应事件中显示此对话框
                    ActivityCompat.requestPermissions(((Activity) context), permissions, permissionRequestCode);
                } else { //2.第一次 或 3.永久拒绝
                    ActivityCompat.requestPermissions(((Activity) context), permissions, permissionRequestCode);
                }
                break;
            }
        }
        if (isAllGranted) {//1.之前已经全都通过授权了
            permissionRunnable.hasPermission();
        }
    }


    protected void toActivity(Class clazz){
        Intent intent = new Intent(context,clazz);
        startActivity(intent);
    }




    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    //****************常用方法缩写****************//
    public void toast(int res){
        toast(String.valueOf(res));
    }

    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void toastOnUI(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast(msg);
            }
        });
    }


}
