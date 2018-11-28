package com.vitec.task.smartrule.dfu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.vitec.task.smartrule.dfu.service.UpdateFirmIntentService;

public class NotificationActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {
            // Start the app before finishing
            final Intent intent = new Intent(this, UpdateFirmIntentService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                intent.putExtras(bundle); // copy all extras
//                startActivity(intent);
                startService(intent);
            }
        }

        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish();
    }
}
