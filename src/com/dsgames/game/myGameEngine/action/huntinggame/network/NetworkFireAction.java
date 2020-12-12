package com.dsgames.game.myGameEngine.action.huntinggame.network;

import java.io.IOException;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.network.ProtocolClient;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class NetworkFireAction extends AbstractInputAction {

	private HuntingGame game;
	private Node avatarNode;
	private ProtocolClient protocolClient;
	
	public NetworkFireAction(HuntingGame g, Node n, ProtocolClient p) {
		avatarNode = n;
		protocolClient = p;
		game = g;
	}
	
	@Override
	public void performAction(float time, Event evt) {
		// TODO Auto-generated method stub
		//System.out.println("Firing bullet at: " + avatarNode.getChild(0).getWorldPosition().toString());
		// we need to implement the bullet
		try {
			game.fireBullet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// protocolClient.send Fire Message
	}

}
