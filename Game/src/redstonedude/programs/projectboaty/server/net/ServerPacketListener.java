package redstonedude.programs.projectboaty.server.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketListener implements Runnable {

	// private PrintWriter out;
	private ObjectOutputStream oos;
	
	public String listener_uuid = "";
	public InetAddress IP;
	private Socket sock;

	public static ExecutorService executorService = Executors.newCachedThreadPool();
	
	public void start(int portNumber) {
		//executorService 
		try (Socket clientSocket = ServerPacketHandler.serverSocket.accept(); ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream())); ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));) {
			// Start a new listener to handle the next client
			ServerPacketHandler.startNewListener();// - MOVED TO playerJoin in ServerPacketHandler
			IP = clientSocket.getInetAddress();
			oos = out2;
			
			sock = clientSocket;
			Object inputObject = null;
			ServerPacketHandler.playerJoin(this);
			// ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player join with null packet for now
			// ServerPacketHandler.playerJoin(this);
			while ((inputObject = in.readObject()) != null) {
				// System.out.println("meme1");
				// if (inputObject != null) {
				ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket((Packet) inputObject, this));
				// }

				// ServerPacketHandler.handlePacket(this, (Packet) inputObject);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			// IOException - client closed connected. Disconnet them.
			Logger.log("Disconnection 1: " + e.getMessage());
			ServerPacketHandler.playerDisconnect(this);
		} catch (Exception e) { // This should never actually occur, since packet handling isn't done in this thread.
			// actual error occured (Possibly cast error), crash the user
			e.printStackTrace();
			if (e.getMessage() != null) {
				Logger.log("Disconnection 2: " + e.getMessage());
			}
			// disconnect
			ServerPacketHandler.playerDisconnect(this);
		}
	}

	//private ConcurrentLinkedQueue<Packet> packetsToSend = new ConcurrentLinkedQueue<Packet>();

	private long time = System.currentTimeMillis();
	private long packets = 0;
	private long nanosCum = 0;
	private long nanos1 = 0;
	private long nanos2 = 0;
	private long nanos3 = 0;
	
	public synchronized void send(Packet data) {
		if (oos != null) {
			try {
				long t = System.currentTimeMillis();
				long n = System.nanoTime();
				packets++;
				if (t > time) {
					time += 1000;
					//System.out.println( packets + " packets in last second, took up " + nanosCum + " nanos");
	//				System.out.println("  Final time: " + nanos1);
	//				System.out.println("  Execu time: " + nanos2);
					//System.out.println("  Flush time: " + nanos3);
					packets = 0;
					nanosCum = 0;
					nanos1 = 0;
					nanos2 = 0;
					nanos3 = 0;
					synchronized(oos) {
						oos.flush();
					}
				}
				// oos.reset();
				// System.out.println("adding");
//				packetsToSend.add(data);
				//packetsToSend.notifyAll();
				final Packet dat = data;
				long n2 = System.nanoTime();
				//Almost all lag comes from running this function, but this is quicker than calling write directly, quicker
				//than a new thread, and quicker than submit()
				executorService.execute(new Runnable() {
					@Override
					public synchronized void run() {
						try {
							synchronized(oos) {
								//System.out.println(dat.packetID);
								oos.writeObject(dat);
								
							}
							//packetsToSend.remove(data);
					//		oos.reset();
		//					oos.writeUnshared(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		//		System.out.println("pre-start: " + dat.packetID);
				//th.start();
		//		System.out.println("post-start: " + dat.packetID);
				//reset to fix sharing, automatically flushes?
				long n3 = System.nanoTime();
				//oos.flush();//flush anyway?
				long n4 = System.nanoTime();
				long nDiff = n2-n;
				nanos1 += nDiff;
				nDiff = n3-n2;
				nanos2 += nDiff;
				nDiff = n4-3;
				nanos3 += nDiff;
				nDiff = n4-n;
				nanosCum += nDiff;

				// If at any point data is going slow its cos of this reset.
				// The stream caches objects so if we change a variable it wont send properly
				// so we need to reset the stream for this. The alternative is to manually serialize
				// or clone objects before sending them and reset less often to clear the cache
				// consider writeUnshared()

				// if (System.nanoTime()%5000 == 0) {
				// }
			} catch (Exception e) {
				e.printStackTrace();
				if (ServerPacketHandler.listeners.contains(this)) {// if not disconnected
					Logger.log("Disconnection 3: " + e.getMessage());
					ServerPacketHandler.playerDisconnect(this);
					// ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player disconnect with null packet for now
					// ServerPacketHandler.playerDisconnect(this);
					oos = null;// close this connection
				}
			}
		}
	}

	@Override
	public void run() {
		// Start the listener on the correct port
		Logger.log("Starting server listener on port " + ServerPacketHandler.portNumber);
		//System.out.println("memec");
//		Thread t = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				//System.out.println("meme0");
//				while (true) {
//					//System.out.println("memeb");
//					try {
//						//packetsToSend.wait();
//						for (Packet p : packetsToSend) {
//							//System.out.println("meme2");
//							packetsToSend.remove(p);
//							oos.writeObject(p);
//							oos.reset();
//							oos.flush();
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//		t.start();
		start(ServerPacketHandler.portNumber);
	}

	public void killConnection() {
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
