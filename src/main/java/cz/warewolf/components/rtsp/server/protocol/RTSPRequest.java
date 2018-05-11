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
    private boolean headersComplete;
    private boolean contentComplete;
    private boolean hasType;
    private int contentLength;
    private String incompleteLine;

    public RTSPRequest() {
        headers = new HashMap<>();
        incompleteLine = "";
        content = "";
    }

    public boolean parse(String input) {
        if (input != null) {
            try {
                input = incompleteLine + input;
                incompleteLine = "";
                String[] lines = input.split("(?<=\n)");

                outer:
                for (String line : lines) {
                    if (!line.endsWith("\n")) {
                        incompleteLine = line;
                        break;
                    }
                    if (!hasType) {
                        try {
                            String[] lineParts = line.split(" ");
                            // check if this is known request, for example OPTIONS, DESCRIBE, etc.
                            for (RTSPRequestType type : RTSPRequestType.values()) {
                                if (type.name().equals(lineParts[0])) {
                                    setType(type);
                                    setUrl(lineParts[1].trim());
                                    setProtocolVersion(lineParts[2].trim());
                                    hasType = true;
                                    continue outer;
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing request type", e);
                        }
                    } else if (!headersComplete) {
                        if (line.trim().length() == 0) {
                            // end of headers
                            headersComplete = true;
                            String contentLengthHeader = getHeader("Content-Length");
                            if (contentLengthHeader != null) {
                                try {
                                    contentLength = Integer.valueOf(contentLengthHeader);
                                } catch (Exception e) {
                                    log.debug("Error parsing content length", e);
                                }
                            }
                        } else if (line.contains(":")) {
                            String[] headerParts = line.split(":", 2);
                            if (headerParts[0].trim().length() > 0) {
                                addHeader(headerParts[0].trim(), headerParts[1].trim());
                            }
                        }
                    } else if (!contentComplete) {
                        content += line;
                    } else {
                        break; // request is complete
                    }
                }
                if (headersComplete &&
                        ((content != null && contentLength <= content.length()) || (content == null && contentLength == 0))) {
                    contentComplete = true;
                }
            } catch (Exception e) {
                log.error("parse()", e);
            }
        }
        return hasType && headersComplete && contentComplete;
    }

    public String getContent() {
        return content;
    }

    private void addHeader(String key, String value) {
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

    private void setType(RTSPRequestType type) {
        this.type = type;
    }

    public RTSPRequestType getType() {
        return type;
    }


    private void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    private void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public boolean isRequestComplete() {
        return hasType && headersComplete && contentComplete;
    }

    @Override
    public String toString() {
        return "RTSPRequest{" +
                "headers=" + headers +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", hasType=" + hasType +
                ", headersComplete=" + headersComplete +
                ", contentComplete=" + contentComplete +
                ", contentLength=" + contentLength + " (" + (content == null ? "0" : content.length() + ")") +
                ", incompleteLine='" + incompleteLine + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
