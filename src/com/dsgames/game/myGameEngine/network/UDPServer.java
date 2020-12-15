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
		String[] position = new String[3];
		UUID clientId = null;
		if (messageTokens.length > 0) {
			
			switch(messageTokens[0]) {
			
			// client-join, localId
			case "client-join":
				try {
					IClientInfo clientInfo;
					clientInfo = getServerSocket().createClientInfo(senderIP, sendPort);
					clientId = UUID.fromString(messageTokens[1]);
					addClient(clientInfo, clientId);
					int team = 0;
					if(connectedClients%2==1)
					{
						team=1;
					}
					sendJoinedMessage(clientId, true, connectedClients++,team);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					System.out.println(clientId + " has joined the server as client " + connectedClients + ".");
				}
				break;

			// client-goodbye, localId	
			case "client-goodbye":
				clientId = UUID.fromString(messageTokens[1]);
				System.out.println(clientId + " has left the server.");
				removeClient(clientId);
				sendByeMessage(clientId);
				connectedClients--;
				break;

			// client-details-for, localId, remoteId, locX, locY, locZ
			case "client-details-for":
				clientId = UUID.fromString(messageTokens[1]);
				UUID remoteId = UUID.fromString(messageTokens[2]);
				position[0] = messageTokens[3];
				position[1] = messageTokens[4];
				position[2] = messageTokens[5];
				sendDetailsForMessage(clientId, remoteId, position);
				break;
				
			// client-create, localId, locX, locY, locZ
			case "client-create":
				clientId = UUID.fromString(messageTokens[1]);
				position[0] = messageTokens[2];
				position[1] = messageTokens[3];
				position[2] = messageTokens[4];
				System.out.println(clientId +" in team "+messageTokens[5]);
				sendCreateMessage(clientId, position,messageTokens[5]);
				sendWantsDetailsForMessage(clientId,messageTokens[5]);
				break;

			
			// client-wants-details, localId
			case "client-wants-details":
				clientId = UUID.fromString(messageTokens[1]);
				position[0] = messageTokens[2];
				position[1] = messageTokens[3];
				position[2] = messageTokens[4];
				
				break;
				
			// client-move, localID, locX, locY, locZ
			case "client-move":
				clientId = UUID.fromString(messageTokens[1]);
				position[0] = messageTokens[2];
				position[1] = messageTokens[3];
				position[2] = messageTokens[4];
				sendMoveMessage(clientId, position);
				break;
				
			case "client-npc-position-message":
				clientId = UUID.fromString(messageTokens[1]);
				int npcId = Integer.parseInt(messageTokens[2]);
				String[] transform = {
						messageTokens[3], messageTokens[4], messageTokens[5], 
						messageTokens[6], messageTokens[7], messageTokens[8],
						messageTokens[9], messageTokens[10], messageTokens[11], 
						messageTokens[12], messageTokens[13], messageTokens[14],
						messageTokens[15], messageTokens[16], messageTokens[17], 
						messageTokens[18]
				};
				String[] vectorPosition = {
						messageTokens[19], messageTokens[20], messageTokens[21]
				};
				sendNpcPositionMessage(clientId, npcId, transform, vectorPosition);
				break;

			default:
				System.out.println("Invalid packet processed. Packet: " + message);
			}
		}
	}

	
	private String processPosition(String[] position) {
		String p = "";
		for(int i = 0; i < position.length; i++) {
			p += "," + position[i];
		}
		return p;
	}

	/**
	 * server-join, success/fail, clientCount
	 * @param clientId
	 * @param success
	 * @param clients
	 */
	public void sendJoinedMessage(UUID clientId, boolean success, int clients,int team) {
		try {
			String message = "server-join,";
			if (success) {
				message += "success," + clients+","+team;
			} else {
				message += "fail," + clients;
			}
			sendPacket(message, clientId);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * server-create, clientId, x, y, z,team
	 * @param clientId
	 * @param position
	 */
	public void sendCreateMessage(UUID clientId, String[] position, String team) {
		try {
			String p = processPosition(position);
			forwardPacketToAll("server-create," + clientId.toString() + p+","+team, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * server-wants-details, clientId
	 * @param clientId
	 */
	public void sendWantsDetailsForMessage(UUID clientId,String team) {
		try {
			forwardPacketToAll("server-wants-details," + clientId.toString()+","+team, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * server-details-for, clientId, x, y, z
	 * @param clientId
	 * @param remoteId
	 * @param position
	 */
	public void sendDetailsForMessage(UUID clientId, UUID remoteId, String[] position) {
		try {
			String p = processPosition(position);
			sendPacket("server-details-for," + clientId.toString() + p, remoteId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * server-move, clientId, x, y, z
	 * @param clientId
	 * @param position
	 */
	private void sendMoveMessage(UUID clientId, String[] position) {
		try {
			String p = processPosition(position);
			forwardPacketToAll("server-move," + clientId.toString() + p, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * server-goodbye, clientId
	 * @param clientId
	 */
	public void sendByeMessage(UUID clientId) {
		try {
			forwardPacketToAll("server-goodbye," + clientId.toString(), clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNpcPositionMessage(UUID clientId, int npcId, String[] npcTransform, String[] position) {
		try {
			String p = "," + npcId + processPosition(npcTransform) + processPosition(position);
			forwardPacketToAll("server-npc-position-message," + clientId.toString() + p, clientId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getConnectedClients() {
		return connectedClients;
	}

}