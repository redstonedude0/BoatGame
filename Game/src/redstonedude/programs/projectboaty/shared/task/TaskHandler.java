package redstonedude.programs.projectboaty.shared.task;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestSetTaskList;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskHandler {
	
	public static Random rand = new Random();
	
	public static void assignTask(Raft raft, EntityCharacter ec) {
		ArrayList<Task> tasks = raft.getTasks();
		Task lowestPriorityTask = null;
		Priority lowestPriority = Priority.getIneligible();
		for (Task t: tasks) {
			Priority priority = t.getPriority(ec);
			//lowest priority but not ineligible.
			if (((priority.priorityModifier < lowestPriority.priorityModifier && priority.priorityType.priority == lowestPriority.priorityType.priority) || priority.priorityType.priority > lowestPriority.priorityType.priority ||lowestPriority.priorityType == PriorityType.INELIGIBLE ) && priority.priorityType != PriorityType.INELIGIBLE) {
				lowestPriority = priority;
				lowestPriorityTask = t;
			}
		}
		if (lowestPriority.priorityType != PriorityType.INELIGIBLE) {
			//there's an actual task we can do, do it.
			lowestPriorityTask.assignedEntity = ec;
			raft.removeTask(lowestPriorityTask);
			ec.currentTask = lowestPriorityTask;
		} else {
			TaskWander tw = new TaskWander(ec);
			tw.assignedEntity = ec;
			ec.currentTask = tw;
		}
	}
	
	public static void sendList(Raft raft) {
		PacketRequestSetTaskList prstl = new PacketRequestSetTaskList();
		prstl.tasks = raft.getTasks();
		ClientPacketHandler.sendPacket(prstl);
	}
	
}
