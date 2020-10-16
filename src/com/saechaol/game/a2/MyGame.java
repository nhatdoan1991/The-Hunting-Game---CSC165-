package com.saechaol.game.a2;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.saechaol.game.myGameEngine.action.AvatarLeftStickXAction;
import com.saechaol.game.myGameEngine.action.AvatarLeftStickYAction;
import com.saechaol.game.myGameEngine.action.AvatarMoveBackwardAction;
import com.saechaol.game.myGameEngine.action.AvatarMoveForwardAction;
import com.saechaol.game.myGameEngine.action.AvatarMoveLeftAction;
import com.saechaol.game.myGameEngine.action.AvatarMoveRightAction;
import com.saechaol.game.myGameEngine.action.ExitGameAction;
import com.saechaol.game.myGameEngine.action.StartPhysicsAction;
import com.saechaol.game.myGameEngine.action.a2.AvatarJumpAction;
import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.display.DisplaySettingsDialog;
import com.saechaol.game.myGameEngine.object.manual.ManualAxisLineObject;
import com.saechaol.game.myGameEngine.object.manual.ManualFloorObject;

import net.java.games.input.Controller;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsEngineFactory;
import ray.physics.PhysicsObject;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderSystemFactory;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.ZBufferState;
import ray.rage.scene.Camera;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneManagerFactory;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.util.Configuration;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;

public class MyGame extends VariableFrameRateGame {

	private Action	moveLeftActionP1, moveRightActionP1, moveForwardActionP1, moveBackwardActionP1, 
					leftStickXActionP2, leftStickYActionP2, exitGameAction, startPhysicsAction,
					avatarJumpAction;
	private Camera3PController orbitCameraOne, orbitCameraTwo;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	float elapsedTime = 0.0f;
	GL4RenderSystem renderSystem;
	int elapsedTimeSeconds, playerOneLives = 2, playerTwoLives = 2, playerOneScore = 0, playerTwoScore = 0;
	String elapsedTimeString, displayString, playerOneLivesString, playerTwoLivesString, playerOneScoreString, playerTwoScoreString;
	public SceneNode dolphinNodeOne, dolphinNodeTwo;
	private static final String SKYBOX = "TestSkybox";
	private static final String BUILD_STATE = "test"; // test for debugging, release for submission
	private TextureManager textureManager;
	private InputManager inputManager;
	private ZBufferState zState;

	/*
	 * Physics stuff
	 */
	private SceneNode ballOneNode, ballTwoNode, groundNode;
	private SceneNode cameraPositionNode;
	private final static String GROUND_E = "Ground";
	private final static String GROUND_N = "GroundNode";
	
