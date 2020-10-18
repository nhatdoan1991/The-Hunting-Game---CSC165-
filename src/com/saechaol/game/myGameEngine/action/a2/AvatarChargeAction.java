package com.saechaol.game.myGameEngine.action.a2;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarChargeAction extends AbstractInputAction {

	private MyGame game;
	private String player;
	
	public AvatarChargeAction(MyGame g, String p) {
		game = g;
		player = p;
	}
	
	@Override
	public void performAction(float time, Event e) {
		int timeSeconds = Math.round(time / 1000.0f);
		switch (player) {
		case "dolphinEntityOneNode":
			if (game.playerCharge.get(game.dolphinNodeOne)) {
				System.out.println("Player One already charged!");
			} else if (game.cooldownP1 < timeSeconds && !game.playerCharge.get(game.dolphinNodeOne)) {
				game.chargeTimeP1 = timeSeconds + 3;
				game.playerCharge.put(game.dolphinNodeOne, true);
				game.addToStretchController(game.dolphinNodeOne);
				game.cooldownP1 = timeSeconds + 13;
			} else {
				System.out.println("In cooldown!");
			}
			break;
		case "dolphinEntityTwoNode":
			if (game.playerCharge.get(game.dolphinNodeTwo)) {
				System.out.println("Player two already charged!");
			} else if (game.cooldownP2 < timeSeconds && !game.playerCharge.get(game.dolphinNodeTwo)) {
				game.chargeTimeP2 = timeSeconds + 3;
				game.playerCharge.put(game.dolphinNodeTwo, true);
				game.addToStretchController(game.dolphinNodeTwo);
				game.cooldownP2 = timeSeconds + 13;
			} else {
				System.out.println("In cooldown!");
			}
			break;
		}
	}

	
	
}
