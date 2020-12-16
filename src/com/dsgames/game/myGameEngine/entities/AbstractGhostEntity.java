package com.dsgames.game.myGameEngine.entities;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public abstract class AbstractGhostEntity {

	private UUID id;
	private SceneNode node;
	private Vector3 position;
	private Entity entity;
	
	public AbstractGhostEntity(UUID id, Vector3 position) {
		this.id = id;
		this.position = position;
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public Node getNode() {
		return node;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	public Entity getEntity() {
		return this.entity;
	}
	
	public void setNode(SceneNode ghostNode) {
		this.node = ghostNode;
	}

	
	
	public void setEntity(Entity ghostEntity) {
		this.entity = ghostEntity;
		
	}
	

	public void setPosition(Vector3 position) {
		this.position = position;
	}
	
}
