package mobi.esys.unl_new_api.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.gson.annotations.SerializedName;

public class UNPlaylist implements Comparable<UNPlaylist> {
    @SerializedName("id")
    String unPlaylistID;
    @SerializedName("name")
    String unPlaylistName;
    @SerializedName("date_inserted")
    String unPlaylistDateCreated;
    @SerializedName("count_videos")
    String unPlaylistSize;

    public UNPlaylist(String unPlaylistID, String unPlaylistName, String unPlaylistDateCreated, String unPlaylistSize) {
        this.unPlaylistID = unPlaylistID;
        this.unPlaylistName = unPlaylistName;
        this.unPlaylistDateCreated = unPlaylistDateCreated;
        this.unPlaylistSize = unPlaylistSize;
    }

    public String getUnPlaylistID() {
        return unPlaylistID;
    }

    public void setUnPlaylistID(String unPlaylistID) {
        this.unPlaylistID = unPlaylistID;
    }

    public String getUnPlaylistName() {
        return unPlaylistName;
    }

    public void setUnPlaylistName(String unPlaylistName) {
        this.unPlaylistName = unPlaylistName;
    }

    public String getUnPlaylistDateCreated() {
        return unPlaylistDateCreated;
    }

    public void setUnPlaylistDateCreated(String unPlaylistDateCreated) {
        this.unPlaylistDateCreated = unPlaylistDateCreated;
    }

    public String getUnPlaylistSize() {
        return unPlaylistSize;
    }

    public void setUnPlaylistSize(String unPlaylistSize) {
        this.unPlaylistSize = unPlaylistSize;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UNPlaylist)) return false;

        UNPlaylist that = (UNPlaylist) o;

        return unPlaylistID.equals(that.unPlaylistID)
                && unPlaylistName.equals(that.unPlaylistName)
                && unPlaylistDateCreated.equals(that.unPlaylistDateCreated)
                && unPlaylistSize.equals(that.unPlaylistSize);

    }

    @Override
    public int hashCode() {
        int result = unPlaylistID.hashCode();
        result = 31 * result + unPlaylistName.hashCode();
        result = 31 * result + unPlaylistDateCreated.hashCode();
        result = 31 * result + unPlaylistSize.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("playlistID", unPlaylistID)
                .add("playlistName", unPlaylistName)
                .add("playlistDate", unPlaylistDateCreated)
                .add("playlistSize", unPlaylistSize)
                .toString();
    }

    @Override
    public int compareTo(UNPlaylist other) {
        return ComparisonChain.start()
                .compare(unPlaylistID, other.unPlaylistID)
                .compare(unPlaylistName, other.unPlaylistName)
                .compare(unPlaylistDateCreated, other.unPlaylistDateCreated)
                .compare(unPlaylistSize, other.unPlaylistSize)
                .result();
    }
}
