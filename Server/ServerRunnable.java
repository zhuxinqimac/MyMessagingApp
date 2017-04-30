// A thread to connect a client after acceptance

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.lang.NullPointerException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ServerRunnable implements Runnable{
	private Socket socket;
	private String threadName;
	public Chat cloud;

	public static List<UserState> listUserState = new ArrayList<UserState>();

	ServerRunnable(Socket connectionSocket, Chat cloud) {
		this.socket = connectionSocket;
		this.threadName = connectionSocket.toString();
		this.cloud = cloud;
		MyPrint.printDebug("Creating " + threadName);
	}

	public static void initiateUserState() {
		Path credentials = Paths.get(".", "credentials.txt");

		try (InputStream in = Files.newInputStream(credentials);
			BufferedReader reader = 
				new BufferedReader(new InputStreamReader(in))) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] word = line.split(" ");
				listUserState.add(new UserState(word[0], word[1]));
			}
		} catch(IOException x) {
			System.out.println("Open credentials.txt error.");
		}
	}

	public static boolean inList(String username) {
		for (int i = 0; i < listUserState.size(); i++) {
			if (username.equals(listUserState.get(i).getUsername())) {
				return true;
			}
		}
		return false;
	}

	public static UserState getUserState(String username) {
		for (int i = 0; i < listUserState.size(); i++) {
			if (username.equals(listUserState.get(i).getUsername())) {
				return listUserState.get(i);
			}
		}
		return null;
	}

	public void run() {
		MyPrint.printDebug("Running thread.");

		BufferedReader inFromClient = null;
		String clientSentence;
		String capitalizedSentence;
		String toSend = "false";
		String username = "_username";
		String password = "_password";
		String[] word;
		DataOutputStream outToClient = null;
		boolean isTryOK = false;
		boolean isLogin = false;
		boolean isBlocked = false;

		BlockingQueue<String> inRequests = new LinkedBlockingQueue<>();
		BlockingQueue<MyProtocol> inReplies = new LinkedBlockingQueue<>();
		BlockingQueue<MyProtocol> outRequests = new LinkedBlockingQueue<>();
		BlockingQueue<String> outReplies = new LinkedBlockingQueue<>();
		MyProtocol sentence;
		MyProtocol sendToClient;

		String toPrivateIP;
		int toPrivatePort;
		String localIP;
		int localPort;
		UserState userState;

		// Debug, print all online users
		for (int i = 0; i < ServerRunnable.listUserState.size(); i++) {
			if (ServerRunnable.listUserState.get(i).isOnline()) {
				MyPrint.printDebug(ServerRunnable.listUserState.get(i).getUsername());
			}		
		}

		// Do authentication
		while (true) {
			try	{
				inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    clientSentence = inFromClient.readLine();
			    word = clientSentence.split(" ");
			    username = word[0];
			    password = word[1];

			    if (inList(username)) {
				    UserState user = getUserState(username);
				    if (user.isOffline()) {
					    toSend = (Authentication.verify(username, password)).toString();
					    MyPrint.printDebug(toSend);
					    outToClient = new DataOutputStream(socket.getOutputStream());
					    outToClient.writeBytes(toSend + '\n');
					} else {
						if (user.isOnline()) {
							toSend = "alreadyLogin";
							MyPrint.printDebug(toSend);
						    outToClient = new DataOutputStream(socket.getOutputStream());
						    outToClient.writeBytes(toSend + '\n');
							isLogin = true;
							break;
						}
						else {
							toSend = "blockedLogin";
							MyPrint.printDebug(toSend);
							outToClient = new DataOutputStream(socket.getOutputStream());
						    outToClient.writeBytes(toSend + '\n');
						    isBlocked = true;
						    break;
						}
					}
				} else {
					toSend = "invalidName";
				    MyPrint.printDebug(toSend);
				    outToClient = new DataOutputStream(socket.getOutputStream());
				    outToClient.writeBytes(toSend + '\n');
					break;
				}

			} catch (IOException e) {
				System.out.println("BufferedReader Error.");
				break;
			} catch (NullPointerException e) {
				MyPrint.printDebug("Client closed.");
				break;
			}

			if (toSend.equals("true")) {
		    	isTryOK = true;
		    	break;
		    }
		}

		if (isBlocked) {
			return;
		}

		if (isLogin) {
			return;
		}

		if (!isTryOK) {

			// Block the user for block duration seconds
			if (inList(username)) {
				getUserState(username).changeToBlockedLogin();
				try {
				    Thread.sleep(1000 * GlobalVar.getBlockDuration());
				} catch(InterruptedException e) {
				    Thread.currentThread().interrupt();
				}
				getUserState(username).changeToOffline();
			}
			return;
		}

		// Successful login
		getUserState(username).changeToOnline();

	    // Set ip and port to username
		userState = getUserState(username);
		userState.setIPAddress((socket.getInetAddress()).toString());
		userState.setPort(socket.getPort());

		try {
            Thread.sleep(Constant.SERVER_OUT_DELAY * 300);
	    } catch (InterruptedException e) {
            MyPrint.printDebug("ServerRunnable thread delay error.");
            return;
	    }

		// Send client ip info
		try {
			outToClient = new DataOutputStream(socket.getOutputStream());
			MyPrint.printDebug((socket.getInetAddress()).toString() + " " + socket.getPort());
		    outToClient.writeBytes((socket.getInetAddress()).toString() + " " + socket.getPort() + '\n');
		} catch(IOException e) {
			MyPrint.printDebug("Server runnable IP info out error.");
		}

		try {
            Thread.sleep(Constant.SERVER_OUT_DELAY * 300);
	    } catch (InterruptedException e) {
            MyPrint.printDebug("ServerRunnable thread delay error.");
            return;
	    }

		// Send timeout
		try {
			outToClient = new DataOutputStream(socket.getOutputStream());
			MyPrint.printDebug("setTimeout " + GlobalVar.getTimeout());
		    outToClient.writeBytes("setTimeout " + GlobalVar.getTimeout() + '\n');
		} catch(IOException e) {
			MyPrint.printDebug("Set timeout error.");
		}

		Thread outThread = new Thread(new ServerOutRunnable(outRequests, outReplies, socket, cloud, username));
		outThread.start();
		Thread inThread = new Thread(new ServerInRunnable(inRequests, inReplies, socket));
		inThread.start();

		// Presence notification
		{
			UserState fromUser = getUserState(username);
			for (int i = 0; i<listUserState.size(); i++) {
				UserState toUser = listUserState.get(i);
				if (!(toUser.getUsername()).equals(fromUser.getUsername())) {
					if (!(toUser.getBlockedByList()).contains(fromUser.getUsername())) {
						if (toUser.isOnline()) {
							MyProtocol iSentence = 
								new MyProtocol("inform", "Ack", "OK", 
									toUser.getUsername(), fromUser.getUsername(), 
									fromUser.getUsername() + " logged in.");
							cloud.send(iSentence);
						}
					}
				}
			}
		}

		// Write login to history file
		LoginHistory.addLogin(username, System.currentTimeMillis());

		// Main loop handling data from inThread
		while(true) {
			try {
			sentence = inReplies.take();
			MyPrint.printDebug(username + "'s ServerRunnable is sending : " + sentence.toString());
			} catch (InterruptedException e) {
				MyPrint.printDebug(username + "'s ServerRunnable inReplies interrupted.");
				break;
			}

			// Handle startprivate
			if ((sentence.name).equals("startprivate")) {
				if (Authentication.checkUsername(sentence.whom)) {
					UserState fromUser = getUserState(sentence.from);
					UserState toUser = getUserState(sentence.whom);
					if ((fromUser.getUsername()).equals(toUser.getUsername())) {
						sentence.type = "Ack";
						sentence.status = "Error";
						sentence.data = "You can not start private with yourself.";
						sentence.whom = username;
						cloud.send(sentence);
					} else {
						if (!toUser.isOnline()) {
							sentence.type = "Ack";
							sentence.status = "Error";
							sentence.data = toUser.getUsername() + " is not online.";
							sentence.whom = username;
							cloud.send(sentence);
						} else {
							if ((fromUser.getBlockedByList()).contains(toUser.getUsername())) {
								sentence.type = "Ack";
								sentence.status = "Error";
								sentence.whom = username;
								sentence.data = "You have been blocked by the recipient.";
								cloud.send(sentence);
							} else {
								// Establish socket connection
								// try {
									toPrivateIP = toUser.getIPAddress();
									toPrivatePort = toUser.getPort();
									localIP = fromUser.getIPAddress();
									localPort = fromUser.getPort();
									sentence.type = "Ack";
									sentence.status = "OK";
									sentence.from = toUser.getUsername();
									sentence.whom = username;
									sentence.data = toPrivateIP+" "+toPrivatePort+" "+localIP+" "+localPort;
									cloud.send(sentence);

									MyProtocol iSentence = 
										new MyProtocol("startprivate", "Response", "OK", 
											toUser.getUsername(), fromUser.getUsername(), "asServer");
									cloud.send(iSentence);

								// }
							}
							
						}
					}
				} else {
					sentence.type = "Ack";
					sentence.status = "Error";
					sentence.data = "No such user. Please try another one!";
					sentence.whom = username;
					cloud.send(sentence);
				}
				continue;
			}

			// Handle message
			if ((sentence.name).equals("message")) {
				if (Authentication.checkUsername(sentence.whom)) {
					UserState fromUser = getUserState(sentence.from);
					UserState toUser = getUserState(sentence.whom);

					if ((fromUser.getUsername()).equals(toUser.getUsername())) {
						sentence.type = "Ack";
						sentence.status = "Error";
						sentence.whom = username;
						sentence.data = "You cannot send message to yourself!";
						cloud.send(sentence);
					} else {
						if ((fromUser.getBlockedByList()).contains(toUser.getUsername())) {
							sentence.type = "Ack";
							sentence.status = "Error";
							sentence.whom = username;
							sentence.data = "Your message could not be delivered as the recipient has blocked you.";
							cloud.send(sentence);
						} else {
							sentence.type = "Response";
							sentence.status = "OK";
							cloud.send(sentence);
						}
					}
				} else {
					sentence.type = "Ack";
					sentence.status = "Error";
					sentence.data = "No such user. Please try another one!";
					sentence.whom = username;
					cloud.send(sentence);
				}
				continue;
			}

			// Handle block somebody
			if ((sentence.name).equals("block")) {
				if (Authentication.checkUsername(sentence.whom)) {
					UserState fromUser = getUserState(sentence.from);
					UserState toUser = getUserState(sentence.whom);
					if ((fromUser.getUsername()).equals(toUser.getUsername())) {
						sentence.type = "Ack";
						sentence.status = "Error";
						sentence.data = "You can not block yourself.";
						sentence.whom = username;
						cloud.send(sentence);
					} else {
						if ((toUser.getBlockedByList()).contains(fromUser.getUsername())) {
							sentence.type = "Ack";
							sentence.status = "Error";
							sentence.data = "Error. " + toUser.getUsername() + " has been blocked.";
							sentence.whom = username;
							cloud.send(sentence);
						} else {
							toUser.addToBlockedByList(fromUser.getUsername());
							sentence.type = "Ack";
							sentence.status = "OK";
							sentence.data = toUser.getUsername() + " is blocked.";
							sentence.whom = username;
							cloud.send(sentence);
						}
					}
				} else {
					sentence.type = "Ack";
					sentence.status = "Error";
					sentence.data = "No such user. Please try another one!";
					sentence.whom = username;
					cloud.send(sentence);
				}
				continue;
			}

			// Handle unblock somebody
			if ((sentence.name).equals("unblock")) {
				if (Authentication.checkUsername(sentence.whom)) {
					UserState fromUser = getUserState(sentence.from);
					UserState toUser = getUserState(sentence.whom);
					if (!(toUser.getBlockedByList()).contains(fromUser.getUsername())) {
						sentence.type = "Ack";
						sentence.status = "Error";
						sentence.data = "Error. " + toUser.getUsername() + " was not blocked.";
						sentence.whom = username;
						cloud.send(sentence);
					} else {
						toUser.removeFromBlockedByList(fromUser.getUsername());
						sentence.type = "Ack";
						sentence.status = "OK";
						sentence.data = toUser.getUsername() + " is unblocked.";
						sentence.whom = username;
						cloud.send(sentence);
					}
				} else {
					sentence.type = "Ack";
					sentence.status = "Error";
					sentence.data = "No such user. Please try another one!";
					sentence.whom = username;
					cloud.send(sentence);
				}
				continue;
			}

			// Handle broadcast
			if ((sentence.name).equals("broadcast")) {
				boolean ifBlockedBySomeone = false;
				for (int i = 0; i<listUserState.size(); i++) {
					UserState toUser = listUserState.get(i);
					if (toUser.isOnline()) {
						if ((getUserState(sentence.from).getBlockedByList()).contains(toUser.getUsername())) {
							ifBlockedBySomeone = true;
						} else {
							if (!(sentence.from).equals(toUser.getUsername())) {
								MyProtocol iSentence = 
									new MyProtocol("message", "Response", "OK", 
										toUser.getUsername(), sentence.from, sentence.data);
								cloud.send(iSentence);
							}
						}
						
					}
				}
				if (ifBlockedBySomeone) {
					sentence.type = "Ack";
					sentence.status = "Error";
					sentence.data = "Your message could not be delivered to some recipients.";
					sentence.whom = username;
					cloud.send(sentence);
				}
				continue;
			}

			// Handle whoelse
			if ((sentence.name).equals("whoelse")) {
				UserState fromUser = getUserState(sentence.from);
				for (int i = 0; i<listUserState.size(); i++) {
					UserState toUser = listUserState.get(i);
					if (!(toUser.getUsername()).equals(fromUser.getUsername())) {
						if (toUser.isOnline()) {
							MyProtocol iSentence = 
								new MyProtocol("whoelse", "Ack", "OK", 
									fromUser.getUsername(), fromUser.getUsername(), toUser.getUsername());
							cloud.send(iSentence);
						}	
					}
				}
				continue;
			}

			// Handle whoelsesince
			if ((sentence.name).equals("whoelsesince")) {
				UserState fromUser = getUserState(sentence.from);
				List<String> elseSinceList = null;

				elseSinceList = LoginHistory.searchElseSince(fromUser.getUsername(), 
					System.currentTimeMillis(), Long.parseLong(sentence.data) * 1000);
				for (int i = 0; i<elseSinceList.size(); i++) {
					MyProtocol iSentence = 
						new MyProtocol("inform", "Ack", "OK", 
							fromUser.getUsername(), fromUser.getUsername(), 
							elseSinceList.get(i));
					cloud.send(iSentence);
				}
				continue;
			}

			// Handle logout
			try {
				if ((sentence.name).equals("logout")) {

					UserState fromUser = getUserState(sentence.from);
					for (int i = 0; i<listUserState.size(); i++) {
						UserState toUser = listUserState.get(i);
						if (!(toUser.getUsername()).equals(fromUser.getUsername())) {
							if (!(toUser.getBlockedByList()).contains(fromUser.getUsername())) {
								if (toUser.isOnline()) {
									MyProtocol iSentence = 
										new MyProtocol("inform", "Ack", "OK", 
											toUser.getUsername(), fromUser.getUsername(), 
											fromUser.getUsername() + " logged out.");
									cloud.send(iSentence);
								}
							}
						}
					}

					sentence.type = "Ack";
					sentence.whom = username;
					cloud.send(sentence);
					outThread.join(Constant.THREAD_WAIT * 1000);
					MyPrint.printDebug("Server out thread joint.");
					inThread.interrupt();
					MyPrint.printDebug("Server in thread interrupted.");
					getUserState(username).changeToOffline();

					// Write login to history file
					LoginHistory.addLogout(username, System.currentTimeMillis());
					break;
				}
			} catch (InterruptedException e) {
				MyPrint.printDebug("ServerRunnable outThread join interrupted.");
				break;
			}
		}

		return;
	}
}