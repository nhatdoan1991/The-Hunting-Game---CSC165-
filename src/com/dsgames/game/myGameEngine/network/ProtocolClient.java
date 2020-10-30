package com.dsgames.game.myGameEngine.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.dsgames.game.hunt.HuntingGame;

import ray.networking.client.GameConnectionClient;

public class ProtocolClient extends GameConnectionClient {

	public ProtocolClient(InetAddress remoteAddr, int remotePort, InetAddress localAddr, int localPort,
			ProtocolType protocolType) throws IOException {
		super(remoteAddr, remotePort, localAddr, localPort, protocolType);
		// TODO Auto-generated constructor stub
	}
	private HuntingGame game;
	private UUID id;
	private ConcurrentHashMap<GhostAvatar, Boolean> ghostAvatars;
	
	
	
}
