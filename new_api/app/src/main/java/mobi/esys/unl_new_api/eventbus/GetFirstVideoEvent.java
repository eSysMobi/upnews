package mobi.esys.unl_new_api.eventbus;


import mobi.esys.unl_new_api.model.UNPlaylist;
import mobi.esys.unl_new_api.model.UNVideo;

public class GetFirstVideoEvent {
    public transient UNVideo firstVideo;
    public transient UNPlaylist playlist;


    public GetFirstVideoEvent(UNVideo firstVideo, UNPlaylist unPlaylist) {
        this.firstVideo = firstVideo;
        this.playlist = unPlaylist;
    }
}
