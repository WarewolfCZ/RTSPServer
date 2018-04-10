package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.IClientConnection;
import cz.warewolf.components.net.IServerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

/**
 * <p>Title: RTSPServerStandalone</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 10.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class RTSPServerStandalone {

    private static final Logger log = LoggerFactory.getLogger(RTSPServerStandalone.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("run(): RTSPServer is starting");
        RTSPServer rtspServer = new RTSPServer("localhost", 1223, 1223, new IServerCallback() {
            @Override
            public void onClientConnected(IClientConnection iClientConnection) {

            }

            @Override
            public void onDataReceived(IClientConnection iClientConnection, byte[] bytes, int i) {

            }

            @Override
            public void onError(IClientConnection iClientConnection, Throwable throwable) {

            }

            @Override
            public void onClientDisconnected(IClientConnection iClientConnection) {

            }

            @Override
            public void onBeforeStart() {

            }

            @Override
            public void onBeforeStop() {

            }
        });
        rtspServer.startServer();
        sleep(5000);
        log.info("run(): RTSPServer is stopping");
        rtspServer.stopServer();
    }

}
