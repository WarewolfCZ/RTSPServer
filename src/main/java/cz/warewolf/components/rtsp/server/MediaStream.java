package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.net.ITCPClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Pattern patternVideo;
    private final Pattern patterAudio;
    private final List<Integer> mUsedPorts;
    private String mSourcePath;
    private String mTargetPath;
    private int rtspPort;
    private String mSdp;
    private ITCPClientConnection originatingClient;
    private Integer mRtpPort;
    private Integer mBaseVideoPort;
    private Integer mBaseAudioPort;

    public MediaStream(String path, List<Integer> usedRtpPorts) throws URISyntaxException {
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
        patternVideo = Pattern.compile("m=video (\\d+) ");
        patterAudio = Pattern.compile("m=audio (\\d+) ");
        mUsedPorts = usedRtpPorts;
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

    public String getSdp() {
        return mSdp;
    }

    public void setSdp(String sdp) {
        this.mSdp = sdp;
        if (sdp != null) {
            try {
                Matcher m = patterAudio.matcher(sdp);
                if (m.find()) {
                    mBaseAudioPort = Integer.valueOf(m.group(1));
                    if (mBaseAudioPort == 0) {
                        mBaseAudioPort = 5000;
                        mSdp = mSdp.replace("m=audio 0", "m=audio " + mBaseAudioPort);
                    }
                    while (mUsedPorts.contains(mBaseAudioPort)) {
                        mSdp = mSdp.replace("m=audio " + mBaseAudioPort, "m=audio " + (mBaseAudioPort + 4));
                        mBaseAudioPort += 4;
                    }
                    mUsedPorts.add(mBaseAudioPort);
                    mUsedPorts.add(mBaseAudioPort + 1);
                }
                m = patternVideo.matcher(sdp);
                if (m.find()) {
                    mBaseVideoPort = Integer.valueOf(m.group(1));
                    if (mBaseVideoPort == 0) {
                        mBaseVideoPort = mBaseAudioPort + 2;
                        mSdp = mSdp.replace("m=video 0", "m=video " + mBaseVideoPort);
                    }

                    while (mUsedPorts.contains(mBaseVideoPort)) {
                        mSdp = mSdp.replace("m=video " + mBaseVideoPort, "m=video " + (mBaseVideoPort + 4));
                        mBaseVideoPort += 4;
                    }
                    mUsedPorts.add(mBaseVideoPort);
                    mUsedPorts.add(mBaseVideoPort + 1);
                }

            } catch (Exception e) {
                log.error("setSdp(): error parsing video/audio port", e);
            }
        }
    }

    public void setOriginatingClient(ITCPClientConnection originatingClient) {
        this.originatingClient = originatingClient;
    }

    public ITCPClientConnection getOriginatingClient() {
        return originatingClient;
    }

    public void setRtpPort(int rtpPort) {
        this.mRtpPort = rtpPort;
    }

    public Integer getRtpPort() {
        return mRtpPort;
    }

    public Integer getBaseVideoPort() {
        return mBaseVideoPort;
    }

    public Integer getBaseAudioPort() {
        return mBaseAudioPort;
    }

    @Override
    public String toString() {
        return "MediaStream{" +
                "mSourcePath='" + mSourcePath + '\'' +
                ", mTargetPath='" + mTargetPath + '\'' +
                ", rtspPort=" + rtspPort +
                ", mSdp='" + mSdp + '\'' +
                ", originatingClient=" + originatingClient +
                ", mRtpPort=" + mRtpPort +
                ", mBaseVideoPort=" + mBaseVideoPort +
                ", mBaseAudioPort=" + mBaseAudioPort +
                '}';
    }
}
