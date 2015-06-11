package mobi.esys.unl_new_api.model;

public class PlayingVideo {
    private String unVideoID;
    private String parentPlaylistID;
    private String videoName;
    private int playedTimes;
    private String filePath;

    public PlayingVideo(String unVideoID, String parentPlaylistID, String videoName, int playedTimes, String filePath) {
        this.unVideoID = unVideoID;
        this.parentPlaylistID = parentPlaylistID;
        this.videoName = videoName;
        this.playedTimes = playedTimes;
        this.filePath = filePath;
    }

    public String getUnVideoID() {
        return unVideoID;
    }

    public void setUnVideoID(String unVideoID) {
        this.unVideoID = unVideoID;
    }

    public String getParentPlaylistID() {
        return parentPlaylistID;
    }

    public void setParentPlaylistID(String parentPlaylistID) {
        this.parentPlaylistID = parentPlaylistID;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public int getPlayedTimes() {
        return playedTimes;
    }

    public void setPlayedTimes(int playedTimes) {
        this.playedTimes = playedTimes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayingVideo)) return false;

        PlayingVideo that = (PlayingVideo) o;

        if (!unVideoID.equals(that.unVideoID)) return false;
        if (!parentPlaylistID.equals(that.parentPlaylistID)) return false;
        if (!videoName.equals(that.videoName)) return false;
        if (playedTimes != that.playedTimes) return false;
        return filePath.equals(that.filePath);

    }

    @Override
    public int hashCode() {
        int result = unVideoID.hashCode();
        result = 31 * result + parentPlaylistID.hashCode();
        result = 31 * result + videoName.hashCode();
        result = 31 * result + playedTimes;
        result = 31 * result + filePath.hashCode();
        return result;
    }
}
