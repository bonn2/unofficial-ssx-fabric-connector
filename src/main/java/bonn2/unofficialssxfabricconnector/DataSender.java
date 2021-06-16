package bonn2.unofficialssxfabricconnector;

import bonn2.unofficialssxfabricconnector.util.SimpleConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DataSender {

    public void run() {
        SimpleConfig CONFIG = SimpleConfig.of( "ServerSelectorX" ).request();

        String ip = CONFIG.getOrDefault("address", "address");

        // No addresses found, skip sending data
        if (ip.equals("address")) return;

        final String address = (!ip.startsWith("https://") && !ip.startsWith("http://"))
                ? "http://" + ip : ip;

        debug(address, "Preparing to send data");

        final String serverName = CONFIG.getOrDefault("server-name", "");

        debug(address, "Using server name '" + serverName + "'");

        if (serverName.equals("")) {
            debug(address, "Server name is empty! Not sending data");
            return;
        }

        debug(address, "Sending Placeholders");

        try {
            sendPlaceholders(address, serverName);
        } catch (IOException e) {
            System.out.println("Failed to send placeholders");
            e.printStackTrace();
        }

        debug(address, "Data sent!");
    }

    private void sendPlaceholders(final String address, final String serverName) throws IOException {

        // Get player counts
        int max = UnofficialSSXFabricConnector.SERVER.getMaxPlayerCount();
        int online = UnofficialSSXFabricConnector.SERVER.getCurrentPlayerCount();

        // Manually format simple json data
        final String json = "{'max': '" + max + "', 'online': '" + online + "'}";
        debug(address, "Placeholders json: " + json);

        // Send json data
        final String parameters = String.format("server=%s&data=%s", this.encode(serverName), this.encode(json));

        final HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(parameters);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Response code " + connection.getResponseCode());
        }
    }

    private String encode(final Object object) {
        return URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
    }

    protected void debug(String address, String message) {
        if (UnofficialSSXFabricConnector.DEBUG) {
            System.out.println(address + " : " + message);
        }
    }
}
