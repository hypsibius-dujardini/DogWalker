package com.example.dogwalker.auth;

import com.example.dogwalker.R;
import com.example.dogwalker.SplashActivity;
import com.example.dogwalker.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class SignUpLoginActivity extends AppCompatActivity implements AuthActionDialogListener {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private TextView instructionText;
    private LinearLayout bottomLayout;
    private TextInputLayout profileNameLayout, phoneNumberLayout;
    private EditText profileName, phoneNumber, emailAddress, passwordInput;
    private Button signUpLoginButton;
    private Menu actionBarMenu;
    private boolean loginMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_login);
        setSupportActionBar(findViewById(R.id.toolbar));
        loginMode = getIntent().getBooleanExtra("login_mode", false);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        instructionText = findViewById(R.id.instruction);
        bottomLayout = findViewById(R.id.bottom_layout);
        profileNameLayout = findViewById(R.id.profile_name_layout);
        phoneNumberLayout = findViewById(R.id.phone_number_layout);
        profileName = findViewById(R.id.profile_name);
        phoneNumber = findViewById(R.id.phone_number);
        emailAddress = findViewById(R.id.email_address);
        passwordInput = findViewById(R.id.password);
        signUpLoginButton = findViewById(R.id.sign_up_login_button);
    }

    private void setUIToLogin() {
        profileNameLayout.setVisibility(View.GONE);
        phoneNumberLayout.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        actionBarMenu.findItem(R.id.action_verification).setVisible(true);
        actionBarMenu.findItem(R.id.action_password).setVisible(true);
        actionBarMenu.findItem(R.id.action_new_account).setVisible(true);
        instructionText.setText(R.string.login);
        signUpLoginButton.setText(R.string.login_button);
        loginMode = true;
    }

    private void setUIToSignUp() {
        profileName.setText("");
        phoneNumber.setText("");
        emailAddress.setText("");
        passwordInput.setText("");
        profileNameLayout.setVisibility(View.VISIBLE);
        phoneNumberLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
        actionBarMenu.findItem(R.id.action_verification).setVisible(false);
        actionBarMenu.findItem(R.id.action_password).setVisible(false);
        actionBarMenu.findItem(R.id.action_new_account).setVisible(false);
        instructionText.setText(R.string.registration);
        signUpLoginButton.setText(R.string.sign_up_button);
        loginMode = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_login, menu);
        actionBarMenu = menu;
        if (loginMode) setUIToLogin();
        return true;
    }

    public void signUpOrLogin(View view) {
        if (loginMode) login();
        else signUp();
    }

    private void signUp() {
        if (profileName.getText().length() <= 0 || phoneNumber.getText().length() <= 0
                || emailAddress.getText().length() <= 0 || passwordInput.getText().length() <= 0) {
            Toast.makeText(this, "Please fill out all fields to create an account.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(emailAddress.getText().toString(), passwordInput.getText().toString())
                .addOnSuccessListener(authResult -> {
                    currentUser = authResult.getUser();
                    assert currentUser != null;
                    currentUser.sendEmailVerification()
                            .addOnSuccessListener(aVoid -> {
                                saveUserToDatabase();
                                setUIToLogin();
                                Toast.makeText(this, "Account created! Please verify your email address to login.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e
                        -> Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserToDatabase() {
        String userId = currentUser.getUid();
        String name = profileName.getText().toString();
        database.getReference("Users/" + userId)
                .setValue(new User(name, phoneNumber.getText().toString(), emailAddress.getText().toString()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /*database.getReference("Names/" + name).runTransaction(new Transaction.Handler() {
                            @NonNull @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                currentData.child(String.valueOf(currentData.getChildrenCount())).setValue(userId);
                                return Transaction.success(currentData);
                            }
                            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { }
                        }))*/
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void goToLogin(View view) {
        setUIToLogin();
    }

    private void login() {

        String email = emailAddress.getText().toString();
        String pass = passwordInput.getText().toString();

        if (email.equals("") || pass.equals("")) {
            Toast.makeText(this, "Your email or password is incorrect.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    currentUser = authResult.getUser();
                    assert currentUser != null;
                    if (currentUser.isEmailVerified()) {
                        Intent intent = new Intent(SignUpLoginActivity.this, SplashActivity.class);
                        intent.putExtra("finish", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpLoginActivity.this, "Please verify your email address to login.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpLoginActivity.this, "Your email or password is incorrect.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int resendVerificationActionId = R.id.action_verification;
        final int resetPasswordActionId = R.id.action_password;
        final int newAccountActionId = R.id.action_new_account;

        switch (item.getItemId()) {
            case resendVerificationActionId:
                AuthActionFragment.newInstance(R.layout.fragment_verify_email, "verify_email", emailAddress.getText().toString())
                        .show(getSupportFragmentManager(), "verify_email");
                return true;
            case resetPasswordActionId:
                AuthActionFragment.newInstance(R.layout.fragment_reset_password, "reset_password", emailAddress.getText().toString())
                        .show(getSupportFragmentManager(), "reset_password");
                return true;
            case newAccountActionId:
                setUIToSignUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onVerifyEmailAttempt(AuthActionFragment fragment, String email, String password) {


        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Please enter your email address and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null && currentUser.getEmail().equals(email)) {
            currentUser.sendEmailVerification()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SignUpLoginActivity.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(SignUpLoginActivity.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(SignUpLoginActivity.this, "Your email or password is incorrect.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onResetPasswordAttempt(AuthActionFragment fragment, String email) {

        if (email.equals("")) {
            Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUpLoginActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    fragment.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}