package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.rtsp.server.protocol.RTSPRequest;

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

    private RTSPRequest  mRequest;

    RTSPClient(String session) {
        mSessionKey = session;
    }

    public String getSessionKey() {
        return mSessionKey;
    }

    public RTSPRequest getRequest() {
        return mRequest;
    }

    public void setRequest(RTSPRequest request) {
        this.mRequest = request;
    }
}
