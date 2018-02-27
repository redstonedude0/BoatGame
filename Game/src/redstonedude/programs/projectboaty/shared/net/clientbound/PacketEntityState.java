package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketEntityState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Entity entity;
	
	public PacketEntityState(Entity t) {
		super("PacketEntityState");
		entity = t;
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("entity", aInputStream.readObject(),PacketEntityState.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeObject(entity);
	}
}
