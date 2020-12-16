package com.dsgames.game.myGameEngine.entities;

import java.util.UUID;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class Bullet {

	private UUID id;
	private String source;
	private Vector3 origin,target;
	private SceneNode bulletNode;
	
	public Bullet(UUID id, String  source, Vector3 origin, Vector3 target, SceneNode bulletNode) {
		this.id = id;
		this.source = source;
		this.origin = origin;
		this.target = target;
		this.bulletNode = bulletNode;
	}
	
	public String getName() {
		return source+ id;
	}
	
	public UUID getId() {
		return id;
	}
	
	public Vector3 getOrigin () 
	{
		return origin;
	}
	public Vector3 getTarger() {
		return target;
	}
	public Vector3 getLocation()
	{
		return bulletNode.getLocalPosition();
	}
	public void SetLocation(Vector3 newLocation) {
		this.bulletNode.setLocalPosition(newLocation);
	}
	
	
}

