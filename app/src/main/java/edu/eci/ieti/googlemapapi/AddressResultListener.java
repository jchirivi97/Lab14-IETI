package edu.eci.ieti.googlemapapi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

public interface AddressResultListener {
    void onAddressFound( String address );
}
