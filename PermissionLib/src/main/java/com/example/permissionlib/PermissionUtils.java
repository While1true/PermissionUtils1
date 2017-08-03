package com.example.permissionlib;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by ck on 2017/8/3.
 */

public class PermissionUtils {

    public static void requestPermission(final FragmentActivity activity, final String[] perssion, final PermissionListener listener) {
        if (Build.VERSION.SDK_INT < 23 || perssion == null)
            return;
        resetMapHolder();
        //第一次申请的权限
        List<String> firstPermission = new ArrayList(1);
        //拒绝过的的权限
        ArrayList<String> delayedPermission = new ArrayList(1);
        for (int i = 0; i < perssion.length; i++) {
            Log.i("PermissionUtils", "requestPermission: "+perssion[i]+ContextCompat.checkSelfPermission(activity, perssion[i])+"e"+ActivityCompat.shouldShowRequestPermissionRationale(activity, perssion[i]));
            if (ContextCompat.checkSelfPermission(activity, perssion[i]) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perssion[i])) {

                    addState(perssion[i], PackageManager.PERMISSION_DENIED, MapHolder.infos[4]);
                    delayedPermission.add(perssion[i]);
                } else {
                    addState(perssion[i], PackageManager.PERMISSION_DENIED, MapHolder.infos[2]);
                    firstPermission.add(perssion[i]);
                }
            } else {
                addState(perssion[i], PackageManager.PERMISSION_GRANTED, MapHolder.infos[0]);
            }
        }

        Log.i("PermissionUtils", "requestPermission:grantResultsMap " + MapHolder.grantResultsMap.toString());
        Log.i("PermissionUtils", "requestPermission:infoMap " + MapHolder.infoMap.toString());

        addListener(activity, listener, perssion,firstPermission, delayedPermission);

    }

    private static void addState(String permission, int Granted, String info) {
        MapHolder.grantResultsMap.put(permission, Granted);
        MapHolder.infoMap.put(permission, info);
    }

    private static void addListener(FragmentActivity activity, PermissionListener listener,String[]all, List<String> firstPermission, ArrayList<String> delayedPermission) {

        //先第一次的权限申请，拒绝过的最后申请提示
        if (!firstPermission.isEmpty()) {
            String[] needPermission = new String[firstPermission.size()];
            for (int i = 0; i < firstPermission.size(); i++) {
                needPermission[i] = firstPermission.get(i);
            }
            PermissionFragment fragment = getListenerFragment(activity, listener,all, delayedPermission);

            fragment.requestPermissions(needPermission, PermissionFragment.REQUEST_PERMISSION);
        } else if (!delayedPermission.isEmpty()) {
            PermissionFragment fragment = getListenerFragment(activity, listener,all, delayedPermission);

            fragment.dealDealyPermission();
        }

    }

    //设置监听
    @NonNull
    private static PermissionFragment getListenerFragment(FragmentActivity activity, PermissionListener listener, String[]all,ArrayList<String> delayedPermission) {
        PermissionFragment fragment = getAddPermissionFragment(activity);
        fragment
                .setAllPermission(all)
                .setDelayRequestString(delayedPermission)
                .setListener(listener);
        return fragment;
    }

    //把fragment添加到activity
    @NonNull
    private static PermissionFragment getAddPermissionFragment(FragmentActivity activity) {
        PermissionFragment fragment = null;
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment request_permission = manager.findFragmentByTag("REQUEST_PERMISSION");
        if (request_permission == null) {
            fragment = new PermissionFragment();
            manager.beginTransaction().add(fragment, "REQUEST_PERMISSION").commitNow();
//            manager.executePendingTransactions();
        } else {
            fragment = (PermissionFragment) request_permission;
        }
        return fragment;
    }

    public interface PermissionListener  {
        void onResult(String[] permission, int[] grantResults, String[] info);
    }

    public static class PermissionFragment extends Fragment {
        private static int REQUEST_PERMISSION = 9;
        private PermissionUtils.PermissionListener listener;
        private ArrayList<String> delayPermission;
        private String[]perssions;
        SharedPreferences permission;




        public void setListener(PermissionUtils.PermissionListener listener) {
            this.listener = listener;
        }

        public PermissionFragment setDelayRequestString(ArrayList<String> delayPermission) {
            this.delayPermission = delayPermission;
            return this;
        }
         public PermissionFragment setAllPermission(String[]perssion) {
             perssions=perssion;
            return this;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            permission = getContext().getSharedPreferences("permission", Context.MODE_PRIVATE);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PermissionFragment.REQUEST_PERMISSION) {
                if (perssions != null) {
                    int length = grantResults.length;
                    if (length > 0) {
                        for (int i = 0; i < length; i++) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                addState(permissions[i], PackageManager.PERMISSION_GRANTED, MapHolder.infos[1]);
                            } else {

                                //第一次拒绝
                                boolean aBoolean = permission.getBoolean(permissions[i], false);
                                if(aBoolean){
                                    delayPermission.add(permissions[i]);
                                }else{
                                    permission.edit().putBoolean(permissions[i],true).commit();
                                }
                                addState(permissions[i], PackageManager.PERMISSION_DENIED, MapHolder.infos[2]);
                            }
                        }
                    }
                    //申请延迟权限 就是已经拒绝过的，需要到设置界面申请
                    dealDealyPermission();
                }
            }
        }


        private void dealDealyPermission() {
            if (delayPermission != null && !delayPermission.isEmpty()) {
                final int size = delayPermission.size();
                StringBuilder delayBuilder = new StringBuilder(4);
                for (int i = 0; i < size; i++) {
                    try {
                        delayBuilder.append((i != 0 ? "、\n" : "") + delayPermission.get(i).split("android.permission.")[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.i("PermissionUtils", "dealDealyPermission: " + delayBuilder.toString());
                new AlertDialog.Builder(getActivity())
                        .setMessage("需要到设置以下权限：\n" + delayBuilder.toString())
                        .setTitle("权限被拒，某些功能可能受限")
                        .setCancelable(true)
                        .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goSetting();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i1 = 0; i1 < size; i1++) {
                                    addState(delayPermission.get(i1), PackageManager.PERMISSION_DENIED, MapHolder.infos[4]);
                                }
                                SendResult();
                            }
                        })
                        .create().show();
            } else {
                SendResult();
            }
        }


        private void SendResult() {
            int size = MapHolder.grantResultsMap.size();
            int[] granted = new int[size];
            String[] infos = new String[size];

            Log.i("PermissionUtils", "SendResult: " + MapHolder.grantResultsMap);
            for (int i = 0; i <perssions.length; i++) {
                granted[i] = MapHolder.grantResultsMap.get(perssions[i]);
                infos[i] = MapHolder.infoMap.get(perssions[i]);
            }

            listener.onResult(perssions
                    , granted
                    , infos);
        }

        private void goSetting() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getApplicationContext().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, PermissionFragment.REQUEST_PERMISSION);
        }



        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PermissionFragment.REQUEST_PERMISSION&&perssions!=null) {
                    for (int i = 0; i <perssions.length; i++) {
                        if (ContextCompat.checkSelfPermission(getActivity(),perssions[i]) != PackageManager.PERMISSION_GRANTED) {
                            addState(perssions[i], PackageManager.PERMISSION_DENIED, MapHolder.infos[4]);
                        } else {
                            if(delayPermission.contains(perssions[i]))
                            addState(perssions[i], PackageManager.PERMISSION_GRANTED, MapHolder.infos[3]);
                        }
                    }
                    Log.i("PermissionUtils", "requestPermission:grantResultsMap onActivityResult" + MapHolder.grantResultsMap.toString());
                    Log.i("PermissionUtils", "requestPermission:infoMap onActivityResult" + MapHolder.infoMap.toString());
                    SendResult();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.i("PermissionUtils", "onDestroy: ");
            listener = null;
            resetMapHolder();
            delayPermission = null;
            permission=null;
        }
    }

    private static void resetMapHolder() {
        MapHolder.grantResultsMap.clear();
        MapHolder.infoMap.clear();
    }

    private static class MapHolder {
        private static String[] infos = new String[]{"已经授权过", "授权成功", "取消授权", "设置界面授权", "设置界面未授权"};
        private static HashMap<String, Integer> grantResultsMap = new HashMap<>(1);
        private static HashMap<String, String> infoMap = new HashMap<>(1);
    }
}
