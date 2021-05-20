package edu.eci.ieti.googlemapapi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

@SuppressLint( "ParcelCreator" )
public class AddressResultReceiver
        extends ResultReceiver
{

    private AddressResultListener addressResultListener;

    public AddressResultReceiver( Handler handler )
    {
        super( handler );
    }

    public void setAddressResultListener( AddressResultListener addressResultListener )
    {
        this.addressResultListener = addressResultListener;
    }

    @Override
    protected void onReceiveResult( int resultCode, Bundle resultData )
    {
        // Display the address string
        // or an error message sent from the intent service.
        String addressOutput = resultData.getString( FetchAddressIntentService.RESULT_DATA_KEY );

        if ( resultCode == FetchAddressIntentService.SUCCESS_RESULT && addressResultListener != null )
        {
            addressResultListener.onAddressFound( addressOutput );
        }

    }

}