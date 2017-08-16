package easymarketapp.com.loyalty.common;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;

import java.io.IOException;
import java.lang.ref.WeakReference;

//
//  Esta clase tipo Callback será llamada cuando una etiqueta NFC sea leída y
//  el dispositivo este encendido en modo lectura NFC.
//
public class LoyaltyCardReader implements NfcAdapter.ReaderCallback {
    private static final String TAG = "LoyaltyCardReader";

    //
    // Esta referencia Weak se utiliza para prevenir que se detenga el loop.
    // mAccountCallback se utiliza para salir del modo de fondo antes que sea
    // invalido (ej. durante onPause() or onStop()).
    //
    private WeakReference<AccountCallback> mAccountCallback;

    public interface AccountCallback {
        public void onAccountReceived(String account);
    }

    public LoyaltyCardReader(AccountCallback accountCallback) {
        mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
    }

    //
    // Este Callback se inicia cuando una etiqueta es
    // descubierta por el sistema.
    //
    @Override
    public void onTagDiscovered(Tag tag) {
        //
        // Las etiquetas validas son NDEF y NCFA y cuando estén descubiertas se
        // procesarán aquí dependiendo de su tipo.
        //
        Ndef ndef = Ndef.get(tag);
        if (ndef != null)
        {
            //
            // Si se descubre una etiqueta NDEF se tendrá
            // que comunicar con el sistema desde aquí.
            //
            ndefDiscovered(ndef);
        }
        else
        {
            NfcA nfcA = NfcA.get(tag);
            if (nfcA != null) {
                //
                // Si se descubre una etiqueta NFCA se tendrá
                // que comunicar con el sistema desde aquí.
                //
                nfcADiscovered(nfcA);
            }
        }
    }

    private void nfcADiscovered(NfcA nfcA) {
        try {

            //
            // Conectarse con una etiqueta NFCA
            //
            nfcA.connect();

            //
            // Leer mensaje de la etiqueta NFCA
            //
            byte[] result = nfcA.transceive(new byte[] {
                    (byte)0x30,  // READ
                    (byte)(1 & 0x0ff)
            });

            if (result == null) {
                mAccountCallback.get().onAccountReceived("XXXX XXXX XXXX XXXX");
            } else if ((result.length == 1) && ((result[0] & 0x00A) != 0x00A)) {
                mAccountCallback.get().onAccountReceived("XXXX XXXX XXXX XXXX");
            } else if (result.length < 16) {
                mAccountCallback.get().onAccountReceived("XXXX XXXX XXXX XXXX");
            } else  {
                String accountNumber = "";
                for (byte digit : result)
                {
                    accountNumber = accountNumber + String.valueOf(Math.abs(digit % 10));
                }

                mAccountCallback.get().onAccountReceived(accountNumber);
            }

        }
        catch (IOException e)
        {
            mAccountCallback.get().onAccountReceived("XXXX XXXX XXXX XXXX");
        }
    }

    private void ndefDiscovered(Ndef ndef) {
        try {
            //
            // Conectarse con una etiqueta NDEF
            //
            ndef.connect();

            //
            // Leer mensaje de la etiqueta NDEF
            //
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String accountNumber = new String(ndefMessage.getRecords()[0].getPayload());
            mAccountCallback.get().onAccountReceived(accountNumber);
        } catch (IOException | FormatException e)
        {
            mAccountCallback.get().onAccountReceived("XXXX XXXX XXXX XXXX");
        }
    }
}
