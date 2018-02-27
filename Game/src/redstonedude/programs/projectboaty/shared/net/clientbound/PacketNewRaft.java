package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Raft;

public class PacketNewRaft  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public Raft raft;
	
	public PacketNewRaft(String u, Raft r) {
		super("PacketNewRaft");
		uuid = u;
		raft = r;
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketNewRaft.class);
		setFieldViaReflection("raft", aInputStream.readObject(),PacketNewRaft.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeObject(raft);
	}

}
