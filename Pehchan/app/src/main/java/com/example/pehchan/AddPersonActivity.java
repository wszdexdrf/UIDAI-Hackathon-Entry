package com.example.pehchan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class AddPersonActivity extends AppCompatActivity {
    private static String captchaTxnId;
    private static final String otpUrl = "https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/generate/aadhaar/otp";
    private static final String captchaUrl = "https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/get/captcha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        getCaptcha();
        ImageButton reload = findViewById(R.id.imageButton);
        Button offline = findViewById(R.id.button3);
        if (!getIntent().hasExtra("verify")) {
            offline.setVisibility(View.GONE);
        }
        offline.setOnClickListener(v -> verifyOffline());
        reload.setOnClickListener(v -> getCaptcha());
        Button next = findViewById(R.id.button);
        next.setOnClickListener(v -> {
            RequestQueue queue = Volley.newRequestQueue(this);
            Intent getOtp = new Intent(this, OTPActivity.class);
            EditText aadharNum = findViewById(R.id.editTextTextPersonName);
            String uid = aadharNum.getText().toString();
            EditText captcha = findViewById(R.id.editTextTextPersonName2);
            String code = captcha.getText().toString();
            String transactionId = "MYAADHAAR:" + UUID.randomUUID().toString();
            JSONObject req2 = new JSONObject();
            try {
                req2.put("uidNumber", uid);
                req2.put("captchaTxnId", captchaTxnId);
                req2.put("captchaValue", code);
                req2.put("transactionId", transactionId);
            } catch (JSONException e) {
                Log.e("Exception", "An exception has occurred", e);
            }
            JsonObjectRequest otpRequest = new JsonObjectRequest(Request.Method.POST, otpUrl, req2, response -> {
                String mobileNumber = "xx";
                try {
                    if (!response.getString("status").equalsIgnoreCase("Success")) {
                        Toast invCaptcha = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG);
                        invCaptcha.show();
                        return;
                    }
                    mobileNumber = response.getString("mobileNumber");
                    getOtp.putExtra("txnID", response.getString("txnId"));
                } catch (JSONException e) {
                    Log.e("Exception", "An exception has occurred", e);
                }
                Intent intent = getIntent();
                if (intent.hasExtra("verify")) {
                    getOtp.putExtra("verify", intent.getStringExtra("verify"));
                }
                getOtp.putExtra("uid", uid);
                getOtp.putExtra("number", mobileNumber);
                startActivity(getOtp);
                finish();
            }, error -> {
                Toast err = Toast.makeText(getApplicationContext(), "Network error. Try again later.", Toast.LENGTH_SHORT);
                err.show();
            });
            queue.add(otpRequest);
        });
    }

    void getCaptcha() {
        Toast pop = Toast.makeText(this, "Network error. Press Refresh button to try again.", Toast.LENGTH_SHORT);
        Toast pop2 = Toast.makeText(this, "Server error. Try again later.", Toast.LENGTH_SHORT);
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject req = new JSONObject();
        ImageView img = findViewById(R.id.imageView2);
        try {
            req.put("langCode", "en");
            req.put("captchaLength", "3");
            req.put("captchaType", "2");
        } catch (JSONException e) {
            Log.e("Exception", "An exception has occurred", e);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, captchaUrl, req, response -> {
                    try {
                        if (response.getInt("statusCode") != 200) {
                            pop2.show();
                            return;
                        }
                        captchaTxnId = response.getString("captchaTxnId");
                        byte[] bytes = Base64.getDecoder().decode(response.getString("captchaBase64String"));
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        int scale = 3;
                        img.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth() * scale, bmp.getHeight() * scale, false));
                    } catch (JSONException e) {
                        Log.e("Exception", "An exception has occurred", e);
                    }
                }, error -> pop.show());
        queue.add(jsonObjectRequest);
    }

    void verifyOffline() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt bp = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent verify = new Intent(AddPersonActivity.this, OTPActivity.class);
                verify.putExtra("verify", getIntent().getStringExtra("verify"));
                verify.putExtra("offline", "Y");
                startActivity(verify);
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(AddPersonActivity.this, errString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AddPersonActivity.this, "Authentication Failed. Please use online services", Toast.LENGTH_LONG).show();
            }
        });
        BiometricPrompt.PromptInfo prompt = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify")
                .setSubtitle("Verify using your device info")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Cancel")
                .build();
        bp.authenticate(prompt);
    }
}