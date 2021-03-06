package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketRequestTileState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Tile tile;
	
	public PacketRequestTileState(Tile t) {
		super("PacketRequestTileState");
		tile = t;
	}

}
