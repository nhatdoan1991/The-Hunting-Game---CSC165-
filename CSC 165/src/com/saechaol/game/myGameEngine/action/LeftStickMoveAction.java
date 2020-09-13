package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class LeftStickMoveAction extends AbstractInputAction {

	private MyGame game;
	private Controller controller;
	
	public LeftStickMoveAction(MyGame g, Controller c) {
		game = g;
		controller = c;
	}

	@Override
	public void performAction(float time, Event e) {
		Component leftStickX = controller.getComponent(net.java.games.input.Component.Identifier.Axis.X);
		Component leftStickY = controller.getComponent(net.java.games.input.Component.Identifier.Axis.Y);
		System.out.println("Left stick moved. \nLeft stick X: " + leftStickX.getPollData());
		System.out.println("Left stick Y: " + leftStickY.getPollData() + "\n");
	}
	
	
	
}
