package com.example.permissionlib;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by ck on 2017/8/3.
 */

public class PermissionFragment extends Fragment {

    private PermissionUtils.PermissionListener listener;

    public void setListener(PermissionUtils.PermissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }
}
