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

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private TextView textInput;
    private String confirmation;
    private String keyWithoutHash = "1234";
    private boolean isSending = false;

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
}
