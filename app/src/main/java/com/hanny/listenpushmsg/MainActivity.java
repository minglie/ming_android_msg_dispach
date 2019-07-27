package com.hanny.listenpushmsg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hanny.listenpushmsg.service.NotifyService;
import com.hanny.listenpushmsg.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvMsg;
    public static String postUrl="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //开始监听
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEnable(MainActivity.this)) {
                    openSetting(MainActivity.this);
                    toggleNotificationListenerService(MainActivity.this);
                }
            }
        });
        tvMsg = findViewById(R.id.tvMsg);

        registBroadCast();
    }


    private void openSetting(Context context) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private boolean isEnable(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void registBroadCast() {
        IntentFilter filter = new IntentFilter(NotifyService.SEND_MSG_BROADCAST);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            tvMsg.setText(msg);

            Map map=new HashMap(1);
            map.put("msg",msg);

            HttpUtil.post(MainActivity.postUrl, map, new HttpUtil.HttpCallBack() {
                @Override
                public void onSuccess(String result) {
                    Log.i("HttpUtil_onSuccess",result);
                }
                @Override
                public void onError(Exception e) {
                    Log.i("HttpUtil_onError",e.getMessage());
                }
                @Override
                public void onFinish() {
                }
            });

        }
    };

    public void setPackageName(View view){
        Button btn= (Button)view;
        EditText editText = (EditText)findViewById(R.id.pkgNameText);
        NotifyService.packageName=editText.getText().toString().trim();
        btn.setText(NotifyService.packageName);
    }


    public void setPostUrl(View view){
        Button btn= (Button)view;
        EditText editText = (EditText)findViewById(R.id.postUrlText);
        MainActivity.postUrl=editText.getText().toString().trim();
        btn.setText(MainActivity.postUrl);
    }

}
