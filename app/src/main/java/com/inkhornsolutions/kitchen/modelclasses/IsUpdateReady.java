package com.inkhornsolutions.kitchen.modelclasses;

import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class IsUpdateReady extends AsyncTask<Void, String, String> {
    String appURL = "";
    private UrlResponce mUrlResponce;

    public IsUpdateReady(String appURL, UrlResponce mUrlResponce) {
        this.appURL = appURL;
        this.mUrlResponce = mUrlResponce;
    }

    @Override

    protected String doInBackground(Void... voids) {

        String newVersion = null;

        try {
            Document document = Jsoup.connect(appURL)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            if (document != null) {
                Elements element = document.getElementsContainingOwnText("Current Version");
                for (Element ele : element) {
                    if (ele.siblingElements() != null) {
                        Elements sibElemets = ele.siblingElements();
                        for (Element sibElemet : sibElemets) {
                            newVersion = sibElemet.text();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newVersion;
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);
        if (onlineVersion != null && !onlineVersion.isEmpty()) {
            mUrlResponce.onReceived(onlineVersion);
        }

        Log.d("update", " playstore App version " + onlineVersion);

    }
}
