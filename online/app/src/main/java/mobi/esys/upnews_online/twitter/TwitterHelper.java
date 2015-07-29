package mobi.esys.upnews_online.twitter;


import android.content.Context;
import android.widget.RelativeLayout;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

public class TwitterHelper {

    public static void startLoadTweets(final TwitterApiClient client,
                                       final RelativeLayout relativeLayout,
                                       final Context context,
                                       final boolean isFirst) {
        loadTweets(client, relativeLayout, context, isFirst);
    }

    private static void loadTweets(final TwitterApiClient client,
                                   final RelativeLayout relativeLayout,
                                   final Context context,
                                   final boolean isFirst) {
        final StatusesService service = client.getStatusesService();
        service.homeTimeline(null, null, null, null, null, null, null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                FeedToText feedToText = new FeedToText(result.data);
                TwitterLine twitterLine= new TwitterLine(relativeLayout, context, feedToText.twProfileImageUrls(),isFirst);
                twitterLine.start(feedToText.getFormattedTweets());
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }
}

