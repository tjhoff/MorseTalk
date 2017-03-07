package com.gentlemeninventors.morsetalk;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button send_message_button;
    private EditText message_text;
    private TextView wpm_text_view;
    private SeekBar wpm_slider;
    private ToggleButton use_flashlight_toggle;
    private SurfaceView camera_view;

    private SurfaceHolder lightIndicatorHolder;
    private SurfaceTexture cameraView;

    private static final String NAME = "Morse";
    private int wpm;
    private boolean useFlashlight = false;
    private Camera camera;
    private Camera.Parameters params;
    private Thread morseThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_message_button = (Button) findViewById(R.id.send_message_button);
        message_text = (EditText) findViewById(R.id.message_text);
        wpm_slider = (SeekBar) findViewById(R.id.wpm_slider);
        wpm_text_view = (TextView) findViewById(R.id.wpm_text_view);
        use_flashlight_toggle = (ToggleButton) findViewById(R.id.use_flashlight_toggle);
        SurfaceView light_indicator = (SurfaceView) findViewById(R.id.light_indicator);
        lightIndicatorHolder = light_indicator.getHolder();

        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        getCamera();
        if (camera == null || hasFlash)
        {
            // handle errors
        }
        cameraSetup();
        setup();
    }

    @Override
    protected void onResume(){
        super.onResume();

        getCamera();
    }

    @Override
    protected void onPause(){
        super.onPause();
        // Interrupt the thread if it's already running.
        if (morseThread != null && morseThread.isAlive()){
            morseThread.interrupt();
        }
        if (camera != null){
            camera.release();
            camera = null;
        }
    }

    private void setup(){
        use_flashlight_toggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useFlashlight = b;
            }
        });

        wpm_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                wpm = i;
                String wpm_text = String.valueOf(i) + " wpm";
                wpm_text_view.setText(wpm_text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        wpm_slider.setProgress(5);

        send_message_button.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                // Interrupt the thread if it's already running.
                if (morseThread != null && morseThread.isAlive()){
                    morseThread.interrupt();
                    return;
                }

                // Create the morse for the thread to run
                String s = message_text.getText().toString();
                final String morse = Morse.convertText(s);

                // Run the morse thread with handling for interrupts.
                morseThread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            sendMessage(morse, wpm);
                        } catch (InterruptedException e) {
                            Log.d(NAME, "Interrupted Thread");
                            off();
                        }

                        // Set a handler to set text on morse finish
                        send_message_button.post(new Runnable(){

                            @Override
                            public void run() {
                                send_message_button.setText(R.string.send_msg);
                            }
                        });
                    }
                }

                );
                send_message_button.setText(R.string.send_msg_cancel);

                morseThread.start();

            }
        });
    }

    private void cameraSetup(){
        try{
            camera.setPreviewTexture(cameraView);
            camera.startPreview();

            Camera.Parameters params = camera.getParameters();
        }
        catch (IOException e){
            Log.w(NAME, "Failed to start camera surface preview");
        }
    }

    private void flash(long length) throws InterruptedException{
        on();
        Thread.sleep(length);
        off();
    }

    private void pause(long length) throws InterruptedException{
        Thread.sleep(length);
    }

    private void sendMessage(String morseMessage, int wpm) throws InterruptedException{

        long element_length_ms = (long) ((60.0 / (wpm * 50)) * 1000);
        boolean previous_dash_or_dot = false;
        for (int i = 0; i < morseMessage.length(); i++) {
            char c = morseMessage.charAt(i);
            if (c == '-') {
                if (previous_dash_or_dot){
                    pause(element_length_ms);
                }
                flash(element_length_ms * 3);
                previous_dash_or_dot = true;
            } else if (c == '.') {
                if (previous_dash_or_dot){
                    pause(element_length_ms);
                }
                flash(element_length_ms);
                previous_dash_or_dot = true;
            } else if (c == ' ') {
                pause(element_length_ms * 3);
                previous_dash_or_dot = false;
            } else if (c == '|') {
                pause(element_length_ms * 7);
                previous_dash_or_dot = false;
            } else {
                // throw error
            }
        }
    }

    private void on(){
        if (useFlashlight) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }
        Canvas light_indicator_canvas = lightIndicatorHolder.lockCanvas();
        if (light_indicator_canvas != null) {
            light_indicator_canvas.drawColor(Color.WHITE);
        }
        lightIndicatorHolder.unlockCanvasAndPost(light_indicator_canvas);
    }

    private void off(){
        if (useFlashlight) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
        Canvas light_indicator_canvas = light_indicator_holder.lockCanvas();
        if (light_indicator_canvas != null) {
            light_indicator_canvas.drawColor(Color.BLACK);
        }
        light_indicator_holder.unlockCanvasAndPost(light_indicator_canvas);
    }

    private void getCamera() {
        if (camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        try {
            camera = Camera.open();
            params = camera.getParameters();
        } catch (RuntimeException e) {
            Log.e("MorseTalk", String.format("Camera Error. Failed to Open. Error: %s", e.getMessage()));
        }

    }
}
