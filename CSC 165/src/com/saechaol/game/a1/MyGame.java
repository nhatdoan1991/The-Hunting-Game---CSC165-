package com.saechaol.game.a1;

/**
 * A RAGE game in which you crash a dolphin through planets in space.
 * 
 * @author Lucas Saechao
 */

import java.awt.*;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.saechaol.game.myGameEngine.action.*;
import com.saechaol.game.myGameEngine.action.a1.*;
import com.saechaol.game.myGameEngine.object.manual.*;

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
	private static final Random RAND = new Random();
	public Camera camera;
	public SceneNode cameraNode, dolphinNode, manualCubePlanetNode, planetZeroNode, planetOneNode, planetTwoNode;
	private Controller controller;
	private Action invertYawAction, rightStickXAction, rightStickYAction, leftStickXAction, leftStickYAction, moveCameraUpAction, moveCameraDownAction, moveCameraBackwardAction, moveCameraLeftAction, moveCameraRightAction, moveCameraForwardAction, pitchCameraUpAction, pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction, pauseGameAction;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	float elapsedTime = 0.0f;
	String elapsedTimeString, counterString, displayString, positionString, dolphinString;
	int elapsedTimeSeconds, counter, score = 0;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	public boolean toggleRide = false;
	public boolean invertYaw = true;
	public HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'V' or 'Y' to INVERT YAW");
		System.out.println("Press 'LSHIFT' or the left bumper to ASCEND");
		System.out.println("Press 'C' or the right bumper to DESCEND");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("Press 'TAB' or 'Start' to PAUSE");
		System.out.println("----------------------------------------------------");
		formatFloat.setRoundingMode(RoundingMode.DOWN);
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
		camera = sceneManager.createCamera("mainCamera", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(camera);
		camera.setMode('n');
		// initialize the camera frustum and set its position to the origin
		camera.setRt( (Vector3f) Vector3f.createFrom(-1.0f, 0.0f, 0.0f));
		camera.setUp( (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 1.0f));
		camera.setPo( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize the cameraNode, add it to the scene graph and then attach the camera to it
		cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
		cameraNode.attachObject(camera);
		
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
		
		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);
		
		// initialize planets
		Entity[] planetEntities = new Entity[4];
		for (int i = 0; i < planetEntities.length; i++) { 
			planetEntities[i] = sceneManager.createEntity("planet" + i, "earth.obj");
			planetEntities[i].setPrimitive(Primitive.TRIANGLES);
		}
		
		// randomized coordinates from <-20, -20, -20> to <20, 20, 20>
		float[][] randomPlanetCoordinates = {
			randomFloatArray(19.0f),
			randomFloatArray(19.0f),
			randomFloatArray(19.0f),
			randomFloatArray(19.0f)
		};
		
		for (int i = 0; i < randomPlanetCoordinates.length; i++) {
			for (int j = 0; j < randomPlanetCoordinates[i].length; j++) {
				float dec = RAND.nextFloat();
				// negate this value
				if (RAND.nextBoolean()) {
					randomPlanetCoordinates[i][j] *= -1.0f;
				}
				
				// add or subtract the decimal value
				if (RAND.nextBoolean()) {
					randomPlanetCoordinates[i][j] += dec;
				} else {
					randomPlanetCoordinates[i][j] -= dec;
				}
			}
		}
		
		planetZeroNode = sceneManager.getRootSceneNode().createChildSceneNode(planetEntities[0].getName() + "Node");
		planetZeroNode.setLocalPosition(randomPlanetCoordinates[0][0], randomPlanetCoordinates[0][1], randomPlanetCoordinates[0][2]);
		planetZeroNode.attachObject(planetEntities[0]);
		activePlanets.put(planetZeroNode, true);
		
		planetOneNode = sceneManager.getRootSceneNode().createChildSceneNode(planetEntities[1].getName() + "Node");
		planetOneNode.setLocalPosition(randomPlanetCoordinates[1][0], randomPlanetCoordinates[1][1], randomPlanetCoordinates[1][2]);
		planetOneNode.attachObject(planetEntities[1]);
		activePlanets.put(planetOneNode, true);
		
		planetTwoNode = sceneManager.getRootSceneNode().createChildSceneNode(planetEntities[2].getName() + "Node");
		planetTwoNode.setLocalPosition(randomPlanetCoordinates[2][0], randomPlanetCoordinates[2][1], randomPlanetCoordinates[2][2]);
		planetTwoNode.attachObject(planetEntities[2]);
		activePlanets.put(planetTwoNode, true);
		
		// Initialize cube node
		ManualObject manualCubePlanetEntity = ManualCubeObject.makeCubeObject(engine, sceneManager);
		manualCubePlanetNode = sceneManager.getRootSceneNode().createChildSceneNode("ManualCubePlanetNode");
		manualCubePlanetNode.scale(1.0f, 1.0f, 1.0f);
		manualCubePlanetNode.setLocalPosition(randomPlanetCoordinates[3][0], randomPlanetCoordinates[3][1], randomPlanetCoordinates[3][2]);
		manualCubePlanetNode.attachObject(manualCubePlanetEntity);
		activePlanets.put(manualCubePlanetNode, true);
		
		// initialize the dolphin node and add it to the scene graph
		dolphinNode = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntity.getName() + "Node");
		dolphinNode.moveBackward(2.0f);
		dolphinNode.attachObject(dolphinEntity);
		dolphinNode.attachObject(camera);
		
		SceneNode dolphinCamera = dolphinNode.createChildSceneNode("dolphinEntity");
		dolphinCamera.moveBackward(0.3f);
		dolphinCamera.moveUp(0.3f);
		dolphinCamera.moveRight(0.01f);
		dolphinCamera.attachObject(camera);
		
		// initialize the ambient light
		sceneManager.getAmbientLight().setIntensity(new Color(0.01f, 0.01f, 0.01f));
		
		// initialize a point light
		Light pointLightOne = sceneManager.createLight("pointLightOne", Light.Type.POINT);
		pointLightOne.setAmbient(new Color(0.05f, 0.05f, 0.05f));
		pointLightOne.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightOne.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightOne.setRange(5.0f);
		
		// initialize a node for pointLightOne and add it to the scene graph
		SceneNode pointLightNode = sceneManager.getRootSceneNode().createChildSceneNode(pointLightOne.getName() + "Node");
		pointLightNode.attachObject(pointLightOne);
		pointLightNode.setLocalPosition(Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize a flashlight
		Light pointLightFlash = sceneManager.createLight("pointLightFlash", Light.Type.POINT);
		pointLightFlash.setAmbient(new Color(0.5f, 0.5f, 0.5f));
		pointLightFlash.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlash.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setRange(10.0f);
		
		// attach the flashlight to the dolphin
		cameraNode.attachObject(pointLightFlash);
		
		// initialize a rotation controller
		RotationController earthRotationController = new RotationController(Vector3f.createUnitVectorY(), 0.02f);
		RotationController moonRotationController = new RotationController(Vector3f.createUnitVectorY(), 0.05f);
		RotationController blueWorldRotationController = new RotationController(Vector3f.createUnitVectorZ(), 0.32f);
		RotationController cubeWorldRotationController = new RotationController(Vector3f.createUnitVectorY(), 0.4f);
		
		earthRotationController.addNode(planetZeroNode);
		moonRotationController.addNode(planetOneNode);
		blueWorldRotationController.addNode(planetTwoNode);
		cubeWorldRotationController.addNode(manualCubePlanetNode);
		
		sceneManager.addController(earthRotationController);
		sceneManager.addController(moonRotationController);
		sceneManager.addController(blueWorldRotationController);
		sceneManager.addController(cubeWorldRotationController);
		
		// manually assign textures textures
		TextureManager textureManager = engine.getTextureManager();
		Texture dolphinTexture = textureManager.getAssetByPath("Dolphin_HighPolyUV.png");
		Texture earthTexture = textureManager.getAssetByPath("earth-day.jpeg");
		Texture moonTexture = textureManager.getAssetByPath("moon.jpeg");
		Texture blueWorld = textureManager.getAssetByPath("blue.jpeg");
		RenderSystem renderSystem = sceneManager.getRenderSystem();
		
		// initialize texture states
		TextureState dolphinTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState earthTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState moonTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState blueWorldTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		
		dolphinTextureState.setTexture(dolphinTexture);
		dolphinEntity.setRenderState(dolphinTextureState);
		
		earthTextureState.setTexture(earthTexture);
		planetEntities[0].setRenderState(earthTextureState);

		moonTextureState.setTexture(moonTexture);
		planetEntities[1].setRenderState(moonTextureState);
		
		blueWorldTextureState.setTexture(blueWorld);
		planetEntities[2].setRenderState(blueWorldTextureState);
	}
	
	/**
	 * Initializes controller inputs
	 */
	protected void setupInputs() {
		inputManager = new GenericInputManager();

		String gamepadName = inputManager.getFirstGamepadName();
		controller = inputManager.getControllerByName(gamepadName);
		
		// Build action objects for listening to user input
		exitGameAction = new ExitGameAction(this);
		moveCameraForwardAction = new MoveCameraForwardAction(this, camera);
		moveCameraBackwardAction = new MoveCameraBackwardAction(this, camera);
		moveCameraLeftAction = new MoveCameraLeftAction(this, camera);
		moveCameraRightAction = new MoveCameraRightAction(this, camera);
		moveCameraUpAction = new MoveCameraUpAction(this, camera);
		moveCameraDownAction = new MoveCameraDownAction(this, camera);
		leftStickXAction = new LeftStickXAction(this, camera);
		leftStickYAction = new LeftStickYAction(this, camera);
		rideDolphinToggleAction = new RideDolphinToggleAction(this);
		yawCameraLeftAction = new YawCameraLeftAction(this);
		yawCameraRightAction = new YawCameraRightAction(this);
		pitchCameraUpAction = new PitchCameraUpAction(this);
		pitchCameraDownAction = new PitchCameraDownAction(this);
		rightStickXAction = new RightStickXAction(this, camera);
		rightStickYAction = new RightStickYAction(this, camera);
		invertYawAction = new InvertYawAction(this);
		
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				// Bind exit action to escape, and gamepad 6 (select)
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.ESCAPE, 
						exitGameAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				// Bind movement actions
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.W, 
						moveCameraForwardAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	

				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.S,
						moveCameraBackwardAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.A,
						moveCameraLeftAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.D, 
						moveCameraRightAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.LSHIFT,
						moveCameraUpAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.C, 
						moveCameraDownAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.V, 
						invertYawAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				// Bind dolphin toggle
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.SPACE,
						rideDolphinToggleAction,
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				// Bind rotational movement to arrow keys
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.LEFT,
						yawCameraLeftAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.RIGHT,
						yawCameraRightAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.UP,
						pitchCameraUpAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.DOWN,
						pitchCameraDownAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			}
		}
		
		if (isGamepadNull(gamepadName)) {
			System.out.println("No gamepad attached!");
		} else {
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._6, 
					exitGameAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._3, 
					invertYawAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			// Left and Right shoulder buttons
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._4, 
					moveCameraUpAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._5, 
					moveCameraDownAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName,
					net.java.games.input.Component.Identifier.Button._0,
					rideDolphinToggleAction,
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					leftStickYAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					leftStickXAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.RX, 
					rightStickXAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.RY, 
					rightStickYAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
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
		displayString = "Time = " + elapsedTimeString;
		displayString += " | Score = " + score;
		displayString += " | Keyboard Counter = " + counterString;
		displayString += " | Camera position: (" + formatFloat.format(camera.getPo().x()) + ", " + formatFloat.format(camera.getPo().y()) + ", " + formatFloat.format(camera.getPo().z()) + ")";
		displayString += " | Dolphin position: (" + formatFloat.format(dolphinNode.getWorldPosition().x()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().y()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().z()) + ")";
		renderSystem.setHUD(displayString, 15, 15);
		inputManager.update(elapsedTime);
		synchronizePlayerDolphinPosition();
		checkPlayerDistanceToDolphin(10.0f);
		planetCollisionDetection();
	}
	
	/**
	 * Ensures that the player's position is the same as the dolphin's when riding the dolphin
	 */
	private void synchronizePlayerDolphinPosition() {
		if (!toggleRide) {
			camera.setPo((Vector3f) Vector3f.createFrom(dolphinNode.getLocalPosition().x(), dolphinNode.getLocalPosition().y(), dolphinNode.getLocalPosition().z()));
		}
		cameraNode.setLocalPosition(camera.getPo());
	}
	
	/**
	 * Places the player back on the dolphin if they stray 10 units too far in any direction
	 * The boundary is spherical rather than a bounded X box
	 */
	private void checkPlayerDistanceToDolphin(float radius) {
		Vector3f playerPosition = (Vector3f) camera.getPo();
		Vector3f dolphinPosition = (Vector3f) dolphinNode.getLocalPosition();
		
		if ((Math.pow((playerPosition.x() - dolphinPosition.x()), 2) + Math.pow((playerPosition.y() - dolphinPosition.y()), 2) + Math.pow((playerPosition.z() - dolphinPosition.z()), 2)) > Math.pow(radius, 2.0f)) {
			System.out.println("You're too far! Position: (" + playerPosition.x() + ", " + playerPosition.y() + ", " + playerPosition.z() + ")");
			((RideDolphinToggleAction) rideDolphinToggleAction).manualAction();
		}
	}
	
	/**
	 * Detects planet collision detection
	 */
	private void planetCollisionDetection() {
		activePlanets.forEach((k, v) -> {
			if (v) {
				Vector3f playerPosition = (Vector3f) camera.getPo();
				Vector3f planetPosition = (Vector3f) k.getLocalPosition();
				if (toggleRide && (Math.pow((playerPosition.x() - planetPosition.x()), 2) + Math.pow((playerPosition.y() - planetPosition.y()), 2) + Math.pow((playerPosition.z() - planetPosition.z()), 2)) < Math.pow((2.15f), 2.0f)) {
					System.out.println("Score!");
					incrementScore();
					activePlanets.put(k, false);
				}
			}
		});
	}
	
	/**
	 * Inverts Yaw rotation for those who like inverted controls
	 */
	public void invertYaw() {
		if (invertYaw)
			invertYaw = false;
		else
			invertYaw = true;
	}
	
	/**
	 * Increments score
	 */
	private void incrementScore() {
		System.out.println("Score incremented!");
		score++;
	}
	
	/**
	 * Checks if there is a functioning gamepad attached to the system
	 * @param gamepadName
	 * @return
	 */
	private boolean isGamepadNull(String gamepadName) {
		if (gamepadName == null) {
			return true;
		} else 
			return false;
	}
	
	/**
	 * Returns a random float array
	 * @param args
	 */
	private float[] randomFloatArray(float upperBound) {
		float[] randomFloat = {
				(RAND.nextFloat() * upperBound), (RAND.nextFloat() * upperBound), (RAND.nextFloat() * upperBound)
		};
		return randomFloat;
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
