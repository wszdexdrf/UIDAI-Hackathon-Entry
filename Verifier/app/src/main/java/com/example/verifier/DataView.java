package com.example.verifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class DataView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        String encrypted = getIntent().getStringExtra("data");
        TextView name = findViewById(R.id.textView3);
        TextView gender = findViewById(R.id.textView5);
        TextView dob = findViewById(R.id.textView7);
        TextView statusText = findViewById(R.id.textView8);
        String nameText = "Incorrect QR Code", genderText = "Incorrect QR Code", dobText = "Incorrect QR Code";
        boolean nameExists = false, dobExists = false, genderExists = false, timeCorrect = false;
        String ans = "";
        try {
            PublicKey publicKey = getKeyFromCertificate();
            ans = decryptor(encrypted, publicKey);
        } catch (Exception e) {
            Log.e("Exception", "An exception has occurred", e);
        }
        Log.e("TAG", ans);
        StringTokenizer st = new StringTokenizer(ans, " =\n");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            switch (s) {
                case "Name":
                    nameText = st.nextToken();
                    nameExists = true;
                    break;
                case "Gender":
                    genderText = st.nextToken();
                    genderExists = true;
                    break;
                case "DOB":
                    dobText = st.nextToken();
                    dobExists = true;
                    break;
                case "Time":
                    long time = Long.parseLong(st.nextToken());
                    time = System.currentTimeMillis() - time;
                    if (Math.abs(time) < 5L * 60L * 1000L) {
                        timeCorrect = true;
                    }
                    break;
                default:
                    Log.d("switch", "No case matched");
            }
        }
        name.setText(nameText);
        gender.setText(genderText);
        dob.setText(dobText);
        if (nameExists && genderExists && dobExists){
            if (timeCorrect){
                ImageView status = findViewById(R.id.imageView2);
                status.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_24));
                status.setBackgroundColor(getResources().getColor(R.color.green));
            }
            else {
                statusText.setText(getResources().getString(R.string.timeout));
                statusText.setVisibility(View.VISIBLE);
            }
        }
    }

    private String decryptor(String data, PublicKey key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data);
        Cipher decrypt = Cipher.getInstance("RSA");
        decrypt.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = decrypt.doFinal(bytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private PublicKey getKeyFromCertificate() throws IOException, CertificateException {
        AssetManager am = getAssets();
        InputStream is = am.open("cert.cer");
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate storedCertificate = (X509Certificate) f.generateCertificate(is);
        return storedCertificate.getPublicKey();
    }
}