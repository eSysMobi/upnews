package mobi.esys.data;

import com.google.api.services.drive.model.File;


public class GDFile implements Comparable {
    private String gdFileId;
    private String gdFileName;
    private String gdFileURL;
    private String gdFileSize;
    private String gdFileExt;
    private String gdFileMD5;
    private File gdFileInst;

    public GDFile(String gdFileId, String gdFileName, String gdFileURL,
                  String gdFileSize, String gdFileExt, String gdFileMD5,
                  File gdFileInst) {
        super();
        this.gdFileId = gdFileId;
        this.gdFileName = gdFileName;
        this.gdFileURL = gdFileURL;
        this.gdFileSize = gdFileSize;
        this.gdFileExt = gdFileExt;
        this.gdFileMD5 = gdFileMD5;
        this.gdFileInst = gdFileInst;
    }

    public GDFile() {
        super();
    }

    public GDFile(GDFile gdFile) {
        super();
        this.gdFileId = gdFile.gdFileId;
        this.gdFileName = gdFile.gdFileName;
        this.gdFileURL = gdFile.gdFileURL;
        this.gdFileSize = gdFile.gdFileSize;
        this.gdFileExt = gdFile.gdFileExt;
        this.gdFileMD5 = gdFile.gdFileMD5;
        this.gdFileInst = gdFile.gdFileInst;
    }

    public String getGdFileId() {
        return gdFileId;
    }

    public String getGdFileName() {
        return gdFileName;
    }

    public String getGdFileURL() {
        return gdFileURL;
    }

    public String getGdFileSize() {
        return gdFileSize;
    }

    public String getGdFileExt() {
        return gdFileExt;
    }

    public String getGdFileMD5() {
        return gdFileMD5;
    }

    public File getGdFileInst() {
        return gdFileInst;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.delete(0, builder.length());
        builder.append("GDFile [getGdFileId()=").append(getGdFileId()).append(", getGdFileName()="
        ).append(getGdFileName()).append(", getGdFileURL()=").append(getGdFileURL()
        ).append(", getGdFileSize()=").append(getGdFileSize()).append(", getGdFileExt()="
        ).append(getGdFileExt()).append(", getGdFileMD5()=").append(getGdFileMD5()).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((gdFileExt == null) ? 0 : gdFileExt.hashCode());
        result = prime * result
                + ((gdFileId == null) ? 0 : gdFileId.hashCode());
        result = prime * result
                + ((gdFileMD5 == null) ? 0 : gdFileMD5.hashCode());
        result = prime * result
                + ((gdFileName == null) ? 0 : gdFileName.hashCode());
        result = prime * result
                + ((gdFileSize == null) ? 0 : gdFileSize.hashCode());
        result = prime * result
                + ((gdFileURL == null) ? 0 : gdFileURL.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GDFile other = (GDFile) obj;
        if (gdFileExt == null) {
            if (other.gdFileExt != null)
                return false;
        } else if (!gdFileExt.equals(other.gdFileExt))
            return false;
        if (gdFileId == null) {
            if (other.gdFileId != null)
                return false;
        } else if (!gdFileId.equals(other.gdFileId))
            return false;
        if (gdFileMD5 == null) {
            if (other.gdFileMD5 != null)
                return false;
        } else if (!gdFileMD5.equals(other.gdFileMD5))
            return false;
        if (gdFileName == null) {
            if (other.gdFileName != null)
                return false;
        } else if (!gdFileName.equals(other.gdFileName))
            return false;
        if (gdFileSize == null) {
            if (other.gdFileSize != null)
                return false;
        } else if (!gdFileSize.equals(other.gdFileSize))
            return false;
        if (gdFileURL == null) {
            if (other.gdFileURL != null)
                return false;
        } else if (!gdFileURL.equals(other.gdFileURL))
            return false;
        return true;
    }


    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(Object another) {
        return this.getGdFileName().compareTo(((GDFile) another).getGdFileName());
    }
}
