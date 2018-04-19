package cz.warewolf.components.rtsp.server.protocol;

/**
 * <p>Title: MediaStreamPart</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 19.04.2018
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class MediaStreamPart {
    private final int mId;
    private final String mName;
    private final int mRtcpPort;
    private final String mProto;
    private final String mMedia;
    private final String mEncodingParams;
    private String mMimeType;
    private int mBitRate;
    private int mPayloadType;
    private String mEncodingName;
    private int mClockRate;
    private double mLength;

    public MediaStreamPart(int id) {
        mId = id;
        mMimeType = "video/MP4V-ES";
        mBitRate = 304018;
        mName = "hinted video track";
        mMedia = "video";
        mProto = "RTP/AVP";
        mEncodingName = "MP4V-ES";
        mClockRate = 5544;
        mEncodingParams = null;
        mPayloadType = 96;
        mRtcpPort = 0;
        mLength = 596.000;
    }

    public String getDescription() {
        String result = "m=" + mMedia + " " + mRtcpPort + " " + mProto + " " + mPayloadType + "\r\n" +
                "a=control:streamid=" + mId + "\r\n" +
                "a=range:npt=0-" + mLength + "\r\n" +
                "a=length:npt=" + mLength + "\r\n" +
                "a=rtpmap:" + mPayloadType + " " + mEncodingName + "/" + mClockRate + (mEncodingParams != null ? "/" + mEncodingParams : "") + "\r\n" +
                "a=mimetype:string;\"" + mMimeType + "\"\r\n" +
                "a=AvgBitRate:integer;" + mBitRate + "\r\n" +
                "a=StreamName:string;\"" + mName + "\"\r\n";
        return result;
    }
}
