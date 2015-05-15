package mobi.esys.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.upnewshashtag.R;


public class TwitterLine implements ViewSwitcher.ViewFactory {
    private transient RelativeLayout mParentView;
    private transient TextSwitcher textSwitcher;
    private transient Context mContext;
    private transient boolean isTextSwitcherInitialized;
    private transient Handler twitterLineHandler;
    private transient Runnable twitterLineRunnable;
    private transient int tweetsCounter;
    private transient RelativeLayout twitterLayout;
    private transient List<String> mTwProfImagesUrls;
    private transient ImageView profileImage;


    public TwitterLine(RelativeLayout parentView, Context context, List<String> twProfImagesUrls) {
        mParentView = parentView;
        mContext = context;
        isTextSwitcherInitialized = false;
        mTwProfImagesUrls = twProfImagesUrls;
        tweetsCounter = 0;
    }

    public void start(final List<Spanned> textToSwitch) {
        if (isTextSwitcherInitialized) {
            removeTextSwitcher();
        }
        initTextSwitcher();

        twitterLineHandler = new Handler();
        twitterLineRunnable = new Runnable() {
            @Override
            public void run() {
                textSwitcher.setText(textToSwitch.get(tweetsCounter));
                twitterLineHandler.postDelayed(this, 10000);

                Glide.with(mContext).load(mTwProfImagesUrls.get(tweetsCounter)).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().error(R.drawable.twitter_128).into(profileImage);
                if (tweetsCounter == textToSwitch.size() - 1) {
                    tweetsCounter = 0;
                } else {
                    tweetsCounter++;
                }
            }
        };

        twitterLineHandler.postDelayed(twitterLineRunnable, ISConsts.times.twitter_get_feed_delay);

    }

    @Override
    public View makeView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextSize(19);
        textView.setPadding(2, 0, 2, 0);
        textView.setMaxLines(2);
        return textView;
    }

    private void initTextSwitcher() {

        twitterLayout = new RelativeLayout(mContext);
        textSwitcher = new TextSwitcher(mContext);
        textSwitcher.setFactory(this);

        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));

        twitterLayout.setBackgroundColor(mContext.getResources().getColor(R.color.rss_line));

        RelativeLayout.LayoutParams tsLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 100);
        tsLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        twitterLayout.setLayoutParams(tsLp);


        profileImage = new ImageView(mContext);
        profileImage.setId(R.id.profileImage);


        RelativeLayout.LayoutParams piLp = new RelativeLayout.LayoutParams(90, 90);
        piLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        piLp.addRule(RelativeLayout.CENTER_VERTICAL);
        piLp.setMargins(3, 5, 3, 5);
        profileImage.setLayoutParams(piLp);

        RelativeLayout.LayoutParams tsiLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tsiLp.addRule(RelativeLayout.RIGHT_OF, R.id.profileImage);
        tsiLp.setMargins(3, 5, 3, 5);
        textSwitcher.setLayoutParams(tsiLp);


        twitterLayout.addView(profileImage);
        twitterLayout.addView(textSwitcher);


        mParentView.addView(twitterLayout);
        isTextSwitcherInitialized = true;
    }


    public void removeTextSwitcher() {
        if (isTextSwitcherInitialized) {
            mParentView.removeView(twitterLayout);
            isTextSwitcherInitialized = false;
            tweetsCounter = 0;
        }
    }
}
