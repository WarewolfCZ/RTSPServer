package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.config.Configurator;
import cz.warewolf.components.config.ConfiguratorInterface;
import cz.warewolf.components.net.ITCPClientConnection;
import cz.warewolf.components.rtsp.server.protocol.MediaStream;
import cz.warewolf.components.rtsp.server.protocol.MediaStreamPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

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

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        ConfiguratorInterface config = new Configurator();
        log.info("run(): RTSPServer is starting");
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

        RTSPServer rtspServer = new RTSPServer(
                config.getValue("address", BuildConfig.DEFAULT_SERVER_ADDRESS),
                Integer.valueOf(config.getValue("tcp.port", BuildConfig.DEFAULT_TCP_PORT)),
                Integer.valueOf(config.getValue("udp.port", BuildConfig.DEFAULT_UDP_PORT)),
                new IRTSPServerCallback() {

                    @Override
                    public void onClientConnected(ITCPClientConnection client) {

                    }

                    @Override
                    public void onDataReceived(ITCPClientConnection clientConnection, byte[] data, int dataLength) {

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
                });
        MediaStream stream = new MediaStream("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov");
        MediaStreamPart part = new MediaStreamPart(0);
        stream.addPart(part);
        rtspServer.addStream("/bunny", stream);
        File f = new File("big_buck_bunny_480p_surround-fix.avi");
        MediaStream stream2 = new MediaStream("file://" + f.getAbsolutePath());
        stream2.addPart(part);
        rtspServer.addStream("/bunny2", stream2);
        rtspServer.startServer();
        //sleep(5000);
        //log.info("run(): RTSPServer is stopping");
        //rtspServer.stopServer();

        config.saveToFile();
    }

}
