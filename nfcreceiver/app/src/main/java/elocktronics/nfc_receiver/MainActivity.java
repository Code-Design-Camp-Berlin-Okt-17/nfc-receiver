package elocktronics.nfc_receiver;

import android.content.Context;
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

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private TextView textInput;
    private String confirmation;
    private String keyWithoutHash = "1234";
    private boolean isSending = false;
    private String[] unlockingDeviceKeys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textInput = (TextView) findViewById(R.id.textInput);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String stringMessage = new String(message.getRecords()[0].getPayload());
            Log.i("Done", stringMessage);
            if(stringMessage.equals(keyWithoutHash)){
                confirmation = "accepted";
                textInput.setText("Accepted");
                isSending = true;
                nfcSend();
            }else{
                confirmation = "denied";
                textInput.setText("Denied");
                isSending = true;
                nfcSend();
            }

        } else
            textInput.setText("Waiting for Transmittion...");
    }

    public void nfcSend() {
        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);

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
        try {
            // key in datei schreiben
        } catch (IOException e) {
            System.out.println(e);
            Context context = getApplicationContext();
            CharSequence text = "File saving error...";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public String genCode(int user){
        String out = "";
        for(int i=0; i<16; i++){
            int r = (int)Math.random()*10;
            out = out + r;
        }
        return out;
    }
}
