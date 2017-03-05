package com.gentlemeninventors.morsetalk;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private Button send_message_button;
    private EditText message_text;
    private final Object morseLock = new Object();
    private static final String NAME = "Morse";
    private TextView wpm_text_view;
    private SeekBar wpm_slider;
    private int wpm;
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
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        getCamera();
        if (camera == null || hasFlash)
        {
            // handle errors
        }

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
                                SendMessage(morse, wpm);
                            } catch (InterruptedException e) {
                                Log.d(NAME, "Interrupted Thread");
                                Off();
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

    private void Flash(long length) throws InterruptedException{
        On();
        Thread.sleep(length);
        Off();
    }

    private void Pause(long length) throws InterruptedException{
        Thread.sleep(length);
    }

    private void SendMessage(String morseMessage, int wpm) throws InterruptedException{

        long element_length_ms = (long) ((60.0 / (wpm * 50)) * 1000);
        for (int i = 0; i < morseMessage.length(); i++) {
            char c = morseMessage.charAt(i);
            if (c == '-') {
                Flash(element_length_ms * 3);
            } else if (c == '.') {
                Flash(element_length_ms);
            } else if (c == ' ') {
                Pause(element_length_ms * 3);
            } else if (c == '|') {
                Pause(element_length_ms * 7);
            } else {
                // throw error
            }
        }
    }

    private void On(){
        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
    }

    private void Off(){
        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
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
