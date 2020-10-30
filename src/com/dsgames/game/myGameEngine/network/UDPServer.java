package com.dsgames.game.myGameEngine.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

public class UDPServer extends GameConnectionServer<UUID> {

	private int connectedClients = 0;

	public UDPServer(int localPort) throws IOException {
		super(localPort, ProtocolType.UDP);
		System.out.println("UDP server successfully initialized.");
		System.out.println("Server address: " + this.getLocalInetAddress());
		System.out.println("Server port: " + localPort);
	}

	@Override
	public void processPacket(Object o, InetAddress senderIP, int sendPort) {
		String message = (String) o;
		String[] messageTokens = message.split(",");
		UUID clientId = null;
		if (messageTokens.length > 0) {
			// join message: (join, localId)
			if (messageTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo clientInfo;
					clientInfo = getServerSocket().createClientInfo(senderIP, sendPort);
					clientId = UUID.fromString(messageTokens[1]);
					addClient(clientInfo, clientId);
					sendJoinedMessage(clientId, true, connectedClients++);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					System.out.println(clientId + " has joined the server as client " + connectedClients + ".");
				}
			}
			
			// create message: (create, localId, x, y, z)
			if (messageTokens[0].compareTo("create") == 0) {
				clientId = UUID.fromString(messageTokens[1]);
				String[] position = {
						messageTokens[2], 
						messageTokens[3], 
						messageTokens[4]
				};
				sendCreateMessage(clientId, position);
				sendWantsDetailsForMessage(clientId);
			}
			
			// goodbye message: (goodbye, localId)
			if (messageTokens[0].compareTo("goodbye") == 0) {
				clientId = UUID.fromString(messageTokens[1]);
				System.out.println(clientId + " has left the server.");
				removeClient(clientId);
				sendByeMessage(clientId);
				connectedClients--;
			}
			
			// button message: (button, localId)
			if (messageTokens[0].compareTo("button") == 0) {
				clientId = UUID.fromString(messageTokens[1]);
				sendButtonMessage(clientId);
			}
		}
	}

	public void sendJoinedMessage(UUID clientId, boolean success, int clients) {
		try {
			String message = "join,";
			if (success) {
				message += "success," + clients;
			} else {
				message += "fail," + clients;
			}
			sendPacket(message, clientId);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCreateMessage(UUID clientId, String[] position) {
		try {
			String p = "";
			for(int i = 0; i < 3; i++) {
				p += "," + position[i];
			}
			forwardPacketToAll("create," + clientId.toString() + p, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendWantsDetailsForMessage(UUID clientId) {
		try {
			forwardPacketToAll("wants," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessage(UUID clientId) {
		try {
			forwardPacketToAll("bye," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendButtonMessage(UUID clientId) {
		try {
			forwardPacketToAll("button," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getConnectedClients() {
		return connectedClients;
	}

}
