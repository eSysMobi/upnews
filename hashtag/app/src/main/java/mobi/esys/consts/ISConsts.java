package mobi.esys.consts;

/**
 * Created by Артем on 13.04.2015.
 */
public final class ISConsts {


    public static final class globals {
        public static final String temp_file_ext = "tmp";
        public static final String dir_name = "/upnewshashtag/";
        public static final String gd_dir_name = "upnewshashtag";
        public static final String pref_prefix = "UNHPref";
        public static final String gd_rss_file_name_type = "text/plain";
        public static final String gd_folder_query = "'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed=false";
        public static final String photo_dir_name = "photo/";
        public static final String music_dir_name = "music/";
        public static final String default_color = "<font color='#11A2F0'>";
        public static final String default_divider = default_color.concat("@</font>");
        public static final String default_hashtag = "#news";
        public static final String default_logtag_devider = ":";
        public static final int default_image_size = 640;
    }


    public static final class twitterconsts {
        public static final String twitter_key = "SZ0iHmFvfVODuQSQBPrIUWNKK";
        public static final String twitter_secret = "1iaQEDahvyTKEPaolhYxhL8qHC3RGlQulEELjoUESgdGnHW7DW";
    }

    public static final class instagramconsts {
        public static final String instagram_client_id = "3a932a7bab8a4ad186e34dfe3902e2ce";
        public static final String instagram_client_secret = "cabb30b0b93e491f953f4a0e3864ebf5";
        public static final String instagram_redirect_uri = "http://esys.mobi/app/upnewshashtag/android/auth";
        public static final String instagram_image_type = "standard_resolution";
        public static final int instagram_page_count = 100;
    }


    public static final class times {
        public static final int app_start_delay = 10000;
        public static final int anim_duration = 7000;
        public static final int twitter_refresh_feed_interval = 1000 * 60 * 15;
        //public static final int instagram_refresh_interval = 1000 * 60 * 6;
        public static final int twitter_get_feed_delay = 10000;
    }

    public static final class acceptedexts {
        public static final String[] sound_accepted_files_exts = {"mp3"};
    }

    public static final class prefstags {
        public static final String twitter_allow = "twitter_allow";
        public static final String drive_allow = "drive_allow";
        public static final String twitter_hashtag = "twHashTag";
        public static final String instagram_hashtag = "igHashTag";
        public static final String drive_folder_id = "drive_folder_id";
        public static final String music_files_urls = "music_files_urls";
        public static final String music_files_urls_set = "music_files_urls_set";
        public static final String music_files_md5s = "music_files_md5s";
        public static final String drive_acc_name = "drive_acc_name";
        public static final String is_music_download = "is_music_download";
    }


}
