package com.example.kids.nodemcu;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.smartirrigationsystem.kids.nodemcu.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference myRef;
    EditText editText;
    Switch aSwitch, field_one, field_two;
    ImageView imageView, imageView2;
    TextView txtAboutUs;
    private float currentLevelSensorA = 0;
    private float currentLevelSensorB = 0;
    TextView txtMOtorStatus;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    TextView tv_notificationsetted;
    long moter = (long) 0;
    long field1 = (long) 0;
    long field2 = (long) 0;
    long status = (long) 0;
    long sensorA = (long) 0;
    long sensorB = (long) 0;
    Button setVal;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.editText);
        aSwitch = findViewById(R.id.switch1);
        field_one = findViewById(R.id.field_one);
        field_two = findViewById(R.id.field_two);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageViewTwo);
        setVal = findViewById(R.id.button);
        txtMOtorStatus = findViewById(R.id.txtMotorStatus);
        txtAboutUs = findViewById(R.id.txtAboutUs);
        circularImageBar(imageView, 0);
        circularImageBar(imageView2, 0);

        // startService(new Intent(MainActivity.this, myService.class));
        if (!isMyServiceRunning(myService.class)) {
            startService(new Intent(MainActivity.this, myService.class));
        }


        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        getExpectedNotificationLevel();
        float f = pref.getFloat("expected_notification_level", 0);

        editText.setText(String.valueOf(f));
       /* if (f != 0.0)
            ;
        else
            Log.w("User Expected Data", String.valueOf(f));*/


        myRef = FirebaseDatabase.getInstance().getReference();
        onSwitch();
        myRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                long moter = dataSnapshot.child("MOTER").getValue(long.class);
                sensorA = dataSnapshot.child("SENSOR_A").getValue(long.class);
                sensorB = dataSnapshot.child("SENSOR_B").getValue(long.class);
                field1 = dataSnapshot.child("Field 1").getValue(long.class);
                field2 = dataSnapshot.child("Field 2").getValue(long.class);
                status = dataSnapshot.child("Motor_response").getValue(long.class);

                if (moter == 1) {
                    aSwitch.setChecked(true);
                }
                if (field1 == 1) {
                    field_one.setChecked(true);
                }
                if (field2 == 1) {
                    field_two.setChecked(true);
                }
                if (status == 1) {
                    txtMOtorStatus.setText("Motor is currently Running");
                }

                currentLevelSensorA = (float) sensorA;
                currentLevelSensorB = (float) sensorB;


                circularImageBar(imageView, (int) currentLevelSensorA);
                circularImageBar(imageView2, (int) currentLevelSensorB);
//                if (currentLevelSensorA < (int) expectedNotificationLevel) {
//
//                    Function notification = new Function();
//                    notification.addNotification(
//                            MainActivity.this,
//                            "Water Level Indication",
//                            "Water level of field one is " + (currentLevelSensorA),
//                            R.drawable.ic_launcher_background
//                    );
//                }
//                if (currentLevelSensorB < (int) expectedNotificationLevel) {
//
//                    Function notification = new Function();
//                    notification.addNotification(
//                            MainActivity.this,
//                            "Water Level Indication",
//                            "Water level of field two is " + (currentLevelSensorB),
//                            R.drawable.ic_launcher_background
//                    );
//                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed to read value.", error.toException());
            }
        });

        txtAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
                View view2 = layoutInflaterAndroid.inflate(R.layout.about_us, null);
                builder.setView(view2);
                builder.setCancelable(false);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Button cancel = view2.findViewById(R.id.btnCancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

    }

    private void getExpectedNotificationLevel() {

        setVal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_notificationsetted = findViewById(R.id.settednotificationid);


                if (!editText.getText().toString().equals("")) {

                    tv_notificationsetted.setText(editText.getText().toString());
                    Toast.makeText(MainActivity.this, "Notification Level is set to " + editText.getText().toString() + "%", Toast.LENGTH_SHORT).show();
                    float f = Float.valueOf(editText.getText().toString());

                    try {
                        editor.putFloat("expected_notification_level", f);
                        editor.commit();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    editText.clearFocus();

                } else {

                    Toast.makeText(MainActivity.this, "Please Select some value first", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    void onSwitch() {
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (field_one.isChecked() || field_two.isChecked()) {
                    if (aSwitch.isChecked())
                        moter = (long) 1.0;
                } else if (!aSwitch.isChecked()) {
                    moter = (long) 0.0;
                    myRef.child("MOTER").setValue(moter);
                } else {
                    aSwitch.setChecked(false);
                    Toast.makeText(MainActivity.this, "Please on at least one field first ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        field_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (field_one.isChecked())
                    field1 = (long) 1.0;
                else
                    field1 = (long) 0.0;
                myRef.child("Field 1").setValue(field1);
            }
        });
        field_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (field_two.isChecked())
                    field2 = (long) 1.0;
                else
                    field2 = (long) 0.0;
                myRef.child("Field 2").setValue(field2);
            }
        });


    }


    private void circularImageBar(ImageView iv2, int i) {

        Bitmap b = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();

        paint.setColor(Color.parseColor("#c4c4c4"));
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(150, 150, 130, paint);
        if (sensorA >= 20 && sensorA < 70) {
            paint.setColor(Color.parseColor("#FFDB4C"));
        }
        if (sensorA < 20) {
            paint.setColor(Color.parseColor("#8B0000"));
        }
        if (sensorA >= 70) {
            paint.setColor(Color.parseColor("#7CFC00"));
        }

        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL);
        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);
        oval.set(10, 10, 290, 290);
        canvas.drawArc(oval, 270, ((i * 360) / 100), false, paint);
        paint.setStrokeWidth(0);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.parseColor("#8E8E93"));
        paint.setTextSize(40);
        canvas.drawText("" + i + "%", 150, 150 + (paint.getTextSize() / 3), paint);
        iv2.setImageBitmap(b);
    }


//    @Override
//    protected void onStop() {
//        startService(new Intent(MainActivity.this, myService.class));
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        startService(new Intent(MainActivity.this, myService.class));
//        super.onDestroy();
//    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, myService.class));
        super.onDestroy();
    }
}
