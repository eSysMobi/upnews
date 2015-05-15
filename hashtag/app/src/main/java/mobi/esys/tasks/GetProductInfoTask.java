package mobi.esys.tasks;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetProductInfoTask extends
        AsyncTask<IInAppBillingService, Void, Void> {
    private transient Context context;

    public GetProductInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(IInAppBillingService... params) {

        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("upnews_hashtag_one_month");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Bundle skuDetails = null;
        try {
            skuDetails = params[0].getSkuDetails(3, context.getPackageName(),
                    "subs", querySkus);
        } catch (RemoteException e1) {
        }

        int response = skuDetails.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList<String> responseList = skuDetails
                    .getStringArrayList("DETAILS_LIST");

            for (String thisResponse : responseList) {
                JSONObject object;
                try {
                    object = new JSONObject(thisResponse);
                    Log.d("price", object.toString());
                } catch (JSONException e) {
                }

            }
        }
        return null;
    }
}

