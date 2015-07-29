package mobi.esys.upnews_online.twitter;


import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

public class FeedToText {
    private transient List<Tweet> mTweets;
    private transient List<String> urls;

    public FeedToText(List<Tweet> tweets) {
        mTweets = tweets;
        urls = new ArrayList<>();
    }

    public List<Spanned> getFormattedTweets() {
        List<Spanned> formattedTweets = new ArrayList<>();
        Log.d("tweets", mTweets.toString());
        for (int i = 0; i < mTweets.size(); i++) {
            Tweet currentTweet = mTweets.get(i);
            formattedTweets.add(html("</font>" + "<font color='#11A2F0'>@" + currentTweet.user.screenName + " " + "</font>" + ":" + currentTweet.text));
            urls.add(currentTweet.user.profileImageUrl);
        }
        return formattedTweets;
    }

    public List<String> twProfileImageUrls() {
        return urls;
    }

    private Spanned html(String src) {
        return Html.fromHtml(src);
    }
}
