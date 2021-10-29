package com.example.pehchan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DataFileParser parser;
    private FlexboxLayout layout;
    private String yourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parser = new DataFileParser(new File(getFilesDir(), "keys"));
        ArrayList<Entry> people = parser.getList();
        updateName(people);
        layout = findViewById(R.id.fbRoot);
        View yourP = findViewById(R.id.fab);
        yourP.setOnClickListener(v -> verifyPerson(yourName));
        View refresh = findViewById(R.id.imageButton4);
        refresh.setOnClickListener(v -> refresh());
        addCards(people);
    }

    private void addCards(ArrayList<Entry> people) {
        layout.removeAllViewsInLayout();
        for (Entry e : people) {
            String person = e.getName();
            View card = LayoutInflater.from(this).inflate(R.layout.saved_person, null);
            CardView placeHolder = card.findViewById(R.id.cardView);
            placeHolder.setElevation(0);
            TextView logo = card.findViewById(R.id.logo);
            TextView name = card.findViewById(R.id.person);
            logo.setBackground(new BitmapDrawable(e.getPhoto()));
            logo.setText("");
//            logo.setText(Character.isAlphabetic(person.trim().charAt(1)) ? person.trim().substring(0, 2) : person.trim().substring(0, 1));
            name.setText(person.trim());
            card.setPadding(15, 0, 0, 0);
            placeHolder.setOnClickListener(v -> verifyPerson(person));
            layout.addView(card);
        }
        View addButton = LayoutInflater.from(getApplicationContext()).inflate(R.layout.saved_person, null);
        CardView placeHolder = addButton.findViewById(R.id.cardView);
        placeHolder.setElevation(0);
        addButton.setPadding(15, 0, 0, 0);
        placeHolder.setOnClickListener(v -> addPerson());
        layout.addView(addButton);
    }

    public void addPerson() {
        Intent intent = new Intent(this, AddPersonActivity.class);
        startActivity(intent);
        refresh();
    }

    public void verifyPerson(String name) {
        if (name == null){
            addPerson();
            return;
        }
        Intent intent = new Intent(this, AddPersonActivity.class);
        intent.putExtra("verify", name);
        startActivity(intent);
        refresh();
    }

    public void refresh() {
        parser = new DataFileParser(new File(getFilesDir(), "keys"));
        ArrayList<Entry> people = parser.getList();
        addCards(people);
        updateName(people);
    }

    public void updateName(ArrayList<Entry> people) {
        if (!people.isEmpty()) {
            yourName = people.get(0).getName();
        }
    }
}