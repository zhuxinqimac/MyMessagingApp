// To print all output to terminal

class MyPrint {
	public static void printDebug(String message) {
		if (Constant.DEBUG == true) {
			System.out.println("Debug : " + message);
		}
	}

	public static void printMessage(String message) {
		System.out.println("-->" + message);
	}

	public static void main(String[] args ) {
		MyPrint.printDebug(args[0]);
	}
}