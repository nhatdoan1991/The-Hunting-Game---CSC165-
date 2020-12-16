package com.dsgames.game.myGameEngine.entities;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class GhostAvatar extends AbstractGhostEntity {

	
	public GhostAvatar(UUID id, Vector3 position) {
		super(id, position);
	}
	
	public SceneNode getSceneNode() {
		return (SceneNode) this.getNode();
	}
	
}
