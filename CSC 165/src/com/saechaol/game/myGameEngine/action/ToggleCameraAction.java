package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;
import com.saechaol.game.myGameEngine.camera.Camera3PController;

import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.scene.Camera;

/**
 * An action handler that switches camera control from 1P to 3P POV.
 * @author Lucas
 *
 */

public class ToggleCameraAction implements Action {

	private MyGame game;
	private Camera camera;
	private InputManager inputManager;
	
	public ToggleCameraAction(MyGame g, Camera c, InputManager im) {
		game = g;
		camera = c;
		inputManager = im;
	}
	
	/**
	 * Checks what mode the camera is in and either switches to or reverts from 3P camera mode
	 * 
	 * @param time
	 * @param e
	 */
	@Override
	public void performAction(float time, Event e) {

		if (game.thirdPerson) {
			game.reinitializeInputs();
		} else {
			if (camera.getMode() == 'n') {
				game.toggleCamera();
				setupOrbitCamera();
			} else {
				game.toggleRide = false;
				game.toggleCamera();
				camera.setMode('n');
				setupOrbitCamera();
			}
			printControls();
		}
	}
	
	/**
	 * Initializes the orbiting 3P camera and its controls
	 */
	private void setupOrbitCamera() {
		game.orbitCameraController = new Camera3PController(camera, game.cameraNode, game.dolphinNode, inputManager.getFirstGamepadName(), inputManager);
		game.dolphinNode.detachChild(game.dolphinCamera);
		game.dolphinCamera.detachAllObjects();
		game.cameraNode.attachObject(camera);
	}
	
	/**
	 * Prints controls to the console
	 */
	private void printControls() {
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ORBIT CAMERA");
		System.out.println("Press 'R/F' or the left and right triggers to ZOOM CAMERA");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'TAB' or 'Start' to TOGGLE 1P CAMERA");
		System.out.println("----------------------------------------------------");
	}
}
