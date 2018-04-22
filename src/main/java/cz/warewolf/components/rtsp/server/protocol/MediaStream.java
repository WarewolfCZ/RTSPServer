package cz.warewolf.components.rtsp.server.protocol;

import cz.warewolf.components.net.ITCPClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>Title: MediaStream</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 12.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class MediaStream {
    private static final Logger log = LoggerFactory.getLogger(MediaStream.class);
    private String mSourcePath;
    private String mTargetPath;
    private int rtspPort;
    private String mSdp;
    private ITCPClientConnection originatingClient;

    public MediaStream(String path) throws URISyntaxException {
        URI uri = new URI(path.replace("\\", "/"));
        mSourcePath = path;
        log.debug("Stream location: " + uri);
        switch (uri.getScheme()) {
            case "file":
                break;
            case "rtsp":
                break;
            case "rtp":
                break;
            default:
                log.warn("Unknown scheme: " + uri.getScheme());
                break;
        }
    }

    public String getSourcePath() {
        return mSourcePath;
    }

    public String getTargetPath() {
        return mTargetPath;
    }

    public void setTargetPath(String path) {
        this.mTargetPath = path;
    }

    public void setRtspPort(int rtspPort) {
        this.rtspPort = rtspPort;
    }

    public int getRtspPort() {
        return rtspPort;
    }

    @Override
    public String toString() {
        return "MediaStream{" +
                "mSourcePath='" + mSourcePath + '\'' +
                ", rtspPort=" + rtspPort +
                ", mSdp='" + mSdp + "\'" +
                '}';
    }

    public String getSdp() {
        return mSdp;
    }

    public void setSdp(String sdp) {
        this.mSdp = sdp;
    }

    public void setOriginatingClient(ITCPClientConnection originatingClient) {
        this.originatingClient = originatingClient;
    }

    public ITCPClientConnection getOriginatingClient() {
        return originatingClient;
    }
}
