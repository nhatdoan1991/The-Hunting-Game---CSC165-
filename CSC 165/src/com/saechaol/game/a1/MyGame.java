package com.saechaol.game.a1;

/**
 * A RAGE game in which you crash a dolphin through planets in space.
 * 
 * @author Lucas Saechao
 */

import java.awt.*;
import java.io.*;

import com.saechaol.game.myGameEngine.action.*;
import com.saechaol.game.myGameEngine.action.a1.*;

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

	/**
	 * Implements VariableFrameRateEngine.setupWindow()
	 * Initializes a window size 1920x1080 for displays larger than 1080p,
	 * and a 1280x720 window for displays smaller
	 * 
	 * @param renderSystem
	 * @param graphicsEnvironment
	 */
	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		int displayHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		int displayWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		if (displayHeight > 1920 && displayWidth > 1080)
			renderSystem.createRenderWindow(new DisplayMode(1920, 1080, 24, 60), false);
		else
			renderSystem.createRenderWindow(new DisplayMode(1280, 720, 24, 60), false);
	}
	
	/**
	 * Initializes a perspective camera and adds it to the scene graph
	 * 
	 * @param sceneManager
	 * @param renderWindow
	 */
	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		// initialize the camera and add it to the viewport
		SceneNode rootNode = sceneManager.getRootSceneNode();
		Camera cameraOne = sceneManager.createCamera("cameraOne", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(cameraOne);
		
		// initialize the camera frustum and set its position to the origin
		cameraOne.setRt( (Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		cameraOne.setUp( (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		cameraOne.setFd( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		cameraOne.setPo( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize the cameraNode, add it to the scene graph and then attach the camera to it
		SceneNode cameraNode = rootNode.createChildSceneNode(cameraOne.getName() + "Node");
		cameraNode.attachObject(cameraOne);
		
	}

	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	protected void setupInputs() {
		
	}

	@Override
	protected void update(Engine engine) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		System.out.println("MyGame.main() running!");
		Game game = new MyGame();
		try {
			game.startup();
			game.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			game.shutdown();
			game.exit();
		}
	}

}
