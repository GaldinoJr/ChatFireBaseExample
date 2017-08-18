package com.android.pocFireBase2.login;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.pocFireBase2.R;
import com.android.pocFireBase2.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                long endTime = System.currentTimeMillis();
                long passedTime = Math.abs(endTime - mStartTime);
                if (passedTime > SPLASH_DURATION) {
                    showNextScreen(firebaseAuth);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showNextScreen(firebaseAuth);
                        }
                    }, SPLASH_DURATION - passedTime);
                }
            }
        };
    }

    private void showNextScreen(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            showMainScreen();
        } else {
            showLoginScreen();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStartTime = System.currentTimeMillis();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void showLoginScreen() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
