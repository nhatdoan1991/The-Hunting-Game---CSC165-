package com.dsgames.game.myGameEngine.ai;

import java.util.Random;
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

	private Npc[] npcs = new Npc[6];
	private int numberofDolphin =3, numberofMonster =1,numberofSnitch =1,numberofBoss=1;
	Random rd = new Random();
	
	BehaviorTree[] behaviorTreeDolphin = {
		new BehaviorTree(BTCompositeType.SELECTOR)	
	};
	BehaviorTree[] behaviorTreeMonster = {
			new BehaviorTree(BTCompositeType.SELECTOR)	
	};
	BehaviorTree[] behaviorTreeSnitch = {
			new BehaviorTree(BTCompositeType.SELECTOR)	
	};
	BehaviorTree[] behaviorTreeBoss = {
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
	public void setupBehaviorTreeMonster(Npc m,BehaviorTree tree) {
		tree.insertAtRoot(new BTSequence(10));
		tree.insertAtRoot(new BTSequence(20));
		tree.insert(10, new CheckFurthestPlayer(m, false));
		tree.insert(10, new GoHomeAction(m));
		tree.insert(20, new CheckClosestPlayer(m, false));
		tree.insert(20, new FollowPlayerAction(m));
		tree.insert(20, new CheckClosestPlayer(m, false));
		tree.insert(20, new ShotPlayer(m));
	}
	public void updateNPCs() {
		for (int i = 0; i < npcs.length; i++) {
			if (npcs[i] != null) {
				npcs[i].updateLocation();
				if(distanceBetween(game.getPlayerPosition(),npcs[i].getNpcLocation())< 1.0f)
				{
					game.playSoundEffect();
				}
				if(npcs[i] instanceof monster)
				{
					if(((monster)npcs[i]).getIsRecharging()==true)
					{
						if(((monster)npcs[i]).getShotingTime() <= game.getElapsedTime()-5000)
						{
							((monster)npcs[i]).setIsShoting(false);
							((monster)npcs[i]).setIsRecharging(false);
						}
					}
					if(((monster)npcs[i]).getIsShoting()==true&&((monster)npcs[i]).getIsRecharging()==false )
					{
						
						((monster)npcs[i]).setShotingTime(game.getElapsedTime());
						((monster)npcs[i]).setIsRecharging(true);
					}
				}
				if(npcs[i] instanceof snitch)
				{
					if(((snitch)npcs[i]).getIsTurning()==true)
					{
						if(((snitch)npcs[i]).getTurnTime() <= game.getElapsedTime()-5000-7000*rd.nextFloat())
						{
							((snitch)npcs[i]).setIsTurning(false);
							((snitch)npcs[i]).setIsTurned(false);
						}
					}
					if(((snitch)npcs[i]).getIsTurned()==true&&((snitch)npcs[i]).getIsTurning()==false )
					{
						
						((snitch)npcs[i]).setTurnTime(game.getElapsedTime());
						((snitch)npcs[i]).setIsTurning(true);
					}
				}
				if(npcs[i] instanceof boss)
				{
					npcs[i].delay((float)rd.nextInt(5000)+10000);
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
		int index=0;
		for (int i=0; i < numberofDolphin; i++) {
			if (npcNodes[i] != null) {
				npcs[i] = new dolphin(npcNodes[i], npcPhysicsObjects[i], i);
				setupBehaviorTree(npcs[i], behaviorTreeDolphin[0]);
				index++;
			}
		}
		for (int i=index; i < numberofDolphin+numberofMonster; i++) {
			if (npcNodes[i] != null) {
					npcs[i]= new monster(npcNodes[i], npcPhysicsObjects[i], i);
					setupBehaviorTreeMonster(npcs[i], behaviorTreeMonster[0]);
				index++;
				
			}
		}
		for (int i=index; i < numberofDolphin+numberofMonster+numberofSnitch; i++) {
			if (npcNodes[i] != null) {
					npcs[i]= new snitch(npcNodes[i], npcPhysicsObjects[i], i);
					setupBehaviorTreeSnitch(npcs[i], behaviorTreeSnitch[0]);
				index++;
			}
		}for (int i=index; i < numberofDolphin+numberofMonster+numberofSnitch+numberofBoss; i++) {
			if (npcNodes[i] != null) {
				npcs[i]= new boss(npcNodes[i], npcPhysicsObjects[i], i);
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
		return (float) Math.sqrt(
				Math.pow(p1.x() - p2.x(), 2) + 
				Math.pow(p1.y() - p2.y(), 2) + 
				Math.pow(p1.z() - p2.z(), 2)
		);
	}
	public class dolphin extends Npc{
		

		public dolphin(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f, super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f);
	 		   
			super.target = super.origin;
		}
		
	}
	public class snitch extends Npc{
		private boolean isTurning=false,isTurned= false;
		private float turnTime=0f;
		public snitch(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f, super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f); 
			super.target = super.origin;
			// TODO Auto-generated constructor stub
		}
		public void setIsTurning(boolean b) {
			this.isTurning = b;
		}
		public boolean getIsTurning() {
			return this.isTurning;
		}
		public void setIsTurned(boolean b) {
			this.isTurned = b;
		}
		public boolean getIsTurned() {
			return this.isTurned;
		}
		public void setTurnTime(float b) {
			this.turnTime = b;
		}
		public float getTurnTime() {
			return this.turnTime;
		}
		public void flyAwayFromPlayer() {
			Vector3 npcLocation = super.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			int closestPlayerIndex = super.closestPlayer();
			System.out.println("flying away from player " + closestPlayerIndex);
			Vector3 position = players.get(closestPlayerIndex).getLocalPosition();
			float deltaX = position.x()-npcLocation.x();
			float deltaY = position.y()-npcLocation.y();
			float deltaZ = position.z()-npcLocation.z();
			super.target = Vector3f.createFrom(npcLocation.x()-deltaX,npcLocation.y()-deltaY,npcLocation.z()-deltaZ);
			super.chasing = true;
			super.active = true;	
		}	
		public void flyRanDomDirection() {

			if(this.isTurned==false)
			{
				Vector3 npcLocation = super.getNpcLocation();
				float radius = 20f;
				float random = rd.nextFloat()*40f-20.0f;
				float circleX = random+npcLocation.x();
				float circleZ = 0;
				if(rd.nextInt(2)==0) {
					circleZ = (float) (npcLocation.z()+Math.sqrt(Math.pow(radius,2) - Math.pow(circleX-npcLocation.x(),2)));
				}else
				{
					circleZ = (float) (npcLocation.z()-Math.sqrt(Math.pow(radius,2) - Math.pow(circleX-npcLocation.x(),2)));
				}
		
				super.target = Vector3f.createFrom(circleX,npcLocation.y(),circleZ);	
				this.setIsTurned(true);
			}
		}
	}
	public class boss extends Npc{

		public boss(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f, super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f); 
			super.target = super.origin;
		}
		
		public void ultimateSkill() {
			if(getIsDelayed()==false)
			{
				System.out.println("Ultimate Skill of Boss ");
				int count=0;
				while(count <36)
				{
					super.npcSceneNode.yaw(Degreef.createFrom(10.0f));
					count++;
					//System.out.println(count);
				}
				super.setIsDelayed(true);
				count=0;
			}	
		}
		
	}
	public class monster extends Npc{
		
		private boolean isShoting = false,isRecharging = false;
		private float shotingTime = 0;
		public monster(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			super(sceneNode, physicsObject, id);
			super.npcTransform = physicsObject.getTransform();
			super.origin = Vector3f.createFrom(super.npcSceneNode.getLocalPosition().x() + 0.3f, super.npcSceneNode.getLocalPosition().y() - 0.3f, super.npcSceneNode.getLocalPosition().z() + 0.3f); 
			super.target = super.origin;
		}
		public void shotPlayer(){
			Vector3 npcLocation = super.getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			float d = 20.0f;
			SceneNode targetToShot = players.get(0);
			float minDistance = 101f;
			int targetIndex = 0;
			for (int i = 0; i < players.size(); i++) {
					Vector3 position = players.get(i).getLocalPosition();
					float distance = HuntingGame.distanceFrom((Vector3f) npcLocation, (Vector3f) position);
					//System.out.println("distance player"+distance);
					if(distance < minDistance)
					{
						minDistance = distance;
						targetIndex = i;
					}		
			}
			if (minDistance < d) {
				targetToShot = players.get(targetIndex);
				if(isShoting==false)
				{
					System.out.println("shooting player " + targetIndex);
					setIsShoting(true);
				}
			}
			
		}
		public void setIsRecharging(boolean b) {
			this.isRecharging = b;
		}
		public boolean getIsRecharging() {
			return this.isRecharging;
		}
		public void setShotingTime(float time) {
			this.shotingTime = time;
		}
		public float getShotingTime() {
			return this.shotingTime;
		}
		public void setIsShoting(boolean b)
		{
			this.isShoting = b;	
		}
		public boolean getIsShoting()
		{
			return this.isShoting;	
		}
		public boolean checkDelayTime(float shotingTime) {
			return true;
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
		private boolean isDelayed = false,isDelaying = false;
		private float delayedTime = 0;
		
		
		public Npc(SceneNode sceneNode, PhysicsObject physicsObject, int id) {
			this.npcSceneNode = sceneNode;
			this.npcPhysicsObject = physicsObject;
			this.npcId = id;
			this.npcTransform = physicsObject.getTransform();
			origin = Vector3f.createFrom(npcSceneNode.getLocalPosition().x() + 0.3f, npcSceneNode.getLocalPosition().y() - 0.3f, npcSceneNode.getLocalPosition().z() + 0.3f);
	 		   
			target = origin;
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
		public void setDelayedTime(float b)
		{
			this.delayedTime = b;	
		}
		public float getDelayedTime()
		{
			return this.delayedTime;	
		}
		
		public void delay(float delayTime) {
			if(getIsDelaying()==true)
			{
				if(getDelayedTime() <= game.getElapsedTime()-delayTime)
				{
					setIsDelaying(false);
					setIsDelayed(false);
				}
			}
			if(getIsDelayed()==true&& getIsDelaying()==false )
			{
				
				setDelayedTime(game.getElapsedTime());
				setIsDelaying(true);
			}
		}
		public int closestPlayer() {
			int targetIndex=0;
			Vector3 npcLocation = getNpcLocation();
			Vector<SceneNode> players = game.getPlayers();
			float d = 20.0f;
			SceneNode targetToShot = players.get(0);
			float minDistance = 101f;
			for (int i = 0; i < players.size(); i++) {
					Vector3 position = players.get(i).getLocalPosition();
					float distance = HuntingGame.distanceFrom((Vector3f) npcLocation, (Vector3f) position);
					//System.out.println("distance player"+distance);
					if(distance < minDistance)
					{
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
			if(m instanceof monster)
			{
				((monster) m).shotPlayer();
			}
			
			return BTStatus.BH_SUCCESS;
		}
		
	}
	public class FlyAway extends BTAction{

		
		private Npc m;
		public FlyAway(Npc m) {
			this.m = m;
		}
		@Override
		protected BTStatus update(float arg0) {
			if(m instanceof snitch)
			{
				((snitch) m).flyAwayFromPlayer();
			}
			
			return null;
		}
		
	}
public class FlyRanDomDirection extends BTAction{

		
		private Npc m;
		public FlyRanDomDirection(Npc m) {
			this.m = m;
		}
		@Override
		protected BTStatus update(float arg0) {
			if(m instanceof snitch)
			{
				((snitch) m).flyRanDomDirection();
			}
			
			return null;
		}
		
	}
	
public class BossUltimateSkill extends BTAction{

	
	private Npc m;
	public BossUltimateSkill(Npc m) {
		this.m = m;
	}
	@Override
	protected BTStatus update(float arg0) {
		if(m instanceof boss)
		{
			((boss) m).ultimateSkill();
		}
		
		return null;
	}
	
}

}