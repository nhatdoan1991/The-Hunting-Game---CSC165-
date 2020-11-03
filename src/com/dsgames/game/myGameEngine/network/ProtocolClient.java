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
			case "join":
				// format: [join, success] or [join, X]
				// we only care about successful connections
				if (messageTokens[1].compareTo("success") == 0) {
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				} else {
					game.setIsConnected(false);
				}
				break;

			case "goodbye":
				// goodbye, remoteId
				ghostId = UUID.fromString(messageTokens[1]);
				removeGhostAvatar(ghostId);
				break;

			case "details-for":
			case "create":
				// details-for, remoteId, x, y, z
				ghostId = UUID.fromString(messageTokens[1]);
				position = Vector3f.createFrom(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4])
						);
				createGhostAvatar(ghostId, position);
				break;

			case "wants-details":
				// wants, remoteId
				ghostId = UUID.fromString(messageTokens[1]);
				position = ghostAvatars.get(ghostId).getPosition();
				sendDetailsForMessage(ghostId, position);
				break;

			case "move":
				// move, x, y, z
				position = Vector3f.createFrom(
						Float.parseFloat(messageTokens[1]),
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3])
						);
				updateGhostAvatarPosition(id, position);
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
		GhostAvatar ghost = ghostAvatars.get(ghostId);
		ghost.setPosition(position);
		ghostAvatars.replace(ghostId, ghost);
	}

	public void createGhostAvatar(UUID id, Vector3 position) {
		GhostAvatar ghost = new GhostAvatar(id, position);
		game.addGhostAvatarToGameWorld(ghost);
		ghostAvatars.put(id, ghost);
	}

	public GhostAvatar removeGhostAvatar(UUID id) {
		GhostAvatar ghost = ghostAvatars.remove(id);
		game.removeGhostAvatarFromGameWorld(ghost);
		return ghost;
	}
	
	private void sendCreateMessage(Vector3 playerPosition) {
		try {
			String message = new String("create," + id.toString());
			message += "," + playerPosition.x() + "," + playerPosition.y() + "," + playerPosition.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinMessage() {
		try {
			String message = new String("join," + id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendByeMessage() {
		try {
			String message = new String("goodbye," + id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMoveMessage(Vector3 worldPosition) {
		try {
			String message = new String("move," + id.toString());
			message += "," + worldPosition.x() + "," + worldPosition.y() + "," + worldPosition.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendDetailsForMessage(UUID remoteId, Vector3 position) {
		try {
			String message = new String("details-for," + id + "," + remoteId.toString());
			message += "," + position.x() + "," + position.y() + "," + position.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendWantsDetailsMessages() {
		try {
			String message = new String("wants-details," + id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
