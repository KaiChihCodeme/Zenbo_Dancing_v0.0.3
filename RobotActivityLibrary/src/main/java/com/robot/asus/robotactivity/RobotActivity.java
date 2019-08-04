package com.robot.asus.robotactivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;


public class RobotActivity extends Activity{
    public RobotAPI robotAPI;
    RobotCallback robotCallback;
    RobotCallback.Listen robotListenCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);
    }

    public RobotActivity (RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        this.robotCallback = robotCallback;
        this.robotListenCallback = robotListenCallback;
    }

    @Override
    protected void onPause() {
        super.onPause();
        robotAPI.robot.unregisterListenCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(robotListenCallback!= null)
            robotAPI.robot.registerListenCallback(robotListenCallback);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robotAPI.release();
    }
}
