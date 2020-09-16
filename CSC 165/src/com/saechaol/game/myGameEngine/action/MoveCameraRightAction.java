package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

import ray.rage.scene.Camera;

public class MoveCameraRightAction extends AbstractInputAction {

	private MyGame game;
	private Camera camera;
	public MoveCameraRightAction(MyGame g, Camera c) {
		game = g;
		camera = c;
	}
	
	@Override
	public void performAction(float time, Event e) {
		if (game.camera.getMode() == 'c') {
			System.out.println("Camera mode right");
			Vector3f vel = camera.getRt();
			Vector3f position = camera.getPo();
			Vector3f pointOne = (Vector3f) Vector3f.createFrom((0.05f * vel.x()), (0.05f * vel.y()), (0.05f * vel.z()));
			Vector3f pointTwo = (Vector3f) position.add(pointOne);
			camera.setPo( (Vector3f) Vector3f.createFrom(pointTwo.x(), pointTwo.y(), pointTwo.z()));
		
		} else {
			System.out.println("Node mode right");
			game.dolphinNode.moveRight(0.05f);
		}
	}

}
