package redstonedude.programs.projectboaty.shared.src;

public class Main {
	
	public static boolean isServer = false;
	
	public static void main(String[] args) {
		for (String arg: args) {
			if (arg.equalsIgnoreCase("server")) {
				isServer = true;
				Server.init();
				return;
			}
		}
		Client.init();
	}
	
	
}
