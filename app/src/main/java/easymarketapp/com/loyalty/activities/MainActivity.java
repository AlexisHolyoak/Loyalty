package easymarketapp.com.loyalty.activities;

import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import easymarketapp.com.loyalty.R;
import easymarketapp.com.loyalty.common.LoyaltyCardReader;

public class MainActivity extends AppCompatActivity implements LoyaltyCardReader.AccountCallback {
    //
    // Solamente leeremos Tags tipo NDEF y NFCA
    //
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A;
    public LoyaltyCardReader mLoyaltyCardReader;
    private TextView mAccountField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountField = (TextView) findViewById(R.id.card_account_field);
        mAccountField.setText("Waiting...");
        mLoyaltyCardReader = new LoyaltyCardReader(this);
        enableReaderMode();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    private void enableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.enableReaderMode(this, mLoyaltyCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }

    @Override
    public void onAccountReceived(final String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAccountField.setText(account);
            }
        });
    }
}
