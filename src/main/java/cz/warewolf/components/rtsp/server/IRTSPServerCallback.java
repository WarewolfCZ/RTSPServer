package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.IClientConnection;

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

    void onClientConnected(IClientConnection client);

    void onDataReceived(IClientConnection clientConnection, byte[] data, int dataLength);

    void onError(IClientConnection clientConnection, Throwable throwable);

    void onClientDisconnected(IClientConnection client);

    /**
     * Called before server has started
     */
    void onBeforeStart();

    /**
     * Called before server has stopped
     */
    void onBeforeStop();
}
