package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketMoveCharacter extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid = "";
	public Location loc;

	public PacketMoveCharacter() {
		super("PacketMoveCharacter");
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketMoveCharacter.class);
		setFieldViaReflection("loc", aInputStream.readObject(),PacketMoveCharacter.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeObject(loc);
	}

}
