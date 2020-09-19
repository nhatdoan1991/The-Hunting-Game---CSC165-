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

public class MoveCameraBackwardAction extends AbstractInputAction {

	private MyGame game;
	private Camera camera;
	
	public MoveCameraBackwardAction(MyGame g, Camera c) {
		game = g;
		camera = c;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		if (game.camera.getMode() == 'c') {
			System.out.println("Camera mode backward");
			Vector3f vel = camera.getFd();
			Vector3f position = camera.getPo();
			Vector3f pointOne = (Vector3f) Vector3f.createFrom((-speed * vel.x()), (-speed * vel.y()), (-speed * vel.z()));
			Vector3f pointTwo = (Vector3f) position.add(pointOne);
			camera.setPo( (Vector3f) Vector3f.createFrom(pointTwo.x(), pointTwo.y(), pointTwo.z()));
		
		} else {
			System.out.println("Node mode backward");
			game.dolphinNode.moveBackward(-speed);
		}
	}
	
	

}