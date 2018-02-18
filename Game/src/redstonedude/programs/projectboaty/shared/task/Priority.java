package redstonedude.programs.projectboaty.shared.task;

public class Priority {

	/**
	 * Can be any value, lower values will be taken up first
	 */
	public int priorityModifier = 0;
	public PriorityType priorityType = PriorityType.INELIGIBLE;

	public Priority() {// default is ineligible,0
	}

	public Priority(PriorityType type, int mod) {
		priorityType = type;
		priorityModifier = mod;
	}

	public enum PriorityType {
		INELIGIBLE(-1), CRITICAL(10), NORMAL(5);

		public int priority = -1;

		PriorityType(int p) {
			priority = p;
		}
	};

	public static Priority getIneligible() {
		return new Priority();// ineligible by default
	}

}
