// Operations about login history file

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

class LoginHistory {

	// Clear the log
	public static synchronized void clearFile() {
		String loginHistory = "./loginhistory.txt";
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(loginHistory));
			writer.write("");
			writer.flush();

		} catch (IOException e) {
			MyPrint.printDebug("Clear : login history file error.");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					MyPrint.printDebug("Clear : close login history file error.");
				}
			}
		}
	}

	public static synchronized void addLogin(String username, long timeMillis) {
		String loginHistory = "./loginhistory.txt";
		BufferedWriter writer = null;
		// Append the log
		try {
			writer = new BufferedWriter(new FileWriter(loginHistory, true));
			String line = "Login " + username + " " + Long.toString(timeMillis);
			writer.write(line);
			writer.newLine();
			writer.flush();

		} catch (IOException e) {
			MyPrint.printDebug(username + " : add login to history file error.");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					MyPrint.printDebug(username + " logout : close history file error.");
				}
			}
		}
	}

	public static synchronized void addLogout(String username, long timeMillis) {
		String loginHistory = "./loginhistory.txt";
		BufferedWriter writer = null;
		// Append the log
		try {
			writer = new BufferedWriter(new FileWriter(loginHistory, true));
			String line = "Logout " + username + " " + Long.toString(timeMillis);
			writer.write(line);
			writer.newLine();
			writer.flush();

		} catch (IOException e) {
			MyPrint.printDebug(username + " : add logout to history file error.");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					MyPrint.printDebug(username + " logout : close history file error.");
				}
			}
		}
	}

	public static synchronized List<String> searchElseSince(String selfName, long nowTime, long period) {
		Path loginHistory = Paths.get(".", "loginhistory.txt");
		List<String> returnList = new ArrayList<String>();
		long startTime = nowTime - period;

		for (int i = 0; i<ServerRunnable.listUserState.size(); i++) {
			UserState toUser = ServerRunnable.listUserState.get(i);
			if (!(toUser.getUsername()).equals(selfName)) {
				if (toUser.isOnline()) {
					if (!returnList.contains(toUser.getUsername())) {
						returnList.add(toUser.getUsername());
					}
				}	
			}
		}

		try (InputStream in = Files.newInputStream(loginHistory);
			BufferedReader reader = 
				new BufferedReader(new InputStreamReader(in))) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] word = line.split(" ");
				String type = word[0];
				String user = word[1];
				String time = word[2];
				if (type.equals("Logout") && !(user.equals(selfName))) {
					if (Long.parseLong(time) >= startTime) {
						if (!returnList.contains(user)) {
							returnList.add(user);
						}
					}
				}
			}
		} catch(IOException x) {
			MyPrint.printDebug("whoelsesince : open loginhistory.txt error.");
		}

		for (int i = 0; i<returnList.size(); i++) {
			MyPrint.printDebug(returnList.get(i));
		}

		return returnList;
	}
}