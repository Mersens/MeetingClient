package svs.meeting.service;

public interface INetManager {
	public static final String CONNECTING="connecting";
	public static final String CONNECTED="connected";
	public static final String CLOSED="closed";
	public static final String READY="ready";
	public void setRelayServer(String server);
	public void startManager();
	public void setMsgListener(MessageListener l);
	public void notifyConnectionEvent(String evt);
	public void send(String msg, String to);
	public void sendWithThread(String msg, String to);
	public void stopManager();
}
