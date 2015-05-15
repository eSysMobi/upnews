package mobi.esys.twitter.model;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.consts.ISConsts;


public class FeedToText {
    private transient List<Tweet> mTweets;
    private transient List<String> urls;
    private transient Context mContext;

    public FeedToText(List<Tweet> tweets, Context context) {
        mTweets = tweets;
        mContext = context;
        urls = new ArrayList<>();
    }

    public List<Spanned> getFormattedTweets() {
        List<Spanned> formattedTweets = new ArrayList<>();
        Log.d("tweets", mTweets.toString());
        for (int i = 0; i < mTweets.size(); i++) {
            Tweet currentTweet = mTweets.get(i);
            StringBuilder builder = new StringBuilder();
            builder.append(ISConsts.globals.default_divider)
                    .append(ISConsts.globals.default_color)
                    .append(currentTweet.user.screenName)
                    .append(" ").append("</font>")
                    .append(":").append(currentTweet.text);
            formattedTweets.add(html(builder.toString()));
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
