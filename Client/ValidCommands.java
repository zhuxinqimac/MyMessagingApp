// Valid commands that a client can use 

import java.util.List;
import java.util.Arrays;

class ValidCommands {
	private static final List<String> validOneWordCommands = 
		Arrays.asList("whoelse", "logout");

	private static final List<String> validTwoWordCommands = 
		Arrays.asList("broadcast", "whoelsesince", "block", "unblock", "message", 
						"startprivate", "private", "stopprivate");

	public static boolean check(String message) {
		String [] word = message.split(" ");
		if (word.length == 1) {
			if (validOneWordCommands.contains(word[0])) {
				return true;
			}
			else return false;
		}

		if (word.length >= 2) {
			if (validTwoWordCommands.contains(word[0])) {
				if (word[0].equals("whoelsesince")) {
					try {
				        Integer.parseInt(word[1]);
				    } catch(NumberFormatException e) {
				        return false;
				    } catch(NullPointerException e) {
				        return false;
				    }
				}
				return true;
			}
			else return false;
		}

		return true;
	}
}