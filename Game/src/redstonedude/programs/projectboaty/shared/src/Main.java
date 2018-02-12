package redstonedude.programs.projectboaty.shared.src;

public class Main {
	
	public static void main(String[] args) {
		for (String arg: args) {
			if (arg.equalsIgnoreCase("server")) {
				Server.init();
				return;
			}
		}
		Client.init();
	}
	
	
}
