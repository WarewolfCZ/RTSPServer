package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ClientConnection;
import cz.warewolf.components.net.IServerCallback;
import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.net.ITCPServerCallback;
import cz.warewolf.components.net.IUDPClientConnection;
import cz.warewolf.components.net.IUDPServerCallback;
import cz.warewolf.components.net.TCPServer;
import cz.warewolf.components.net.UDPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: RTSPServer</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 10.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class RTSPServer implements IRTSPServer {

    private static final Logger log = LoggerFactory.getLogger(RTSPServer.class);
    private final IServerCallback mCallback;
    private Map<String, ClientConnection> mClients;
    private final Runnable mRunnable;
    private int mStartStopTimeout = 1000; //[ms]
    private long mClientSessionTimeout = 60_000L; //[ms]
    private TCPServer tcpServer;
    private UDPServer udpServer;
    private ITCPServerCallback tcpCallback;
    private IUDPServerCallback udpCallback;
    private boolean tcpRunning;
    private boolean udpRunning;

    @SuppressWarnings("WeakerAccess")
    public RTSPServer(String serverAddress, int tcpPort, int udpPort, IServerCallback callback) {
        this.mCallback = callback;
        mClients = new HashMap<>();
        tcpCallback = new ITCPServerCallback() {
            @Override
            public void onClientConnected(ITCPClientConnection client) {

            }

            @Override
            public void onDataReceived(ITCPClientConnection client, byte[] data, int dataLength) {

            }

            @Override
            public void onError(ITCPClientConnection client, Throwable throwable) {

            }

            @Override
            public void onClientDisconnected(ITCPClientConnection client) {

            }

            @Override
            public void onBeforeStart() {
                tcpRunning = true;
            }

            @Override
            public void onBeforeStop() {
                tcpRunning = false;
            }
        };

        udpCallback = new IUDPServerCallback() {
            @Override
            public void onClientConnected(IUDPClientConnection iudpClientConnection) {

            }

            @Override
            public void onDataReceived(IUDPClientConnection iudpClientConnection, byte[] bytes, int i) {

            }

            @Override
            public void onError(IUDPClientConnection iudpClientConnection, Throwable throwable) {

            }

            @Override
            public void onClientDisconnected(IUDPClientConnection iudpClientConnection) {

            }

            @Override
            public void onBeforeStart() {
                udpRunning = true;
            }

            @Override
            public void onBeforeStop() {
                udpRunning = false;
            }
        };

        this.mRunnable = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName(this.getClass().getSimpleName() + "-" + Thread.currentThread().getId());


                try {
                    tcpServer = new TCPServer(serverAddress, tcpPort, tcpCallback);
                    tcpServer.startServer();
                    udpServer = new UDPServer(serverAddress, udpPort, udpCallback);
                    udpServer.startServer();
                } catch (Exception e) {
                    log.error("run():", e);
                    if (mCallback != null) {
                        mCallback.onError(null, e);
                    }
                }
            }
        };
    }

    /**
     * Start server and wait for the server socket to connect (maximum waiting time is 1000ms)
     * <p>
     * Note: Blocking operation
     *
     * @return true on success
     */
    @Override
    public boolean startServer() {
        return startServer(true);
    }

    /**
     * Start server
     *
     * @param wait true if method should return after the server socket has connected, or after timeout of 1000ms passed
     * @return true on success
     */
    @Override
    public boolean startServer(boolean wait) {
        boolean result = true;
        try {
            log.info("run(): Server is starting");
            if (mCallback != null) {
                mCallback.onBeforeStart();
            }

            log.trace("startServer(): runnable: " + mRunnable.toString()); // used in tests to trigger exception
            new Thread(mRunnable).start();
            int i = mStartStopTimeout / 10;
            while (wait && i > 0 && (!tcpRunning || !udpRunning)) {
                i--;
                Thread.sleep(10);
            }
            if (i <= 0) {
                log.warn("startServer(): waiting for server to start takes too long (" + i * 10 + "ms), returning...");
                result = false;
            }
        } catch (Exception e) {
            log.error("startServer():", e);
            result = false;
        }
        return result;
    }

    /**
     * Stop server and wait for the server socket to close (maximum waiting time is 1000ms)
     * <p>
     * Note: Blocking operation
     *
     * @return true on success
     */
    @Override
    public boolean stopServer() {
        return stopServer(true);
    }

    /**
     * Stop server
     *
     * @param wait true if method should return after the server socket was closed, or after timeout of 1000ms passed
     * @return true on success
     */
    @Override
    public boolean stopServer(boolean wait) {
        if (tcpServer != null) {
            tcpServer.stopServer();
        }
        if (udpServer != null) {
            udpServer.stopServer();
        }
        log.info("run(): Server is stopping");
        if (mCallback != null) {
            mCallback.onBeforeStop();
        }
        boolean result = true;
        try {
            log.trace("startServer(): runnable: " + mRunnable.toString()); // used in tests to trigger exception
            int i = mStartStopTimeout / 10;
            while (wait && i > 0 && (tcpRunning || udpRunning)) {
                i--;
                Thread.sleep(10);
            }
            if (i <= 0) {
                log.warn("stopServer(): waiting for server to stop takes too long (" + i * 10 + "ms), returning...");
                result = false;
            }
        } catch (Exception e) {
            log.error("stopServer():", e);
            result = false;
        }
        return result;
    }

    @Override
    public boolean isRunning() {
        return tcpServer != null && tcpServer.isRunning() && udpServer != null && udpServer.isRunning();
    }

    @Override
    public List<ClientConnection> getClients() {
        return new ArrayList<>(mClients.values());
    }

    @Override
    public void setClientSessionTimeout(long timeout) {
        this.mClientSessionTimeout = timeout;
    }

    private void cleanupSessions() {
        if (mClients != null) {
            List<String> sessionKeysToDelete = new ArrayList<>();
            for (String key : mClients.keySet()) {
                ClientConnection client = mClients.get(key);
                if (client != null && System.currentTimeMillis() - client.getLastRead() > mClientSessionTimeout) {
                    sessionKeysToDelete.add(key);
                }
            }
            for (String key : sessionKeysToDelete) {
                ClientConnection client = mClients.remove(key);
                log.debug("cleanupSessions(): client " + client + " was quiet for too long, deleting session");
                if (client != null && mCallback != null) {
                    mCallback.onClientDisconnected(client);
                }
            }
        }
    }
}
