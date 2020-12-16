package com.dsgames.game.myGameEngine.entities;

import java.util.UUID;

import ray.rml.Vector3;

public class GhostNpc extends AbstractGhostEntity {
	
	private EntityType type;
	
	public GhostNpc(UUID id, Vector3 position, EntityType entityType) {
		super(id, position);
		this.type = entityType;
	}
	
	public EntityType getEntityType() {
		return this.type;
	}
}
