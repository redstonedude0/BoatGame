package redstonedude.programs.projectboaty.client.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.src.Logger;

public class ClientPacketListener implements Runnable {

	private static PrintWriter out;

	public void start(int portNumber, String hostName) {
		try (Socket socket = new Socket(hostName, portNumber); PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			
			out = out2;
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				ClientPacketHandler.handlePacket(this, inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void send(String data) {
		out.println(data);
	}
	
	@Override
	public void run() {
		Logger.log("Starting client packet listener");
		start(ClientPacketHandler.portNumber,ClientPacketHandler.hostName);
	}

}
