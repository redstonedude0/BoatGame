package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;
import java.util.UUID;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketRequestTileState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Tile tile;
	public String uniqueTestingID;
	
	public PacketRequestTileState(Tile t) {
		super("PacketRequestTileState");
		uniqueTestingID = UUID.randomUUID().toString();
		tile = t;
	}

}
