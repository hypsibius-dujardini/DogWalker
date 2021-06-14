package com.example.dogwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView instructionText;
    private LinearLayout bottomLayout;
    private TextInputLayout profileNameLayout, phoneNumberLayout;
    private EditText profileName, phoneNumber, emailAddress, password;
    private Button signUpLoginButton;
    private Menu actionBarMenu;
    private boolean loginMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_login);
        setSupportActionBar(findViewById(R.id.toolbar));
        loginMode = getIntent().getBooleanExtra("login_mode", false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        instructionText = findViewById(R.id.instruction);
        bottomLayout = findViewById(R.id.bottom_layout);
        profileNameLayout = findViewById(R.id.profile_name_layout);
        phoneNumberLayout = findViewById(R.id.phone_number_layout);
        profileName = findViewById(R.id.profile_name);
        phoneNumber = findViewById(R.id.phone_number);
        emailAddress = findViewById(R.id.email_address);
        password = findViewById(R.id.password);
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
        password.setText("");
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
                || emailAddress.getText().length() <= 0 || password.getText().length() <= 0) {
            Toast.makeText(this, "Please fill out all fields to create an account.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailAddress.getText().toString(), password.getText().toString())
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.child(currentUser.getUid()).setValue(new User(profileName.getText().toString(),
                phoneNumber.getText().toString(), emailAddress.getText().toString()));
    }

    public void goToLogin(View view) {
        setUIToLogin();
    }

    private void login() {

        String email = emailAddress.getText().toString();
        String pass = password.getText().toString();

        if (email.equals("") || pass.equals("")) {
            Toast.makeText(this, "Your email or password is incorrect.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    currentUser = authResult.getUser();
                    assert currentUser != null;
                    if (currentUser.isEmailVerified()) {
                        // TODO: take to profile edit activity with special extra signaling that this is their first login
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
                resendVerification();
                return true;
            case resetPasswordActionId:
                resetPassword();
                return true;
            case newAccountActionId:
                setUIToSignUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resendVerification() {

        /*if (currentUser == null) {
            // TODO: have them enter email & password in Popup
            Toast.makeText(this, "Current user is null", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.sendEmailVerification()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(SignUpLoginActivity.this, "Verification email sent! Please verify your email address to login.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
         */
    }

    private void resetPassword() {

        /*String email = emailAddress.getText().toString();

        if (currentUser == null) {
            if (email.equals("")) {
                // TODO: have them enter email & password in Popup
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SignUpLoginActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignUpLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {

        }
         */
    }
}