package server;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocketendpoint", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public class WebServer {
	
	private static final String USERNAME_KEY = "username";
	private static final String CHAT_CHANNEL = "channel";
	private static Map<String, List<Session>> peers = Collections.synchronizedMap(new LinkedHashMap<String, List<Session>>());
	
	@OnOpen
	public void onOpen(Session session){
		
		Map<String, List<String>> parameter = session.getRequestParameterMap();
		List<String> username = parameter.get(USERNAME_KEY);
		List<String> chatname = parameter.get(CHAT_CHANNEL);
		List<Session> sessions =  peers.get(CHAT_CHANNEL);
		sessions.add(session);
		peers.put(chatname.get(0), sessions);
		System.out.println(username + " joined the chat room.");
	}
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, EncodeException {
		Message m = new Message();
		m.setSender(session.getId());
		m.setContent(message);
		m.setReceived(new Date());
		for (Session peer : peers.values()) {
            if (!session.getId().equals(peer.getId())) { // do not resend the message to its sender
                peer.getBasicRemote().sendObject(m);
            }
        }
	}
	
	@OnClose
	public void onClose(Session session) throws IOException, EncodeException{
		System.out.println(session.getId()+" left the chat room.");
        peers.remove(session);
        //notify peers about leaving the chat room
        for (Session peer : peers.values()) {
            Message message = new Message();
            message.setSender("Server");
            message.setContent((String) session.getUserProperties().get("user") + " left the chat room");
            message.setReceived(new Date());
            peer.getBasicRemote().sendObject(message);
        }
	}
}
