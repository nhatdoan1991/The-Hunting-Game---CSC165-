package com.saechaol.game.myGameEngine.ai;

import java.util.Vector;

import com.dsgames.game.hunt.HuntingGame;
import com.dsgames.game.myGameEngine.network.ProtocolClient;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTCondition;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BehaviorTree;
import ray.physics.PhysicsObject;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class NPCController {

	private Npc[] npcs = new Npc[3];
	BehaviorTree[] behaviorTree = {
		new BehaviorTree(BTCompositeType.SELECTOR)	
	};
	private float thinkStartTime, tickStateTime, lastThinkUpdateTime, lastTickUpdateTime;
	private HuntingGame game; 
	private ProtocolClient protocolClient;
	private SceneNode[] npcNodes;
	private PhysicsObject[] npcPhysicsObjects;
	private int host;
	
	public NPCController(HuntingGame g, ProtocolClient client, SceneNode[] nodes, PhysicsObject[] objects, int hostStatus) {
		this.game = g;
		this.protocolClient = client;
		this.npcNodes = nodes;
		this.npcPhysicsObjects = objects;
		if (host < 1) {
			startHost();
		} else {
			startClient();
		}
	}
	
	public void setupBehaviorTree(Npc n, BehaviorTree tree) {
		tree.insertAtRoot(new BTSequence(10));
		tree.insertAtRoot(new BTSequence(20));
		tree.insert(10, new CheckFurthestPlayer(n, false));
		tree.insert(10, new GoHomeAction(n));
		tree.insert(20, new CheckClosestPlayer(n, false));
		tree.insert(20, new FollowPlayerAction(n));
		
	}
	
	public void updateNPCs() {
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null) {
				npcs[i].updateLocation();
				if(distanceBetween(game.getPlayerPosition(),npcs[i].getNpcLocation())< 1.0f)
				{
					game.playSoundEffect();
				}
				//setupBehaviorTree(npcs[i], behaviorTree[0]);
			}
		}
	}
	
	public void startHost() {
		thinkStartTime = System.nanoTime();
		tickStateTime = System.nanoTime();
		lastThinkUpdateTime = thinkStartTime;
		lastTickUpdateTime = tickStateTime;
		setupNPC();
	}
	
	public void startClient() {
		setupNPC();
	}
	
	public void setupNPC() {
		for (int i = 0; i < npcs.length; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new Npc(npcNodes[i], npcPhysicsObjects[i], i);
				setupBehaviorTree(npcs[i], behaviorTree[0]);
			}
		}
	}
	
	public Npc getNpc(int index) {
		return npcs[index];
	}
	
	public void runNpcLoop() {
		long currentTime = System.nanoTime();
		float elapsedThinkMilliseconds = (currentTime - lastThinkUpdateTime) / (1000000.0f);
		float elapsedTickMilliseconds = (currentTime - lastTickUpdateTime) / (1000000.0f);
		
		if (elapsedTickMilliseconds >= 50.0f) {
			lastTickUpdateTime = currentTime;
			updateNPCs();
		}
		
		if (elapsedThinkMilliseconds >= 500.0f) {
			lastThinkUpdateTime = currentTime;
			behaviorTree[0].update(game.getElapsedTime());
		}
		
		Thread.yield();
	}
	
	private float distanceBetween(Vector3 p1, Vector3 p2) {
		return (float) Math.sqrt(
				Math.pow(p1.x() - p2.x(), 2) + 
				Math.pow(p1.y() - p2.y(), 2) + 
				Math.pow(p1.z() - p2.z(), 2)
		);
	}
	
	public class Npc {
		
		double[] npcTransform;
		private SceneNode npcSceneNode;
		private PhysicsObject npcPhysicsObject;
		private Vector3 origin;
		private Vector3 target;
		private int npcId;
		private boolean chasing;
		private boolean active;
		
		public Npc(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			this.npcSceneNode = sceneNode;
			this.npcPhysicsObject = physicsObject;
			this.npcId = id;
			this.npcTransform = physicsObject.getTransform();
			origin = Vector3f.createFrom(npcSceneNode.getLocalPosition().x() + 0.3f, npcSceneNode.getLocalPosition().y() - 0.3f, npcSceneNode.getLocalPosition().z() + 0.3f);
	 		   
			target = origin;
		}
		
		public double[] getNpcTransform() {
			return this.npcTransform;
		}
		
		public Vector3 getNpcLocation() {
			return this.npcSceneNode.getLocalPosition();
		}
		
		public SceneNode getNpcSceneNode() {
			return this.npcSceneNode;
		}
		
		public float distanceToOrigin() {
			return distanceBetween(npcSceneNode.getLocalPosition(), origin);
		}
		
		public void updateLocation() {
			if (distanceBetween(getNpcLocation(), target) > 7.0f) {
				npcSceneNode.lookAt(Vector3f.createFrom(target.x(), target.y(), target.z()));
				float[] playerDirection = {
						npcSceneNode.getLocalForwardAxis().x() * 12.0f,
						0.0f,
						npcSceneNode.getLocalForwardAxis().z() * 12.0f
				};
				npcPhysicsObject.setLinearVelocity(playerDirection);
			}
			npcTransform = npcPhysicsObject.getTransform();
			protocolClient.sendNpcPositionMessage(npcTransform, npcId, target);
		}
		
		public void followPlayer() {
			Vector3 npcLocation = getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			float d = 40.0f;
			SceneNode toFollow = players.get(0);
		
			for (int i = 0; i < players.size(); i++) {
				if (players.size() > 1) {
					Vector3 position = players.get(i).getLocalPosition();
					float distance = HuntingGame.distanceFrom((Vector3f) npcLocation, (Vector3f) position);
					if (distance < d) {
						toFollow = players.get(i);
						d = distance;
					}
				}
			}
			
			target = toFollow.getLocalPosition();
			chasing = true;
			active = true;
		}
		
		public void goHome() {
			target = origin;
			chasing = false;
		}

		public void setTransform(double[] transform) {
			npcTransform = transform;
			npcPhysicsObject.setTransform(transform);
			
		}
		
		
	}
	
	public class CheckClosestPlayer extends BTCondition {
		
		private Npc npc;
		
		public CheckClosestPlayer(Npc n, boolean toNegate) {
			super(toNegate);
			npc = n;
		}

		@Override
		protected boolean check() {
			boolean isPlayerClose = false;
			
			Vector3 npcLocation = npc.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			
			if (players != null) {
				for (SceneNode player : players) {
					Vector3 playerPosition = player.getLocalPosition();
					float distanceFromPlayer = distanceBetween((Vector3f) npcLocation, (Vector3f) playerPosition);
					float distanceFromOrigin = npc.distanceToOrigin();
					if (distanceFromPlayer < 20.0f && distanceFromOrigin < 30.0f) {
						isPlayerClose = true;
					}
				}
			}
			
			return isPlayerClose;
		}
		
	}
	
	public class CheckFurthestPlayer extends BTCondition {

		private Npc npc;
		
		public CheckFurthestPlayer(Npc n, boolean toNegate) {
			super(toNegate);
			npc = n;
		}

		@Override
		protected boolean check() {
			boolean isPlayerClose = false;
			
			Vector3 npcLocation = npc.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			
			if (players != null) {
				for (SceneNode player : players) {
					Vector3 playerPosition = player.getLocalPosition();
					float distanceFromPlayer = distanceBetween((Vector3f) npcLocation, (Vector3f) playerPosition);
					float distanceFromOrigin = npc.distanceToOrigin();
					if (distanceFromPlayer > 20.0f || distanceFromOrigin > 30.0f) {
						isPlayerClose = true;
					}
				}
			}
			
			return isPlayerClose;
		}
		
	}
	
	public class FollowPlayerAction extends BTAction {

		private Npc npc;
		public FollowPlayerAction(Npc n) {
			npc = n;
		}
		
		@Override
		protected BTStatus update(float elapsedTime) {
			npc.followPlayer();
			return BTStatus.BH_SUCCESS;
		}
		
	}
	
	public class GoHomeAction extends BTAction {

		private Npc npc;
		public GoHomeAction(Npc n) {
			npc = n;
		}
		
		@Override
		protected BTStatus update(float elapsedTime) {
			npc.goHome();
			return BTStatus.BH_SUCCESS;
		}
		
	}
	
}
