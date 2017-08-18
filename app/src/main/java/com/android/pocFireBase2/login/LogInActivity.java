package com.android.pocFireBase2.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.android.pocFireBase2.FireChatHelper.ChatHelper;
import com.android.pocFireBase2.R;
import com.android.pocFireBase2.adapter.UsersChatAdapter;
import com.android.pocFireBase2.register.RegisterActivity;
import com.android.pocFireBase2.ui.MainActivity;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OSSubscriptionState;
import com.onesignal.OneSignal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = LogInActivity.class.getSimpleName();
    @BindView(R.id.edit_text_email_login) EditText mUserEmail;
    @BindView(R.id.edit_text_password_log_in) EditText mUserPassWord;

    private FirebaseAuth mAuth;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

//        hideActionBar();
        bindButterKnife();
        setAuthInstance();
    }

    private void hideActionBar() {
        this.getActionBar().hide();
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_login)
    public void logInClickListener(Button button) {
        onLogInUser();
    }

    @OnClick(R.id.btn_register)
    public void registerClickListener(Button button) {
        goToRegisterActivity();
    }

    private void onLogInUser() {
        if(getUserEmail().equals("") || getUserPassword().equals("")){
            showFieldsAreRequired();
        }else {
            logIn(getUserEmail(), getUserPassword());
        }
    }

    private void showFieldsAreRequired() {
        showAlertDialog(getString(R.string.error_incorrect_email_pass),true);
    }

    private void logIn(String email, String password) {

        showAlertDialog("Log In...",false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                DatabaseReference dataBase = FirebaseDatabase.getInstance().getReference();

                // TODO registra o player ID
                String playerId = getPlayerID();
                String userId = mAuth.getCurrentUser().getUid();
                dataBase.child("users").child(userId).child("playerID").setValue(playerId);

                dismissAlertDialog();

                if(task.isSuccessful()){
                    setUserOnline();
                    goToMainActivity();
                }else {
                    showAlertDialog(task.getException().getMessage(),true);
                }
            }
        });
    }

    private void setUserOnline() {
        if(mAuth.getCurrentUser()!=null ) {
            String userId = mAuth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                    .getReference().
                    child("users").
                    child(userId).
                    child("connection").
                    setValue(UsersChatAdapter.ONLINE);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private String getUserEmail() {
        return mUserEmail.getText().toString().trim();
    }

    private String getUserPassword() {
        return mUserPassWord.getText().toString().trim();
    }

    private void showAlertDialog(String message, boolean isCancelable){
        dialog = ChatHelper.buildAlertDialog(getString(R.string.login_error_title), message,isCancelable,LogInActivity.this);
        dialog.show();
    }

    private void dismissAlertDialog() {
        dialog.dismiss();
    }

    private String getPlayerID()
    {
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        OSSubscriptionState var = status.getSubscriptionStatus();
        String playerID = var.getUserId();
//        String pushToken = var.getPushToken();
        return playerID;

    }
}
