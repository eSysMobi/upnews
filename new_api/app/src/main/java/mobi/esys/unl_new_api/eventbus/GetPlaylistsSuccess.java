package mobi.esys.unl_new_api.eventbus;


import com.orhanobut.logger.Logger;

import mobi.esys.unl_new_api.model.UNPlaylist;

public class GetPlaylistsSuccess {
    public transient UNPlaylist pl;

    public GetPlaylistsSuccess(UNPlaylist pl) {
        Logger.d(pl.toString());
        this.pl = pl;
    }
}
