package cz.warewolf.components.rtsp.server.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: RTSPRequest</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 11.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class RTSPRequest {
    private static final Logger log = LoggerFactory.getLogger(RTSPRequest.class);

    private Map<String, String> headers;
    private RTSPRequestType type;
    private String url;
    private String protocolVersion;
    private String content;

    public RTSPRequest() {
        headers = new HashMap<>();
    }

    public static RTSPRequest parse(String input) {
        RTSPRequest request = null;
        if (input != null) {
            try {
                request = new RTSPRequest();
                input = input.replaceAll("\r\n", "\n");
                String[] lines = input.split("\n");
                String[] lineParts = lines[0].split(" ");
                // check if this is known request, for example OPTIONS, DESCRIBE, etc.
                for (RTSPRequestType type : RTSPRequestType.values()) {
                    if (type.name().equals(lineParts[0])) {
                        request.setType(type);
                        request.setUrl(lineParts[1].trim());
                        request.setProtocolVersion(lineParts[2].trim());
                        break;
                    }
                }

                int i = 1;
                for (; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.trim().length() == 0) {
                        // end of headers
                        break;
                    }
                    if (line.contains(":")) {
                        String[] headerParts = line.split(":", 2);
                        if (headerParts[0].trim().length() > 0) {
                            request.addHeader(headerParts[0].trim(), headerParts[1].trim());
                        }
                    }
                }

                StringBuilder content = new StringBuilder();
                i++;
                for (; i < lines.length; i++) {
                    content.append(lines[i]).append("\n");
                }
                request.setContent(content.toString());

            } catch (Exception e) {
                log.error("parse()", e);
            }
        }
        return request;
    }

    private void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void addHeader(String key, String value) {
        if (key != null) {
            headers.put(key.toLowerCase(), value);
        }
    }

    public String getHeader(String key) {
        if (key != null) {
            return headers.get(key.toLowerCase());
        } else {
            return null;
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setType(RTSPRequestType type) {
        this.type = type;
    }

    public RTSPRequestType getType() {
        return type;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {
        return "RTSPRequest{" +
                "headers=" + headers +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
