package com.example.permissionlib;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ck on 2017/8/3.
 */

public class PermissionUtils {
    public static String[] infos = new String[]{"已经授权过", "授权成功", "取消授权", "设置界面授权", "设置界面未授权"};

    public static void requestPermission(final FragmentActivity activity, final String[] perssion, final PermissionListener listener) {
        if (Build.VERSION.SDK_INT < 23||perssion==null)
            return;
        List<String> arrasy = new ArrayList(1);
        boolean hasshow = false;
        A:
        for (int i = 0; i < perssion.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, perssion[i]) != PackageManager.PERMISSION_GRANTED) {

                //用户拒绝了提示
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perssion[i])) {
                    if (hasshow)
                        return;
                    new AlertDialog.Builder(activity)
                            .setMessage("用户选择了不再提示权限，或系统默认不提示")
                            .setTitle("需要权限" + perssion[i])
                            .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addResultListener(activity,listener,perssion);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(activity, "用户取消授权，系统某些功能可能无法正常使用", Toast.LENGTH_LONG).show();
                                }
                            })
                            .create().show();
                    hasshow = true;
                } else {
                    arrasy.add(perssion[i]);
                }
            } else {
                listener.onResult(perssion[i], true, infos[0]);
            }
        }
        addListener(activity, listener, arrasy);

    }

    private static void addListener(FragmentActivity activity, PermissionListener listener, List<String> arrasy) {
        if (!arrasy.isEmpty()) {

            PermissionFragment fragment = getAddPermissionFragment(activity);

            String[] needPermission = new String[arrasy.size()];
            for (int i = 0; i < arrasy.size(); i++) {
                needPermission[i] = arrasy.get(i);
            }

            fragment.setRequestString(needPermission).setListener(listener);

            fragment.requestPermissions(needPermission, PermissionFragment.REQUEST_PERMISSION);
        }
    }

    //设置页申请
    private static void addResultListener(FragmentActivity activity, PermissionListener listener, String[] all) {

        PermissionFragment fragment = getAddPermissionFragment(activity);
        List<String> needarray = new ArrayList(1);
        for (int i = 0; i < all.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, all[i]) != PackageManager.PERMISSION_GRANTED) {
                needarray.add(all[i]);
            }
        }
        String[] needPermission = new String[needarray.size()];
        for (int i = 0; i < needarray.size(); i++) {
            needPermission[i] = needarray.get(i);
        }

        fragment.setRequestString(needPermission).setListener(listener);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        fragment.startActivityForResult(intent, PermissionFragment.REQUEST_PERMISSION);

    }

    @NonNull
    private static PermissionFragment getAddPermissionFragment(FragmentActivity activity) {
        PermissionFragment fragment = null;
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment request_permission = manager.findFragmentByTag("REQUEST_PERMISSION");
        if (request_permission == null) {
            fragment = new PermissionFragment();
            manager.beginTransaction().add(fragment, "REQUEST_PERMISSION").commit();
            manager.executePendingTransactions();
        } else {
            fragment = (PermissionFragment) request_permission;
        }
        return fragment;
    }

    public interface PermissionListener {
        void onResult(String i, boolean granted, String info);
    }

    public static class PermissionFragment extends Fragment {
        private static int REQUEST_PERMISSION = 9999;

        private PermissionUtils.PermissionListener listener;
        private String[] permission;

        public void setListener(PermissionUtils.PermissionListener listener) {
            this.listener = listener;

        }

        public PermissionFragment setRequestString(String[] permission) {
            this.permission = permission;
            return this;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PermissionFragment.REQUEST_PERMISSION) {
                int length = grantResults.length;
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            listener.onResult(permissions[i], true, infos[1]);
                        } else {
                            listener.onResult(permissions[i], false, infos[2]);
                        }
                    }
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.i("PermissionUtils", "onActivityResult: " + requestCode);
            if (requestCode == PermissionFragment.REQUEST_PERMISSION) {
                if (permission != null) {
                    for (int i = 0; i < permission.length; i++) {
                        if (ContextCompat.checkSelfPermission(getActivity(), permission[i]) != PackageManager.PERMISSION_GRANTED) {
                            listener.onResult(permission[i], false, infos[4]);
                        } else {
                            listener.onResult(permission[i], true, infos[3]);
                        }
                    }
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            listener = null;
        }
    }
}
