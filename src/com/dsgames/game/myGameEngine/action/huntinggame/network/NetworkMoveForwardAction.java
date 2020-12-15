package com.dsgames.game.myGameEngine.action.huntinggame.network;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.network.ProtocolClient;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class NetworkMoveForwardAction extends AbstractInputAction {

	private HuntingGame game;
	private Node avatarNode;
	private ProtocolClient protocolClient;
	
	public NetworkMoveForwardAction(HuntingGame g, Node n, ProtocolClient p) {
		avatarNode = n;
		protocolClient = p;
		game = g;
	}
	
	@Override
	public void performAction(float time, Event evt) {
		game.setIsRunned(true);
		game.setPlayerLastRunTime(game.getGameTime());
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		avatarNode.moveForward(speed * 3.0f);
		protocolClient.sendMoveMessage(avatarNode.getLocalPosition());
		game.synchronizeAvatarPhysics(avatarNode);
		
	}
}
