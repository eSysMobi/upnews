package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mobi.esys.tasks.GetProductInfoTask;


public class InAppBillingActivity extends Activity {
    private transient static final int BILL_INTENT_CODE = 1001;
    private transient IInAppBillingService billingService;
    private transient ServiceConnection billingServiceConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inappbilling);


        billingServiceConn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                billingService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                billingService = IInAppBillingService.Stub.asInterface(service);
                GetProductInfoTask getProductInfoTask = new GetProductInfoTask(
                        InAppBillingActivity.this);
                getProductInfoTask.execute(billingService);
                try {
                    String purchaseData = "";
                    Bundle ownedItems = billingService.getPurchases(3,
                            getPackageName(), "subs", null);
                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> purchaseDataList = ownedItems
                                .getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            purchaseData = purchaseDataList.get(i);
                            Log.d("info", purchaseData);
                        }

                    }
                    if (purchaseData == "") {
                        Bundle buyIntentBundle = billingService.getBuyIntent(3,
                                getPackageName(), "upnews_hashtag_one_month", "subs", "");
                        PendingIntent pendingIntent = buyIntentBundle
                                .getParcelable("BUY_INTENT");

                        startIntentSenderForResult(
                                pendingIntent.getIntentSender(),
                                BILL_INTENT_CODE, new Intent(),
                                Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    } else {
                        startActivity(new Intent(InAppBillingActivity.this,
                                InstaLoginActivity.class));
                        finish();

                    }

                } catch (RemoteException e) {
                } catch (IntentSender.SendIntentException e) {
                }
            }

        };

        bindService(new Intent(
                        "com.android.vending.billing.InAppBillingService.BIND").setPackage("com.android.vending"),
                billingServiceConn, BIND_AUTO_CREATE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingService != null && billingServiceConn != null) {
            unbindService(billingServiceConn);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d("buy", "You have bought the " + sku
                            + ". Excellent choice," + "adventurer!");
                    startActivity(new Intent(InAppBillingActivity.this,
                            InstaLoginActivity.class));

                } catch (JSONException e) {
                    Log.d("buy", "Failed to parse purchase data.");
                    e.printStackTrace();
                }
            } else {
                finish();
            }
        }
    }
}
