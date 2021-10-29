package com.example.pehchan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.StringTokenizer;

public class DataFileParser {
    private final File file;
    private final ArrayList<Entry> entries;

    DataFileParser(File file) {
        this.file = file;
        entries = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String entry = br.readLine();
            while (entry != null) {
                StringTokenizer st = new StringTokenizer(entry, ";");
                entries.add(new Entry(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken()));
                entry = br.readLine();
            }
        } catch (FileNotFoundException e) {
            Log.w("Warning", "Data file is empty/not present", e);
        } catch (IOException e) {
            Log.e("Exception", "An exception has occurred", e);
        }
    }

    protected ArrayList<Entry> getList() {
        ArrayList<Entry> copy = new ArrayList<>(entries.size());
        copy.addAll(entries);
        return copy;
    }

    public String getFile(String name, int index) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.getName().equalsIgnoreCase(name) && index == 0) {
                return e.getFileName();
            } else if (e.getName().equalsIgnoreCase(name)) {
                index--;
            }
        }
        return null;
    }

    public Bitmap getPhoto(String name, int index) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.getName().equalsIgnoreCase(name) && index == 0) {
                return e.getPhoto();
            } else if (e.getName().equalsIgnoreCase(name)) {
                index--;
            }
        }
        return null;
    }

    public String getPasscode(String fileName) {
        for (Entry e : entries) {
            if (e.getFileName().equalsIgnoreCase(fileName)) {
                return e.getPasscode();
            }
        }
        return null;
    }

    public boolean checkExists(String name) {
        for (Entry e : entries) {
            if (e.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getAll(String name) {
        ArrayList<String> sameName = new ArrayList<>();
        for (Entry e : entries) {
            if (e.getName().equalsIgnoreCase(name)) {
                sameName.add(e.getName());
            }
        }
        return sameName;
    }

    public void remove(String name, int index, Context context) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.getName().equalsIgnoreCase(name) && index == 0) {
                File file = new File(context.getFilesDir(), e.getFileName());
                if (file.exists()) file.delete();
                entries.remove(index);
            } else if (e.getName().equalsIgnoreCase(name)) {
                index--;
            }
        }
    }

    public void addEntry(String name, String fileName, String passcode, String photo) {
        Entry entry = new Entry(name, fileName, passcode, photo);
        this.entries.add(entry);
    }

    public void flush() throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        for (Entry e : entries) {
            fos.write(e.toString().getBytes(StandardCharsets.UTF_8));
        }
        fos.close();
    }
}

class Entry {
    private final String name;
    private final String fileName;
    private final String passcode;
    private final String photo;

    Entry(String name, String fileName, String passcode, String photo) {
        this.name = name;
        this.fileName = fileName;
        this.passcode = passcode;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPasscode() {
        return passcode;
    }

    @NonNull
    public String toString() {
        return name + ";" + fileName + ";" + passcode + ";" + photo + "\n";
    }

    public Bitmap getPhoto() {
        byte[] bytes = Base64.getDecoder().decode(photo);
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return Bitmap.createScaledBitmap(bmp, 100, 100, false);
    }
}
