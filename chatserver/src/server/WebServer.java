package server;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocketendpoint", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public class WebServer {
	
	static Set<Session> peers = Collections.synchronizedSet(new HashSet<>());
	
	@OnOpen
	public void onOpen(Session session){
		System.out.println(session.getId() + " joined the chat room.");
		peers.add(session);
	}
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, EncodeException {
		Message m = new Message();
		m.setSender(session.getId());
		m.setContent(message);
		m.setReceived(new Date());
		for (Session peer : peers) {
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
        for (Session peer : peers) {
            Message message = new Message();
            message.setSender("Server");
            message.setContent((String) session.getUserProperties().get("user") + " left the chat room");
            message.setReceived(new Date());
            peer.getBasicRemote().sendObject(message);
        }
	}
}
