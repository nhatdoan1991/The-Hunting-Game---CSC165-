package com.saechaol.game.myGameEngine.player;

import ray.physics.PhysicsObject;
import ray.rage.Engine;
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SceneObject;
import ray.rml.Angle;
import ray.rml.Matrix3;
import ray.rml.Matrix4;
import ray.rml.Vector3;

public class Player {

	private Engine engine;
	private SceneManager sceneManager;
	
	public Player (Engine e, SceneManager s) {
		engine = e;
		sceneManager = s;
	}
}
