package com.saechaol.game.myGameEngine.action;

/**
 * Polls the right stick for data
 */

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class RightStickMoveAction extends AbstractInputAction {

	private MyGame game;
	private Controller controller;
	
	public RightStickMoveAction(MyGame g, Controller c) {
		game = g;
		controller = c;
	}

	@Override
	public void performAction(float time, Event e) {
		Component rightStickX = controller.getComponent(net.java.games.input.Component.Identifier.Axis.RX);
		Component rightStickY = controller.getComponent(net.java.games.input.Component.Identifier.Axis.RY);
		System.out.println("Right stick moved. \nRight stick X: " + rightStickX.getPollData());
		System.out.println("Right stick Y: " + rightStickY.getPollData() + "\n");
	}
	
	
	
}
