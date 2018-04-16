package cz.warewolf.components.rtsp.server;

import cz.warewolf.components.config.Configurator;
import cz.warewolf.components.config.ConfiguratorInterface;
import cz.warewolf.components.net.IClientConnection;
import cz.warewolf.components.net.IServerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

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

    public static void main(String[] args) throws InterruptedException {
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
                new IServerCallback() {
                    @Override
                    public void onClientConnected(IClientConnection iClientConnection) {

                    }

                    @Override
                    public void onDataReceived(IClientConnection iClientConnection, byte[] bytes, int i) {

                    }

                    @Override
                    public void onError(IClientConnection iClientConnection, Throwable throwable) {

                    }

                    @Override
                    public void onClientDisconnected(IClientConnection iClientConnection) {

                    }

                    @Override
                    public void onBeforeStart() {

                    }

                    @Override
                    public void onBeforeStop() {

                    }
                });
        rtspServer.startServer();
        //sleep(5000);
        //log.info("run(): RTSPServer is stopping");
        //rtspServer.stopServer();

        config.saveToFile();
    }

}
