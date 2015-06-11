package mobi.esys.unl_new_api.eventbus;


public class NextVideoPlayEvent {
    public transient final String lastVideoName;

    public NextVideoPlayEvent(String lastVideoName) {
        this.lastVideoName = lastVideoName;

    }
}
