# Pehchan
A simple resident app which saves user's Aadhar info and verifies his/her data from it. Both archives must be extracted and merged into one folder for the app to run.


# Verifier
A sample verifier app which authenticates data produced by Resident app.

---

## About Pehchan (Resident) App
The Pehchan app is a simple app with the help of which you can verify your identiy (Name, gender and dob) and check in quickly at hotels, airline etc. The app will generate a secure QR code which the would be scanned at the reception of hotel, airline etc. which would enable them to confirm your identity and only let you in for your booking. You just need to add your info the first time, then you can freely verify yourself using "Your Pehchan" button or selecting your info.

### Data stored on your phone
The app stores a password protected zip file containing your XML. Each password is randomly generated 4 digit number You don't need to remember your passwords as the app will manage that itself. Your Aadhar number is not stored on the phone, so you can rest assured! 

**Sidenote:** I wanted to encrypt the file containing the passwords and file names, but then realised that it would generate another key which I would have to store somewhere again. Therefore the file containing the zip passwords is simply kept in the private folder of the app. In case the user's phone is rooted, he would compromise the security of this app.

### Secure QR Code
The app generates a String of your information (non critical, which is only Name, Date of Birth, Gender and Time stamp ) and encrypts it using RSA algorithm. This data can now be decrypted by a public key available to anyone. Anyone who decrypts this information can be sure that this information was sent only by a Pehchan app and nothing else. Also, the QR code is valid only till 5 minutes after generetion, so noone can take your QR code's photograph and check-in at your bookings.

**Sidenote:** The public-private key pair is the same as the one provided by UIDAI. The Verifier app contains the certificate already. Ideally, a different key pair should be used for each client-verifier, but I was unable to find a secure way of communicating the public key to the verifier, as anyone nefarious can also providea a public key for his private key.

### Verification of Data in the app
The app verifies the data it receives (directly from UIDAI servers, always). This verification is done everytime you generate a QR code.

**Sidenote:** Here I had a lot of problem with UIDAI API. I could not verify the signature of the XML. There are two parts of signature verification, first "Digest Value" which is a hash of the data in the XML. So if the XML data is changed, the hash will not match. Second is encryption of that Digest Value using asymmetric keys, so that someone can't change the hash along with the data in the xml. I was able to match the Digest Value, but could not match the Signature info. When I used the private keys provided and generated the signature myself, I could verify that using the same code. (Though that would be not be useful if I verified my own Signature) I deduced that there may be some discrepancy in either the server-side keys or in the APIs I was using, therefore my code only matches the digest value for now. Another API was provided midway in the hackathon, but it required changing a lot of things (And I was losing password safety) therefore I went ahead with the old API. If the XML is simply changed, then it won't match and an error would be raised. I think that would be enough given that the zip file is already password protected.

### Support multiple identities
The app can save multiple people's data. It asks whether the user wants to overwrite data for some aadhaar if it encounters entries with same name. It even provides the photograph for aiding in selecting, in case the user has multiple entries with same name.

### Helpful messages
The app provides helpful info regarding solution whenever there is any problem.

## Offline Mode
The App also works in offline mode. This does your verification via your fingerprint on your phone (against the stored fingerprint on your phone). Rest of the functionality is same. This is done to ensure that only the user can access the Aadhaar data stored on phone and no one can perform identity theft if he gets users's phone/Signed XML. Of course Aadhaar data needs to be downloaded beforehand on the phone. 

## About Verifier App
A sample Verifier app is also provided. That is a simple app which just scans a QR code, decrypts it using a public key (certificate is packaged with app) and then verifies its tags. It also checks if the QR code was generated in the last 5 minutes. Then it displays the information on the screen with a small Tick or Cross indicating if the data was verified or not. 

# About format of projects
All projects provided in archives. These are Android studio projects, and should run fine when executed through the same on any android phone. All dependencies are indicated in the gradle file and are automatically resolved. Both the app require minimum Android SDK 26 (Oreo).
