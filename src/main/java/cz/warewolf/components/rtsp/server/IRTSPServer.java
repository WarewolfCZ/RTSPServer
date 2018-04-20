package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.rtsp.server.protocol.MediaStream;

import java.util.List;

/**
 * <p>Title: IRTSPServer</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2017</p>
 * Created on 15.12.2017
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public interface IRTSPServer {
    boolean startServer();

    boolean startServer(boolean wait);

    /**
     * Stop server and wait for the server socket to close (maximum waiting time is 1000ms)
     *
     * @return true on success
     */
    boolean stopServer();

    /**
     * Stop server
     *
     * @param wait true if method should wait until the server socket closes, or until timeout of 1000ms passes
     * @return true on success
     */
    boolean stopServer(boolean wait);

    boolean isRunning();

    List<RTSPClient> getClients();

    void registerStream(String url, MediaStream stream);

    void removeStream(String url);
}
