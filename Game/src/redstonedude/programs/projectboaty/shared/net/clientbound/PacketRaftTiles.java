package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketRaftTiles extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Tile> tiles;
	public String uuid;
	
	public PacketRaftTiles() {
		super("PacketRaftTiles");
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketRaftTiles.class);
		setFieldViaReflection("tiles", aInputStream.readObject(),PacketRaftTiles.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeObject(tiles);
	}

}
