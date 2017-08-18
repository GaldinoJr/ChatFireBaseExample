package com.android.pocFireBase2.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.pocFireBase2.FireChatHelper.ChatHelper;
import com.android.pocFireBase2.R;
import com.android.pocFireBase2.adapter.UsersChatAdapter;
import com.android.pocFireBase2.model.User;
import com.android.pocFireBase2.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OSSubscriptionState;
import com.onesignal.OneSignal;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = LogInActivity.class.getSimpleName();
//    @BindView(R.id.edit_text_email_login) EditText mUserEmail;
//    @BindView(R.id.edit_text_password_log_in) EditText mUserPassWord;

    @BindView(R.id.edittext_login_user_nickname) EditText mUserNicknameEditText;

    private FirebaseAuth mAuth;
    private AlertDialog dialog;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

    @OnClick(R.id.button_login_connect)
    public void logInClickListener(Button button) {
        if(TextUtils.isEmpty(mUserNicknameEditText.getText())) {
            Toast.makeText(this,getString(R.string.login_error_message),Toast.LENGTH_LONG).show();
        }
        else
        {
            onLogInUser();
        }
    }

//    @OnClick(R.id.btn_register)
//    public void registerClickListener(Button button) {
//        goToRegisterActivity();
//    }

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

    private void logIn(final String email, final String password) {

        showAlertDialog("Log In...",false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(mAuth.getCurrentUser() != null) {
                    dismissAlertDialog();

                    if (task.isSuccessful()) {
                        setUserOnline();
                        goToMainActivity();
                    } else {
                        signUp(email, password);
                        //                    showAlertDialog(task.getException().getMessage(),true);
                    }
                }
                else {
                    signUp(email, password);
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

//    private void goToRegisterActivity() {
//        Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
//        startActivity(intent);
//    }

    private String getUserEmail() {
        String email = mUserNicknameEditText.getText().toString().trim();
        email += "@gmail.com";
        return email;
    }

    private String getUserPassword()
    {
        return "123456";
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

    private void signUp(String email, String password) {

        showAlertDialog("Registering...",true);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                dismissAlertDialog();

                if(task.isSuccessful()){
                    onAuthSuccess(task.getResult().getUser());
                    // TODO registra o player ID
//                    DatabaseReference dataBase = FirebaseDatabase.getInstance().getReference();
//                    String playerId = getPlayerID();
//                    String userId = mAuth.getCurrentUser().getUid();
//                    dataBase.child("users").child(userId).child("playerID").setValue(playerId);
                }else {
                    showAlertDialog(task.getException().getMessage(), true);
                }
            }
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        createNewUser(user.getUid());
        goToMainActivity();
    }

    private void createNewUser(String userId){
        User user = buildNewUser();
        setDatabaseInstance();
        mDatabase.child("users").child(userId).setValue(user);
    }

    private User buildNewUser() {
        return new User(
                getUserDisplayName(),
                getUserEmail(),
                UsersChatAdapter.ONLINE,
                ChatHelper.generateRandomAvatarForUser(),
                new Date().getTime(),
                getPlayerID()
        );
    }

    private String getUserDisplayName() {
        return mUserNicknameEditText.getText().toString().trim();
    }

    private void setDatabaseInstance() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

}
