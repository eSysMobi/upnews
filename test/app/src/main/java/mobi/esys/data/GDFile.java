package mobi.esys.data;

import com.google.api.services.drive.model.File;


public class GDFile {
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


}
