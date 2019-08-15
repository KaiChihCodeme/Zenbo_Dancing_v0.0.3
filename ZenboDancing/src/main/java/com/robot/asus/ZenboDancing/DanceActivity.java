package com.robot.asus.ZenboDancing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.WheelLights;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.robot.asus.ZenboDancing.R;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DanceActivity extends RobotActivity {
    private String TAG = "DanceActivity";
    private TextView start, genderTextView;
    private static String gender;
    private MediaPlayer music_cha = new MediaPlayer();
    final Handler handler = new Handler();
    final Handler handler2 = new Handler();
    final Handler handler3 = new Handler();
    final Handler handlerRemoteControl = new Handler();
    private int motion;
    private int state_man;
    private int state_lady;
    private static int motion_number; //this zenbo next want to do
    private static int iCurrentSpeakSerialNO;
    private static int iCurrentMoveSerial;
    private static int count = 0;
    private static boolean isDancing;
    private static Boolean EmergencyStop;
    private static Boolean isCanStartLady;
    private static Boolean isCanStartMan;

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
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

            if (serial == iCurrentSpeakSerialNO && state != RobotCmdState.ACTIVE) {
                //如果講完話，就傳好了
                Log.d("RobotDevSample", "serial: " + serial + "\ncommand: " + iCurrentSpeakSerialNO + " SUCCEED");
                uploadOKState();
            }

            if (serial == iCurrentMoveSerial && state == RobotCmdState.SUCCEED) {
                //如果講完話，就傳好了
                Log.d("RobotDevSample", "moveserial: " + serial + "\ncommand: " + iCurrentMoveSerial + " SUCCEED");
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

        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        gender = intent.getStringExtra("motion");

        genderTextView = (TextView) findViewById(R.id.genderTextView);
        genderTextView.setText(gender);

        start = (TextView) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //isDancing代表zenbo是否正在跳舞
                if (gender.equals("man") && !isDancing) {
                    getinitMotionNumber();
                    count = 3;
                    commandSameTime();
                    downloadData(); // download both state and motion number
                    /** ------------------------------------------------------------------------------------------------ */

                    musicPlay();
                } else if (gender.equals("lady") && !isDancing) {
                    downloadforLadyData(); // download both state and motion number
                }

                isDancing = true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);


        remoteControl();

    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onPause() {
        super.onPause();
        music_cha.stop();

    }


    private static void uploadOKState() {
        //upload man or lady's stand by state
        //代表我做完上個動作了，準備做下個動作
        if (gender.equals("man")) {
            docRef.update("state_man", 1); //1 represent he finish the motion and stand by.
        } else if (gender.equals("lady")) {
            Log.d("gender", "gender lady");
            docRef.update("state_lady", 1);
        }
    }

    private void downloadData() {
        //男生接收動作指令
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
                                motion = ((Number) document.getData().get("motion")).intValue();
                                if (temp != motion) {
                                    //do 將在什麼數字時要做什麼動作寫在這邊
                                    switch (motion) {
                                        case 1:
                                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFF9000);
                                            robotAPI.wheelLights.setBrightness(WheelLights.Lights.SYNC_BOTH, 0xff, 50);
                                            robotAPI.wheelLights.startMarquee(WheelLights.Lights.SYNC_BOTH, WheelLights.Direction.DIRECTION_FORWARD, 1, 2, 0);
                                            robotAPI.robot.setExpression(RobotFace.HELPLESS);
                                            iCurrentSpeakSerialNO = robotAPI.robot.speak("Can you dance with me? miss?"); //因為這句要先講
                                            break;
                                        case 2:
                                            uploadOKState();
                                            break;
                                        case 3:
                                            //start.setText("11111111111111");
                                            robotAPI.robot.setExpression(RobotFace.SINGING);
                                            robotAPI.motion.moveBody(0f, 0f, -3.14f);
                                            robotAPI.motion.moveBody(1f, 0f, 0f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);//逆
                                            break;
                                        case 4:
                                            robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            robotAPI.utility.playAction(20);
                                            robotAPI.motion.moveBody(0f, 0f, -3.14f);
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0f); //this is 4
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////

                                        case 5:
                                            //start.setText("11111111111111");
                                            robotAPI.robot.setExpression(RobotFace.EXPECTING);
                                            robotAPI.motion.moveBody(0f, 0f, -3.14f);
                                            robotAPI.motion.moveBody(1f, 0f, 0f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);//逆
                                            break;
                                        case 6:
                                            robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            robotAPI.utility.playAction(22);
                                            robotAPI.motion.moveBody(0f, 0f, -3.14f);
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0f); //this is 4
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 7:
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.1f, 0f, 0f);
                                            break;
                                        case 8:
                                            robotAPI.utility.playAction(22);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            break;
                                        case 9:
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            break;
                                        case 10:
                                            robotAPI.motion.moveBody(0f, 0f, 1.8f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.8f);
                                            break;
                                        case 11:
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.5f, 0f, 0f);
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 12:
                                            robotAPI.robot.setExpression(RobotFace.INTERESTED);
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);//順
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.1f, 0f, 0f);
                                            Log.d("check123", "5");
                                            break;
                                        case 13:
                                            robotAPI.motion.moveBody(0f, 0f, 2f);
                                            robotAPI.motion.moveBody(0f, 0f, -2f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            Log.d("check123", "6");
                                            break;
                                        case 14:
                                            uploadOKState();
                                            Log.d("check123", "7");
                                            break;
                                        case 15:
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            Log.d("check123", "8");
                                            break;
///
                                        case 16:
                                            Log.d("check123", "9");
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            break;//男
                                        ///////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 17:
                                            robotAPI.robot.setExpression(RobotFace.SHOCKED);
                                            uploadOKState();
                                            break;
                                        case 18:
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);//順
                                            break;
                                        case 19:
                                            uploadOKState();
                                            break;
                                        case 20:
                                            robotAPI.robot.setExpression(RobotFace.SHY);
                                            iCurrentMoveSerial = robotAPI.robot.speak("Wow! It's Fantastic!");
                                            break;
                                        case 21:
                                            uploadOKState();
                                            break;
                                        case 22:
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0, 0f, 1.57f);
                                            break;
                                        case 23:
                                            musicStop();
                                            robotAPI.robot.setExpression(RobotFace.HAPPY);
                                            iCurrentSpeakSerialNO = robotAPI.robot.speak("Thank you!");
                                            break;
                                        case 24:

                                            robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
                                            robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                                            uploadOKState();
                                            break;
                                        case 25:
                                            docRef.update("motion", 0);
                                            handler2.removeCallbacksAndMessages(null);
                                            docRef.update("state_man", 1);
                                            docRef.update("state_lady", 1);
                                            handler.removeCallbacksAndMessages(null);
                                            isDancing = false;
                                            break;


                                    }

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
        //firebase*/
    }

    private void downloadforLadyData() {
        //firebase
        //firebase
        //女生接收指令
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
                                motion = ((Number) document.getData().get("motion")).intValue();
                                // 如果上一次抓的跟現在抓到的一樣，代表動作不變
                                if (temp != motion) {
                                    //do 將在什麼數字時做什麼動作寫在這
                                    switch (motion) {
                                        case 1:
                                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x00ff0000);
                                            robotAPI.wheelLights.setBrightness(WheelLights.Lights.SYNC_BOTH, 0xff, 50);
                                            robotAPI.wheelLights.startMarquee(WheelLights.Lights.SYNC_BOTH, WheelLights.Direction.DIRECTION_FORWARD, 1, 2, 0);
                                            robotAPI.robot.setExpression(RobotFace.EXPECTING);
                                            uploadOKState();
                                            Log.d("two", "one");
                                            break;
                                        case 2:
                                            Log.d("two", "two");
                                            iCurrentSpeakSerialNO = robotAPI.robot.speak("Sure! Let's Dance!", new SpeakConfig().pitch(120)); //因為這句要先講
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 3:
                                            robotAPI.robot.setExpression(RobotFace.SHY);
                                            robotAPI.motion.moveBody(0f, 0f, 3.14f);
                                            robotAPI.motion.moveBody(1f, 0f, 0f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            break;
                                        case 4:
                                            robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            robotAPI.utility.playAction(20);
                                            robotAPI.motion.moveBody(0f, 0f, 3.14f);
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0f);
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 5:
                                            robotAPI.robot.setExpression(RobotFace.SHY);
                                            robotAPI.motion.moveBody(0f, 0f, 3.14f);
                                            robotAPI.motion.moveBody(1f, 0f, 0f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            break;
                                        case 6:
                                            robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            robotAPI.utility.playAction(22);
                                            robotAPI.motion.moveBody(0f, 0f, 3.14f);
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0f);
                                            break;


                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////

                                        case 7:
                                            robotAPI.robot.setExpression(RobotFace.PLEASED);
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            break;
                                        case 8:
                                            robotAPI.utility.playAction(22);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.1f, 0f, 0f);
                                            break;
                                        case 9:
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            break;
                                        case 10:
                                            robotAPI.motion.moveBody(0f, 0f, -1.8f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.8f);
                                            break;
                                        case 11:
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.5f, 0f, 0f);
                                            break;
                                        //////////////以上動作是完整一組//以上動作是完整一組//以上動作是完整一組//////////////
                                        case 12:
                                            robotAPI.robot.setExpression(RobotFace.INNOCENT);
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(-0.1f, 0f, 0f);
                                            Log.d("check123", "5");
                                            break;
                                        case 13:
                                            robotAPI.motion.moveBody(0f, 0f, -2f);
                                            robotAPI.motion.moveBody(0f, 0f, 2f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0.1f, 0f, 0f);
                                            Log.d("check123", "6");
                                            break;
                                        case 14:
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            Log.d("check123", "7");
                                            break;
                                        case 15:
                                            uploadOKState();
                                            Log.d("check123", "8");
                                            break;

                                        case 16:
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, -1.57f);
                                            Log.d("check123", "9");
                                            break;//女

                                        case 17:
                                            //變顏色
                                            robotAPI.robot.setExpression(RobotFace.PROUD);
                                            iCurrentMoveSerial = robotAPI.robot.speak("It's my solo time!", new SpeakConfig().pitch(120));
                                            break;
                                        case 18:
                                            robotAPI.motion.moveBody(0f, 0f, -1.57f);//順
                                            robotAPI.motion.moveHead(45, 0, MotionControl.SpeedLevel.Head.L3);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0);
                                            break;
                                        case 19:
                                            robotAPI.motion.moveHead(0, 0, MotionControl.SpeedLevel.Head.L3);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            break;
                                        case 20:

                                            iCurrentMoveSerial = robotAPI.utility.playAction(22);

                                            break;
                                        case 21:
                                            robotAPI.robot.setExpression(RobotFace.HAPPY);
                                            robotAPI.motion.moveBody(0f, 0f, 1.57f);
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(1f, 0f, 0);
                                            break;
                                        case 22:
                                            iCurrentMoveSerial = robotAPI.motion.moveBody(0, 0f, -1.57f);
                                            break;
                                        case 23:
                                            uploadOKState();
                                            Log.d("check123", "10");
                                            break;
                                        case 24:
                                            robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
                                            iCurrentSpeakSerialNO = robotAPI.robot.speak("You are welcome");
                                            robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                                            Log.d("check123", "11");
                                            handler.removeCallbacksAndMessages(null);
                                            isDancing = false;
                                            break;

                                    }
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
        handler2.post(new Runnable() {

            @Override
            public void run() {

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                state_man = ((Number) document.get("state_man")).intValue();
                                state_lady = ((Number) document.get("state_lady")).intValue();
                                boolean EmergencyStop = (Boolean) document.get("EmergencyStop");
                                if (EmergencyStop) {
                                    handler.removeCallbacksAndMessages(null);
                                    handler3.removeCallbacksAndMessages(null);
                                    docRef.update("state_man", 0);
                                    docRef.update("state_lady", 0);
                                    handler2.removeCallbacksAndMessages(null);
                                }

                                if (state_man == 1 && state_lady == 1 && count == 3 && !EmergencyStop) {
                                    count = 0;
                                    //代表都好了，上船下一個動作指令，並把雙方狀態歸零
                                    Log.d("fuckyou", "fuckyou");
                                    //handler2.removeCallbacksAndMessages(null);
                                    motion_number += 1; //執行下個動作號碼
                                    //docRef.update("motion", motion_number);
                                    docRef.update("motion", motion_number).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            count += 1;
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                    docRef.update("state_man", 0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            count += 1;
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                    docRef.update("state_lady", 0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            count += 1;
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                }
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
                handler.postDelayed(this, 100);
            }
        });
        //firebase
        //firebase*/
    }

    public void getinitMotionNumber() {
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        motion_number = ((Number) document.get("motion")).intValue();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void remoteControl() {

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                EmergencyStop = (Boolean) snapshot.getData().get("EmergencyStop");
                isCanStartMan = (Boolean) snapshot.getData().get("isCanStartMan");
                isCanStartLady = (Boolean) snapshot.getData().get("isCanStartLady");
                if (EmergencyStop) {
                    handler2.removeCallbacksAndMessages(null);
                    handler.removeCallbacksAndMessages(null);
                    musicStop();
                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                    robotAPI.cancelCommandAll();
                    robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
                    docRef.update("motion", 0);
                    docRef.update("state_man", 1);
                    docRef.update("state_lady", 1);
                    EmergencyStop = false;
                    isDancing = false;
                    docRef.update("EmergencyStop", EmergencyStop);
                }
                if (isCanStartMan && !isDancing&&gender.equals("man")) {
                    getinitMotionNumber();
                    count = 3;
                    commandSameTime();
                    downloadData(); // download both state and motion number
                    musicPlay();
                    isCanStartMan = false;
                    docRef.update("isCanStartMan", isCanStartMan);
                    isCanStartLady = false;
                    docRef.update("isCanStartLady", isCanStartLady);
                    isDancing = true;
                } else if (isCanStartLady && !isDancing&&gender.equals("lady")) {

                    downloadforLadyData(); // download both state and motion number

                    isDancing = true;
                }


            }
        });


    }

    private void musicPlay() {
        music_cha = MediaPlayer.create(this, R.raw.chacha2);
        music_cha.start();
    }

    private void musicStop() {
        music_cha.stop();
    }

    private void handlerForUpdate() {
        handler3.post(new Runnable() {

            @Override
            public void run() {
                Log.d("count", Integer.toString(count));
                if (count == 3) {
                    commandSameTime();
                }
                handler.postDelayed(this, 100);
            }
        });
    }
}
