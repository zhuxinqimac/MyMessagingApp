// Things about authentication

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class Authentication {
	public static Boolean verify(String username, String password) {
		Path credentials = Paths.get(".", "credentials.txt");
		boolean notThere = true;
		
		// Read the credentials.txt file to verify username and password
		try (InputStream in = Files.newInputStream(credentials);
			BufferedReader reader = 
				new BufferedReader(new InputStreamReader(in))) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] word = line.split(" ");
				if ((username.equals(word[0])) && (password.equals(word[1]))) {
					notThere = false;
					break;
				}
			}
		} catch(IOException x) {
			System.out.println("Open credentials.txt error.");
		}

		if (notThere) {
			MyPrint.printDebug("AuthenticationNotOK");
			return false;
		}
		else {
			MyPrint.printDebug("AuthenticationOK");
			return true;
		}
	}

	public static Boolean checkUsername(String username) {
		Path credentials = Paths.get(".", "credentials.txt");
		boolean notThere = true;

		// Read the credentials.txt file to verify username
		try (InputStream in = Files.newInputStream(credentials);
			BufferedReader reader = 
				new BufferedReader(new InputStreamReader(in))) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] word = line.split(" ");
				if (username.equals(word[0])) {
					notThere = false;
					break;
				}
			}
		} catch(IOException x) {
			System.out.println("Open credentials.txt error.");
		}

		if (notThere) {
			return false;
		}
		else {
			return true;
		}
	}

	public static void main(String[] args) {
		Authentication.verify("c3p00", "droid");
	}
}