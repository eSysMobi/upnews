package mobi.esys.twitter.model;

import android.content.Context;
import android.widget.RelativeLayout;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.services.SearchService;

import mobi.esys.view.TwitterLine;


public class TwitterHelper {

    public static void startLoadTweets(final TwitterApiClient client, final String hashTag, final RelativeLayout relativeLayout, final Context context) {
        loadTweets(client, hashTag, relativeLayout, context);
    }

    private static void loadTweets(final TwitterApiClient client, final String hashTag, final RelativeLayout relativeLayout, final Context context) {
        final SearchService service = client.getSearchService();
        if (!hashTag.isEmpty() && hashTag != null) {
            String tag = hashTag.replace("#", "");
            service.tweets(tag, null, null, null, null, null, null, null, null, null, new Callback<Search>() {
                @Override
                public void success(Result<Search> searchResult) {
                    FeedToText feedToText = new FeedToText(searchResult.data.tweets, context);
                    TwitterLine twitterLine = new TwitterLine(relativeLayout, context, feedToText.twProfileImageUrls());
                    twitterLine.removeTextSwitcher();
                    twitterLine.start(feedToText.getFormattedTweets());
                }

                @Override
                public void failure(TwitterException e) {

                }
            });
        }
    }
}
