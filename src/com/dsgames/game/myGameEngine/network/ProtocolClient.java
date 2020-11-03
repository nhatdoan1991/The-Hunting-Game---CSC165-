package com.dsgames.game.myGameEngine.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.entities.GhostAvatar;

import ray.networking.client.GameConnectionClient;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class ProtocolClient extends GameConnectionClient {

	private HuntingGame game;
	private UUID id;
	private ConcurrentHashMap<UUID, GhostAvatar> ghostAvatars;
	private Vector3 otherPosition;

	public ProtocolClient(InetAddress remoteAddress, int remotePort, ProtocolType protocolType, HuntingGame game)
			throws IOException {
		super(remoteAddress, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new ConcurrentHashMap<UUID, GhostAvatar>();
	}

	protected void processPacket(Object message) {
		String stringMessage = (String) message;
		String[] messageTokens = stringMessage.split(",");
		UUID ghostId = null;
		Vector3 position = null;
		if (messageTokens.length > 0) {

			switch (messageTokens[0]) {
			
			// server-join, success/fail, clientCount
			case "server-join":
				// we only care about successful connections
				if (messageTokens[1].compareTo("success") == 0) {
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				} else {
					game.setIsConnected(false);
				}
				break;
				
			// server-goodbye, clientId
			case "server-goodbye":
				ghostId = UUID.fromString(messageTokens[1]);
				removeGhostAvatar(ghostId);
				break;
				
			// server-details-for, clientId, x, y, z
			// server-create, clientId, x, y, z
			case "server-details-for":
				
			case "server-create":
				ghostId = UUID.fromString(messageTokens[1]);
				position = Vector3f.createFrom(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4])
						);
				try {
					createGhostAvatar(ghostId, position);
				} catch (IOException e) {
					System.out.println("Ghost avatar for player " + messageTokens[1] + "already exists");
				}
				break;
				
			// server-wants-details, clientId
			case "server-wants-details":
				ghostId = UUID.fromString(messageTokens[1]);
				position = ghostAvatars.get(ghostId).getPosition();
				sendDetailsForMessage(ghostId, position);
				break;

			// server-move, clientId, x, y, z
			case "server-move":
				ghostId = UUID.fromString(messageTokens[1]);
				position = Vector3f.createFrom(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4])
						);
				game.moveGhostAvatar(ghostId, position);
				//	updateGhostAvatarPosition(ghostId, position);
				break;

			default:
				System.out.println("Invalid packet processed. Packet: " + stringMessage);
			}

		}

	}
	
	public Vector3 getOtherPosition() {
		return otherPosition;
	}

	public void updateGhostAvatarPosition(UUID ghostId, Vector3 position) {
		if (!ghostAvatars.containsKey(ghostId)) {
			ghostAvatars.put(ghostId, new GhostAvatar(ghostId, position));
		}
		GhostAvatar ghost = ghostAvatars.get(ghostId);
		ghost.setPosition(position);
		game.moveGhostAvatar(ghostId, position);
		ghostAvatars.replace(ghostId, ghost);
	}

	public void createGhostAvatar(UUID id, Vector3 position) throws IOException {
		GhostAvatar ghost = new GhostAvatar(id, position);
		game.addGhostAvatarToGameWorld(ghost);
		ghostAvatars.put(id, ghost);
	}

	public GhostAvatar removeGhostAvatar(UUID id) {
		GhostAvatar ghost = ghostAvatars.remove(id);
		game.removeGhostAvatarFromGameWorld(ghost);
		return ghost;
	}
	
	/**
	 * client-create, localId, locX, locY, locZ
	 * @param playerPosition
	 */
	private void sendCreateMessage(Vector3 playerPosition) {
		try {
			String message = new String("client-create," + this.id.toString());
			message += "," + playerPosition.x() + "," + playerPosition.y() + "," + playerPosition.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * client-join, localId
	 */
	public void sendJoinMessage() {
		try {
			String message = new String("client-join," + this.id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * client-goodbye, localId
	 */
	public void sendByeMessage() {
		try {
			String message = new String("client-goodbye," + this.id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * client-move, localID, locX, locY, locZ
	 * @param worldPosition
	 */
	public void sendMoveMessage(Vector3 worldPosition) {
		try {
			String message = new String("client-move," + this.id.toString());
			message += "," + worldPosition.x() + "," + worldPosition.y() + "," + worldPosition.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * client-details-for, localId, remoteId, locX, locY, locZ
	 * @param remoteId
	 * @param position
	 */
	public void sendDetailsForMessage(UUID remoteId, Vector3 position) {
		try {
			String message = new String("client-details-for," + this.id.toString() + "," + remoteId.toString());
			message += "," + position.x() + "," + position.y() + "," + position.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * client-wants-details, localId, x, y, z
	 */
	public void sendWantsDetailsMessages(Vector3 position) {
		try {
			String message = new String("client-wants-details," + this.id.toString());
			message += "," + position.x() + "," + position.y() + "," + position.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
