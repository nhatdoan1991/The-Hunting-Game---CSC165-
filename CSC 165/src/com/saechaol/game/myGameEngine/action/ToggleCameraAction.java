package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;
import com.saechaol.game.myGameEngine.camera.Camera3PController;

import net.java.games.input.Event;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.Engine;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneManager;

public class ToggleCameraAction implements Action {

	private MyGame game;
	private Camera camera;
	private InputManager inputManager;
	
	public ToggleCameraAction(MyGame g, Camera c, InputManager im) {
		game = g;
		camera = c;
		inputManager = im;
	}
	
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
		}
	}
	
	private void setupOrbitCamera() {
		if (!game.isGamepadNull(inputManager.getFirstGamepadName())) {
			game.orbitCameraController = new Camera3PController(camera, game.cameraNode, game.dolphinNode, inputManager.getFirstGamepadName(), inputManager);
		}
		game.dolphinNode.detachChild(game.dolphinCamera);
		game.dolphinCamera.detachAllObjects();
		game.cameraNode.attachObject(camera);
	}
}