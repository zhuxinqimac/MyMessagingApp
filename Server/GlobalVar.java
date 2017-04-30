// Global variables

class GlobalVar {
	private static int serverPort = Constant.SERVER_PORT;
	private static int blockDuration = Constant.BLOCK_DURATION;
	private static int timeout = Constant.TIMEOUT;

	public static int getServerPort() {
		return serverPort;
	}

	public static int getBlockDuration() {
		return blockDuration;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static void setServerPort(int servPort) {
		serverPort = servPort;
	}

	public static void setBlockDuration(int block) {
		blockDuration = block;
	}

	public static void setTimeout(int time) {
		timeout = time;
	}
}