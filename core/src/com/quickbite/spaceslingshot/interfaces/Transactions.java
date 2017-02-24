package com.quickbite.spaceslingshot.interfaces;

/**
 * Created by Paha on 2/8/2017.
 */
public interface Transactions {
    // (arbitrary) request code for the purchase flow
    int RC_REQUEST = 10001;

    String SKU_NOADS = "remove_ads";

    void purchaseNoAds();
}
