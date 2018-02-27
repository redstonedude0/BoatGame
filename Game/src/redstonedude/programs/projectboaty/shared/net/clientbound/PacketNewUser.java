package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketNewUser  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String uuid;
	
	public PacketNewUser(String u) {
		super("PacketNewUser");
		uuid = u;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketNewUser.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
	}
	
}
