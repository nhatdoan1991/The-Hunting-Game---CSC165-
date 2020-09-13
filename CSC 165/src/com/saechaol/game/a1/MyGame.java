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

import net.java.games.input.Controller;
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
	private Controller controller;
	private Action leftStickMoveAction, rightStickMoveAction, moveCameraDownAction, moveCameraLeftAction, moveCameraRightAction, moveCameraUpAction, pitchCameraUpAction, pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction, pauseGameAction, incrementCounterAction, incrementCounterModifierAction;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	float elapsedTime = 0.0f;
	String elapsedTimeString, counterString, displayString;
	int elapsedTimeSeconds, counter = 0;
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("Press 'TAB' or 'Start' to PAUSE");
		System.out.println("Press 'C' or 'X' to INCREMENT COUNTER");
		System.out.println("Press 'V' or 'Y' to INCREMENT COUNTER MODIFIER");
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

	/**
	 * Initializes the game's scene with a single dolphin entity, an ambient and point light,
	 * and rotates it about its Y axis
	 * 
	 * @param engine
	 * @param sceneManager
	 */
	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		// initialize input manager
		setupInputs();
		
		// initialize the dolphin entity
		Entity dolphinEntity = sceneManager.createEntity("dolphinEntity", "dolphinHighPoly.obj");
		dolphinEntity.setPrimitive(Primitive.TRIANGLES);
		
		// initialize the dolphin node and add it to the scene graph
		SceneNode dolphinNode = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntity.getName() + "Node");
		dolphinNode.moveBackward(2.0f);
		dolphinNode.attachObject(dolphinEntity);
		
		// initialize the ambient light
		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
		
		// initialize a point light
		Light pointLightOne = sceneManager.createLight("pointLightOne", Light.Type.POINT);
		pointLightOne.setAmbient(new Color(0.3f, 0.3f, 0.3f));
		pointLightOne.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightOne.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightOne.setRange(5.0f);
		
		// initialize a node for pointLightOne and add it to the scene graph
		SceneNode pointLightNode = sceneManager.getRootSceneNode().createChildSceneNode(pointLightOne.getName() + "Node");
		pointLightNode.attachObject(pointLightOne);
		
		// initialize a rotation controller
		//RotationController rotationController = new RotationController(Vector3f.createUnitVectorY(), 0.02f);
		//rotationController.addNode(dolphinNode);
		//sceneManager.addController(rotationController);
		
		// manually assign dolphin textures
		TextureManager textureManager = engine.getTextureManager();
		Texture redTexture = textureManager.getAssetByPath("red.jpeg");
		RenderSystem renderSystem = sceneManager.getRenderSystem();
		TextureState state = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		state.setTexture(redTexture);
		dolphinEntity.setRenderState(state);
	}
	
	/**
	 * Initializes controller inputs
	 */
	protected void setupInputs() {
		inputManager = new GenericInputManager();
		String keyboardName = inputManager.getKeyboardName();
		String gamepadName = inputManager.getFirstGamepadName();
		System.out.println("Keyboard: " + keyboardName + "\nGamepad: " + gamepadName);
		controller = inputManager.getControllerByName(gamepadName);
		
		// Build action objects for listening to user input
		exitGameAction = new ExitGameAction(this);
		incrementCounterModifierAction = new IncrementCounterModifierAction(this);
		incrementCounterAction = new IncrementCounterAction(this, (IncrementCounterModifierAction) incrementCounterModifierAction);
		leftStickMoveAction = new LeftStickMoveAction(this, controller);
		rightStickMoveAction = new RightStickMoveAction(this, controller);
		moveCameraUpAction = new MoveCameraUpAction(this);
		
		// Bind exit action to escape, and gamepad 6 (select)
		inputManager.associateAction(keyboardName, 
				net.java.games.input.Component.Identifier.Key.ESCAPE, 
				exitGameAction, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		// Bind increment counter action to C, and gamepad 2 (X)
		inputManager.associateAction(keyboardName, 
				net.java.games.input.Component.Identifier.Key.C, 
				incrementCounterAction, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		
		// Bind increment counter modifier action to V, and gamepad 3 (Y)
		inputManager.associateAction(keyboardName, 
				net.java.games.input.Component.Identifier.Key.V, 
				incrementCounterModifierAction, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);		

		
		// Bind move camera up action to W, and gamepad left stick Y
		inputManager.associateAction(keyboardName, 
				net.java.games.input.Component.Identifier.Key.W, 
				moveCameraUpAction, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);	
		
		if (isGamepadNull(gamepadName)) {
			System.out.println("No gamepad attached!");
		} else {
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._6, 
					exitGameAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._2, 
					incrementCounterAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._3, 
					incrementCounterModifierAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					moveCameraUpAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			// Poll data from the control sticks
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					leftStickMoveAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.RX, 
					rightStickMoveAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}
	}

	/**
	 * Updates and redraws the viewport with running elapsed time and keyboard count
	 * 
	 * @param engine
	 */
	@Override
	protected void update(Engine engine) {
		renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		elapsedTime += engine.getElapsedTimeMillis();
		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		counterString = Integer.toString(counter);
		displayString = "Time = " + elapsedTimeString + " Keyboard Counter = " + counterString;
		renderSystem.setHUD(displayString, 15, 15);
		inputManager.update(elapsedTime);
		
	}
	
	public void moveCameraForward() {
		
	}
	
	public void incrementCounter(int increment) {
		counter += increment;
	}
	
	private boolean isGamepadNull(String gamepadName) {
		if (gamepadName == null) {
			return true;
		} else 
			return false;
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
