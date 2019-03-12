package com.github.archerlml.gymbuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton mSignInButton;

    private GoogleApiClient mGoogleApiClient;

    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mApplicationData.signedIn()) {
            onSignInSuccess();
            return;
        }
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                onSignInSuccess();
                Log.i("Sign in");
                // User is signed in
            } else {
                Log.i("Sign out");
                // User is signed out
            }
            // ...
        };

        // Assign fields
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        final EditText email = findViewById(R.id.email, EditText.class);
        final EditText password = findViewById(R.id.password, EditText.class);
        email.setText(mSharedPreferences.getString("email", ""));
        password.setText(mSharedPreferences.getString("password", ""));
        updateButtons(password.getText());
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateButtons(charSequence);
            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        findViewById(R.id.sign_up, Button.class).setOnClickListener(
                view -> register(email.getText().toString(), password.getText().toString()));
        findViewById(R.id.sign_in, Button.class).setOnClickListener(
                view -> signInWithEmail(email.getText().toString(), password.getText().toString()));
        initFBLogin();
    }

    private void initFBLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("facebook:onError", error);
                // ...
                showFailed();
            }
        });
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d("onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d("onAuthStateChanged:signed_out");
            }
            // ...
        };
    }

    private void updateButtons(CharSequence charSequence) {
        if (charSequence.length() < 6) {
            findViewById(R.id.sign_in).setEnabled(false);
            findViewById(R.id.sign_up).setEnabled(false);
        } else {
            findViewById(R.id.sign_in).setEnabled(true);
            findViewById(R.id.sign_up).setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signInWithGoogle();
                break;
            default:
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.i("Sign In with Google success");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.i("Sign In with Google failed");
            }
            Log.i(result.getStatus().getStatusMessage());
        }
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the mFirebaseAuth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        onSignInSuccess();
                    }
                });
    }

    private void onSignInSuccess() {
        mApplicationData.initUserData();
        enterMainOrSetup(0);
    }

    boolean credentialValid() {

        final EditText email = findViewById(R.id.email, EditText.class);
        final EditText password = findViewById(R.id.password, EditText.class);

        if (!Util.isValidEmail(email.getText().toString().toLowerCase().trim())) {
            showToast("EmailId is invalid");
            return false;
        }
        if (password == null || password.length() < 1) {
            showToast("Password should not be less then 1 characters");
            return false;
        }
        return true;
    }

    private void register(final String email, final String password) {
        Log.i("email = ", email, ", psw = ", password);
        if (!credentialValid()) {
            return;
        }
        showProgressDialog();
        mFirebaseAuth.createUserWithEmailAndPassword(email.toLowerCase().trim(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("createUserWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (!task.isSuccessful()) {
                            Log.i(task.getException());
                            showToast("Failed to sign Up");
                            return;
                        }
                        signInWithEmail(email, password);
                    }
                });


    }


    private void signInWithEmail(String email, String password) {
        Log.i("email = ", email, ", psw = ", password);
        if (!credentialValid()) {
            return;
        }
        showProgressDialog();
        mFirebaseAuth.signInWithEmailAndPassword(email.toLowerCase().trim(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();

                        if (!task.isSuccessful()) {
                            Log.i(task.getException());
                            showToast("Failed to sign in");
                            return;
                        }

                        mSharedPreferences.edit().putString("email", findViewById(R.id.email, EditText.class).getText().toString()).apply();
                        mSharedPreferences.edit().putString("password", findViewById(R.id.password, EditText.class).getText().toString()).apply();
                        onSignInSuccess();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_sign_in;
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("handleFacebookAccessToken:" + token);
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.i("signInWithCredential:onComplete:" + task.isSuccessful());
                    hideProgressDialog();
                    if (!task.isSuccessful()) {
                        Log.i("signInWithCredential", task.getException());
                        showToast("Failed to sign in with Facebook");
                        return;
                    }
                    onSignInSuccess();
                });
    }

    private void showFailed() {
        showToast("Sign In failed");
    }
}
