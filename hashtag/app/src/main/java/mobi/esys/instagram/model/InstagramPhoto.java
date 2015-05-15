package mobi.esys.instagram.model;

public class InstagramPhoto {
    private String igPhotoID;
    private String igThumbURL;
    private String igOriginURL;

    public InstagramPhoto(final String igPhotoID, final String igThumbURL,
                          final String igOriginURL) {
        super();
        this.igPhotoID = igPhotoID;
        this.igThumbURL = igThumbURL;
        this.igOriginURL = igOriginURL;
    }

    /**
     * @return the igPhotoID
     */
    public String getIgPhotoID() {
        return igPhotoID;
    }

    /**
     * @param igPhotoID the igPhotoID to set
     */
    public void setIgPhotoID(final String igPhotoID) {
        this.igPhotoID = igPhotoID;
    }

    /**
     * @return the igThumbURL
     */
    public String getIgThumbURL() {
        return igThumbURL;
    }

    /**
     * @param igThumbURL the igThumbURL to set
     */
    public void setIgThumbURL(final String igThumbURL) {
        this.igThumbURL = igThumbURL;
    }

    /**
     * @return the igOriginURL
     */
    public String getIgOriginURL() {
        return igOriginURL;
    }

    /**
     * @param igOriginURL the igOriginURL to set
     */
    public void setIgOriginURL(final String igOriginURL) {
        this.igOriginURL = igOriginURL;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstagramPhoto{");
        sb.append("igPhotoID='").append(igPhotoID).append('\'');
        sb.append(", igThumbURL='").append(igThumbURL).append('\'');
        sb.append(", igOriginURL='").append(igOriginURL).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
