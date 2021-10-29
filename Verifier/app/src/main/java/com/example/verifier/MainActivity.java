package com.example.verifier;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scan = findViewById(R.id.button);
        scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        IntentIntegrator ii = new IntentIntegrator(this);
        ii.setPrompt("Scan Aadhaar Resident App token");
        ii.setOrientationLocked(true);
        ii.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult ir = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (ir != null) {
            if (ir.getContents() == null) {
                Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String text = ir.getContents();
                Intent showData = new Intent(this, DataView.class);
                showData.putExtra("data", text);
                startActivity(showData);
            }
        }
    }
}