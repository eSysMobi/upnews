package mobi.esys.unl_new_api.model;

import com.google.gson.annotations.SerializedName;

/**
 * id: "56",
 * name: null,
 * original_name: null,
 * size: "7420720",
 * date_inserted: "2014-09-26 14:09:55",
 * path: "uploads/2014/apple-apple-watch-reveal.mp4",
 * md5: "6e096404a2475a77ec643030f026ff6d",
 * mime_type: "video/mp4"
 */
public class UNVideoFile {
    @SerializedName("id")
    private String unVideoFileID;
    @SerializedName("name")
    private String unVideoFileName;
    @SerializedName("original_name")
    private String unVideoFileOriginName;
    @SerializedName("size")
    private String unVideoFileSize;
    @SerializedName("date_inserted")
    private String unVideoFileDate;
    @SerializedName("path")
    private String unVideoFilePath;
    @SerializedName("md5")
    private String unVideoFileMD5;
    @SerializedName("mime_type")
    private String unVideoFileMimeType;

    public UNVideoFile(String unVideoFileID, String unVideoFileName, String unVideoFileOriginName, String unVideoFileSize, String unVideoFileDate, String unVideoFilePath, String unVideoFileMD5, String unVideoFileMimeType) {
        this.unVideoFileID = unVideoFileID;
        this.unVideoFileName = unVideoFileName;
        this.unVideoFileOriginName = unVideoFileOriginName;
        this.unVideoFileSize = unVideoFileSize;
        this.unVideoFileDate = unVideoFileDate;
        this.unVideoFilePath = unVideoFilePath;
        this.unVideoFileMD5 = unVideoFileMD5;
        this.unVideoFileMimeType = unVideoFileMimeType;
    }

    public String getUnVideoFileID() {
        return unVideoFileID;
    }

    public void setUnVideoFileID(String unVideoFileID) {
        this.unVideoFileID = unVideoFileID;
    }

    public String getUnVideoFileName() {
        return unVideoFileName;
    }

    public void setUnVideoFileName(String unVideoFileName) {
        this.unVideoFileName = unVideoFileName;
    }

    public String getGetUnVideoFileOriginName() {
        return unVideoFileOriginName;
    }

    public void setGetUnVideoFileOriginName(String getUnVideoFileOriginName) {
        this.unVideoFileOriginName = getUnVideoFileOriginName;
    }

    public String getUnVideoFileSize() {
        return unVideoFileSize;
    }

    public void setUnVideoFileSize(String unVideoFileSize) {
        this.unVideoFileSize = unVideoFileSize;
    }

    public String getUnVideoFileDate() {
        return unVideoFileDate;
    }

    public void setUnVideoFileDate(String unVideoFileDate) {
        this.unVideoFileDate = unVideoFileDate;
    }

    public String getUnVideoFilePath() {
        return unVideoFilePath;
    }

    public void setUnVideoFilePath(String unVideoFilePath) {
        this.unVideoFilePath = unVideoFilePath;
    }

    public String getUnVideoFileMD5() {
        return unVideoFileMD5;
    }

    public void setUnVideoFileMD5(String unVideoFileMD5) {
        this.unVideoFileMD5 = unVideoFileMD5;
    }

    public String getUnVideoFileMimeType() {
        return unVideoFileMimeType;
    }

    public void setUnVideoFileMimeType(String unVideoFileMimeType) {
        this.unVideoFileMimeType = unVideoFileMimeType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UNVideoFIle{");
        sb.append("unVideoFileID='").append(unVideoFileID).append('\'');
        sb.append(", unVideoFileName='").append(unVideoFileName).append('\'');
        sb.append(", getUnVideoFileOriginName='").append(unVideoFileOriginName).append('\'');
        sb.append(", unVideoFileSize='").append(unVideoFileSize).append('\'');
        sb.append(", unVideoFileDate='").append(unVideoFileDate).append('\'');
        sb.append(", unVideoFilePath='").append(unVideoFilePath).append('\'');
        sb.append(", unVideoFileMD5='").append(unVideoFileMD5).append('\'');
        sb.append(", unVideoFileMimeType='").append(unVideoFileMimeType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UNVideoFile)) return false;

        UNVideoFile that = (UNVideoFile) o;

        if (!unVideoFileID.equals(that.unVideoFileID)) return false;
        if (!unVideoFileName.equals(that.unVideoFileName)) return false;
        if (!unVideoFileOriginName.equals(that.unVideoFileOriginName)) return false;
        if (!unVideoFileSize.equals(that.unVideoFileSize)) return false;
        if (!unVideoFileDate.equals(that.unVideoFileDate)) return false;
        if (!unVideoFilePath.equals(that.unVideoFilePath)) return false;
        if (!unVideoFileMD5.equals(that.unVideoFileMD5)) return false;
        return unVideoFileMimeType.equals(that.unVideoFileMimeType);

    }

    @Override
    public int hashCode() {
        int result = unVideoFileID.hashCode();
        result = 31 * result + unVideoFileName.hashCode();
        result = 31 * result + unVideoFileOriginName.hashCode();
        result = 31 * result + unVideoFileSize.hashCode();
        result = 31 * result + unVideoFileDate.hashCode();
        result = 31 * result + unVideoFilePath.hashCode();
        result = 31 * result + unVideoFileMD5.hashCode();
        result = 31 * result + unVideoFileMimeType.hashCode();
        return result;
    }
}
