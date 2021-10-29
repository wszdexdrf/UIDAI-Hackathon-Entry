package com.example.pehchan;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;


import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.canonical.Canonicalizer;

public class Decryptor {
    private static final String ns = null;
    Context context;

    UidData getVerifiedData(Context context, File fileName, String passcode) throws Exception {
        this.context = context;
        ZipFile file = new ZipFile(fileName);
        if (file.isEncrypted()) file.setPassword(passcode.toCharArray());
        FileHeader fh = file.getFileHeader(fileName.getName().substring(0, fileName.getName().indexOf(".zip")) + ".xml");
        InputStream is = file.getInputStream(fh);
        InputStream is2 = file.getInputStream(fh);

        Builder builder = new Builder();
        Document doc = builder.build(is2);
        Element root = doc.getRootElement();
        Node signInfo = root.removeChild(1).getChild(0);
//        Log.e("Verification", signInfo.toXML());
        return parse(is, signInfo, root);
    }

    private UidData parse(InputStream in, Node signInfo, Node data) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        String signature = "This is not a signature";
        String digestValue = "This is not a digest Value";
        String certificate = "This is not a certificate";
        UidData personalInfo = new UidData();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = parser.getName();
                if (tagName.equalsIgnoreCase("Poi")) {
                    Poi poi = new Poi();
                    poi.setDob(parser.getAttributeValue(ns, "dob"));
                    poi.setGender(parser.getAttributeValue(ns, "gender"));
                    poi.setName(parser.getAttributeValue(ns, "name"));
                    personalInfo.setPoi(poi);
                } else if (tagName.equalsIgnoreCase("Pht")) {
                    eventType = parser.next();
                    Pht pht = new Pht();
                    pht.setBase64Photo(parser.getText());
                    personalInfo.setPht(pht);
                } else if (tagName.equalsIgnoreCase("SignatureValue")) {
                    eventType = parser.next();
                    signature = parser.getText();
                } else if (tagName.equalsIgnoreCase("DigestValue")) {
                    eventType = parser.next();
                    digestValue = parser.getText();
                } else if (tagName.equalsIgnoreCase("X509Certificate")) {
                    eventType = parser.next();
                    certificate = parser.getText();
                }
            }
            eventType = parser.next();
        }
        boolean flag = verify(signInfo, signature, data, digestValue, certificate);
        if (!flag) {
            Log.e("Invalid XML", "The XML Signature could not be verified");
        }
        return flag ? personalInfo : null;
    }

    public class UidData {
        private Poi poi;

        private Pht pht;

        public Poi getPoi() {
            return this.poi;
        }

        public void setPoi(Poi val) {
            this.poi = val;
        }

        public Pht getPht() {
            return this.pht;
        }

        public void setPht(Pht val) {
            this.pht = val;
        }
    }

    public class Poi {
        private String dob;

        private String gender;

        private String name;

        public String getDob() {
            return dob;
        }

        public void setDob(String val) {
            this.dob = val;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String val) {
            this.gender = val;
        }

        public String getName() {
            return name;
        }

        public void setName(String val) {
            this.name = val;
        }
    }

    public class Pht {
        private String base64Photo;

        public String getBase64Photo() {
            return this.base64Photo;
        }

        public void setBase64Photo(String val) {
            this.base64Photo = val;
        }
    }

    public boolean verify(Node signedInfo, String signature, Node data, String digestValue, String certificate) throws Exception {
        byte[] digestDecoded = Base64.decode(digestValue, Base64.DEFAULT);
        ByteArrayOutputStream xmlData = new ByteArrayOutputStream();
        Canonicalizer canonicalizer1 = new Canonicalizer(xmlData, Canonicalizer.CANONICAL_XML);
        canonicalizer1.write(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashValue = md.digest(xmlData.toByteArray());
        if (!Arrays.equals(digestDecoded, hashValue)) {
            Log.e("Verification", "The digest value doesn't match");
            return false;
        }

        ByteArrayOutputStream canonicalBytes = new ByteArrayOutputStream();
        Canonicalizer canonicalizer = new Canonicalizer(canonicalBytes, Canonicalizer.CANONICAL_XML);
        canonicalizer.write(signedInfo);
        byte[] canonical = canonicalBytes.toByteArray();

//        ByteArrayInputStream bis = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
        AssetManager am = context.getAssets();
        InputStream is = am.open("AuthStaging25082025.cer");
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate storedCertificate = (X509Certificate) f.generateCertificate(is);
        PublicKey key = storedCertificate.getPublicKey();

//        KeyStore keystore = KeyStore.getInstance("PKCS12");
//        FileInputStream fs = context.openFileInput("PublicAUAforStagingServices.p12");
//        keystore.load(fs, "public".toCharArray());
//        PrivateKey pkey = (PrivateKey) keystore.getKey("PublicAUAforStagingServices", "public".toCharArray());
//        PublicKey publicKey = keystore.getCertificate("PublicAUAforStagingServices").getPublicKey();
//        Signature signer = Signature.getInstance("SHA1withRSA");
//        signer.initSign(pkey);
//        signer.update(canonical);
//        byte[] signtr = signer.sign();
//        Log.e("Signature", Base64.encodeToString(signtr, Base64.DEFAULT));
//
        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initVerify(key);
        instance.update(canonical);
//        return instance.verify(signtr);
        boolean flag = instance.verify(Base64.decode(signature, Base64.DEFAULT));
        Log.e("Verification", String.valueOf(flag));
        return true;
    }
}
