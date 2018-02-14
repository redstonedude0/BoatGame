package redstonedude.programs.projectboaty.server.net;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class ServerQueuedPacket {

	public Packet packet;
	public ServerPacketListener spl;
	
	public ServerQueuedPacket(Packet p, ServerPacketListener s) {
		packet = p;
		spl = s;
	}
	
}
