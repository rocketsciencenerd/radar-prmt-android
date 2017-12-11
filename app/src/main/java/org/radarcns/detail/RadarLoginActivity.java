/*
 * Copyright 2017 The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.detail;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;
import org.radarcns.android.RadarConfiguration;

import org.radarcns.android.auth.AppAuthState;
import org.radarcns.android.auth.LoginActivity;
import org.radarcns.android.auth.LoginManager;

import org.radarcns.android.auth.portal.ManagementPortalLoginManager;
//import org.radarcns.android.auth.QrLoginManager;
//import org.radarcns.android.auth.oauth2.OAuth2LoginManager;
import org.radarcns.android.util.Boast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.radarcns.android.RadarConfiguration.RADAR_CONFIGURATION_CHANGED;

public class RadarLoginActivity extends LoginActivity {

    private LoginManager trivialLoginManager;

    private static final Logger logger = LoggerFactory.getLogger(RadarLoginActivity.class);

    //private OAuth2LoginManager oauthManager;
    //private QrLoginManager qrManager;
    private ManagementPortalLoginManager mpManager;
    private boolean canLogin;
    private ProgressDialog progressDialog;
    private final BroadcastReceiver configBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onDoneProcessing();
            mpManager.refresh();
        }
    };


    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RadarConfiguration.getInstance().getStatus() == RadarConfiguration.FirebaseStatus.READY) {
            onProcessing(R.string.retrieving_configuration);
        }
        canLogin = true;
        registerReceiver(configBroadcastReceiver, new IntentFilter(RADAR_CONFIGURATION_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(configBroadcastReceiver);
    }

    @NonNull
    @Override

    protected List<LoginManager> createLoginManagers(final AppAuthState state) {
        this.trivialLoginManager = new LoginManager() {
            private final AppAuthState authState = state;

            @Override
            public AppAuthState refresh() {
                if (authState.isValid()) {
                    return authState;
                }
                return null;
            }

            @Override
            public void start() {
                TextView userIdText = (TextView) findViewById(R.id.inputUserId);
                String userId = userIdText.getText().toString();
                if (!userId.isEmpty()) {
                    SharedPreferences preferences = RadarLoginActivity.this.getSharedPreferences("main", Context.MODE_PRIVATE);
                    preferences.edit().putString("userId", userId).apply();
                    loginSucceeded(this, new AppAuthState.Builder().projectId("0").userId(userId).expiration(Long.MAX_VALUE).build());
                } else {
                    loginFailed(this, null);
                }
            }

            @Override
            public void onActivityCreate() {
                // noop
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                // noop
            }
        };
        return Arrays.asList(this.trivialLoginManager);
    }

    /*
    protected List<LoginManager> createLoginManagers(AppAuthState state) {
        logger.info("Creating mpManager");
        this.mpManager = new ManagementPortalLoginManager(this, state);
        this.qrManager = new QrLoginManager(this, new AuthStringParser() {
            @Override
            public AppAuthState parse(@NonNull String s) {
                onProcessing(R.string.logging_in);
                try {
                    JSONObject object = new JSONObject(s);
                    if (!object.has("refreshToken")) {
                        throw new IllegalArgumentException("No valid refresh token found");
                    }
                    String refreshToken = object.getString("refreshToken");
                    mpManager.setRefreshToken(refreshToken);
                    return mpManager.refresh();
                } catch (JSONException e) {
                    throw new IllegalArgumentException("QR code does not contain valid JSON.", e);
                }
            }
        });
        return Arrays.asList(this.qrManager, this.mpManager);
    }
    */


    private void onProcessing(int titleResource) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(titleResource);
        progressDialog.show();
    }

    private void onDoneProcessing() {
        if (progressDialog != null) {
            logger.info("Closing progress window");
            progressDialog.cancel();
            logger.info("Closed progress window");
            progressDialog = null;
        }
    }

    @NonNull
    @Override
    protected Class<? extends Activity> nextActivity() {
        return DetailMainActivity.class;
    }

    public void scan(View view) {
        logger.warn("Scan functionality not available!");
    }

    public void login(View view) {
        if (canLogin) {
            canLogin = false;
            //onProcessing(R.string.firebase_fetching);
            final RadarConfiguration config = RadarConfiguration.getInstance();
            trivialLoginManager.start();
            /*
            config.fetch().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    onDoneProcessing();
                    config.activateFetched();
                    oauthManager.start();
                }
            });
            */
        }
    }

    @Override
    public void loginFailed(LoginManager manager, Exception ex) {
        canLogin = true;
        onDoneProcessing();
        super.loginFailed(manager, ex);
    }

    @Override
    public void loginSucceeded(LoginManager manager, @NonNull AppAuthState state) {
        onDoneProcessing();
        super.loginSucceeded(manager, state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
