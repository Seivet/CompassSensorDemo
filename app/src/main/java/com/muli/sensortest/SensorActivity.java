package com.muli.sensortest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 指南针传感器
 * 新版API里方向传感器的SensorManager.getOrientation方法替换了旧版的Sensor.TYPE_ORIENTATION
 * SensorManager.getOrientation实际上需要用到磁场和加速度两个感应器共同工作来获取方向数据
 */
public class SensorActivity extends Activity implements SensorEventListener{

    private SensorManager mSensorManager;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;

    float[] mAccelerometerValues = new float[3];
    float[] mMagneticFieldValues = new float[3];

    private float mLastDegree = 0f;
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mImageView = (ImageView)findViewById(R.id.compass);
        mTextView = (TextView)findViewById(R.id.sensor_text);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // 更新显示数据
        getCompassOrientation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册
        mSensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            mMagneticFieldValues = event.values;

        }else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mAccelerometerValues = event.values;
        }

        getCompassOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // activity暂停时要释放
    public void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    private  void getCompassOrientation() {

        float[] values = new float[3];
        float[] R = new float[9];

        // SensorManager.getOrientation方法替换了Sensor.TYPE_ORIENTATION
        SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(R, values);

        // 转换为度
        float degree = (float) Math.toDegrees(values[0]);

        /**
         * 用SensorManager.getOrientation得到的方向（磁场+加速度）数据范围是（-180～180）
         * 0表示正北，90表示正东，180/-180表示正南，-90表示正西
         * 出于习惯，这里把负的度数化为正
         */
        if ( degree < 0 ){
            degree = degree +360;
        }

        // 转动动画
        if(Math.abs(degree - mLastDegree) > 1) {
            RotateAnimation rotateAnimation = new RotateAnimation(mLastDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setFillAfter(true);
            mImageView.startAnimation(rotateAnimation);
            mLastDegree = -degree;
        }

        // TextView显示角度
        String degreeToString = String.valueOf((int) degree) + "°";

        if(degree <= 5 && degree >= 0 || degree >= 355 && degree <= 360 ){
            mTextView.setText("正北:"+ degreeToString);
        }
        else if(degree > 5 && degree < 85){
            mTextView.setText("东北:"+ degreeToString);
        }
        else if(degree >= 85 && degree <= 95 ){
            mTextView.setText("正东:"+ degreeToString);
        }
        else if(degree > 95 && degree < 175){
            mTextView.setText("东南:"+ degreeToString);
        }
        else if(degree >= 170 && degree <= 185){
            mTextView.setText("正南:"+ degreeToString);
        }
        else if(degree > 185 && degree < 265){
            mTextView.setText("西南:"+ degreeToString);
        }
        else if(degree >= 265 && degree <= 275 ){
            mTextView.setText("正西:"+ degreeToString);
        }
        else if(degree > 275 && degree < 360){
            mTextView.setText("西北:"+ degreeToString);
        }
    }
}
