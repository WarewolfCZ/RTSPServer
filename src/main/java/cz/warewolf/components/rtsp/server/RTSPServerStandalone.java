package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.config.Configurator;
import cz.warewolf.components.config.ConfiguratorInterface;
import cz.warewolf.components.net.server.tcp.ITCPClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: RTSPServerStandalone</p>
 * <p>
 * Description: </p>
 * <p>Copyright (c) 2018</p>
 * Created on 10.04.2018.
 *
 * @author WarewolfCZ $Revision: $ $Id: $
 */
public class RTSPServerStandalone {

    private static final Logger log = LoggerFactory.getLogger(RTSPServerStandalone.class);

    public static void main(String[] args) throws URISyntaxException {
        Thread.currentThread().setName("main-" + Thread.currentThread().getId());

        ConfiguratorInterface config = new Configurator();
        log.info("run(): Standalone RTSPServer is starting");
        // Handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            log.error("main(): Uncaught exception ", e);
            try {
                log.info("main(): saving configuration");
                config.saveToFile();
            } catch (Exception e2) {
                log.warn("main():", e2);
            }
            System.exit(1); // kill off the crashed app
        });


        if (args.length > 0) {
            config.loadFromFile(args[0]);
        } else {
            config.loadFromFile(BuildConfig.DEFAULT_CONFIG_FILE);
        }
        List<Integer> mAudioPorts = new ArrayList<>();

        RTSPServer rtspServer = new RTSPServer(
                config.getValue("address", BuildConfig.DEFAULT_SERVER_ADDRESS),
                Integer.valueOf(config.getValue("rtsp.port", BuildConfig.DEFAULT_RTSP_PORT)),
                Integer.valueOf(config.getValue("rtp.port", BuildConfig.DEFAULT_RTP_PORT)),
                mAudioPorts,
                new IRTSPServerCallback() {

                    @Override
                    public void onClientConnected(ITCPClientConnection client) {

                    }

                    @Override
                    public void onError(ITCPClientConnection clientConnection, Throwable throwable) {

                    }

                    @Override
                    public void onClientDisconnected(ITCPClientConnection client) {

                    }

                    @Override
                    public void onBeforeStart() {

                    }

                    @Override
                    public void onBeforeStop() {

                    }

                    @Override
                    public void onStreamAdded(MediaStream stream) {
                        log.warn("Stream added: " + stream.getTargetPath());
                    }

                    @Override
                    public void onStreamRemoved(MediaStream stream) {
                        log.warn("Stream removed: " + stream.getTargetPath());
                    }
                });
        for (int i = 1; i < args.length; i += 2) {
            String rtspPath = args[i];
            String filePath = args[i + 1];
            if (!filePath.startsWith("file://") &&
                    !filePath.startsWith("rtsp://") &&
                    !filePath.startsWith("http://") &&
                    !filePath.startsWith("rtp://")) {
                filePath = "file://" + filePath;
            }
            MediaStream stream = new MediaStream(filePath, mAudioPorts);
            rtspServer.registerStream(rtspPath, stream);
        }
        rtspServer.startServer();
        config.saveToFile();
    }

}