	public PhysicsEngine physicsEngine;
	public PhysicsObject	ballOnePhysicsObject, ballTwoPhysicsObject, groundPlane,
							dolphinOnePhysicsObject, dolphinTwoPhysicsObject;
	public boolean running = false;
	/*
	 * End of physics stuff
	 */
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("----------------------------------------------------");
		formatFloat.setRoundingMode(RoundingMode.DOWN);
	}

	/**
	 * Implements a dialogue to allow the user to pick their preferred viewport size
	 * settings
	 * 
	 * @param renderSystem
	 * @param graphicsEnvironment
	 */
	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		if (BUILD_STATE.equalsIgnoreCase("release")) {
			DisplaySettingsDialog displaySettingsDialogue = new DisplaySettingsDialog(graphicsEnvironment.getDefaultScreenDevice());
			displaySettingsDialogue.showIt();
			renderSystem.createRenderWindow(displaySettingsDialogue.getSelectedDisplayMode(),displaySettingsDialogue.isFullScreenModeSelected()).setTitle("Competitive Planet Chaser | Saechao Lucas A2");	
		} else if (BUILD_STATE.equalsIgnoreCase("test")) {
			int displayHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
			int displayWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
			if (displayHeight > 1920 && displayWidth > 1080)
				renderSystem.createRenderWindow(new DisplayMode(1920, 1080, 24, 60), false);
			else
				renderSystem.createRenderWindow(new DisplayMode(1280, 720, 24, 60), false);
		}
	}

	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		SceneNode rootNode = sceneManager.getRootSceneNode();

		Camera cameraOne = sceneManager.createCamera("cameraOne", Projection.PERSPECTIVE);
		renderWindow.getViewport(1).setCamera(cameraOne);

		SceneNode cameraOneNode = rootNode.createChildSceneNode(cameraOne.getName() + "Node");
		
		cameraOneNode.attachObject(cameraOne);
		cameraOne.setMode('n');
		cameraOne.getFrustum().setFarClipDistance(1000.0f);
		
		Camera cameraTwo = sceneManager.createCamera("cameraTwo", Projection.PERSPECTIVE); 
		renderWindow.getViewport(0).setCamera(cameraTwo);
		
		SceneNode cameraTwoNode = rootNode.createChildSceneNode(cameraTwo.getName() + "Node");
		
		cameraTwoNode.attachObject(cameraTwo);
		cameraTwo.setMode('n');
		cameraTwo.getFrustum().setFarClipDistance(1000.0f);
	}

	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		if (inputManager == null)
			inputManager = new GenericInputManager();
		
		if (textureManager == null)
			textureManager = engine.getTextureManager();
		
		if (renderSystem == null)
			renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		
		setupSkybox(engine, sceneManager);
		
		// initialize zState
		zState = (ZBufferState) renderSystem.createRenderState(RenderState.Type.ZBUFFER);
		zState.setEnabled(true);
		
		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);
		
		Entity dolphinEntityOne = sceneManager.createEntity("dolphinEntityOne", "dolphinHighPoly.obj");
		Entity dolphinEntityTwo = sceneManager.createEntity("dolphinEntityTwo", "dolphinHighPoly.obj");
		
		dolphinEntityOne.setPrimitive(Primitive.TRIANGLES);
		dolphinEntityTwo.setPrimitive(Primitive.TRIANGLES);
		
		dolphinNodeOne = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityOne.getName() + "Node");
		dolphinNodeTwo = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityTwo.getName() + "Node");
		
		dolphinNodeOne.attachObject(dolphinEntityOne);
		dolphinNodeTwo.attachObject(dolphinEntityTwo);
		
		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
		
		Light pointLightFlash = sceneManager.createLight("pointLightFlash", Light.Type.POINT);
		pointLightFlash.setAmbient(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlash.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setRange(20.0f);
	
		SceneNode pointLightFlashNode = sceneManager.getRootSceneNode().createChildSceneNode(pointLightFlash.getName() + "Node");
		pointLightFlashNode.attachObject(pointLightFlash);
		
		dolphinNodeOne.moveLeft(0.5f);
		dolphinNodeTwo.moveRight(0.5f);
		
		// manually assign textures
		Texture dolphinOneTexture = textureManager.getAssetByPath("red.jpeg");
		Texture dolphinTwoTexture = textureManager.getAssetByPath("blue.jpeg");
		
		TextureState dolphinOneTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState dolphinTwoTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		
		dolphinOneTextureState.setTexture(dolphinOneTexture);
		dolphinTwoTextureState.setTexture(dolphinTwoTexture);
		
		dolphinEntityOne.setRenderState(dolphinOneTextureState);
		dolphinEntityTwo.setRenderState(dolphinTwoTextureState);
		
		setupOrbitCameras(engine, sceneManager);
		setupInputs(sceneManager);
	
		/*
		 * Physics stuff
		 */
		Entity ballOneEntity = sceneManager.createEntity("ballOneEntity", "earth.obj");
		ballOneNode = sceneManager.getRootSceneNode().createChildSceneNode(ballOneEntity.getName() + "Node");
		ballOneNode.attachObject(ballOneEntity);
		ballOneNode.moveLeft(4.0f);
		ballOneNode.moveUp(2.0f);
		
		Entity ballTwoEntity = sceneManager.createEntity("ballTwoEntity", "earth.obj");
		ballTwoNode = sceneManager.getRootSceneNode().createChildSceneNode(ballTwoEntity.getName() + "Node");
		ballTwoNode.attachObject(ballTwoEntity);
		ballTwoNode.moveForward(4.0f);
		
		ManualObject groundEntity = ManualFloorObject.manualFloorObject(engine, sceneManager);
		
		groundNode = sceneManager.getRootSceneNode().createChildSceneNode(GROUND_N);
		groundNode.attachObject(groundEntity);
		groundNode.setLocalPosition(0.0f, -0.5f, 0.0f);
		/*
		 * End of physics stuff
		 */
		
		setupPhysics();
		setupPhysicsWorld();
		
		System.out.println("Press SPACE to start the physics engine");
	}

	private void setupPhysics() {
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0.0f, -9.8f, 0.0f };
		
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
	}
	
	private void setupPhysicsWorld() {
		float mass = 1.0f;
		float up[] = { 0.0f, 1.0f, 0.0f };
		double[] temptf;
		
		temptf = toDoubleArray(ballOneNode.getLocalTransform().toFloatArray());
		ballOnePhysicsObject = physicsEngine.addSphereObject(physicsEngine.nextUID(), mass, temptf, 2.0f);
		
		ballOnePhysicsObject.setBounciness(1.0f);
		ballOneNode.setPhysicsObject(ballOnePhysicsObject);
		
		temptf = toDoubleArray(ballTwoNode.getLocalTransform().toFloatArray());
		ballTwoPhysicsObject = physicsEngine.addSphereObject(physicsEngine.nextUID(), mass, temptf, 2.0f);
		
		ballTwoPhysicsObject.setBounciness(1.0f);
		ballTwoNode.setPhysicsObject(ballTwoPhysicsObject);
		
		temptf = toDoubleArray(dolphinNodeOne.getLocalTransform().toFloatArray());
		dolphinOnePhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, temptf, 0.5f, 0.5f);
		
		dolphinOnePhysicsObject.setBounciness(0.0f);
		dolphinOnePhysicsObject.setFriction(0.0f);
	//	dolphinNodeOne.setPhysicsObject(dolphinOnePhysicsObject);
		
		temptf = toDoubleArray(dolphinNodeTwo.getLocalTransform().toFloatArray());
		dolphinTwoPhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, temptf, 0.5f, 0.5f);
		
		dolphinTwoPhysicsObject.setBounciness(1.0f);
		dolphinTwoPhysicsObject.setFriction(0.0f);
	//	dolphinNodeTwo.setPhysicsObject(dolphinTwoPhysicsObject);
		
		temptf = toDoubleArray(groundNode.getLocalTransform().toFloatArray());
		groundPlane = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), temptf, up, 0.0f);
		
		groundPlane.setBounciness(0.5f);
		groundNode.scale(100.0f, 0.5f, 100.0f);
		groundNode.setLocalPosition(0.0f, -0.5f, 0.0f);
		groundNode.setPhysicsObject(groundPlane);
		
	}
	
	private float[] toFloatArray(double[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}
	
	private double[] toDoubleArray(float[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}
	
	@Override
	protected void setupWindowViewports(RenderWindow renderWindow) {
		renderWindow.addKeyListener(this);
		renderWindow.addMouseListener(this);
		renderWindow.addMouseMotionListener(this);
		renderWindow.addMouseWheelListener(this);

		Viewport playerOneViewport = renderWindow.getViewport(0);
		playerOneViewport.setDimensions(0.0f, 0.0f, 1.0f, 0.5f);
		playerOneViewport.setClearColor(new Color(0.5f, 1.0f, 0.5f));

		Viewport playerTwoViewport = renderWindow.createViewport(0.5f, 0.0f, 1.0f, 0.5f);
		playerTwoViewport.setClearColor(new Color(0.5f, 1.0f, 0.5f));
	}
	
	protected void setupSkybox(Engine engine, SceneManager sceneManager) throws IOException {
		SkyBox worldSkybox = sceneManager.createSkyBox(SKYBOX);
		Configuration configuration = engine.getConfiguration();
		
		// initialize skybox textures
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path.test"));
		Texture skyboxFrontTexture = textureManager.getAssetByPath("front.jpg");
		Texture skyboxBackTexture = textureManager.getAssetByPath("back.jpg");
		Texture skyboxLeftTexture = textureManager.getAssetByPath("left.jpg");
		Texture skyboxRightTexture = textureManager.getAssetByPath("right.jpg");
		Texture skyboxTopTexture = textureManager.getAssetByPath("top.jpg");
		Texture skyboxBottomTexture = textureManager.getAssetByPath("bottom.jpg");
		
		// transform skybox textures
		AffineTransform skyboxAffineTransform = new AffineTransform();
		skyboxAffineTransform.translate(0.0, skyboxFrontTexture.getImage().getHeight());
		skyboxAffineTransform.scale(1.0, 1.0);
		skyboxFrontTexture.transform(skyboxAffineTransform);
		skyboxBackTexture.transform(skyboxAffineTransform);
		skyboxLeftTexture.transform(skyboxAffineTransform);
		skyboxRightTexture.transform(skyboxAffineTransform);
		skyboxTopTexture.transform(skyboxAffineTransform);
		skyboxBottomTexture.transform(skyboxAffineTransform);
		
		// set skybox textures
		worldSkybox.setTexture(skyboxFrontTexture, SkyBox.Face.FRONT);
		worldSkybox.setTexture(skyboxBackTexture, SkyBox.Face.BACK);
		worldSkybox.setTexture(skyboxLeftTexture, SkyBox.Face.LEFT);
		worldSkybox.setTexture(skyboxRightTexture, SkyBox.Face.RIGHT);
		worldSkybox.setTexture(skyboxTopTexture, SkyBox.Face.TOP);
		worldSkybox.setTexture(skyboxBottomTexture, SkyBox.Face.BOTTOM);
		
		// assign skybox to sceneManager
		sceneManager.setActiveSkyBox(worldSkybox);
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.textures.path"));
	}

	protected void setupOrbitCameras(Engine engine, SceneManager sceneManager) {
		SceneNode cameraOneNode = sceneManager.getSceneNode("cameraOneNode");
		SceneNode cameraTwoNode = sceneManager.getSceneNode("cameraTwoNode");
		
		Camera cameraOne = sceneManager.getCamera("cameraOne");
		Camera cameraTwo = sceneManager.getCamera("cameraTwo");
		
		String keyboardName = inputManager.getKeyboardName();
		String gamepadName = inputManager.getFirstGamepadName();
		
		orbitCameraOne = new Camera3PController(cameraOne, cameraOneNode, dolphinNodeOne, keyboardName, inputManager);
		orbitCameraTwo = new Camera3PController(cameraTwo, cameraTwoNode, dolphinNodeTwo, gamepadName, inputManager);
	}
	
	protected void setupInputs(SceneManager sceneManager) {
		String gamepadName = inputManager.getFirstGamepadName();
		String mouseName = inputManager.getMouseName();
		
		moveLeftActionP1 = new AvatarMoveLeftAction(this, dolphinNodeOne.getName());
		moveRightActionP1 = new AvatarMoveRightAction(this, dolphinNodeOne.getName());
		moveForwardActionP1 = new AvatarMoveForwardAction(this, dolphinNodeOne.getName());
		moveBackwardActionP1 = new AvatarMoveBackwardAction(this, dolphinNodeOne.getName());
		leftStickXActionP2 = new AvatarLeftStickXAction(this, dolphinNodeTwo.getName());
		leftStickYActionP2 = new AvatarLeftStickYAction(this, dolphinNodeTwo.getName());
		exitGameAction = new ExitGameAction(this);
		startPhysicsAction = new StartPhysicsAction(this);
		avatarJumpAction = new AvatarJumpAction(this);
		
		/*
		 * Player One - KB / Mouse
		 * 	- WASD		:	Move
		 * 	- Arrows	:	Orbit camera
		 * 	- SPACE		:	Jump
		 * 	- ESC		:	Quit
		 * 	- P			:	Pause or Self Destruct
		 * 	- Tab		:	Reset camera
		 * 	- F			:	Zoom out
		 * 	- R			:	Zoom in
		 */
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.A, 
						moveLeftActionP1, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.D, 
						moveRightActionP1, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.W, 
						moveForwardActionP1, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.S, 
						moveBackwardActionP1, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.ESCAPE, 
						exitGameAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.P, 
						startPhysicsAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.SPACE, 
						avatarJumpAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			}
		}
		
		/*
		 * Player Two - Gamepad
		 *	- LStick 	:	X/Y 	:	Move
		 *	- RStick	:	RX/RY	:	Orbit camera
		 * 	- Dpad		:	POV		:	
		 *	- A			:	0		:	Jump
		 * 	- B			:	1		:	
		 * 	- X			:	2		:	
		 * 	- Y			:	3		:	
		 * 	- LB		:	4		:	
		 * 	- RB		:	5		:	
		 * 	- View		:	6		:	Quit
		 * 	- Menu		:	7		:	Pause or Self Destruct
		 *	- LS		:	8		:	
		 *	- RS		:	9		:	Reset camera
		 * 	- LT		:	Z+		:	Zoom out
		 * 	- RT		:	Z-		:	Zoom in
		 */
		if (gamepadName == null) {
			System.out.println("No gamepad detected!");
		} else {
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					leftStickXActionP2, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					leftStickYActionP2, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._6, 
					exitGameAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
		}
		
		
	}

	protected void setupAudio() {

	}

	@Override
	protected void update(Engine engine) {
		renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		elapsedTime += engine.getElapsedTimeMillis();
		
		if (running) {
			Matrix4 matrix;
			physicsEngine.update(elapsedTime);
			for (SceneNode sceneNode : engine.getSceneManager().getSceneNodes()) {
				if (sceneNode.getPhysicsObject() != null) {
					matrix = Matrix4f.createFrom(toFloatArray(sceneNode.getPhysicsObject().getTransform()));
					sceneNode.setLocalPosition(matrix.value(0, 3), matrix.value(1, 3), matrix.value(2, 3));
				}
			}
		}
		
		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		playerOneLivesString = Integer.toString(playerOneLives);
		playerTwoLivesString = Integer.toString(playerTwoLives);
		playerOneScoreString = Integer.toString(playerOneScore);
		playerTwoScoreString = Integer.toString(playerTwoScore);
		
		displayString = "Player One Time: " + elapsedTimeString;
		displayString += " | Lives = " + playerOneLivesString;
		displayString += " | Score = " + playerOneScoreString;
		displayString += " | Position: (" + formatFloat.format(dolphinNodeOne.getWorldPosition().x()) + ", " + formatFloat.format(dolphinNodeOne.getWorldPosition().y()) + ", " + formatFloat.format(dolphinNodeOne.getWorldPosition().z()) + ")";
		renderSystem.setHUD(displayString, 15, (renderSystem.getRenderWindow().getViewport(1).getActualBottom()) + 15);
		
		displayString = "Player Two Time: " + elapsedTimeString;
		displayString += " | Lives = " + playerTwoLivesString;
		displayString += " | Score = " + playerTwoScoreString;
		displayString += " | Position: (" + formatFloat.format(dolphinNodeTwo.getWorldPosition().x()) + ", " + formatFloat.format(dolphinNodeTwo.getWorldPosition().y()) + ", " + formatFloat.format(dolphinNodeTwo.getWorldPosition().z()) + ")";

		renderSystem.setHUD2(displayString, 15, 15);
		
		inputManager.update(elapsedTime);
		
		/*
 		displayString += " | Lives = " + livesString;
		displayString += " | Score = " + score;
		displayString += " | Camera position: (" + formatFloat.format(camera.getPo().x()) + ", " + formatFloat.format(camera.getPo().y()) + ", " + formatFloat.format(camera.getPo().z()) + ")";
		displayString += " | Dolphin position: (" + formatFloat.format(dolphinNode.getWorldPosition().x()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().y()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().z()) + ")";
		displayString += " | Current song: ";
		 */
		
		orbitCameraOne.updateCameraPosition();
		orbitCameraTwo.updateCameraPosition();
	}

	@Override
	protected void loadConfiguration(Configuration config) throws IOException {
		config.load("assets/config/a2.properties");
	}

	public static void main(String[] args) {
		System.out.println("a2.MyGame.main() running!");
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
