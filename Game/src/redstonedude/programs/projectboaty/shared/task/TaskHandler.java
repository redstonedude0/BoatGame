package redstonedude.programs.projectboaty.shared.task;

import java.util.ArrayList;
import java.util.Random;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestSetTaskList;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Raft;

public class TaskHandler {
	
	public static Random rand = new Random();
	
	//change to 'assignTask?'
	public static void assignTask(Raft raft, EntityCharacter ec) {
		ArrayList<Task> tasks = raft.getTasks();
		int size = tasks.size();
		if (size != 0) {
			int index = rand.nextInt(size);
			Task t = tasks.get(index);
			if (t.isEligible(ec)) {
				t.assignedEntity = ec;
				raft.removeTask(t);
				ec.currentTask = t;
				return;
			}
		}//else
		TaskWander tw = new TaskWander(ec);
		tw.targetLoc = new VectorDouble(0,1);
		tw.targetLoc_absolute = false;
		ec.currentTask = tw;
	}
	
	public static void sendList(Raft raft) {
		PacketRequestSetTaskList prstl = new PacketRequestSetTaskList();
		prstl.tasks = raft.getTasks();
		ClientPacketHandler.sendPacket(prstl);
	}
	
}
