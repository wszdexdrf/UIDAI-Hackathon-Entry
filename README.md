# Pehchan
A simple resident app which saves user's Aadhar info and verifies his/her data from it. Both the archives contain same files. The different formats are provided for ease of access.


# Verifier
A sample verifier app which authenticates data sent by Resident app.

---

## About Pehchan (Resident) App
The Pehchan app is a simple app with the help of which you can verify your identiy (Name, gender and dob) and check in quickly at hotels, airline etc. The app will generate a secure QR code which the would be scanned at the reception of hotel, airline etc. which would enable them to confirm your identity and only let you in for your booking.

### Data stored on your phone.
The app stores a password protected zip file containing your XML. Each password is randomly generated 4 digit number You don't need to remember your passwords as the app will manage that itself.

**Sidenote:** I wanted to encrypt the file containing the passwords and file names, but then realised that it would generate another key which I would have to store somewhere again. Therefore the file containing the zip passwords is simply kept in the private folder of the app. In case the user's phone is rooted, he would compromise the security of this app.
