package bonn2.unofficialssxfabricconnector;

import bonn2.unofficialssxfabricconnector.util.SimpleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

@net.fabricmc.api.Environment(EnvType.SERVER)
public class UnofficialSSXFabricConnector implements ModInitializer {

    public static boolean DEBUG = false;
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        // Generate config
        SimpleConfig CONFIG = SimpleConfig.of("ServerSelectorX").provider(this::provider).request();
        // Set debug mode
        DEBUG = CONFIG.getOrDefault("debug", false);
        // Start async ping loop when server is finished starting
        ServerStartCallback.EVENT.register((server) -> {
            SERVER = server;
            CompletableFuture.runAsync(this::restartPingTask);
        });
    }

    // Default config file
    private String provider( String filename ) {
        return """
                # Unofficial SSX Fabric Connector Config
                address=localhost:9782
                server-name=
                send-interval=3
                
                # Spam console with debug messages
                debug=false
                """;
    }

    // Repeating ping task, called asynchronously
    void restartPingTask() {
        SimpleConfig CONFIG = SimpleConfig.of( "ServerSelectorX" ).request();

        // Set sleep interval
        final int interval = CONFIG.getOrDefault("send-interval", 3);

        // Send data
        new DataSender().run();

        try {
            // Sleep for predefined interval
            Thread.sleep(interval * 1000L);
        } catch (InterruptedException e) {
            // If thread is killed while sleeping print message and exit
            System.out.println("SSX ping task was interrupted!");
            e.printStackTrace();
            return;
        }
        // Call function again
        restartPingTask();
    }

}
