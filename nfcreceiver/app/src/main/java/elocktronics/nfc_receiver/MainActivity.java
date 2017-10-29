package elocktronics.nfc_receiver;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private TextView textInput;
    private String confirmation;
    private String keyWithoutHash = "1234";
    private boolean isSending = false;
    private String[] unlockingDeviceKeys;
    NfcAdapter mAdapter;
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    AES aes = new AES();
    boolean newProject = true;

    int devices = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textInput = (TextView) findViewById(R.id.textInput);
        setup();

    }

    @Override
    protected void onResume(){
        super.onResume();

        main();
    }

    public void main() {
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String stringMessage = new String(message.getRecords()[0].getPayload());

            if (stringMessage.equals("setup")) {
                int devicesCount = getPreferences(MODE_PRIVATE).getInt("ELOCK_DEVICES", -1);
                if (devicesCount == -1) {
                    setup();
                    Log.i("Setup?", Integer.toString(devicesCount));
                } else if (devicesCount != 0) {
                    Log.i("Setup?", Integer.toString(devicesCount));
                    int devicesTotal = getPreferences(MODE_PRIVATE).getInt("ELOCK_DEVICES_TOTAL", -1);
                    String keys = getPreferences(MODE_PRIVATE).getString("ELOCK_KEY", "");
                    String key = slice((devicesCount * 16) - 1, ((devicesCount + 1) * 16) - 1, keys);

                    confirmation = "";
                    nfcSend();
                } else if (devicesCount == 0) {
                    Log.i("Setup?", Integer.toString(devicesCount));
                    // setup done - lock ready
                }
            } else {

            }

            if (stringMessage.equals(keyWithoutHash)) {
                //confirmation = "accepted";
                textInput.setText("Accepted");
                isSending = true;
                nfcSend();
            } else {
                //confirmation = "denied";
                textInput.setText("Denied");
                isSending = true;
                nfcSend();
            }
        } else
            textInput.setText("Waiting for Transmittion...");
    }

    public void setup(){
        String encryptKey = "geheim";
        String encryptString = genCode(devices);
        Log.i("Generaged key", encryptString);
        try {
            byte[] encryptedKey = aes.encrypt(encryptKey, encryptString);
            System.out.println(encryptedKey);
            String encryptedString = new String(encryptedKey);
            getPreferences(MODE_PRIVATE).edit().putString("ELOCK_KEY", encryptedString).commit();
            getPreferences(MODE_PRIVATE).edit().putInt("ELOCK_DEVICES", devices - 1).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sliced = slice(0, 15, encryptString);
        confirmation = sliced;
        Log.i("Confirmation", sliced);
    }

    public String slice(int start, int end, String s) {
        String temp = "";
        end++;
        for(int i = start; i < end; i++) {
            temp += String.valueOf(s.charAt(i));
        }
        return temp;
    }

    public void countdownDevices(String message) {

    }

    public void receiveKeys() {

    }

    public void nfcSend() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (!mAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
        }

        mAdapter.setNdefPushMessageCallback(this, this);
        mAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", confirmation.getBytes());
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        isSending = false;
    }

    public void receiveResult() {
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage NDEFMessage = (NdefMessage) rawMessages[0]; // only one message transferred
            String message = new String(NDEFMessage.getRecords()[0].getPayload());

            // On receive
            File f = new File("config.json");
            if (f.exists()) {
                if(message.length()==1) {
                    // Fehler an Sender senden: Schloss wurde schon einmal eingerichtet!
                } else {
                    //prüfen, ob key in gesamt key vorhanden
                }
            } else {
                newLock(Integer.parseInt(message));
            }
        }
    }

    public void newLock(int user){
        String key = "";
        for(int i=0; i<key.length(); i++) {
            key = key + genCode(user);
        }
        /*try {
            // key in datei schreiben
        } catch (IOException e) {
            System.out.println(e);
            Context context = getApplicationContext();
            CharSequence text = "File saving error...";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }*/
    }

    public String genCode(int user){
        String out = "";
        Random random = new Random();
        for(int i=0; i < (user * 16); i++){
            int r = random.nextInt(9);
            out += r;
        }
        return out;
    }
}
