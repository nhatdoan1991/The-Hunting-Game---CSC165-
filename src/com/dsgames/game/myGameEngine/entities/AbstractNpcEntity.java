package com.dsgames.game.myGameEngine.entities;


import ray.physics.PhysicsObject;
import ray.rage.scene.Entity;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class AbstractNpcEntity {
	private int id;
	private SceneNode node;
	private Vector3 position;
	private Entity entity;
	private PhysicsObject physicObject;
	
	public AbstractNpcEntity(int id, SceneNode node, Entity entity) {
		this.id = id;
		this.node =node;
		this.entity = entity;
	}
	
	public void setPhysicObject(PhysicsObject p) {
		this.physicObject = p;
	}
	public PhysicsObject getPhysicObject(){
		return this.physicObject;
	}
	public void setNode(SceneNode ghostNode) {
		this.node = ghostNode;
	}

	public void setEntity(Entity ghostEntity) {
		this.entity = ghostEntity;	
	}
	public Entity getEntity() {
		return this.entity;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	public void setPosition(Vector3 position) {
		this.position = position;
	}
	
	public int getId() {
		return this.id;
	}

	public Node getNode() {
		return node;
	}
}
