package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketTileState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Tile tile;
	public String uuid;
	
	public PacketTileState(Tile t) {
		super("PacketTileState");
		tile = t;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketTileState.class);
		setFieldViaReflection("tile", aInputStream.readObject(),PacketTileState.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeObject(tile);
	}
	
}
