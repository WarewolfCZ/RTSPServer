package cz.warewolf.components.rtsp.server.protocol;

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
    private String mPath;
    private int rtspPort;
    private String mSdp;

    public MediaStream(String path) throws URISyntaxException {
        URI uri = new URI(path.replace("\\", "/"));
        mPath = path;
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

    public String getPath() {
        return mPath;
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
                "mPath='" + mPath + '\'' +
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
}
