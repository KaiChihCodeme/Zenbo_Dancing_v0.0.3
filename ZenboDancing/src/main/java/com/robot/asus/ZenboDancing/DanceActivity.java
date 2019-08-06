package com.robot.asus.ZenboDancing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.robot.asus.ZenboDancing.R;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DanceActivity extends RobotActivity {
    private String TAG = "DanceActivity";
    private TextView start;
    private static String gender;
    private MediaPlayer music_cha = new MediaPlayer();
    final Handler handler = new Handler();
    private int motion;
    private int state_man;
    private int state_lady;
    private static int motion_number = 1; //this zenbo next want to do
    private static int iCurrentSpeakSerial;

    private static FirebaseFirestore db =FirebaseFirestore.getInstance();
    private static DocumentReference docRef = db.collection("dancing").document("serial");
    private static Map<String, Object> FirebaseData = new HashMap<>();

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

            if (serial == iCurrentSpeakSerial && state != RobotCmdState.ACTIVE) {
                //如果講完話，就傳好了
                Log.d("RobotDevSample", "command: " + iCurrentSpeakSerial + " SUCCEED");
                uploadOKState();
            }
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
                    //robotAPI.robot.speak("Can you dance with me? miss?");
                    //uploadOKState();
                    /** ------------------------------------------------------------------------------------------------ */
                    //motion_number = "1";
                    //docRef.update("motion","1");
                   // FirebaseData.put("motion", "1"); // upload motion 1
                   // docRef.set(FirebaseData);
                    //uploadMotion();
                    commandSameTime();
                    downloadData(); // download both state and motion number

                    //compareData();
                    /** ------------------------------------------------------------------------------------------------ */

                    //motion_number = "2";
                    //FirebaseData.put("motion", motion_number); // 上傳motion 2

                    music_cha.start();
                    manDance();

                } else if (gender.equals("lady")) {
                    downloadforLadyData(); // download both state and motion number
                    //compareData(); // if motion number in db is 1, will operate below instruction, and init lady state
                    /*while (!motion.equals("1")) {
                        downloadData();
                    }*/
                    //compareData();

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

        robotAPI.motion.moveBody(0f,0f,-3.14f);
        robotAPI.motion.moveBody(1f,0f,0f);
        robotAPI.motion.moveBody(0f,0f,1.57f);
        robotAPI.motion.moveBody(-0.1f,0f,0f);
        robotAPI.utility.playAction(22);
        robotAPI.motion.moveBody(0f,0f,-3.14f);
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        robotAPI.motion.moveBody(1f,0f,0f);
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
        robotAPI.motion.moveBody(0f,0f,3.14f);
        robotAPI.motion.moveBody(1f,0f,0f);
        robotAPI.motion.moveBody(0f,0f,-1.57f);
        robotAPI.motion.moveBody(-0.1f,0f,0f);
        robotAPI.utility.playAction(22);
        robotAPI.motion.moveBody(0f,0f,3.14f);
        robotAPI.motion.moveBody(0f,0f,1.57f);
        robotAPI.motion.moveBody(1f,0f,0f);
    }

    /*private void uploadMotion() {
        //one of the zenbo upload the next motion
        FirebaseData.put("motion", motion_number);
        docRef.set(FirebaseData);
    }*/

    private static void uploadOKState() {
        //upload man or lady's stand by state
        //代表我做完上個動作了，準備做下個動作
        if (gender.equals("man")) {
            FirebaseData.put("state_man", 1); //1 represent he finish the motion and stand by.
            docRef.set(FirebaseData);
        } else if (gender.equals("woman")) {
            FirebaseData.put("state_woman", 1);
            docRef.set(FirebaseData);
        }
    }

    private void downloadData() {
        //firebase

        handler.post(new Runnable() {

            int temp = -1;
            @Override
            public void run() {

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                motion = ((Number)document.getData().get("motion_man")).intValue();
                                if (temp == motion) {

                                } else {
                                    //do
                                    if (motion == 1)
                                        iCurrentSpeakSerial = robotAPI.robot.speak("Can you dance with me? miss?"); //因為這句要先講
                                }
                                temp = motion;
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

                handler.postDelayed(this, 10);
            }
        });
        /*docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", source + " data: " + snapshot.getData());
                    FirebaseData = snapshot.getData();
                    Log.d("fuck","fuck");
                    Log.d("fuck",FirebaseData.toString());
                    //Retrieve the state from the database
                    motion = ((Number)FirebaseData.get("motion_man")).intValue();
                    //state_man = FirebaseData.get("state_man").toString();
                    //state_lady = FirebaseData.get("state_lady").toString();
                    Log.d(TAG, "motion: " + motion + ",\n state_man: " + state_man + ",\n state_lady: " + state_lady);
                    //compareData();
                    //做事
                    if (motion == 1)
                        iCurrentSpeakSerial = robotAPI.robot.speak("Can you dance with me? miss?"); //因為這句要先講
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
        //firebase*/
    }

    private void downloadforLadyData() {
        //firebase
        /*docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", source + " data: " + snapshot.getData());
                    FirebaseData = snapshot.getData();
                    Log.d("fuck","fuck");
                    Log.d("fuck",FirebaseData.toString());
                    //Retrieve the state from the database
                    motion = ((Number)FirebaseData.get("motion_lady")).intValue();
                    /*if (motion.equals("1")) {
                        state_man = FirebaseData.get("state_man").toString();
                        if (state_man.equals("1")) {

                            state_lady = FirebaseData.get("state_lady").toString();
                            Log.d(TAG, "motion: " + motion + ",\n state_man: " + state_man + ",\n state_lady: " + state_lady);
                            compareData();
                        }
                    }*/
                    /*//做事
                    if (motion == 1)
                        iCurrentSpeakSerial = robotAPI.robot.speak("sure!");
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });*/
        //firebase
        handler.post(new Runnable() {

            int temp = -1;
            @Override
            public void run() {

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                motion = ((Number)document.getData().get("motion_man")).intValue();
                                if (temp == motion) {

                                } else {
                                    //do
                                    if (motion == 1)
                                        iCurrentSpeakSerial = robotAPI.robot.speak("Sure! Let's Dance!"); //因為這句要先講
                                }
                                temp = motion;
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

                handler.postDelayed(this, 10);
            }
        });
    }

    private void commandSameTime() {
        //firebase
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", source + " data: " + snapshot.getData());
                    FirebaseData = snapshot.getData();
                    Log.d("fuck","fuck");
                    Log.d("fuck",FirebaseData.toString());
                    //Retrieve the state from the database
                    state_man = ((Number)FirebaseData.get("state_man")).intValue();
                    state_lady = ((Number)FirebaseData.get("state_lady")).intValue();
                    //做事
                    if (state_man == 1 && state_lady == 1 ) {
                        //下一位
                        docRef.update("motion_man", motion_number);
                        docRef.update("motion_lady", motion_number);
                        docRef.update("state_man", 0);
                        docRef.update("state_lady", 0);
                    }
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
        //firebase
    }

    /*private void commandManFirst() {
        docRef.update("motion_man", motion_number);

        //firebase
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", source + " data: " + snapshot.getData());
                    FirebaseData = snapshot.getData();
                    Log.d("fuck","fuck");
                    Log.d("fuck",FirebaseData.toString());
                    //Retrieve the state from the database
                    state_man = ((Number)FirebaseData.get("state_man")).intValue();
                    //做事
                    if (state_man == 1) {
                        docRef.update("motion_lady", motion_number);

                        //firebase
                        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w("TAG", "Listen failed.", e);
                                    return;
                                }

                                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                        ? "Local" : "Server";

                                if (snapshot != null && snapshot.exists()) {
                                    Log.d("TAG", source + " data: " + snapshot.getData());
                                    FirebaseData = snapshot.getData();
                                    Log.d("fuck","fuck");
                                    Log.d("fuck",FirebaseData.toString());
                                    //Retrieve the state from the database
                                    state_lady = ((Number)FirebaseData.get("state_lady")).intValue();
                                    //做事
                                    if (state_lady == 1) {
                                        //下一位
                                    }
                                } else {
                                    Log.d(TAG, source + " data: null");
                                }
                            }
                        });
                        //firebase
                    }
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
        //firebase
    }*/

    private void compareData() {
        /*if (!(state_man.equals("1") && state_lady.equals("1"))) {
            // if don't meet any conditions, it will enter a loop until meet all conditions
            //都做完上個動作才能做現在這個動作
            downloadData(); // 如果有一方還沒好，就繼續抓資料庫的狀態
        } else {
            //到這邊代表兩邊都做好了
            //歸零
            if (gender.equals("man")) {
                FirebaseData.put("state_man", "0"); // if data is correct, we should init the data. 0 represent false.
            } else if (gender.equals("lady")) {
                FirebaseData.put("state_lady", "0");
            }
        }

        //到這邊條件達成後，代表都做好了這個動作，可以下一個動作了*/

        //while (!(state_man.equals("1") && state_lady.equals("1"))) {
        //    compareData();
        //}
        //if (state_man.equals("1") && state_lady.equals("1")) {
            //到這邊代表兩邊都做好了
            //歸零
            if (gender.equals("man")) {
                FirebaseData.put("state_man", "0"); // if data is correct, we should init the data. 0 represent false.
            } else if (gender.equals("lady")) {
                FirebaseData.put("state_lady", "0");
            }
        //}
    }

    /*
    private void initMotion() {
        // because 0 represent they both have retreived motion instruction and start motion
        while (!(state_man.equals("0") && state_lady.equals("0"))) {
            // if don't meet any conditions, it will enter a loop until meet all conditions
        }

        FirebaseData.put("motion", "0"); //initial the motion
    }*/


}
