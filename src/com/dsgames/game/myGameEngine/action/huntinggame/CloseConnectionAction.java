package com.dsgames.game.myGameEngine.action.huntinggame;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.network.ProtocolClient;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.game.Game;

public class CloseConnectionAction extends AbstractInputAction {

	private ProtocolClient protocolClient;
	private HuntingGame game;
	private boolean isClientConnected;
	
	public CloseConnectionAction(ProtocolClient client, HuntingGame g, boolean clientConnectionStatus) {
		protocolClient = client;
		game = g;
		isClientConnected = clientConnectionStatus;
	}
	
	@Override
	public void performAction(float time, Event event) {
		if (protocolClient != null && isClientConnected) {
			protocolClient.sendByeMessage();
		}
		System.out.println("Closed connection. Requesting game shutdown. ");
		game.setState(Game.State.STOPPING);
	}

}
