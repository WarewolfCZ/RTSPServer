package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.rtsp.server.protocol.MediaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(RTSPClient.class);

    private final ITCPClientConnection mConnection;
    private int mRtpPort;
    private int mRtspPort;
    private String mSessionKey;
    private String transport;
    private String transportProtocol;
    private boolean mMulticast;
    private MediaStream mediaStream;

    public RTSPClient(int rtpPort, int rtspPort, ITCPClientConnection connection, String session) {
        mRtpPort = rtpPort;
        mRtspPort = rtspPort;
        mConnection = connection;
        mSessionKey = session;
    }

    public int getRtspPort() {
        return mRtpPort;
    }

    public void setServerRtpPort(int rtpPort) {
        this.mRtpPort = rtpPort;
    }

    public int getRtpPort() {
        return mRtspPort;
    }

    public void setServerRtcpPort(int rtcpPort) {
        this.mRtspPort = rtcpPort;
    }

    public ITCPClientConnection getConnection() {
        return mConnection;
    }

    public String getSessionKey() {
        return mSessionKey;
    }

    public void setTransport(String transport) {
        this.transport = transport;
        if (transport != null) {
            String[] parts = transport.split(";");
            if (parts.length > 0) {
                transportProtocol = parts[0];
                for (int i = 1; i < parts.length; i++) {
                    String param = parts[i];
                    log.info("transport parameter: " + param);
                    if ("multicast".equalsIgnoreCase(param)) {
                        mMulticast = true;
                    } else if (param.startsWith("client_port=")) {
                        String ports = param.substring("client_port=".length(), param.length());
                        String[] portArr = ports.split("-");
                        if (portArr.length > 0) {
                            //mClientRtpPort = Integer.parseInt(portArr[0]);
                        }
                        if (portArr.length > 1) {
                            //mClientRtcpPort = Integer.parseInt(portArr[1]);
                        }
                    }
                }
            }
        }
    }


    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

}
