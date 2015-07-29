package mobi.esys.upnews_online.facebook;


import com.google.gson.annotations.SerializedName;

public class FacebookVideoItem {
    @SerializedName("id")
    private String id;
    @SerializedName("description")
    private String description;
    @SerializedName("source")
    private String source;

    public FacebookVideoItem(String id, String description, String source) {
        this.id = id;
        this.description = description;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookVideoItem)) return false;

        FacebookVideoItem that = (FacebookVideoItem) o;

        if (!id.equals(that.id)) return false;
        if (!description.equals(that.description)) return false;
        return source.equals(that.source);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FacebookVideoItem{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", source='" + source + '\'' + '}';
    }
}
