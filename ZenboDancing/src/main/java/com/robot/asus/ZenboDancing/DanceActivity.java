package com.robot.asus.ZenboDancing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.robot.asus.ZenboDancing.R;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class DanceActivity extends RobotActivity {
    private String TAG = "DanceActivity";
    private TextView start;
    private String gender;
    private MediaPlayer music_cha = new MediaPlayer();
    final Handler handler = new Handler();

    public DanceActivity() {
        super(robotCallback, robotListenCallback);
    }

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
        setContentView(R.layout.activity_dance);

        Intent intent = getIntent();
        gender = intent.getStringExtra("motion");
        Log.d(TAG, "gender: " + gender);

        start =(TextView) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gender.equals("man")) {

                    music_cha.start();
                    manDance();

                } else if (gender.equals("lady")) {
                    ladyDance();

                }
            }});


    }

    @Override
    protected void onResume() {
        super.onResume();
        music_cha = MediaPlayer.create(this, R.raw.chacha);
    }

    @Override
    protected void onStop() {
        super.onStop();


    }
    @Override
    protected void onPause(){
        super.onPause();
        music_cha.stop();

    }


    private void musicPlay() {
        music_cha = MediaPlayer.create(this, R.raw.chacha);
        music_cha.start();
    }

    private void manDance(){
        robotAPI.robot.speak("Man");

        //順90
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        //前進
        robotAPI.motion.moveBody(1f,0f,0f);
        //順90
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        //前進
        robotAPI.motion.moveBody(0.5f,0f,0f);
        //逆90
        robotAPI.motion.moveBody(0f,0f,1.57f);
        //後退
        robotAPI.motion.moveBody(-0.1f,0f,0f);
        Timer timer =new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                manDance2();
            }
        };
        timer.schedule(task,20000);
    }

    private void manDance2(){

        //逆90
        robotAPI.motion.moveBody(0f,0f,1.57f);
        //前進
        robotAPI.motion.moveBody(0.5f,0f,0f);
    }
    private void ladyDance(){

        // float a = (float)Math.PI;
        robotAPI.robot.speak("Lady");
        //逆90
        robotAPI.motion.moveBody(0f,0f,1.57f);
        //前進
        robotAPI.motion.moveBody(1f,0f,0f);
        //逆90
        robotAPI.motion.moveBody(0f,0f,1.57f);
        //前進
        robotAPI.motion.moveBody(0.5f,0f,0f);
        //順90
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        //360
        robotAPI.motion.moveBody(0f,0f,3.13f);
        robotAPI.motion.moveBody(0f,0f,3.13f);
        //順90
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        //前進
        robotAPI.motion.moveBody(0.5f,0f,0f);
    }


}
