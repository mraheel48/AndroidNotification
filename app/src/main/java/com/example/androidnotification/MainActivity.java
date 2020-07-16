package com.example.androidnotification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    //1. Notification channel
    //2. Notification builder
    //3. Notification Manager

    private static final String Channel_id = "ElgoByte";
    private static final String Channel_name = "ElgoByte";
    private static final String Channel_desc = "ElgoByte Notification";

    Button btnClick;
    TextView tvText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnClick = findViewById(R.id.btnClick);
        tvText = findViewById(R.id.textView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(Channel_id, Channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(Channel_desc);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayNotification();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (task.isSuccessful()) {

                            String token = task.getResult().getToken();
                            tvText.setText("Token :" + token);

                        } else {

                            tvText.setText(task.getException().getMessage());
                            Toast.makeText(MainActivity.this, "Token not genrated", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void displayNotification() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, Channel_id)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Hurry!  It is working...")
                        .setContentText("Your first notification")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(1, mBuilder.build());

    }


}
