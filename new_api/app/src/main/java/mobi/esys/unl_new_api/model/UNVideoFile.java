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
        return "UNVideoFIle{" + "unVideoFileID='"
                + unVideoFileID + '\''
                + ", unVideoFileName='" + unVideoFileName + '\''
                + ", getUnVideoFileOriginName='" + unVideoFileOriginName + '\''
                + ", unVideoFileSize='" + unVideoFileSize + '\''
                + ", unVideoFileDate='" + unVideoFileDate + '\''
                + ", unVideoFilePath='" + unVideoFilePath + '\''
                + ", unVideoFileMD5='" + unVideoFileMD5 + '\''
                + ", unVideoFileMimeType='" + unVideoFileMimeType + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UNVideoFile)) return false;

        UNVideoFile that = (UNVideoFile) o;

        return unVideoFileID.equals(that.unVideoFileID)
                && unVideoFileName.equals(that.unVideoFileName)
                && unVideoFileOriginName.equals(that.unVideoFileOriginName)
                && unVideoFileSize.equals(that.unVideoFileSize)
                && unVideoFileDate.equals(that.unVideoFileDate)
                && unVideoFilePath.equals(that.unVideoFilePath)
                && unVideoFileMD5.equals(that.unVideoFileMD5)
                && unVideoFileMimeType.equals(that.unVideoFileMimeType);

    }

}
