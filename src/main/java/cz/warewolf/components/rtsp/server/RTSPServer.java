package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ClientConnection;
import cz.warewolf.components.net.IServerCallback;
import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.net.ITCPServerCallback;
import cz.warewolf.components.net.IUDPClientConnection;
import cz.warewolf.components.net.IUDPServerCallback;
import cz.warewolf.components.net.TCPServer;
import cz.warewolf.components.net.UDPServer;
import cz.warewolf.components.rtsp.server.protocol.RTSPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
                log.info("TCP Client " + client + " connected");
            }

            @Override
            public void onDataReceived(ITCPClientConnection client, byte[] data, int dataLength) {
                log.info("TCP Client sent data: " + new String(data));
                RTSPRequest request = RTSPRequest.parse(new String(data));
                log.info("TCP Client parsed request: " + request);
                try {
                    String response = "";
                    if (request.getType() != null) {
                        switch (request.getType()) {
                            case OPTIONS:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Public: DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE\r\n" +
                                        "\r\n";
                                break;
                            case DESCRIBE:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Content-Type: application/sdp\r\n" +
                                        "Content-Length: 460\r\n" +
                                        "\r\n\r\n" +
                                        "m=video 0 RTP/AVP 96\r\n" +
                                        "a=control:streamid=0\r\n" +
                                        "a=range:npt=0-7.741000\r\n" +
                                        "a=length:npt=7.741000\r\n" +
                                        "a=rtpmap:96 MP4V-ES/5544\r\n" +
                                        "a=mimetype:string;\"video/MP4V-ES\"\r\n" +
                                        "a=AvgBitRate:integer;304018\r\n" +
                                        "a=StreamName:string;\"hinted video track\"\r\n" +
                                        "m=audio 0 RTP/AVP 97\r\n" +
                                        "a=control:streamid=1\r\n" +
                                        "a=range:npt=0-7.712000\r\n" +
                                        "a=length:npt=7.712000\r\n" +
                                        "a=rtpmap:97 mpeg4-generic/32000/2\r\n" +
                                        "a=mimetype:string;\"audio/mpeg4-generic\"\r\n" +
                                        "a=AvgBitRate:integer;65790\r\n" +
                                        "a=StreamName:string;\"hinted audio track\"\r\n\r\n";
                                break;
                            case SETUP:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Transport: " + request.getHeader("Transport") + ";server_port=9000-9001;ssrc=1234ABCD\r\n" +
                                        "Session: 12345678\r\n\r\n";
                                break;
                            case PLAY:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n" +
                                        "RTP-Info: url=rtsp://example.com/media.mp4/streamid=0;seq=9810092;rtptime=3450012\r\n\r\n";
                                break;
                            case PAUSE:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n\r\n";
                                break;
                            case TEARDOWN:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n\r\n";
                                break;
                            case ANNOUNCE:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n\r\n";
                                break;
                            case RECORD:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n\r\n";
                                break;
                            case GET_PARAMETER:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n\r\n";
                                break;
                            case SET_PARAMETER:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n\r\n";
                                break;
                        }
                        client.write(response.getBytes());
                    }
                } catch (IOException e) {
                    log.error("TCP Client write error", e);
                }
            }

            @Override
            public void onError(ITCPClientConnection client, Throwable throwable) {
                log.error("TCP Client " + client + " error: ", throwable);
            }

            @Override
            public void onClientDisconnected(ITCPClientConnection client) {
                log.info("TCP Client " + client + " disconnected");
            }

            @Override
            public void onBeforeStart() {
                log.info("TCP Server starting");
                tcpRunning = true;
            }

            @Override
            public void onBeforeStop() {
                log.info("TCP Server stopping");
                tcpRunning = false;
            }
        };

        udpCallback = new IUDPServerCallback() {
            @Override
            public void onClientConnected(IUDPClientConnection udpClientConnection) {
                log.info("UDP Client " + udpClientConnection + " connected");
            }

            @Override
            public void onDataReceived(IUDPClientConnection udpClientConnection, byte[] data, int i) {
                log.info("UDP Client " + udpClientConnection + " sent data: " + new String(data));
            }

            @Override
            public void onError(IUDPClientConnection udpClientConnection, Throwable throwable) {
                log.error("UDP Client " + udpClientConnection + " error: ", throwable);
            }

            @Override
            public void onClientDisconnected(IUDPClientConnection udpClientConnection) {
                log.info("UDP Client " + udpClientConnection + " disconnected");
            }

            @Override
            public void onBeforeStart() {
                log.info("UDP Server starting");
                udpRunning = true;
            }

            @Override
            public void onBeforeStop() {
                log.info("UDP Server stopping");
                udpRunning = false;
            }
        };

        this.mRunnable = () -> {
            Thread.currentThread().setName(RTSPServer.this.getClass().getSimpleName() + "-" + Thread.currentThread().getId());


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
