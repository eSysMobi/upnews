package mobi.esys.unl_new_api.model;

/**
 * result: {
 * id: "56",
 * name: "apple-watch",
 * file_path: "uploads/2014/apple-apple-watch-reveal.mp4",
 * file_id: "56",
 * duration: null,
 * duration_info: null,
 * date_inserted: null,
 * insert_user_id: "1",
 * file: {
 * id: "56",
 * name: null,
 * original_name: null,
 * size: "7420720",
 * date_inserted: "2014-09-26 14:09:55",
 * path: "uploads/2014/apple-apple-watch-reveal.mp4",
 * md5: "6e096404a2475a77ec643030f026ff6d",
 * mime_type: "video/mp4"
 * },
 * file_webpath: "http://upnews.tv/dev-api/uploads/2014/apple-apple-watch-reveal.mp4"
 * }
 */

public class UNVideo {
    private String unVideoID;
    private String unVideoName;
    private String unVideoURL;
    private UNVideoFile unVideoFileInstance;
    private int unOrderNum;

    public UNVideo(String unVideoID, String unVideoName, String unVideoURL, UNVideoFile unVideoFileInstance, int unOrderNum) {
        this.unVideoID = unVideoID;
        this.unVideoName = unVideoName;
        this.unVideoURL = unVideoURL;
        this.unVideoFileInstance = unVideoFileInstance;
        this.unOrderNum = unOrderNum;
    }

    public String getUnVideoID() {
        return unVideoID;
    }

    public void setUnVideoID(String unVideoID) {
        this.unVideoID = unVideoID;
    }

    public String getUnVideoName() {
        return unVideoName;
    }

    public void setUnVideoName(String unVideoName) {
        this.unVideoName = unVideoName;
    }

    public String getUnVideoURL() {
        return unVideoURL;
    }

    public void setUnVideoURL(String unVideoURL) {
        this.unVideoURL = unVideoURL;
    }

    public UNVideoFile getUnVideoFileInstance() {
        return unVideoFileInstance;
    }

    public void setUnVideoFileInstance(UNVideoFile unVideoFileInstance) {
        this.unVideoFileInstance = unVideoFileInstance;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UNVideo)) return false;

        UNVideo unVideo = (UNVideo) o;

        return unVideoID.equals(unVideo.unVideoID)
                && unVideoName.equals(unVideo.unVideoName)
                && unVideoURL.equals(unVideo.unVideoURL)
                && unOrderNum == unVideo.unOrderNum
                && unVideoFileInstance.equals(unVideo.unVideoFileInstance);

    }

    @Override
    public int hashCode() {
        int result = unVideoID.hashCode();
        result = 31 * result + unVideoName.hashCode();
        result = 31 * result + unVideoURL.hashCode();
        result = 31 * result + unVideoFileInstance.hashCode();
        result = 31 * result + unOrderNum;
        return result;
    }

    @Override
    public String toString() {
        return "UNVideo{" + "unVideoID='" + unVideoID
                + '\'' + ", unVideoName='" + unVideoName
                + '\'' + ", unVideoURL='" + unVideoURL
                + '\'' + ", unVideoFileInstance="
                + unVideoFileInstance
                + ", unOrderNum=" + unOrderNum + '}';
    }

    public int getUnOrderNum() {
        return unOrderNum;
    }

    public void setUnOrderNum(int unOrderNum) {
        this.unOrderNum = unOrderNum;
    }


}
