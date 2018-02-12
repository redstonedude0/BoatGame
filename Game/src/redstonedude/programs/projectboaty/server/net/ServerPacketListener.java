package redstonedude.programs.projectboaty.server.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ServerPacketListener, each listener waits for, and then handles, an
 * individual client
 * 
 */
public class ServerPacketListener implements Runnable {

    // PrintWriter to print data to the client via a stream
    private PrintWriter out;
    // The UUID of this listener
    public String listener_uuid = "";

    /**
     * Start the listener listening on a port
     * 
     * @param portNumber
     *            The port to listen on
     */
    public void start(int portNumber) {
	// Try-with-resources, to ensure no memory leaks occur
	try (
	// Wait for a new connection into the server socket, and accept the
	// connection
	Socket clientSocket = ServerPacketHandler.serverSocket.accept();
		// Create a PrintWriter to write to the clients output stream
		PrintWriter out2 = new PrintWriter(clientSocket.getOutputStream(), true);
		// Create a BufferedReader to read the clients input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
	    //Start a new listener to handle the next client
	    ServerPacketHandler.startNewListener();
	    //Update out to point to this clients PrintWriter
	    out = out2;
	    String inputLine;
	    // For each line in the BufferedReader (this waits for the client to send a line, then handles it)
	    while ((inputLine = in.readLine()) != null) {
		//Handle that packet
		ServerPacketHandler.handlePacket(this, inputLine);
	    }
	} catch (Exception e) {
	    //If an exception occurs then tell everyone that a player crashed and print the error
	    //ServerChatHelper.chat(ChatType.PlayerCrash, listener_uuid, e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Send data to the client
     * @param data
     * 		The data to send
     */
    public synchronized void send(String data) {
	//If the PrintWriter exists, then print the data to it
	if (out != null) {
	    out.println(data);
	}
    }

    /**
     * Run, called when the ServerPacketListener first starts within its own thread
     */
    @Override
    public void run() {
	//Start the listener on the correct port
	//Logger.log("Starting server listener on port " + Server.connection_port);
	//start(Server.connection_port);
    }

}
