package cz.warewolf.components.rtsp.server.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: SDP</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 12.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class SDP {
    private static List<MediaStreamInfo> streams;

    public static String getSdp() {
        streams = new ArrayList<>();
        String result = "";
        for (MediaStreamInfo stream : streams) {
            result += stream.getSdp();
        }

        result += "m=video 0 RTP/AVP 96\r\n" +
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
                "a=StreamName:string;\"hinted audio track\"\r\n";
        return result;

    }
}
