package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.net.ITCPServerCallback;
import cz.warewolf.components.net.TCPServer;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    private final int mBaseRtpPort;
    private final Runnable mRunnable;
    private final String mServerAddress;
    private final int mRtspPort;
    private int mStartStopTimeout = 1000; //[ms]
    private TCPServer mTcpServer;
    private ITCPServerCallback mTcpCallback;
    private boolean mTcpRunning;
    private List<Integer> mRtpPorts;
    private Map<ITCPClientConnection, RTSPClient> mClients;
    private Map<String, MediaStream> mStreams;
    private Map<String, MediaPlayer> mMediaPlayers;
    private MediaPlayerEventListener mMediaPlayerEventListener = new MediaPlayerEventListener() {
        @Override
        public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
            Thread.currentThread().setName("MediaPlayerEventListener-" + Thread.currentThread().getId());
            log.debug("media changed: " + mrl);
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            log.debug("opening " + mediaPlayer.getMediaMeta().getTitle());
        }

        @Override
        public void buffering(MediaPlayer mediaPlayer, float newCache) {
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            log.debug("playing " + mediaPlayer.getMediaMeta().getTitle());
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {

        }

        @Override
        public void stopped(MediaPlayer mediaPlayer) {
            log.debug("stopped " + mediaPlayer.getMediaMeta().getTitle());
        }

        @Override
        public void forward(MediaPlayer mediaPlayer) {

        }

        @Override
        public void backward(MediaPlayer mediaPlayer) {

        }

        @Override
        public void finished(MediaPlayer mediaPlayer) {
            log.debug("finished " + mediaPlayer.getMediaMeta().getTitle());
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            log.trace("time changed: " + newTime);
        }

        @Override
        public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
            log.trace("position changed: " + newPosition);
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
            log.debug("snapshot taken: " + filename);
        }

        @Override
        public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {

        }

        @Override
        public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
            log.debug("video output: " + newCount);
        }

        @Override
        public void scrambledChanged(MediaPlayer mediaPlayer, int newScrambled) {

        }

        @Override
        public void elementaryStreamAdded(MediaPlayer mediaPlayer, int type, int id) {
            log.debug("elementary stream added " + mediaPlayer.getVideoDimension());
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
            log.debug("media duration changed: " + newDuration);
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
    };

    @SuppressWarnings("WeakerAccess")
    public RTSPServer(String serverAddress, int rtspPort, int rtpPort, IRTSPServerCallback callback) {
        boolean found = new NativeDiscovery().discover();
        log.debug("VLC library found using NativeDiscovery: " + found);
        log.debug("VLC version: " + LibVlc.INSTANCE.libvlc_get_version());

        mCallback = callback;
        mRtpPorts = new ArrayList<>();
        mRtspPort = rtspPort;
        mBaseRtpPort = rtpPort;
        mClients = new HashMap<>();
        mStreams = new HashMap<>();
        mMediaPlayers = new HashMap<>();
        mServerAddress = serverAddress;

        mTcpCallback = new ITCPServerCallback() {
            @Override
            public void onClientConnected(ITCPClientConnection client) {
                log.info("TCP Client " + client.getAddress() + " connected");
                String sessionId = generateSessionId();
                RTSPClient rtspClient = new RTSPClient(sessionId);
                mClients.put(client, rtspClient);
                if (mCallback != null) {
                    mCallback.onClientConnected(client);
                }
            }

            @Override
            public void onDataReceived(ITCPClientConnection client, byte[] data, int dataLength) {
                log.debug("onDataReceived(): RTSPServer received data: \n" + new String(data));
                RTSPRequest request = RTSPRequest.parse(new String(data));
                log.debug("onDataReceived(): RTSPServer parsed request: " + request);
                try {
                    URI url = new URI(request.getUrl());
                    String response = "";
                    String content = "";
                    String path = url.getPath();
                    if (path.contains("/trackID=")) {
                        path = path.substring(0, path.lastIndexOf("/trackID="));
                    }
                    log.debug("onDataReceived(): RTSPServer parsed url: " + url + ", path: " + url.getPath() + ", media path: " + path);
                    RTSPClient rtspClient = mClients.get(client);
                    String sessionId = rtspClient.getSessionKey();
                    MediaStream mediaStream = mStreams.get(path);
                    MediaPlayer mediaPlayer = mMediaPlayers.get(path);

                    if (request.getType() != null) {
                        switch (request.getType()) {

                            case OPTIONS:
                                if (mediaStream != null) {
                                    if (mediaStream.getOriginatingClient() == client) {
                                        mediaPlayer.play();
                                        response = "RTSP/1.0 200 OK\r\n" + "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                                "Public: DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, ANNOUNCE\r\n";
                                    } else {
                                        //Send redirection headers and let VLC handle the streaming
                                        mediaPlayer.play();
                                        Thread.sleep(2000);
                                        int timer = 200;
                                        while (!mediaPlayer.isPlaying() && timer > 0) {
                                            Thread.sleep(30);
                                            timer--;
                                        }
                                        String redirectTarget = "rtsp://" + mServerAddress + ":" + mediaStream.getRtspPort() + "/stream.sdp";
                                        mediaStream.setTargetPath(redirectTarget);
                                        response = "RTSP/1.0 302 Moved Temporarily\r\n" +
                                                "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                                "Location: " + redirectTarget + "\r\n";

                                        log.info("TCP Client " + client.getAddress() + " redirected to " + redirectTarget);

                                    }
                                } else {
                                    response = "RTSP/1.0 404 Not Found\r\n";
                                }
                                break;
                            case ANNOUNCE:
                                // Re-stream the incoming stream with VLC
                                log.info("TCP Client " + client.getAddress() + " is announcing stream " + url);
                                if (mediaStream == null) {
                                    response = "RTSP/1.0 200 OK\r\n" + "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                            "Session: " + sessionId + "\r\n";


                                    MediaStream newStream = new MediaStream(url.toString());
                                    newStream.setSdp(request.getContent());
                                    newStream.setOriginatingClient(client);
                                    newStream.setTargetPath("rtsp://" + mServerAddress + ":" + mRtspPort + path);
                                    if (registerStream(path, newStream)) {
                                        mediaPlayer = mMediaPlayers.get(path);
                                        mediaPlayer.play();
                                        log.info("New stream address: " + newStream.getTargetPath());
                                    }
                                } else {
                                    response = "RTSP/1.0 403 Forbidden\r\n";
                                    log.error("Stream " + url + " already exists");
                                }
                                break;
                            case SETUP:
                                if (mediaStream.getOriginatingClient() == client) {
                                    response = "RTSP/1.0 200 OK\r\n" + "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                            "Transport: " + request.getHeader("Transport") +
                                            //";server_port=" + mediaStream.getRtspPort() + "-" + (mediaStream.getRtspPort() + 1) +
                                            ";ssrc=1234ABCD\r\n" +
                                            "Session: " + sessionId + "\r\n";
                                } else {
                                    response = "RTSP/1.0 404 Not Found\r\n";
                                }
                                break;
                            case RECORD:
                                if (mediaStream.getOriginatingClient() == client) {
                                    response = "RTSP/1.0 200 OK\r\n" +
                                            "Cseq: " + request.getHeader("CSeq") + "\r\n" +
                                            "Session: " + request.getHeader("Session") + "\r\n";
                                } else {
                                    response = "RTSP/1.0 404 Not Found\r\n";
                                }
                                break;
                            case TEARDOWN:
                                if (mediaStream.getOriginatingClient() == client) {
                                    response = "RTSP/1.0 200 OK\r\n" + "Cseq: " + request.getHeader("CSeq") + "\r\n";
                                    mediaPlayer.stop();
                                    removeStream(path);
                                } else {
                                    response = "RTSP/1.0 403 Forbidden\r\n";
                                }
                                break;
                            default:
                                response = "RTSP/1.0 404 Not Found\r\n";
                                break;
                        }
                    } else {
                        log.error("onDataReceived(): request type is null");
                    }
                    response += wrapContent(content);
                    log.debug("onDataReceived(): RTSPServer response: \n" + response);
                    client.write(response.getBytes());
                } catch (IOException e) {
                    log.error("onDataReceived(): RTSPServer write error", e);
                    String errorResponse = "RTSP/1.0 500 Internal Server Error\r\n\r\n";
                    log.debug("onDataReceived(): RTSPServer error response: \n" + errorResponse);
                    try {
                        client.write(errorResponse.getBytes());
                    } catch (IOException e1) {
                        log.error("", e1);
                    }
                } catch (URISyntaxException e) {
                    log.error("onDataReceived(): Invalid url", e);
                    String errorResponse = "RTSP/1.0 404 Not Found\r\n\r\n";
                    log.debug("onDataReceived(): RTSPServer error response: \n" + errorResponse);
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
                if (mCallback != null) {
                    mCallback.onError(client, throwable);
                }
            }

            @Override
            public void onClientDisconnected(ITCPClientConnection client) {
                log.info("TCP Client " + client.getAddress() + " disconnected");
                mClients.remove(client);
                for (MediaStream stream : mStreams.values()) {
                    if (client == stream.getOriginatingClient()) {
                        try {
                            URI url = new URI(stream.getSourcePath());
                            String path = url.getPath();
                            removeStream(path);
                        } catch (URISyntaxException e) {
                            log.warn("onClientDisconnected()", e);
                        }
                    }
                }
                if (mCallback != null) {
                    mCallback.onClientDisconnected(client);
                }
            }

            @Override
            public void onBeforeStart() {
                Thread.currentThread().setName("ITCPServerCallback-" + Thread.currentThread().getId());
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
                mTcpServer = new TCPServer(mServerAddress, mRtspPort, mTcpCallback);
                mTcpServer.startServer();
            } catch (Exception e) {
                log.error("run():", e);
                if (mCallback != null) {
                    mCallback.onError(null, e);
                }
            }
        };
    }

    private MediaPlayer getMediaPlayer(int rtpPort, String rtspAddress, int rtspPort, String path) {
        path = path.replace("file://", "");
        String[] vlcArgs = {
                "--rtsp-host=" + rtspAddress
        };
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(vlcArgs);
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        mediaPlayer.addMediaPlayerEventListener(mMediaPlayerEventListener);
        String options = ":sout=#rtp{port=" + rtpPort + ",sdp=rtsp://" + rtspAddress + ":" + rtspPort + "/stream.sdp}";
        log.debug("VLC Options: " + options);
        mediaPlayer.prepareMedia(path, options);

        return mediaPlayer;
    }

    private MediaPlayer getIncomingStreamMediaPlayer(int rtpPort, String rtspAddress, int rtspPort, String sdp) throws IOException {
        String[] vlcArgs = {
                "--rtsp-host=" + rtspAddress
        };
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(vlcArgs);
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        mediaPlayer.addMediaPlayerEventListener(mMediaPlayerEventListener);
        File tempFile = File.createTempFile("incoming-stream-", ".sdp");
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile.getAbsoluteFile(), false));
        writer.append(sdp);
        writer.flush();
        writer.close();
        tempFile.deleteOnExit();
        String options = ":sout=#rtp{port=" + rtpPort + ",sdp=rtsp://" + rtspAddress + ":" + rtspPort + "/stream.sdp}";
        log.debug("VLC Options: " + options);
        log.debug("SDP file: " + tempFile.getAbsolutePath());
        mediaPlayer.prepareMedia(tempFile.getAbsolutePath(), options);

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
            log.info("startServer(): RTSP Server is starting");
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
        log.info("stopServer(): RTSP Server is stopping");
        if (mCallback != null) {
            mCallback.onBeforeStop();
        }
        boolean result = true;
        try {
            log.trace("stopServer(): runnable: " + mRunnable.toString()); // used in tests to trigger exception
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
    public boolean registerStream(String path, MediaStream mediaStream) {
        boolean result = false;
        if (path != null && mediaStream != null) {
            log.info("registerStream(): Registering stream " + mediaStream.getSourcePath() + " to " + path);
            MediaPlayer mediaPlayer = null;
            int rtpPort = generateRtpPort();
            int rtspPort = rtpPort + 1;
            if (mediaStream.getSdp() != null) {
                try {
                    mediaPlayer = getIncomingStreamMediaPlayer(rtpPort, mServerAddress, rtspPort, mediaStream.getSdp());
                } catch (IOException e) {
                    log.error("registerStream(): can't register stream", e);
                    mRtpPorts.remove(rtpPort);
                }
            } else {
                mediaPlayer = getMediaPlayer(rtpPort, mServerAddress, rtspPort, mediaStream.getSourcePath());
            }
            mediaStream.setRtspPort(rtspPort);
            mediaStream.setTargetPath("rtsp://" + mServerAddress + ":" + mRtspPort + path);
            if (mediaPlayer != null) {
                mMediaPlayers.put(path, mediaPlayer);
                mStreams.put(path, mediaStream);
                if (mCallback != null) {
                    mCallback.onStreamAdded(mediaStream);
                }
                result = true;
            }
        }
        return result;
    }

    @Override
    public void removeStream(String path) {
        if (path != null) {
            MediaStream mediaStream = mStreams.remove(path);
            log.info("removeStream(): removing stream " + path + ", success: " + (mediaStream != null));
            if (mCallback != null) {
                mCallback.onStreamRemoved(mediaStream);
            }
            MediaPlayer mediaPlayer = mMediaPlayers.get(path);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mMediaPlayers.remove(path);
            }
        }
    }

    @Override
    public List<String> getStreams() {
        List<String> result = new ArrayList<>();
        for (MediaStream mediaStream : mStreams.values()) {
            result.add(mediaStream.getTargetPath());
        }
        return result;
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
        if (mRtpPorts.isEmpty()) {
            result = mBaseRtpPort;
            mRtpPorts.add(result);
        } else {
            for (int port : mRtpPorts) {
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
