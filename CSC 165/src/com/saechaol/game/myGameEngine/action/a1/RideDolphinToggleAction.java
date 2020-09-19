package com.saechaol.game.myGameEngine.action.a1;

import ray.input.action.AbstractInputAction;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.rml.Vector3f;

public class RideDolphinToggleAction extends AbstractInputAction {

	private MyGame game;
	
	public RideDolphinToggleAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		if (game.toggleRide) {
			System.out.println("Dolphin Camera Off");
			game.toggleRide = false;
			game.camera.setMode('n');
		} else {
			System.out.println("Dolphin Camera On");
			game.toggleRide = true;
			game.camera.setMode('c');
			dismountDolphin();
		}
	}
	
	private void dismountDolphin() {
		Vector3f dolphinNodePoint = (Vector3f) game.dolphinNode.getLocalPosition();
		Vector3f pointOne = (Vector3f) Vector3f.createFrom(0.35f, -0.25f, 0.25f);
		Vector3f pointTwo = (Vector3f) dolphinNodePoint.add(pointOne);
		game.camera.setPo(pointTwo);
	}
	
}
