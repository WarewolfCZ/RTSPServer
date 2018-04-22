package cz.warewolf.components.rtsp.server;

/**
 * <p>Title: RTSPClient</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 16.04.2018
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class RTSPClient {

    private String mSessionKey;

    RTSPClient(String session) {
        mSessionKey = session;
    }

    public String getSessionKey() {
        return mSessionKey;
    }

}
