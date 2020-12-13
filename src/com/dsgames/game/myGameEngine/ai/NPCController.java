package com.dsgames.game.myGameEngine.ai;

import java.util.Random;
import java.io.IOException;
import java.lang.*;
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
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class NPCController {

	private int numberofDolphin = 20, numberofMonster = 20, numberofSnitch = 1, numberofBoss = 1;
	private Npc[] npcs = new Npc[numberofDolphin + numberofMonster + numberofSnitch + numberofBoss];
	Random rd = new Random();

	BehaviorTree[] behaviorTreeDolphin = { new BehaviorTree(BTCompositeType.SELECTOR) };
	BehaviorTree[] behaviorTreeMonster = { new BehaviorTree(BTCompositeType.SELECTOR) };
	BehaviorTree[] behaviorTreeSnitch = { new BehaviorTree(BTCompositeType.SELECTOR) };
	BehaviorTree[] behaviorTreeBoss = { new BehaviorTree(BTCompositeType.SELECTOR) };
	private float thinkStartTime, tickStateTime, lastThinkUpdateTime, lastTickUpdateTime;
	private HuntingGame game;
	private ProtocolClient protocolClient;
	private SceneNode[] npcNodes;
	private PhysicsObject[] npcPhysicsObjects;
	private int host;

	public NPCController(HuntingGame g, ProtocolClient client, SceneNode[] nodes, PhysicsObject[] objects,
			int hostStatus) {
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

	public void setupBehaviorTreeBoss(Npc n, BehaviorTree tree) {
		tree.insertAtRoot(new BTSequence(10));
		tree.insert(10, new CheckClosestPlayer(n, false));
		tree.insert(10, new BossUltimateSkill(n));
	}

	public void setupBehaviorTree(Npc n, BehaviorTree tree) {
		tree.insertAtRoot(new BTSequence(10));
		tree.insertAtRoot(new BTSequence(20));
		tree.insert(10, new CheckFurthestPlayer(n, false));
		tree.insert(10, new GoHomeAction(n));
		tree.insert(20, new CheckClosestPlayer(n, false));
		tree.insert(20, new FollowPlayerAction(n));

	}

	public void setupBehaviorTreeSnitch(Npc n, BehaviorTree tree) {
		tree.insertAtRoot(new BTSequence(10));
		tree.insertAtRoot(new BTSequence(20));
		tree.insert(10, new CheckFurthest(n, false));
		tree.insert(10, new FlyRanDomDirection(n));
		tree.insert(20, new CheckClosest(n, false));
		tree.insert(20, new FlyAway(n));
	}

	public void setupBehaviorTreeMonster(Npc m, BehaviorTree tree) {
		// tree.insertAtRoot(new BTSequence(5));
		tree.insertAtRoot(new BTSequence(10));
		tree.insertAtRoot(new BTSequence(20));
		tree.insertAtRoot(new BTSequence(30));
		tree.insert(10, new CheckInitMove(m, false));
		tree.insert(10, new NpcWalkAround(m));
		tree.insert(20, new CheckFurthestPlayer(m, false));
		tree.insert(20, new GoHomeAction(m));
		tree.insert(30, new CheckClosestPlayer(m, false));
		tree.insert(30, new FollowPlayerAction(m));
		tree.insert(30, new CheckClosestPlayer(m, false));
		tree.insert(30, new ShotPlayer(m));
	}

	public void updateNPCs() {
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null) {

				npcs[i].updateLocation();
				if (distanceBetween(game.getPlayerPosition(), npcs[i].getNpcLocation()) < 1.0f) {
					game.playSoundEffect();
				}
				if (npcs[i] instanceof Dolphin) {
					npcs[i].walkAround();
				}
				if (npcs[i] instanceof Monster) {
					npcs[i].delay(5000f);
					npcs[i].walkAround();
				}
				if (npcs[i] instanceof Snitch) {
					npcs[i].delay((float) rd.nextInt(5000) + 3000);
				}
				if (npcs[i] instanceof Boss) {
					npcs[i].delay((float) rd.nextInt(5000) + 10000);
					npcs[i].walkAround();
				}
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
		int index = 0;
		for (int i = 0; i < numberofDolphin; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new Dolphin(npcNodes[i], npcPhysicsObjects[i], i);
				npcs[i].walkAround();
				setupBehaviorTree(npcs[i], behaviorTreeDolphin[0]);
				index++;
			}
		}
		for (int i = index; i < numberofDolphin + numberofMonster; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new Monster(npcNodes[i], npcPhysicsObjects[i], i);
				npcs[i].walkAround();
				setupBehaviorTreeMonster(npcs[i], behaviorTreeMonster[0]);
				index++;

			}
		}
		for (int i = index; i < numberofDolphin + numberofMonster + numberofSnitch; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new Snitch(npcNodes[i], npcPhysicsObjects[i], i);
				setupBehaviorTreeSnitch(npcs[i], behaviorTreeSnitch[0]);
				index++;
			}
		}
		for (int i = index; i < numberofDolphin + numberofMonster + numberofSnitch + numberofBoss; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new Boss(npcNodes[i], npcPhysicsObjects[i], i);
				setupBehaviorTreeBoss(npcs[i], behaviorTreeBoss[0]);
				index++;
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
			behaviorTreeDolphin[0].update(game.getElapsedTime());
			behaviorTreeMonster[0].update(game.getElapsedTime());
			behaviorTreeSnitch[0].update(game.getElapsedTime());
			behaviorTreeBoss[0].update(game.getElapsedTime());
		}

		Thread.yield();
	}

	private float distanceBetween(Vector3 p1, Vector3 p2) {
		return (float) Math
				.sqrt(Math.pow(p1.x() - p2.x(), 2) + Math.pow(p1.y() - p2.y(), 2) + Math.pow(p1.z() - p2.z(), 2));
	}

	public class Dolphin extends Npc {
		public Dolphin(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f,
					super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f);
			super.target = super.origin;
		}

	}

	public class Snitch extends Npc {

		public Snitch(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f,
					super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f);
			super.target = super.origin;
		}

		public void flyAwayFromPlayer() {
			Vector3 npcLocation = super.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			int closestPlayerIndex = super.closestPlayer();
			System.out.println("flying away from player " + closestPlayerIndex);
			Vector3 position = players.get(closestPlayerIndex).getLocalPosition();
			float deltaX = position.x() - npcLocation.x();
			float deltaY = position.y() - npcLocation.y();
			float deltaZ = position.z() - npcLocation.z();
			super.target = Vector3f.createFrom(npcLocation.x() - deltaX, npcLocation.y() - deltaY,
					npcLocation.z() - deltaZ);
			super.chasing = true;
			super.active = true;
		}

		public void flyRandomDirection() {

			if (super.getIsDelayed() == false) {
				Vector3 npcLocation = super.getNpcLocation();
				float radius = 20f;
				float random = rd.nextFloat() * 40f - 20.0f;
				float circleX = random + npcLocation.x();
				float circleZ = 0;
				if (rd.nextInt(2) == 0) {
					circleZ = (float) (npcLocation.z()
							+ Math.sqrt(Math.pow(radius, 2) - Math.pow(circleX - npcLocation.x(), 2)));
				} else {
					circleZ = (float) (npcLocation.z()
							- Math.sqrt(Math.pow(radius, 2) - Math.pow(circleX - npcLocation.x(), 2)));
				}

				super.target = Vector3f.createFrom(circleX, npcLocation.y(), circleZ);
				this.setIsDelayed(true);
			}
		}
	}

	public class Boss extends Npc {

		public Boss(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f,
					super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f);
			super.target = super.origin;
		}

		public void ultimateSkill() throws IOException {
			if (getIsDelayed() == false) {
				int count = 0;
				while (count < 18) {
					super.npcSceneNode.yaw(Degreef.createFrom(20.0f));
					Vector3 target = Vector3f.createFrom(
							super.npcSceneNode.getLocalPosition().x()
									+ super.npcSceneNode.getLocalForwardAxis().x() * 10f,
							super.npcSceneNode.getLocalPosition().y(), super.npcSceneNode.getLocalPosition().z()
									+ super.npcSceneNode.getLocalForwardAxis().z() * 10f);
					game.npcFireBullet(super.npcSceneNode, target);
					count++;
					// System.out.println(count);
				}
				super.setIsDelayed(true);
				count = 0;
			}
		}

	}

	public class Monster extends Npc {

		public Monster(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f,
					super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f);
			super.target = super.origin;
		}

		public void shotPlayer() throws IOException {
		//	System.out.println("shot player test");
			Vector3 npcLocation = super.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			float d = 20.0f;
			SceneNode targetToShot = players.get(0);
			float minDistance = 101f;
			int targetIndex = 0;
			for (int i = 0; i < players.size(); i++) {
				Vector3 position = players.get(i).getLocalPosition();
				float distance = HuntingGame.distanceFrom((Vector3f) npcLocation, (Vector3f) position);
				// System.out.println("distance player"+distance);
				if (distance < minDistance) {
					minDistance = distance;
					targetIndex = i;
				}
			}
			if (minDistance < d) {
				targetToShot = players.get(targetIndex);
				if (super.getIsDelayed() == false) {
					game.npcFireBullet(super.npcSceneNode, targetToShot.getLocalPosition());
					super.setIsDelayed(true);
				}
			}

		}
	}

	abstract public class Npc {

		double[] npcTransform;
		private SceneNode npcSceneNode;
		private PhysicsObject npcPhysicsObject;
		private Vector3 origin;
		private Vector3 target;
		private int npcId;
		private boolean chasing;
		private boolean active;
		private boolean isDelayed = false, isDelaying = false, isWalking = true, initMove = false;
		private float delayedTime = 0;

		public Npc(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			this.npcSceneNode = sceneNode;
			this.npcPhysicsObject = physicsObject;
			this.npcId = id;
			this.npcTransform = physicsObject.getTransform();
			origin = Vector3f.createFrom(npcSceneNode.getLocalPosition().x() + 0.3f,
					npcSceneNode.getLocalPosition().y() - 0.3f, npcSceneNode.getLocalPosition().z() + 0.3f);

			target = origin;
		}

		public void setIsWalking(boolean b) {
			this.isWalking = b;
		}

		public boolean getIsWalking() {
			return this.isWalking;
		}

		public void setIsDelayed(boolean b) {
			this.isDelayed = b;
		}

		public boolean getIsDelayed() {
			return this.isDelayed;
		}

		public void setIsDelaying(boolean b) {
			this.isDelaying = b;
		}

		public boolean getIsDelaying() {
			return this.isDelaying;
		}

		public void setDelayedTime(float b) {
			this.delayedTime = b;
		}

		public float getDelayedTime() {
			return this.delayedTime;
		}

		public boolean getInitMove() {
			return this.initMove;
		}

		public void setInitMove(boolean b) {
			this.initMove = b;
		}

		public void walkAround() {
			if (Math.round(game.getElapsedTime() / 1000) % (rd.nextInt(5) + 8) == 0) {
				Vector3 npcLocation = getNpcLocation();
				float radius = 100f;
				float random = rd.nextFloat() * 200f - 100.0f;
				float circleX = random + npcLocation.x();
				float circleZ = 0;
				if (rd.nextInt(2) == 0) {
					circleZ = (float) (npcLocation.z()
							+ Math.sqrt(Math.pow(radius, 2) - Math.pow(circleX - npcLocation.x(), 2)));
				} else {
					circleZ = (float) (npcLocation.z()
							- Math.sqrt(Math.pow(radius, 2) - Math.pow(circleX - npcLocation.x(), 2)));
				}

				target = Vector3f.createFrom(circleX, npcLocation.y(), circleZ);
				this.setInitMove(true);
			}

		}

		public void delay(float delayTime) {
			if (getIsDelaying() == true) {
				if (getDelayedTime() <= game.getElapsedTime() - delayTime) {
					setIsDelaying(false);
					setIsDelayed(false);
				}
			}
			if (getIsDelayed() == true && getIsDelaying() == false) {

				setDelayedTime(game.getElapsedTime());
				setIsDelaying(true);
			}
		}

		public int closestPlayer() {
			int targetIndex = 0;
			Vector3 npcLocation = getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			float d = 20.0f;
			SceneNode targetToShot = players.get(0);
			float minDistance = 101f;
			for (int i = 0; i < players.size(); i++) {
				Vector3 position = players.get(i).getLocalPosition();
				float distance = HuntingGame.distanceFrom((Vector3f) npcLocation, (Vector3f) position);
				// System.out.println("distance player"+distance);
				if (distance < minDistance) {
					minDistance = distance;
					targetIndex = i;
				}
			}
			return targetIndex;
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
				float[] playerDirection = { npcSceneNode.getLocalForwardAxis().x() * 12.0f, 0.0f,
						npcSceneNode.getLocalForwardAxis().z() * 12.0f };
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

	public class CheckInitMove extends BTCondition {

		private Npc npc;

		public CheckInitMove(Npc n, boolean toNegate) {
			super(toNegate);
			npc = n;
		}

		@Override
		protected boolean check() {
			// This must be false at first

			if (npc.getInitMove() == false) {
				return true;
			} else {
				return false;
			}
		}

	}

	public class CheckTooFarFromOrigin extends BTCondition {

		private Npc npc;

		public CheckTooFarFromOrigin(Npc n, boolean toNegate) {
			super(toNegate);
			npc = n;
		}

		@Override
		protected boolean check() {
			boolean isTooFar = false;

			float distanceFromOrigin = npc.distanceToOrigin();
			if (distanceFromOrigin > 30.0f) {
				isTooFar = true;
			}

			return isTooFar;
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

	public class CheckClosest extends BTCondition {

		private Npc npc;

		public CheckClosest(Npc n, boolean toNegate) {
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
					if (distanceFromPlayer < 20.0f) {
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

	public class CheckFurthest extends BTCondition {

		private Npc npc;

		public CheckFurthest(Npc n, boolean toNegate) {
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
					if (distanceFromPlayer > 20.0f) {
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

	public class ShotPlayer extends BTAction {

		private Npc m;

		public ShotPlayer(Npc m) {
			this.m = m;
		}

		@Override
		protected BTStatus update(float elapsedTime) {
			if (m instanceof Monster) {
				try {
					((Monster) m).shotPlayer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return BTStatus.BH_SUCCESS;
		}

	}

	public class FlyAway extends BTAction {

		private Npc m;

		public FlyAway(Npc m) {
			this.m = m;
		}

		@Override
		protected BTStatus update(float arg0) {
			if (m instanceof Snitch) {
				((Snitch) m).flyAwayFromPlayer();
			}

			return null;
		}

	}

	public class FlyRanDomDirection extends BTAction {

		private Npc m;

		public FlyRanDomDirection(Npc m) {
			this.m = m;
		}

		@Override
		protected BTStatus update(float arg0) {
			if (m instanceof Snitch) {
				((Snitch) m).flyRandomDirection();
			}

			return null;
		}

	}

	public class BossUltimateSkill extends BTAction {

		private Npc m;

		public BossUltimateSkill(Npc m) {
			this.m = m;
		}

		@Override
		protected BTStatus update(float arg0) {
			if (m instanceof Boss) {
				try {
					((Boss) m).ultimateSkill();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

	}

	public class NpcWalkAround extends BTAction {

		private Npc m;

		public NpcWalkAround(Npc m) {
			this.m = m;
		}

		@Override
		protected BTStatus update(float arg0) {
			m.walkAround();
			return null;
		}

	}

}