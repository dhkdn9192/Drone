package com.example.bugstick;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.example.bugstick.common.logger.Log;
import com.example.bugstick.common.logger.LogFragment;
import com.example.bugstick.common.logger.LogWrapper;
import com.example.bugstick.common.logger.MessageOnlyLogFilter;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class MainActivity extends AppCompatActivity {

    private static final float MAX_DRONE_SPEED_DP_PER_S = 300f;
    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    public static final String TAG = "MainActivity";
    public BluetoothChatFragment fragment = new BluetoothChatFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        Log.w("ChatMain", "onCreate called");

        final TextView angleView1 = (TextView) findViewById(R.id.tv_angle1);
        final TextView offsetView1 = (TextView) findViewById(R.id.tv_offset1);

        final TextView angleView2 = (TextView) findViewById(R.id.tv_angle2);
        final TextView offsetView2 = (TextView) findViewById(R.id.tv_offset2);

        final DroneView droneView = (DroneView) findViewById(R.id.droneview);

        final String angleNoneString = getString(R.string.angle_value_none);
        final String angleValueString = getString(R.string.angle_value);
        final String offsetNoneString = getString(R.string.offset_value_none);
        final String offsetValueString = getString(R.string.offset_value);

        Joystick joystick1 = (Joystick) findViewById(R.id.joystick1);
        Joystick joystick2 = (Joystick) findViewById(R.id.joystick2);

        joystick1.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                String degrees_str = String.format(angleValueString, degrees);
                String offset_str = String.format(angleValueString, offset);
                angleView1.setText(degrees_str);
                offsetView1.setText(offset_str);

                droneView.setVelocity(
                        (float) Math.cos(degrees * Math.PI / 180f) * offset * MAX_DRONE_SPEED_DP_PER_S,
                        -(float) Math.sin(degrees * Math.PI / 180f) * offset * MAX_DRONE_SPEED_DP_PER_S);

                fragment.sendMessage("L - " + degrees_str + ", " + offset_str);
            }

            @Override
            public void onUp() {
                angleView1.setText(angleNoneString);
                offsetView1.setText(offsetNoneString);

                droneView.setVelocity(0, 0);
            }
        });

        joystick2.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                String degrees_str = String.format(angleValueString, degrees);
                String offset_str = String.format(angleValueString, offset);
                angleView2.setText(degrees_str);
                offsetView2.setText(offset_str);

                droneView.setRotation(
                        (float) Math.cos(degrees * Math.PI / 180f) * offset * MAX_DRONE_SPEED_DP_PER_S,
                        -(float) Math.sin(degrees * Math.PI / 180f) * offset * MAX_DRONE_SPEED_DP_PER_S);

                fragment.sendMessage("R - " + degrees_str + ", " + offset_str);
            }

            @Override
            public void onUp() {
                angleView2.setText(angleNoneString);
                offsetView2.setText(offsetNoneString);

                droneView.setRotation(0, 0);
            }
        });
    }

    @Override
    protected  void onStart() {
        super.onStart();
        initializeLogging();
    }

    /** Set up targets to receive log data */
    public void initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("ChatMain", "on create options menu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
