package mobi.esys.upnews_online.twitter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spanned;
import android.util.TypedValue;
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

import mobi.esys.upnews_online.R;


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
    private transient boolean isFirst;


    public TwitterLine(RelativeLayout parentView,
                       Context context,
                       List<String> twProfImagesUrls,
                       boolean isFirst) {
        mParentView = parentView;
        mContext = context;
        isTextSwitcherInitialized = false;
        mTwProfImagesUrls = twProfImagesUrls;
        tweetsCounter = 0;
        this.isFirst=isFirst;
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

        twitterLineHandler.postDelayed(twitterLineRunnable, 10000);

    }

    @Override
    public View makeView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.LEFT);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setMaxLines(4);
        textView.setMaxWidth(650);
        return textView;
    }

    private void initTextSwitcher() {
        if(isFirst) {
            twitterLayout = new RelativeLayout(mContext);
            twitterLayout.setId(R.id.twitterLayout);
        }
        else{
            twitterLayout=(RelativeLayout)mParentView.findViewById(R.id.twitterLayout);
            twitterLayout.removeAllViews();
        }
        textSwitcher = new TextSwitcher(mContext);
        textSwitcher.setFactory(this);

        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));

        twitterLayout.setBackgroundColor(mContext.getResources().getColor(R.color.rss_line));

        RelativeLayout.LayoutParams tsLp = new RelativeLayout.LayoutParams(1000, 100);
        tsLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tsLp.setMargins(0, 0, 0, 60);
        twitterLayout.setLayoutParams(tsLp);


        profileImage = new ImageView(mContext);
        profileImage.setId(R.id.profileImage);


        RelativeLayout.LayoutParams piLp = new RelativeLayout.LayoutParams(90, 90);
        piLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        piLp.addRule(RelativeLayout.CENTER_VERTICAL);
        piLp.setMargins(10, 5, 3, 5);
        profileImage.setLayoutParams(piLp);

        RelativeLayout.LayoutParams tsiLp = new RelativeLayout.LayoutParams(900, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tsiLp.addRule(RelativeLayout.RIGHT_OF, R.id.profileImage);
        tsiLp.setMargins(10, 1, 0, 1);
        textSwitcher.setLayoutParams(tsiLp);


        twitterLayout.addView(profileImage);
        twitterLayout.addView(textSwitcher);

        if(isFirst) {
            mParentView.addView(twitterLayout);
        }
        isTextSwitcherInitialized = true;
    }

    public void clear(){
        if(!isFirst) {
            twitterLayout.removeAllViews();
        }
    }


    public void removeTextSwitcher() {
        mParentView.removeView(twitterLayout);
        isTextSwitcherInitialized = false;
        tweetsCounter = 0;
    }
}
