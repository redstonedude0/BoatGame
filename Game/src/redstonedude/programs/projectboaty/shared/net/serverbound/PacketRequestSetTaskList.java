package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.task.Task;

public class PacketRequestSetTaskList extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Task> tasks;
	
	public PacketRequestSetTaskList() {
		super("PacketRequestSetTaskList");
	}

}
