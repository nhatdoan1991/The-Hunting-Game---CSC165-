package com.saechaol.game.a1;

/**
 * A RAGE game in which you crash a dolphin through planets in space.
 * 
 * @author Lucas Saechao
 */

import java.awt.*;
import java.io.*;

//import com.saechaol.game.myGameEngine.*;

import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;

public class MyGame extends VariableFrameRateGame {
	
	private InputManager inputManager;
	private Action moveCameraUpAction, moveCameraDownAction, moveCameraLeftAction, moveCameraRightAction, pitchCameraUpAction, pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction, pauseGameAction, incrementCounterAction, incrementCounterModifierAction;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	float elapsedTime = 0.0f;
	String elapsedTimeString, counterString, displayString;
	int elapsedTimeSeconds, counter = 0;
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'ESC' or 'Start' to EXIT");
		System.out.println("Press 'TAB' or 'Y' to PAUSE");
		System.out.println("Press 'C' or 'X' to INCREMENT COUNTER");
		System.out.println("Press 'V' or 'DPAD-UP' to INCREMENT COUNTER MODIFIER");
		System.out.println("----------------------------------------------------");
	}

	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		//TODO Auto-generated method stub
	}
	
	@Override
	protected void setupCameras(SceneManager arg0, RenderWindow arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setupScene(Engine arg0, SceneManager arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(Engine arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		System.out.println("MyGame.main() running!");
	}

}
