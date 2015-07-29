package mobi.esys.upnews_online.cbr;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mobi.esys.upnews_online.PlayerActivity;

public class GetCurrencies extends AsyncTask<Date,Void,CurrenciesList> {
    private static final String LOG_TAG = "getCurrencies";
    private transient Context context;
    private transient Date yeasterDay;
    private transient CurrenciesList yeasterdayList;

    //u0024 dollar
    //u00a3 pound
    //u00a5 cny
    //u20ac euro


    public GetCurrencies(Context context) {
        this.context = context;
    }

    @Override
    protected CurrenciesList doInBackground(Date... params) {


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        yeasterDay=cal.getTime();

        yeasterdayList=getCurrencyListByDate(yeasterDay);

        return getCurrencyListByDate(params[0]);
    }


    private CurrenciesList getCurrencyListByDate(Date date){
        CurrenciesList currenciesList = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

            String today=format.format(date);

            URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp?date_req=".concat(today));

            Reader reader = new InputStreamReader(getInputStream(url));
            Persister serializer = new Persister();
            try
            {
                currenciesList = serializer.read(CurrenciesList.class, reader, false);
                Log.v("SimpleTest", "stock: " + currenciesList.currencies.toString());
            }
            catch (Exception e)
            {
                Log.e("SimpleTest", e.getMessage());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return currenciesList;
    }

    private  InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(CurrenciesList currenciesList) {
        super.onPostExecute(currenciesList);
        ((PlayerActivity)context).loadCurrencyDashboard(currenciesList,yeasterdayList);
    }
}
