package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Window;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.CopyDbFileFromAsset;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;

import java.io.IOException;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        initDb();

    }

    private void initDb() {
         CopyDbFileFromAsset copyDbFileFromAsset = new CopyDbFileFromAsset(getApplicationContext());
        try {
            copyDbFileFromAsset.CopySqliteFileFromRawToDatabases(DataBaseParams.databaseName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final User user = OperateDbUtil.getUser(getApplicationContext());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user.getUserID() > 0 ) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    SplashActivity.this.finish();
                } else {
                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    SplashActivity.this.finish();
                }
            }
        }, 1000);

    }
}
