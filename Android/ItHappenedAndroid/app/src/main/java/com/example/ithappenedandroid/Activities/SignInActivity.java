package com.example.ithappenedandroid.Activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ithappenedandroid.R;
import com.example.ithappenedandroid.Retrofit.RetrofitRequests;
import com.example.ithappenedandroid.StaticInMemoryRepository;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.SignInButton;
import com.nvanbenschoten.motion.ParallaxImageView;

import java.io.IOException;
import java.util.UUID;

public class SignInActivity extends Activity {

    private final static String G_PLUS_SCOPE =
            "oauth2:https://www.googleapis.com/auth/plus.me";
    private final static String USERINFO_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile";
    private final static String EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email";
    private final static String SCOPES = G_PLUS_SCOPE + " " + USERINFO_SCOPE + " " + EMAIL_SCOPE;
    private static final String TAG = "EXC";

    ParallaxImageView mainBackground;
    SignInButton signIn;
    TextView mainTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        findControlsById();
        mainBackground.registerSensorManager();
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(3000);
        animation.setFillAfter(true);
        mainTitle.setAnimation(animation);

        mainBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserActionsActivity.class);
                startActivity(intent);
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, 228);
            }
        });
    }

    private void findControlsById(){

        signIn = (SignInButton) findViewById(R.id.signin);
        mainBackground = (ParallaxImageView) findViewById(R.id.mainBackground);
        mainTitle = (TextView) findViewById(R.id.mainTitle);

    }

    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data){

        if (requestCode == 228 && resultCode == RESULT_OK) {
            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            AsyncTask<Void, Void, String> getToken = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {

                    String idToken = "";

                    try {
                        idToken = GoogleAuthUtil.getToken(SignInActivity.this, accountName,
                                SCOPES);
                        Toast.makeText(getApplicationContext(), idToken, Toast.LENGTH_SHORT).show();
                        return idToken;

                    } catch (UserRecoverableAuthException userAuthEx) {
                        startActivityForResult(userAuthEx.getIntent(), 228);
                    } catch (IOException ioEx) {
                        //Log.d(TAG, "IOException");
                        Toast.makeText(getApplicationContext(),"IOException",Toast.LENGTH_SHORT).show();
                    } catch (GoogleAuthException fatalAuthEx) {
                        //Log.d(TAG, "Fatal Authorization Exception" + fatalAuthEx.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(),"Fatal Authorization Exception" + fatalAuthEx.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return idToken;
                }

                @Override
                protected void onPostExecute(String idToken) {
                    reg(idToken);
                }
            };

            getToken.execute(null, null, null);
        }
    }

    private void reg(String idToken){
        //Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
        RetrofitRequests retrofitRequests = new RetrofitRequests(StaticInMemoryRepository.getInstance(), getApplicationContext(), UUID.randomUUID());
        String userId = retrofitRequests.userRegistration(idToken);
        Toast.makeText(getApplicationContext(), userId, Toast.LENGTH_SHORT).show();
    }
}
