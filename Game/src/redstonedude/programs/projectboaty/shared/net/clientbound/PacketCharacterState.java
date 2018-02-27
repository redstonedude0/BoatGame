package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.task.Task;

public class PacketCharacterState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String characterUUID;
	public Task currentTask;
	public EntityResource carrying;
	
	public PacketCharacterState() {
		super("PacketCharacterState");
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("characterUUID", aInputStream.readUTF(),PacketCharacterState.class);
		setFieldViaReflection("currentTask", aInputStream.readObject(),PacketCharacterState.class);
		setFieldViaReflection("carrying", aInputStream.readObject(),PacketCharacterState.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(characterUUID);
		aOutputStream.writeObject(currentTask);
		aOutputStream.writeObject(carrying);
	}
	
}
