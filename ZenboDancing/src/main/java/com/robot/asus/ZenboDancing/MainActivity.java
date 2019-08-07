package com.robot.asus.ZenboDancing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.robot.asus.ZenboDancing.R;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

public class MainActivity extends RobotActivity {

    private LinearLayout man,lady;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        man = (LinearLayout)findViewById(R.id.man);
        lady = (LinearLayout)findViewById(R.id.lady);

        man.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // next layout
                Intent intent = new Intent();
                intent.putExtra("motion", "man");
                intent.setClass(MainActivity.this, com.robot.asus.ZenboDancing.DanceActivity.class);
                startActivity(intent);
            }
        });

        lady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // next layout
                Intent intent = new Intent();
                intent.putExtra("motion", "lady");
                intent.setClass(MainActivity.this, com.robot.asus.ZenboDancing.DanceActivity.class);
                startActivity(intent);
            }
        });


    }

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        robotAPI.robot.speak("who am i?");
    }
}
