package com.dsgames.game.myGameEngine.entities;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class GhostAvatar {

	private UUID id;
	private SceneNode node;
	private Vector3 position;
	private Entity entity;
	
	public GhostAvatar(UUID id, Vector3 position) {
		this.id = id;
		this.position = position;
	}
	
	public void setNode(SceneNode ghostNode) {
		this.node = ghostNode;
		
	}

	public void setEntity(Entity ghostEntity) {
		this.entity = ghostEntity;
		
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	public void setPosition(Vector3 position) {
		this.position = position;
	}
	
	public UUID getId() {
		return this.id;
	}
	
}
