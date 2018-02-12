package redstonedude.programs.projectboaty.client.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Packet Listener to handle connection to the server
 */
public class ClientPacketListener implements Runnable {

    //PrintWriter to write data to the server
    private static PrintWriter out;

    /**
     * Start the listener
     * @param portNumber
     * 		The port number to connect on
     * @param hostName
     * 		the IP of the server
     */
    public void start(int portNumber, String hostName) {
	//Connect to the server, and setup a PrintWriter and BufferedReader to write and read to/from the server respectively
	try (Socket socket = new Socket(hostName, portNumber); PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
	    //Set the pointer to the printwriter
	    out = out2;
	    String inputLine;
	    //Handle each packet the server sends to the client
	    while ((inputLine = in.readLine()) != null) {
		ClientPacketHandler.handlePacket(this, inputLine);
	    }
	} catch (Exception e) {
	    //If the server is shutdown then log that the client was disconnected
	    //Logger.log("Disconnected from server: " + e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Send data to the server
     * @param data
     * 		The data to send to the server
     */
    public static synchronized void send(String data) {
	out.println(data);
    }

    /**
     * Start the listener
     */
    @Override
    public void run() {
	//Logger.log("Starting client packet listener");
	//start(Client.connection_port, Client.connection_host);
    }

}
