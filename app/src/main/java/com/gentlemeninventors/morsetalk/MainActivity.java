package com.gentlemeninventors.morsetalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button flashlightButton = (Button) findViewById(R.id.flashlight_button);

        flashlightButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                flashlightButton.setVisibility(View.INVISIBLE);
            }
        });
    }
}
