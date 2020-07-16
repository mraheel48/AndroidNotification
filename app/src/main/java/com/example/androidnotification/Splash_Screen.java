package com.example.androidnotification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Splash_Screen extends AppCompatActivity {

    ImageView imgWelcome;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tvUserNameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        imgWelcome = findViewById(R.id.imgWelcome);

        if (getIntent().getExtras() != null) {

            //Key of Project
            tvUserNameId = getIntent().getStringExtra("click");
        }

        Toast.makeText(this, tvUserNameId, Toast.LENGTH_LONG).show();

        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();

                try {

                    imgWelcome.animate().alpha(1f).setDuration(2000);

                    sleep(2000);


                } catch (Exception ex) {

                    ex.printStackTrace();

                } finally {

                    ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo info = cm.getActiveNetworkInfo();

                    if (info != null) {

                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {

                            // Toast.makeText(Splash_Screen.this, "Your connect to wifi", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(Splash_Screen.this, Login.class));
                            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();

                        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {

                            startActivity(new Intent(Splash_Screen.this, Login.class));
                            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();

                            // Toast.makeText(Splash_Screen.this, "Your your connect to mobile network", Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        // Toast.makeText(Splash_Screen.this, "Your not Connect ", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Splash_Screen.this, NetWorkConnect.class));
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }


                }
            }
        };
        thread.start();



    }
}
