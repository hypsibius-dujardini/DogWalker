package com.example.dogwalker;

import com.example.dogwalker.auth.SignUpLoginActivity;
import com.example.dogwalker.messaging.WalkActivity;
import com.example.dogwalker.setupprofile.SetUpProfileActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private boolean finishActivity;
    private boolean exitApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        finishActivity = getIntent().getBooleanExtra("finish", false);
        exitApp = getIntent().getBooleanExtra("exit", false);

        int[] welcomeMessages = { R.string.welcome_1, R.string.welcome_2, R.string.welcome_3, R.string.welcome_4, R.string.welcome_5,
                R.string.welcome_6, R.string.welcome_7, R.string.welcome_8, R.string.welcome_9, R.string.welcome_10 };
        ((TextView) findViewById(R.id.welcome_text)).setText(welcomeMessages[(int) (Math.random() * 10)]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exitApp) return;
        new CountDownTimer(3000, 1000) {
            @Override public void onTick(long millisUntilFinished) { }
            @Override public void onFinish() {
                //if (exitApp) finishAndRemoveTask();
                //if (exitApp) finishAffinity();
                if (currentUser == null) // No user found
                    signUpActivity();
                else if (currentUser.isEmailVerified()) // User found and verified
                    enterMainApp();
                else // User found but not verified
                    loginActivity();
            }
        }.start();
    }

    private void signUpActivity() {
        Intent intent = new Intent(SplashActivity.this, SignUpLoginActivity.class);
        intent.putExtra("login_mode", false);
        startActivity(intent);
        if (finishActivity) finish();
    }

    private void loginActivity() {
        Intent intent = new Intent(SplashActivity.this, SignUpLoginActivity.class);
        intent.putExtra("login_mode", true);
        startActivity(intent);
        if (finishActivity) finish();
    }

    private void enterMainApp() {
        database.getReference("Users/" + currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if (user.isDogOwner() || user.isDogWalker() || getIntent().getBooleanExtra("setup_complete", false)) // Profile setup not needed
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                else // Profile setup needed
                    startActivity(new Intent(SplashActivity.this, SetUpProfileActivity.class));
                if (finishActivity) finish();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}