package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ITCPClientConnection;

/**
 * <p>Title: IRTSPServerCallback</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2017</p>
 * Created on 11.12.2017
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public interface IRTSPServerCallback {

    void onClientConnected(ITCPClientConnection client);

    void onError(ITCPClientConnection clientConnection, Throwable throwable);

    void onClientDisconnected(ITCPClientConnection client);

    /**
     * Called before server has started
     */
    void onBeforeStart();

    /**
     * Called before server has stopped
     */
    void onBeforeStop();
}
