package com.dsgames.game.myGameEngine.network;

import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;

public class NetworkServer {

	private UDPServer udpServer;
	
	public NetworkServer(int serverPort, String protocol) {
		try {
			udpServer = new UDPServer(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length > 1) {
			NetworkServer application = new NetworkServer(Integer.parseInt(args[0]), args[1]);
			System.out.println("NetworkServer instantiated!");
			System.out.println("Serving over port: " + args[0]);
			System.out.println("Using protocol: " + args[1]);
		} else {
			System.out.println("No arguments!");
		}
	}
}
