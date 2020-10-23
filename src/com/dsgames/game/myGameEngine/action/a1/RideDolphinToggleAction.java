package com.dsgames.game.myGameEngine.action.a1;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Vector3f;

/**
 * An action handler that allows the player to mount and dismount the dolphin. 
 * The player's position will always be relative to, and nearby the dolphin.
 * @author Lucas
 *
 */

public class RideDolphinToggleAction extends AbstractInputAction {

	private MyGame game;
	public RideDolphinToggleAction(MyGame g) {
		game = g;
	}
	
	public void manualAction() {
		if (game.toggleRide) {
			System.out.println("Dolphin Camera On");
			game.toggleRide = false;
			game.camera.setMode('n');
		} else {
			System.out.println("Dolphin Camera Off");
			if (game.thirdPerson) {
				game.reinitializeInputs();
			}
			game.toggleRide = true;
			game.camera.setMode('c');
			dismountDolphin();
		}
	}
	
	@Override
	public void performAction(float time, Event e) {
		if (game.toggleRide) {
			System.out.println("Dolphin Camera On");
			game.toggleRide = false;
			game.camera.setMode('n');
		} else {
			System.out.println("Dolphin Camera Off");
			if (game.thirdPerson) {
				game.reinitializeInputs();
			}
			game.toggleRide = true;
			game.camera.setMode('c');
			dismountDolphin();
		}
	}
	
	/**
	 * Handles dismounting of the dolphin
	 */
	private void dismountDolphin() {
		Vector3f dolphinNodePoint = (Vector3f) game.dolphinNode.getLocalPosition();
		Vector3f pointOne = (Vector3f) Vector3f.createFrom(0.30f, 0.30f, -0.35f);
		Vector3f pointTwo = (Vector3f) dolphinNodePoint.add(pointOne);
		game.camera.setPo(pointTwo);
	}
	
}
