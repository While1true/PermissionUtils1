# PermissionUtils
6.0动态授权封装
# PermissionUtils
6.0动态授权封装

##使用
```
//appcompatActivity 直接传this   fragment中授权 传getActivity
 PermissionUtils.requestPermission(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , new PermissionUtils.PermissionListener() {
                    @Override
                    public void onResult(String i, boolean granted,String info) {
                    /i 申请的的权限  granted 是否被授权  info授权方式（{"已经授权过", "授权成功", "取消授权", "设置界面授权", "设置界面未授权"}）
                        Log.i("DEBUGa", "onResult: "+i+"----"+granted+"--"+info);
                    }
                });
```
#日志##
```
DEBUGa: onResult: android.permission.CALL_PHONE----false--设置界面未授权

```


##内部利用fragment，省掉在activity重写回掉

##详细使用请看之前拙文，或者utils源码
[你所不知道的Fragment的妙用](http://www.jianshu.com/p/57ce48c73dbe)


