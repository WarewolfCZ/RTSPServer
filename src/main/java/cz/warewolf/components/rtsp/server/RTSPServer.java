package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.net.ITCPServerCallback;
import cz.warewolf.components.net.TCPServer;
import cz.warewolf.components.rtsp.server.protocol.MediaStream;
import cz.warewolf.components.rtsp.server.protocol.RTSPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final IRTSPServerCallback mCallback;
    private final int mBaseRtpDataPort;
    private final Runnable mRunnable;
    private int mStartStopTimeout = 1000; //[ms]
    private long mClientSessionTimeout = 60_000L; //[ms]
    private TCPServer mTcpServer;
    private ITCPServerCallback mTcpCallback;
    private boolean mTcpRunning;
    private List<Integer> mRtpDataPorts;
    private Map<ITCPClientConnection, RTSPClient> mClients;
    private Map<String, MediaStream> mStreams;
    private Map<String, MediaPlayer> mMediaPlayers;

    @SuppressWarnings("WeakerAccess")
    public RTSPServer(String serverAddress, int tcpPort, int udpPort, IRTSPServerCallback callback) {
        this.mCallback = callback;
        mRtpDataPorts = new ArrayList<>();
        mBaseRtpDataPort = udpPort;
        mClients = new HashMap<>();
        mStreams = new HashMap<>();
        mMediaPlayers = new HashMap<>();

        mTcpCallback = new ITCPServerCallback() {
            @Override
            public void onClientConnected(ITCPClientConnection client) {
                log.info("TCP Client " + client + " connected");
                int rtpPort = generateRtpPort();
                int rtspPort = rtpPort + 1;
                String sessionId = generateSessionId();
                RTSPClient rtspClient = new RTSPClient(rtpPort, rtspPort, client, sessionId);
                mClients.put(client, rtspClient);
            }

            @Override
            public void onDataReceived(ITCPClientConnection client, byte[] data, int dataLength) {
                log.info("RTSPServer received data: \n" + new String(data));
                RTSPRequest request = RTSPRequest.parse(new String(data));
                log.info("RTSPServer parsed request: " + request);
                try {
                    URI url = new URI(request.getUrl());
                    String response = "";
                    String content = "";
                    String path = url.getPath();
                    if (path.contains("/streamid=")) {
                        path = path.substring(0, path.lastIndexOf("/streamid="));
                    }
                    log.info("RTSPServer parsed url: " + url + ", path: " + url.getPath() + ", media path: " + path);
                    RTSPClient rtspClient = mClients.get(client);
                    MediaStream mediaStream = mStreams.get(path);
                    MediaPlayer mediaPlayer = mMediaPlayers.get(path);
                    if (request.getType() != null && rtspClient != null && mediaStream != null) {
                        switch (request.getType()) {
                            case OPTIONS:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Public: DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE\r\n";

                                if (mediaPlayer == null) {
                                    //new Thread(() -> startMediaPlayer(null, 1559, serverAddress, serverPort, mediaStream.getPath())).start();
                                    mediaPlayer = startMediaPlayer(rtspClient.getRtpPort(), serverAddress, rtspClient.getRtspPort(), mediaStream.getPath());
                                    mMediaPlayers.put(path, mediaPlayer);
                                    mediaStream.setRtspPort(rtspClient.getRtspPort());
                                } else {
                                    mediaPlayer.play();
                                }

                                Thread.sleep(3000);
                                response = "RTSP/1.0 302 Moved Temporarily\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Location: rtsp://" + serverAddress + ":" + mediaStream.getRtspPort() + "/test.sdp\r\n";
                                break;
                            case DESCRIBE:
                                content = mediaStream.getDescription();
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Content-Type: application/sdp\r\n";
                                break;
                            case SETUP:
                                String sessionId = mClients.get(client).getSessionKey();
                                rtspClient.setTransport(request.getHeader("Transport"));
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Transport: " + request.getHeader("Transport") + ";server_port=" +
                                        rtspClient.getRtspPort() + "-" + rtspClient.getRtpPort() +
                                        ";ssrc=1234ABCD\r\n" +
                                        "Session: " + sessionId + "\r\n";
                                break;
                            case PLAY:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n" +
                                        "RTP-Info: url=" + url.getScheme() + "://" + url.getHost() + ":" + url.getPort() + "" + url.getPath() + "/streamid=0;seq=9810092;rtptime=3450012\r\n";

                                response = "RTSP/1.0 302 Moved Temporarily\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Location: rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov\r\n";


                                break;
                            case PAUSE:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n";
                                mediaPlayer.pause();
                                break;
                            case TEARDOWN:
                                response = "RTSP/1.0 200 OK\r\n" + "Cseq: " + request.getHeader("CSeq") + "\r\n";
                                rtspClient.setMediaStream(null);
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                break;
                            case ANNOUNCE:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n";
                                break;
                            case RECORD:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                        "Session: " + request.getHeader("Session") + "\r\n";
                                break;
                            case GET_PARAMETER:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n";
                                break;
                            case SET_PARAMETER:
                                response = "RTSP/1.0 200 OK\r\n" +
                                        "Cseq: " + request.getHeader("CSeq") + "\r\n";
                                break;
                        }
                        response += wrapContent(content);
                        log.info("RTSPServer response: \n" + response);
                        client.write(response.getBytes());
                    } else if (mediaStream == null) {
                        response = "RTSP/1.0 404 Not Found\r\n";
                        response += wrapContent(content);
                        log.info("RTSPServer response: \n" + response);
                        client.write(response.getBytes());
                    }
                } catch (IOException e) {
                    log.error("RTSPServer write error", e);
                    String errorResponse = "RTSP/1.0 500 Internal Server Error\r\n\r\n";
                    log.info("RTSPServer error response: \n" + errorResponse);
                    try {
                        client.write(errorResponse.getBytes());
                    } catch (IOException e1) {
                        log.error("", e1);
                    }
                } catch (URISyntaxException e) {
                    log.error("Invalid url", e);
                    String errorResponse = "RTSP/1.0 404 Not Found\r\n\r\n";
                    log.info("RTSPServer error response: \n" + errorResponse);
                    try {
                        client.write(errorResponse.getBytes());
                    } catch (IOException e1) {
                        log.error("", e1);
                    }
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }

            @Override
            public void onError(ITCPClientConnection client, Throwable throwable) {
                log.error("TCP Client " + client + " error: ", throwable);
            }

            @Override
            public void onClientDisconnected(ITCPClientConnection client) {
                log.info("TCP Client " + client + " disconnected");
                mClients.remove(client);
            }

            @Override
            public void onBeforeStart() {
                log.info("TCP Server starting");
                mTcpRunning = true;
            }

            @Override
            public void onBeforeStop() {
                log.info("TCP Server stopping");
                mTcpRunning = false;
            }
        };

        this.mRunnable = () -> {
            Thread.currentThread().setName(RTSPServer.this.getClass().getSimpleName() + "-" + Thread.currentThread().getId());


            try {
                boolean found = new NativeDiscovery().discover();
                log.info("VLC library found using NativeDiscovery: " + found);
                log.info("VLC version: " + LibVlc.INSTANCE.libvlc_get_version());

                mTcpServer = new TCPServer(serverAddress, tcpPort, mTcpCallback);
                mTcpServer.startServer();
            } catch (Exception e) {
                log.error("run():", e);
                if (mCallback != null) {
                    mCallback.onError(null, e);
                }
            }
        };
    }

    private MediaPlayer startMediaPlayer(int rtpPort, String rtspAddress, int rtspPort, String path) {
        path = path.replace("file://", "");
        String[] vlcArgs = {
                "--rtsp-host=" + rtspAddress
        };
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(vlcArgs);
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventListener() {
                                                    @Override
                                                    public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
                                                        log.info("media changed: " + mrl);
                                                    }

                                                    @Override
                                                    public void opening(MediaPlayer mediaPlayer) {
                                                        log.info("opening");
                                                    }

                                                    @Override
                                                    public void buffering(MediaPlayer mediaPlayer, float newCache) {
                                                        //log.info("buffering: " + newCache);
                                                    }

                                                    @Override
                                                    public void playing(MediaPlayer mediaPlayer) {
                                                        log.info("playing " + mediaPlayer.getMediaMeta());
                                                    }

                                                    @Override
                                                    public void paused(MediaPlayer mediaPlayer) {

                                                    }

                                                    @Override
                                                    public void stopped(MediaPlayer mediaPlayer) {
                                                        log.info("stopped");
                                                    }

                                                    @Override
                                                    public void forward(MediaPlayer mediaPlayer) {

                                                    }

                                                    @Override
                                                    public void backward(MediaPlayer mediaPlayer) {

                                                    }

                                                    @Override
                                                    public void finished(MediaPlayer mediaPlayer) {
                                                        log.info("finished");
                                                    }

                                                    @Override
                                                    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                                                        log.debug("time changed: " + newTime);
                                                    }

                                                    @Override
                                                    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                                                        log.debug("position changed: " + newPosition);
                                                    }

                                                    @Override
                                                    public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {

                                                    }

                                                    @Override
                                                    public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {

                                                    }

                                                    @Override
                                                    public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {

                                                    }

                                                    @Override
                                                    public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
                                                        log.info("snapshot taken: " + filename);
                                                    }

                                                    @Override
                                                    public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {

                                                    }

                                                    @Override
                                                    public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
                                                        log.info("video output: " + newCount);
                                                    }

                                                    @Override
                                                    public void scrambledChanged(MediaPlayer mediaPlayer, int newScrambled) {

                                                    }

                                                    @Override
                                                    public void elementaryStreamAdded(MediaPlayer mediaPlayer, int type, int id) {
                                                        log.info("elementary stream added " + mediaPlayer.getVideoDimension());
                                                    }

                                                    @Override
                                                    public void elementaryStreamDeleted(MediaPlayer mediaPlayer, int type, int id) {

                                                    }

                                                    @Override
                                                    public void elementaryStreamSelected(MediaPlayer mediaPlayer, int type, int id) {

                                                    }

                                                    @Override
                                                    public void corked(MediaPlayer mediaPlayer, boolean corked) {

                                                    }

                                                    @Override
                                                    public void muted(MediaPlayer mediaPlayer, boolean muted) {

                                                    }

                                                    @Override
                                                    public void volumeChanged(MediaPlayer mediaPlayer, float volume) {

                                                    }

                                                    @Override
                                                    public void audioDeviceChanged(MediaPlayer mediaPlayer, String audioDevice) {

                                                    }

                                                    @Override
                                                    public void chapterChanged(MediaPlayer mediaPlayer, int newChapter) {

                                                    }

                                                    @Override
                                                    public void error(MediaPlayer mediaPlayer) {
                                                        log.error("Error");
                                                    }

                                                    @Override
                                                    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
                                                        log.debug("media meta changed: " + mediaPlayer.getMediaMeta());
                                                    }

                                                    @Override
                                                    public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {

                                                    }

                                                    @Override
                                                    public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
                                                        log.info("media duration changed: " + newDuration);
                                                    }

                                                    @Override
                                                    public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {

                                                    }

                                                    @Override
                                                    public void mediaFreed(MediaPlayer mediaPlayer) {

                                                    }

                                                    @Override
                                                    public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {

                                                    }

                                                    @Override
                                                    public void mediaSubItemTreeAdded(MediaPlayer mediaPlayer, libvlc_media_t item) {

                                                    }

                                                    @Override
                                                    public void newMedia(MediaPlayer mediaPlayer) {

                                                    }

                                                    @Override
                                                    public void subItemPlayed(MediaPlayer mediaPlayer, int subItemIndex) {

                                                    }

                                                    @Override
                                                    public void subItemFinished(MediaPlayer mediaPlayer, int subItemIndex) {

                                                    }

                                                    @Override
                                                    public void endOfSubItems(MediaPlayer mediaPlayer) {

                                                    }
                                                }
        );
        String options = ":sout=#rtp{" +
                "port=" + rtpPort + "," +
                "sdp=rtsp://" + rtspAddress + ":" + rtspPort + "/test.sdp}";

        log.info("VLC Options: " + options);
        mediaPlayer.prepareMedia(path, options);
        mediaPlayer.play();

        return mediaPlayer;
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
            while (wait && i > 0 && !mTcpRunning) {
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
        if (mTcpServer != null) {
            mTcpServer.stopServer();
        }
        log.info("run(): Server is stopping");
        if (mCallback != null) {
            mCallback.onBeforeStop();
        }
        boolean result = true;
        try {
            log.trace("startServer(): runnable: " + mRunnable.toString()); // used in tests to trigger exception
            int i = mStartStopTimeout / 10;
            while (wait && i > 0 && mTcpRunning) {
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
        return mTcpServer != null && mTcpServer.isRunning();
    }

    @Override
    public List<RTSPClient> getClients() {
        return new ArrayList<>(mClients.values());
    }

    @Override
    public void setClientSessionTimeout(long timeout) {
        this.mClientSessionTimeout = timeout;
    }

    @Override
    public void addStream(String url, MediaStream stream) {
        if (url != null && stream != null) {
            mStreams.put(url, stream);
        }
    }

    @Override
    public void removeStream(String url) {
        if (url != null) {
            mStreams.remove(url);
        }
    }

    private void cleanupSessions() {
        if (mClients != null) {
            List<ITCPClientConnection> clientConnections = new ArrayList<>();
            for (ITCPClientConnection client : mClients.keySet()) {
                if (client != null && System.currentTimeMillis() - client.getLastRead() > mClientSessionTimeout) {
                    clientConnections.add(client);
                }
            }
            for (ITCPClientConnection client : clientConnections) {
                log.debug("cleanupSessions(): client " + client + " was quiet for too long, deleting session");
                if (client != null && mCallback != null) {
                    mClients.remove(client);
                    mCallback.onClientDisconnected(client);
                }
            }
        }
    }

    private String generateSessionId() {
        String result;
        do {
            result = String.valueOf((int) (Math.random() * 10_000_000));
        } while (sessionExists(result));
        return result;
    }

    private boolean sessionExists(String session) {
        boolean result = false;
        for (ITCPClientConnection connection : mClients.keySet()) {
            RTSPClient client = mClients.get(connection);
            if (client != null && Objects.equals(session, client.getSessionKey())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private int generateRtpPort() {
        int result = -1;
        if (mRtpDataPorts.isEmpty()) {
            result = mBaseRtpDataPort;
            mRtpDataPorts.add(result);
        } else {
            for (int port : mRtpDataPorts) {
                if (port > result) {
                    result = port + 2;
                }
            }
        }
        log.debug("generateRtpPort(): result: " + result);
        return result;
    }

    private String wrapContent(String content) {
        String result = "";
        if (content != null && !content.isEmpty()) {
            int contentLength = content.length();
            result += "Content-Length: " + contentLength + "\r\n";
            result += "\r\n\r\n";
            result += content;
        } else {
            result += "\r\n";
        }

        return result;
    }
}
