package com.lyullin.grisha.magicball;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
        SensorEventListener {

    private SharedPreferences settings;

    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Vibrator vibrator;

    private ImageView magicBall_back;
    private TextView magicBall_answer;

    private Button settingsButton;

    private int shakeCount = 0;
    private float[] previousValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This code makes the current Activity Fullscreen (without status bar)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), SettingsActivity.class));
            }
        });

        settings = getSharedPreferences(Constants.APP_PREFERENCES,
                MainActivity.MODE_PRIVATE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        magicBall_back = (ImageView) findViewById(R.id.imageView_magicBall_back);
        magicBall_answer = (TextView) findViewById(R.id.textView_answer);

        registerSensorListener();

        if (isSensorListenerRegistered()) {
            showAnswer(getString(R.string.viaShake));
        } else {
            showAnswer(getString(R.string.viaMenu));
        }

        magicBall_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShake();
            }
        });
    }

    @Override
    protected void onPause() {
        unregisterSensorListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerSensorListener();
        if (isSensorListenerRegistered()) {
            showAnswer(getString(R.string.viaShake));
        } else {
            showAnswer(getString(R.string.viaMenu));
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_shake) {
            onShake();
            return true;
        } else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isEnoughToShake(event.values)) {
                onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        // Do nothing
    }

    private boolean isEnoughToShake(float[] values) {

        boolean isEnoughToShake = false;
        double force = 0;

        int minShakeCount = settings.getInt(Constants.SHAKE_COUNT,
                Defaults.SHAKE_COUNT);
        float minForce = settings.getFloat(Constants.SHAKE_FORCE,
                Defaults.SHAKE_FORCE);

        if (previousValues == null) {
            previousValues = new float[values.length];
        }

        for (int i = 0; i < values.length; i++) {
            force += Math.pow((values[i] - previousValues[i])
                    / SensorManager.GRAVITY_EARTH, 2.0);
        }
        force = Math.sqrt(force);

        if (previousValues != null) {
            System.arraycopy(values, 0, previousValues, 0, values.length);
        }

        if (force >= minForce) {
            shakeCount++;
            if (shakeCount >= minShakeCount) {
                shakeCount = 0;
                isEnoughToShake = true;
            }
        }

        return isEnoughToShake;
    }

    private void onShake() {

        unregisterSensorListener();

        magicBall_back.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.magicball_shaking));

        showAnswer(getAnswer());

        if (settings.getBoolean(Constants.VIBRATE_ON, Defaults.VIBRATE_ON)) {
            vibrator.vibrate(settings.getInt(Constants.VIBRATE_TIME_LONG,
                    Defaults.VIBRATE_TIME_LONG));
        }

        registerSensorListener();
    }

    private void showAnswer(String answer) {
        magicBall_answer.setText(answer);
        magicBall_answer.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.answer_fade_in));
    }

    private String getAnswer() {
        String[] answers = getResources().getStringArray(
                R.array.magicball_answers);
        int answer_number = (int) (Math.random() * (answers.length - 1));
        return answers[answer_number];
    }

    private void registerSensorListener() {
        sensorManager.registerListener(MainActivity.this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private boolean isSensorListenerRegistered() {
        return (accelerometer != null);
    }

    private void unregisterSensorListener() {
        sensorManager.unregisterListener(this);
    }
}