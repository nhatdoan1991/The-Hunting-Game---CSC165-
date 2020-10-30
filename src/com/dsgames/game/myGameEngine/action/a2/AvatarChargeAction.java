package com.dsgames.game.myGameEngine.action.a2;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarChargeAction extends AbstractInputAction {

	private HuntingGame game;
	private String player;
	
	public AvatarChargeAction(HuntingGame myGame, String p) {
		game = myGame;
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
		}
	}

	
	
}
