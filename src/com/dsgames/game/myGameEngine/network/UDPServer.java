package com.dsgames.game.myGameEngine.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Vector3f;

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
			
			// details-for message: (details-for, remoteId, localId, x, y, z)
			if (messageTokens[0].compareTo("details-for") == 0) {
				clientId = UUID.fromString(messageTokens[1]);
				UUID remoteId = UUID.fromString(messageTokens[2]);
				String[] position = {
						messageTokens[3], 
						messageTokens[4], 
						messageTokens[5]
				};
				sendDetailsForMessage(clientId, remoteId, position);
			}
			
			// move message: (move, localId, x, y, z)
			if (messageTokens[0].compareTo("move") == 0) {
				clientId = UUID.fromString(messageTokens[1]);
				String[] position = {
						messageTokens[2], 
						messageTokens[3], 
						messageTokens[4]
				};
				sendMoveMessage(clientId, position);
			}
		}
	}

	
	private String processPosition(String[] position) {
		String p = "";
		for(int i = 0; i < 3; i++) {
			p += "," + position[i];
		}
		p += ",";
		return p;
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
			String p = processPosition(position);
			forwardPacketToAll("create," + clientId.toString() + p, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendWantsDetailsForMessage(UUID clientId) {
		try {
			forwardPacketToAll("wants-details," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendDetailsForMessage(UUID clientId, UUID remoteId, String[] position) {
		try {
			String p = processPosition(position);
			sendPacket("details-for," + clientId.toString() + p, remoteId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMoveMessage(UUID clientId, String[] position) {
		try {
			String p = processPosition(position);
			forwardPacketToAll("details-for," + clientId.toString() + p, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendByeMessage(UUID clientId) {
		try {
			forwardPacketToAll("goodbye," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getConnectedClients() {
		return connectedClients;
	}

}
