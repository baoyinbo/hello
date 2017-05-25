package com.gxb.gxbshare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.gxb.gxbshare.config.GSAppConfing;
import com.gxb.gxbshare.ui.base.BaseActivity;
import com.gxb.gxbshare.ui.base.CommonActivity;
import com.gxb.gxbshare.ui.base.LaunchBody;
import com.gxb.gxbshare.ui.debug.GSDebugActivity;
import com.gxb.gxbshare.ui.test.Test1;

public class MainActivity extends BaseActivity {
    private static int OVERLAY_PERMISSION_REQ_CODE = 1000;

    @Override
    public void doCreate(Bundle savedInstanceState) {
        setSwipeBackEnable(false);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initView() {
        if (GSAppConfing.IS_DEBUG) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    GSDebugActivity.initFloatBtn(getLazyApplication());
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                }
            } else {
                GSDebugActivity.initFloatBtn(getLazyApplication());
            }
//            if (PermissionUtils.checkPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
//                WJDebugActivity.initFloatBtn(getLazyApplication());
//            }
        }

        findViewById(R.id.btnJump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchBody.Builder builder = new LaunchBody.Builder(MainActivity.this,
                        Test1.class);
                builder.launchType(LaunchBody.LaunchType.SINGLE_TOP);
                CommonActivity.launch(builder);
            }
        });
    }




    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    GSDebugActivity.initFloatBtn(this);
                }
            }
        }
    }
}
