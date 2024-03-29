package com.dsgames.game.myGameEngine.action.huntinggame.network;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.network.ProtocolClient;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class NetworkMoveBackwardAction extends AbstractInputAction {

	private HuntingGame game;
	private Node avatarNode;
	private ProtocolClient protocolClient;
	
	public NetworkMoveBackwardAction(HuntingGame g, Node n, ProtocolClient p) {
		avatarNode = n;
		protocolClient = p;
		game = g;
	}
	
	@Override
	public void performAction(float time, Event evt) {
		game.setIsRunned(true);
		game.setPlayerLastRunTime(game.getGameTime());
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		avatarNode.moveBackward(speed * 1.5f);
		game.synchronizeAvatarPhysics(avatarNode);
		protocolClient.sendMoveMessage(avatarNode.getWorldPosition());
	}
}
