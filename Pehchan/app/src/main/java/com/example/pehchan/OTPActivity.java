package com.example.pehchan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class OTPActivity extends AppCompatActivity {
    private Bundle extras;
    private final static String eKYCUrl = "https://stage1.uidai.gov.in/eAadhaarService/api/downloadOfflineEkyc";
    private final static String passcode = String.format("%04d", new Random().nextInt(10000));
    private Decryptor.UidData info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);
        extras = getIntent().getExtras();
        TextView message = findViewById(R.id.textView4);
        message.setText(String.format("%s%s", getString(R.string.opt_prompt), extras.getString("number")));
        Button submit = findViewById(R.id.button2);
        EditText otpField = findViewById(R.id.editTextNumberPassword);
        boolean toVerify = getIntent().hasExtra("verify");
        DataFileParser dfp = new DataFileParser(new File(getFilesDir(), "keys"));
        if (getIntent().hasExtra("offline")) {
            verify(dfp, extras.getString("verify"));
        } else {
            submit.setOnClickListener(v -> {
                Toast error = Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT);
                String otp = otpField.getText().toString();
                RequestQueue queue = Volley.newRequestQueue(this);
                JSONObject req = new JSONObject();
                try {
                    req.put("uid", extras.getString("uid"));
                    req.put("txnNumber", extras.getString("txnID"));
                    req.put("otp", otp);
                    req.put("shareCode", passcode);

                } catch (JSONException e) {
                    Log.e("Exception", "An exception has occurred", e);
                }
                JsonObjectRequest ekycxmlRequest = new JsonObjectRequest(Request.Method.POST, eKYCUrl, req, response -> {
                    try {
                        if (!response.getString("status").equalsIgnoreCase("Success")) {
                            error.show();
                            return;
                        }
                        byte[] bytes = Base64.getDecoder().decode(response.getString("eKycXML"));
                        File file = new File(getFilesDir(), response.getString("fileName"));
                        FileOutputStream fos = openFileOutput(response.getString("fileName"), Context.MODE_PRIVATE);
                        fos.write(bytes);
                        Decryptor decrypt = new Decryptor();
                        info = decrypt.getVerifiedData(this, file, passcode);
                        fos.close();
                        ArrayList<String> list = dfp.getAll(info.getPoi().getName());
                        if (list.size() > 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            AtomicInteger index = new AtomicInteger(0);
                            builder.setTitle(R.string.overwrite)
                                    .setSingleChoiceItems(list.toArray(new CharSequence[0]), 0, (dialog, i) -> {
                                        index.set(i);
                                    })
                                    .setPositiveButton(R.string.overwrite, (dialogInterface, i) -> {
                                        if (index.get() < 0) {
                                            Toast.makeText(this, "Select anyone or click Add new", Toast.LENGTH_SHORT).show();
                                        } else {
                                            try {
                                                dfp.remove(info.getPoi().getName(), index.get(), getApplicationContext());
                                                addData(dfp, toVerify, response.getString("fileName"));
                                            } catch (IOException | JSONException e) {
                                                Log.e("Exception", "An exception has occurred", e);
                                            }
                                        }
                                    })
                                    .setIcon(new BitmapDrawable(dfp.getPhoto(info.getPoi().getName(), index.get())))
                                    .setNegativeButton(R.string.add_new, (dialogInterface, i) -> {
                                        try {
                                            addData(dfp, toVerify, response.getString("fileName"));
                                        } catch (JSONException | IOException e) {
                                            Log.e("Exception", "An exception has occurred", e);
                                        }
                                    });
                            builder.show();
                        } else {
                            try {
                                addData(dfp, toVerify, response.getString("fileName"));
                            } catch (JSONException | IOException e) {
                                Log.e("Exception", "An exception has occurred", e);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Exception", "An exception has occurred", e);
                    }
                }, error1 -> {
                    if (toVerify) {
                        Toast.makeText(this, "Network error. Continuing in offline mode", Toast.LENGTH_SHORT).show();
                        verify(dfp, extras.getString("verify"));
                    }
                    Toast.makeText(this, "Network error. Try again later", Toast.LENGTH_SHORT).show();
                });
                queue.add(ekycxmlRequest);
            });
        }
    }

    void addData(DataFileParser dfp, boolean toVerify, String fileName) throws IOException {
        dfp.addEntry(info.getPoi().getName(), fileName, passcode, info.getPht().getBase64Photo());
        Toast.makeText(this, "Aadhaar Data Updated.", Toast.LENGTH_SHORT).show();
        if (toVerify) {
            verify(dfp, extras.getString("verify"));
        } else {
            dfp.flush();
            finish();
        }
    }

    void verify(DataFileParser dfp, String name) {
        Log.e("TAG", name);
        if (info == null) {
            if (!dfp.checkExists(name)) {
                Toast.makeText(this, "Aadhaar data is not saved. Connect to internet to download.", Toast.LENGTH_LONG).show();
                return;
            }
            ArrayList<String> list = dfp.getAll(name);
            final File[] file = {new File(getFilesDir(), dfp.getFile(name, 0))};
            if (list.size() > 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.choose_name)
                        .setItems(list.toArray(new CharSequence[0]), (dialogInterface, i) -> {
                            file[0] = new File(getFilesDir(), dfp.getFile(name, i));
                        })
                        .setPositiveButton(R.string.add_new, (dialogInterface, i) -> {
                        });
            }
            String passcode = dfp.getPasscode(file[0].getName());
            Decryptor decrypt = new Decryptor();
            try {
                info = decrypt.getVerifiedData(this, file[0], passcode);
            } catch (Exception e) {
                Log.e("Exception", "An exception has occurred", e);
            }
        }
        try {
            String data = "Name = " + info.getPoi().getName() + "\nGender = " + info.getPoi().getGender() + "\nDOB = " + info.getPoi().getDob() + "\nTime = " + System.currentTimeMillis();
            data = encrypt(data);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;
            QRGEncoder encoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, Math.min(width, height) * 3 / 4);
            Bitmap bmp = encoder.encodeAsBitmap();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View image = inflater.inflate(R.layout.qr_code, null);
            ImageView qr = image.findViewById(R.id.imageView4);
            qr.setImageBitmap(bmp);
            builder.setView(image).setNegativeButton("Done", ((dialogInterface, i) -> {
                try {
                    dfp.flush();
                    finish();
                } catch (IOException e) {
                    Log.e("Exception", "An exception has occurred", e);

                }
            }));
            builder.show();
        } catch (Exception e) {
            Log.e("Exception", "An exception has occurred", e);
        }
    }


    private String encrypt(String message) throws Exception {
        AssetManager am = getAssets();
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        InputStream fs = am.open("PublicAUAforStagingServices.p12");
        keystore.load(fs, "public".toCharArray());
        PrivateKey pkey = (PrivateKey) keystore.getKey("PublicAUAforStagingServices", "public".toCharArray());
        Cipher encryptor = Cipher.getInstance("RSA");
        encryptor.init(Cipher.ENCRYPT_MODE, pkey);
        byte[] encrypted = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
