package com.dsgames.game.hunt;

import static ray.rage.scene.SkeletalEntity.EndType.LOOP;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.dsgames.game.myGameEngine.action.AvatarChargeAction;
import com.dsgames.game.myGameEngine.action.AvatarJumpAction;
import com.dsgames.game.myGameEngine.action.ExitGameAction;
import com.dsgames.game.myGameEngine.action.SkipSongAction;
import com.dsgames.game.myGameEngine.action.huntinggame.CloseConnectionAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkFireAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveBackwardAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveForwardAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveLeftAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveRightAction;
import com.dsgames.game.myGameEngine.ai.NPCController;
import com.dsgames.game.myGameEngine.entities.AbstractNpcEntity;
import com.dsgames.game.myGameEngine.entities.GhostAvatar;
import com.dsgames.game.myGameEngine.network.ProtocolClient;
import com.dsgames.game.myGameEngine.node.controller.StretchController;
import com.dsgames.game.myGameEngine.node.controller.VerticalOrbitController;
import com.dsgames.game.myGameEngine.camera.Camera3PController;
import com.dsgames.game.myGameEngine.display.DisplaySettingsDialog;

import net.java.games.input.Controller;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.networking.IGameConnection.ProtocolType;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsEngineFactory;
import ray.physics.PhysicsObject;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.ZBufferState;
import ray.rage.scene.Camera;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkeletalEntity;
import ray.rage.scene.Tessellation;
import ray.rage.scene.controllers.OrbitController;
import ray.rage.util.Configuration;
import ray.rml.Degreef;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class HuntingGame extends VariableFrameRateGame {

	public boolean running = true, jumpP1 = false;
	public float cooldownP1 = 0;
	public float velocityP1 = 0.0f;
	public HashMap<SceneNode, Boolean> playerCharge = new HashMap<SceneNode, Boolean>();
	public IAudioManager audioManager;
	public int chargeTimeP1 = 1000;
	public SceneNode dolphinNodeOne, originNode, tessellationNode;
	public PhysicsEngine physicsEngine;
	public PhysicsObject ballOnePhysicsObject, groundPlane, dolphinOnePhysicsObject;

	private Action moveLeftAction, moveRightAction, moveForwardAction, moveBackwardAction, exitGameAction,
			skipSongAction, avatarJumpAction, avatarChargeAction, closeConnectionAction, fireAction;
	private boolean isClientConnected, isRecentering, mouseInit = false;
	private Camera3PController orbitCameraOne;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	private ConcurrentHashMap<UUID, GhostAvatar> ghostAvatars = new ConcurrentHashMap<UUID, GhostAvatar>();
	private ConcurrentHashMap<SceneNode, Vector3[]> bullets = new ConcurrentHashMap<SceneNode, Vector3[]>();
	private AbstractNpcEntity[] npcEntity;
	private double sensitivity = 0.5;
	private float lastMouseX, lastMouseY, mouseX, mouseY;
	private InputManager inputManager;
	private int starUID = 0, serverPort, ghostEntityCount = 0, centerX, centerY, hostStatus = 0;
	private NPCController npcController;
	private OrbitController playerOrbitController;
	private static ProtocolClient protocolClient;
	private ProtocolType serverProtocol;
	private RenderWindow renderWindow;
	private Robot mouseRobot;
	private SceneNode groundNode, targetNode;
	private SceneNode[] npcs;
	private PhysicsObject[] npcPhysicsObjects;
	private Sound[] music = new Sound[3];
	private Sound[] sfx = new Sound[3];
	private StretchController playerStretchController;
	private String serverAddress;
	private Tessellation tessellationEntity, groundTessellation;
	private TextureManager textureManager;
	private Texture starTexture;
	private Vector<UUID> objectsToRemove;
	private VerticalOrbitController playerOrbitControllerVertical;
	private ZBufferState zState;
	private int numberOfNpc = 0, numberOfDolphin = 20, numberOfMonster = 20, numberOfSnitch = 1, numberOfBoss = 1;
	private boolean isStarted = false;

	protected ScriptEngine jsEngine;
	protected File test, addLight, setupSkybox, setupTerrain, setupAudio;

	private static final int INVULNERABLE_SECONDS = 3;
	private static final int TERMINAL_VELOCITY = 1000;
	private static final Random RAND = new Random();
	private static final String BUILD_STATE = "test"; // "test" for debugging, "release" for submission
	private final static String GROUND_NODE = "GroundNode";
	private static final String SKYBOX = "OceanSkybox";
	private static final String SEPARATOR = "----------------------------------------------------";

	float elapsedTime = 0.0f;
	GL4RenderSystem renderSystem;
	HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	int elapsedTimeSeconds, playerOneLives = 2, playerOneScore = 0, currentSong = 0;
	String elapsedTimeString, displayString, playerOneLivesString, playerTwoLivesString, playerOneScoreString,
			playerTwoScoreString;

	public HuntingGame(String serverAddress, int serverPort) {
		super();
		System.out.println("Initializing...!");
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.serverProtocol = ProtocolType.UDP;
		if (BUILD_STATE.equalsIgnoreCase("test"))
			System.out.println("Server address set to " + serverAddress + ":" + serverPort + "!");
		System.out.println(SEPARATOR);
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Q/E' or the left and right bumpers to YAW DOLPHIN");
		System.out.println("Press 'P' or 'Y' to PLAY NEXT SONG");
		System.out.println("Press 'Space' or 'A' to JUMP");
		System.out.println("Press 'ESC' or 'Menu' to EXIT");
		System.out.println(SEPARATOR);
		formatFloat.setRoundingMode(RoundingMode.DOWN);
	}

	public String getAddress() {
		return this.serverAddress;
	}

	public int getPort() {
		return this.serverPort;
	}

	public float getElapsedTime() {
		return elapsedTime;
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
			DisplaySettingsDialog displaySettingsDialogue = new DisplaySettingsDialog(
					graphicsEnvironment.getDefaultScreenDevice());
			displaySettingsDialogue.showIt();
			renderSystem
					.createRenderWindow(displaySettingsDialogue.getSelectedDisplayMode(),
							displaySettingsDialogue.isFullScreenModeSelected())
					.setTitle("Hunting Game | Saechao Lucas/Nhat Doan A3");
		} else if (BUILD_STATE.equalsIgnoreCase("test")) {
			int displayHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDisplayMode().getHeight();
			int displayWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDisplayMode().getWidth();
			if (displayHeight > 1920 && displayWidth > 1080)
				renderWindow = renderSystem.createRenderWindow(new DisplayMode(1920, 1080, 24, 60), false);
			else
				renderWindow = renderSystem.createRenderWindow(new DisplayMode(1280, 720, 24, 60), false);
		}
	}

	/**
	 * Initializes two viewports and instantiates camera projection matrices for
	 * each.
	 */
	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		SceneNode rootNode = sceneManager.getRootSceneNode();

		Camera cameraOne = sceneManager.createCamera("cameraOne", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(cameraOne);

		SceneNode cameraOneNode = rootNode.createChildSceneNode(cameraOne.getName() + "Node");

		cameraOneNode.attachObject(cameraOne);
		cameraOne.setMode('n');
		cameraOne.getFrustum().setFarClipDistance(1000.0f);
		initializeMouse(renderSystem, renderWindow);
	}

	/**
	 * Initializes the game state, and calls related "setup" functions
	 */
	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		if (inputManager == null)
			inputManager = new GenericInputManager();

		if (textureManager == null)
			textureManager = engine.getTextureManager();

		if (renderSystem == null)
			renderSystem = (GL4RenderSystem) engine.getRenderSystem();

		// initialize zState
		zState = (ZBufferState) renderSystem.createRenderState(RenderState.Type.ZBUFFER);
		zState.setEnabled(true);

		// initialize script engine
		ScriptEngineManager factory = new ScriptEngineManager();
		java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");
		Invocable invocableEngine = (Invocable) jsEngine;

		// initialize game
		setupTerrain(engine, sceneManager, invocableEngine);
		setupSkybox(engine, sceneManager, invocableEngine);
		setupPlayer(engine, sceneManager);
		setupLights(engine, sceneManager, invocableEngine);
		setupNetwork();
		setupInputs(sceneManager);
		setupNpc(engine, sceneManager);
		setupPhysics();
		setupPhysicsWorld();
		setupOrbitCameras(engine, sceneManager);
		setupAudio(engine, sceneManager, invocableEngine);
	}

	/**
	 * Initializes the player entity and node objects
	 * 
	 * @param engine
	 * @param sceneManager
	 * @throws IOException
	 */
	private void setupPlayer(Engine engine, SceneManager sceneManager) throws IOException {
		// Entity dolphinEntityOne = sceneManager.createEntity("dolphinEntityOne",
		// "playerModel.obj");
		SkeletalEntity dolphinEntityOne = sceneManager.createSkeletalEntity("dolphinEntityOne", "test.rkm", "test.rks");
		dolphinEntityOne.setPrimitive(Primitive.TRIANGLES);

		// load animations
		dolphinEntityOne.loadAnimation("wave", "wave.rka");
		dolphinEntityOne.loadAnimation("walk", "walk.rka");

		dolphinNodeOne = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityOne.getName() + "Node");
		dolphinNodeOne.attachObject(dolphinEntityOne);

		// initialize 'charge' state
		playerCharge.put(dolphinNodeOne, false);
		playerStretchController = new StretchController();
		sceneManager.addController(playerStretchController);
		playerOrbitController = new OrbitController(dolphinNodeOne, 1.0f, 0.5f, 0.0f, false);
		sceneManager.addController(playerOrbitController);

		// initialize position and scale
		dolphinNodeOne.setLocalPosition(0.0f, -2.5f, 0.0f);
		dolphinNodeOne.scale(0.04f, 0.04f, 0.04f);

		targetNode = dolphinNodeOne.createChildSceneNode("targetNode");
		targetNode.setLocalPosition(0.0f, 10.0f, 50.0f);

		// initialize textures
		Texture dolphinOneTexture = textureManager.getAssetByPath("playerModel.png");
		TextureState dolphinOneTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		dolphinOneTextureState.setTexture(dolphinOneTexture);

		dolphinEntityOne.setRenderState(dolphinOneTextureState);
	}

	/**
	 * Imports and generates a height map from a noise map image, and scales it to
	 * the size of the ground plane. A normal map is then applied to the original
	 * noise map to give the terrain surface some semblance of texture.
	 * 
	 * @param sceneManager
	 */
	protected void setupTerrain(Engine engine, SceneManager sceneManager, Invocable invocableEngine) {
		groundTessellation = sceneManager.createTessellation("groundTessellationEntity, 8");
		groundTessellation.setSubdivisions(32.0f);

		groundNode = sceneManager.getRootSceneNode().createChildSceneNode(groundTessellation.getName() + "Node");
		groundNode.attachObject(groundTessellation);
		groundNode.setLocalPosition(0.0f, 0.0f, 0.0f);
		groundNode.scale(10000.0f, 1.0f, 10000.0f);
		groundTessellation.setHeightMap(engine, "waterHeight.jpg");
		groundTessellation.setTexture(engine, "waterTexture.jpg");
		groundTessellation.setQuality(8);

		setupTerrain = new File("setupTerrain.js");
		this.runScript(jsEngine, setupTerrain);
		try {
			invocableEngine.invokeFunction("setupTessellation", this);
		} catch (ScriptException e1) {
			System.out.println("ScriptException in " + setupTerrain + e1);
		} catch (NoSuchMethodException e2) {
			System.out.println("No such method in " + setupTerrain + e2);
		} catch (NullPointerException e3) {
			System.out.println("Null pointer exception reading " + setupTerrain + e3);
		}
		tessellationEntity = (Tessellation) jsEngine.get("tessellationEntity");
		tessellationNode = (SceneNode) jsEngine.get("tessellationNode");
	}

	protected void setupSkybox(Engine engine, SceneManager sceneManager, Invocable invocableEngine) {
		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
		setupSkybox = new File("setupSkybox.js");
		this.runScript(jsEngine, setupSkybox);

		try {
			invocableEngine.invokeFunction("setupSkybox", sceneManager, engine, textureManager);
		} catch (ScriptException e1) {
			System.out.println("ScriptException in " + setupSkybox + e1);
		} catch (NoSuchMethodException e2) {
			System.out.println("No such method in " + setupSkybox + e2);
		} catch (NullPointerException e3) {
			System.out.println("Null pointer exception reading " + setupSkybox + e3);
		}
	}

	protected void setupLights(Engine engine, SceneManager sceneManager, Invocable invocableEngine) {
		addLight = new File("addLight.js");
		this.runScript(jsEngine, addLight);
		Light keyLight = sceneManager.createLight("keyLightOne", Light.Type.POINT);
		SceneNode keyLightNode = sceneManager.getRootSceneNode().createChildSceneNode(keyLight.getName() + "Node");
		Light pointLightFlashOne = sceneManager.createLight("pointLightFlashOne", Light.Type.SPOT);
		SceneNode flashNodeOne = dolphinNodeOne.createChildSceneNode(pointLightFlashOne.getName() + "Node");

		try {
			invocableEngine.invokeFunction("addKeyLight", keyLight, keyLightNode);
			invocableEngine.invokeFunction("addLightFlashOne", pointLightFlashOne, flashNodeOne);
		} catch (ScriptException e1) {
			System.out.println("ScriptException in " + addLight + e1);
		} catch (NoSuchMethodException e2) {
			System.out.println("No such method in " + addLight + e2);
		} catch (NullPointerException e3) {
			System.out.println("Null pointer exception reading " + addLight + e3);
		}
	}

	/**
	 * Initializes and loads audio resources from the asset folder, and sets music[]
	 * and sfx[] to their respective resources, and plays them.
	 * 
	 * @param sceneManager
	 */
	protected void setupAudio(Engine engine, SceneManager sceneManager, Invocable invocableEngine) {
		setupAudio = new File("setupAudio.js");
		jsEngine.put("currentSong", currentSong);
		jsEngine.put("music", music);
		jsEngine.put("sfx", sfx);
		this.runScript(jsEngine, setupAudio);
		try {
			invocableEngine.invokeFunction("setupAudio", this);

		} catch (ScriptException e1) {
			System.out.println("ScriptException in " + setupAudio + e1);
		} catch (NoSuchMethodException e2) {
			System.out.println("No such method in " + setupAudio + e2);
		} catch (NullPointerException e3) {
			System.out.println("Null pointer exception reading " + setupAudio + e3);
		}
		music = (Sound[]) jsEngine.get("music");
		sfx = (Sound[]) jsEngine.get("sfx");
	}

	private void setupNpc(Engine engine, SceneManager sceneManager) throws IOException {

		numberOfNpc = numberOfDolphin + numberOfMonster + numberOfSnitch + numberOfBoss;
		npcEntity = new AbstractNpcEntity[numberOfNpc];
		npcs = new SceneNode[numberOfNpc];
		npcPhysicsObjects = new PhysicsObject[numberOfNpc];
		if (npcEntity != null) {
			for (int i = 0; i < npcEntity.length; i++) {
				if (i < numberOfDolphin) {
					spawningDolphin(engine, sceneManager, i);
				} else if (i < numberOfMonster + numberOfDolphin) {
					spawningMonster(engine, sceneManager, i);
				} else if (i < numberOfMonster + numberOfDolphin + numberOfSnitch) {
					spawningSnitch(engine, sceneManager, i);
				} else {
					spawningBoss(engine, sceneManager, i);
				}

			}
		}

	}

	private void spawningDolphin(Engine engine, SceneManager sceneManager, int index) throws IOException {
		Entity NpcEntity = sceneManager.createEntity("npcEntityOne" + Integer.toString(index), "dolphinLowPoly.obj");
		NpcEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode NpcNode = sceneManager.getRootSceneNode().createChildSceneNode(NpcEntity.getName() + "Node");
		Texture textureOne = textureManager.getAssetByPath("leggedDolphinBlue.png");
		TextureState textureStateOne = (TextureState) sceneManager.getRenderSystem()
				.createRenderState(RenderState.Type.TEXTURE);
		textureStateOne.setTexture(textureOne);
		NpcEntity.setRenderState(textureStateOne);
		NpcNode.attachObject(NpcEntity);
		NpcNode.scale(10.0f, 10.00f, 10.0f);
		Vector3 randomLocation = randomLocationDolphin(sceneManager);
		NpcNode.setLocalPosition(randomLocation);
		npcEntity[index] = new AbstractNpcEntity(index, NpcNode, NpcEntity);
		npcs[index] = (SceneNode) npcEntity[index].getNode();
	}

	private void spawningMonster(Engine engine, SceneManager sceneManager, int index) throws IOException {
		
		SkeletalEntity NpcEntity = sceneManager.createSkeletalEntity("npcEntityOne" + Integer.toString(index), "monster.rkm", "monster.rks");
		NpcEntity.setPrimitive(Primitive.TRIANGLES);

		// load animations
		NpcEntity.loadAnimation("monster_walking", "monster_walking.rka");
		//NpcEntity.loadAnimation("walk", "walk.rka");
		
		//Entity NpcEntity = sceneManager.createEntity("npcEntityOne" + Integer.toString(index), "monster.obj");
		NpcEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode NpcNode = sceneManager.getRootSceneNode().createChildSceneNode(NpcEntity.getName() + "Node");
		Texture textureOne = textureManager.getAssetByPath("npcTexture.png");
		TextureState textureStateOne = (TextureState) sceneManager.getRenderSystem()
				.createRenderState(RenderState.Type.TEXTURE);
		textureStateOne.setTexture(textureOne);
		NpcEntity.setRenderState(textureStateOne);
		
		playMonsterWalking(NpcEntity);
		
		NpcNode.attachObject(NpcEntity);
		NpcNode.scale(0.2f, 0.2f, 0.2f);
		Vector3 randomLocation = randomLocationMonster(sceneManager);
		NpcNode.setLocalPosition(randomLocation);
		npcEntity[index] = new AbstractNpcEntity(index, NpcNode, NpcEntity);
		npcs[index] = (SceneNode) npcEntity[index].getNode();
	}
	public void playMonsterWalking(SkeletalEntity x) {
		x.stopAnimation();
		x.playAnimation("monster_walking", 0.5f, LOOP, 0);
	}
	private void spawningSnitch(Engine engine, SceneManager sceneManager, int index) throws IOException {
		Entity NpcEntity = sceneManager.createEntity("npcEntityOne" + Integer.toString(index), "dolphinLowPoly.obj");
		NpcEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode NpcNode = sceneManager.getRootSceneNode().createChildSceneNode(NpcEntity.getName() + "Node");
		Texture textureOne = textureManager.getAssetByPath("Dolphin_HighPolyUV.png");
		TextureState textureStateOne = (TextureState) sceneManager.getRenderSystem()
				.createRenderState(RenderState.Type.TEXTURE);
		textureStateOne.setTexture(textureOne);
		NpcEntity.setRenderState(textureStateOne);
		NpcNode.attachObject(NpcEntity);
		NpcNode.scale(1.0f, 1.0f, 1.0f);
		Vector3 randomLocation = randomLocationMonster(sceneManager);
		NpcNode.setLocalPosition(randomLocation);
		npcEntity[index] = new AbstractNpcEntity(index, NpcNode, NpcEntity);
		npcs[index] = (SceneNode) npcEntity[index].getNode();
	}

	private void spawningBoss(Engine engine, SceneManager sceneManager, int index) throws IOException {
		Entity NpcEntity = sceneManager.createEntity("npcEntityOne" + Integer.toString(index), "boss.obj");
		NpcEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode NpcNode = sceneManager.getRootSceneNode().createChildSceneNode(NpcEntity.getName() + "Node");
		Texture textureOne = textureManager.getAssetByPath("Dragon_ground_color.jpg");
		TextureState textureStateOne = (TextureState) sceneManager.getRenderSystem()
				.createRenderState(RenderState.Type.TEXTURE);
		textureStateOne.setTexture(textureOne);
		NpcEntity.setRenderState(textureStateOne);
		NpcNode.attachObject(NpcEntity);
		NpcNode.scale(1.0f, 1.0f, 1.0f);
		NpcNode.setLocalPosition(0f, 0f, 0f);
		npcEntity[index] = new AbstractNpcEntity(index, NpcNode, NpcEntity);
		npcs[index] = (SceneNode) npcEntity[index].getNode();
	}

	/**
	 * Random Location for Dolphin NPC
	 **/
	private Vector3 randomLocationDolphin(SceneManager sceneManager) {
		Vector3 center = sceneManager.getRootSceneNode().getLocalPosition();
		Vector3 randomPosition;
		float[] randomFloat;
		do {
			randomFloat = randomFloatArray(1000);
			randomPosition = Vector3f.createFrom(randomFloat[0], 0.0f, randomFloat[2]);
		} while (distanceFrom(center, randomPosition) < 420f);
		return randomPosition;
	}

	/**
	 * Random Location for Monsters
	 **/
	private Vector3 randomLocationMonster(SceneManager sceneManager) {
		Vector3 center = sceneManager.getRootSceneNode().getLocalPosition();
		Vector3 randomPosition;
		float[] randomFloat;
		do {
			randomFloat = randomFloatArray(1000);
			randomPosition = Vector3f.createFrom(randomFloat[0], 1.5f, randomFloat[2]);
		} while (distanceFrom(center, randomPosition) > 380f);
		return randomPosition;
	}

	/**
	 * Initializes the JBullet physics engine and sets gravity to -9.8 units along
	 * the world Y component.
	 */
	protected void setupPhysics() {
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0.0f, -9.8f, 0.0f };

		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
	}

	/**
	 * Initializes the player physics objects and adds them to the physics world, as
	 * well as the ground plane
	 */
	protected void setupPhysicsWorld() {
		float mass = 1.0f;
		float up[] = { 0.0f, 1.0f, 0.0f };
		double[] transform;

		transform = toDoubleArray(dolphinNodeOne.getLocalTransform().toFloatArray());
		dolphinOnePhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transform, 0.3f, 1.0f);

		dolphinOnePhysicsObject.setBounciness(0.0f);
		dolphinOnePhysicsObject.setFriction(0.0f);
		dolphinOnePhysicsObject.setDamping(0.99f, 0.99f);
		dolphinOnePhysicsObject.setSleepThresholds(0.0f, 0.0f);
		dolphinNodeOne.setPhysicsObject(dolphinOnePhysicsObject);

		transform = toDoubleArray(groundNode.getLocalTransform().toFloatArray());
		groundPlane = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), transform, up, 0.0f);

		groundPlane.setBounciness(0.0f);

		double[] planeTransform = groundPlane.getTransform();
		planeTransform[12] = groundNode.getLocalPosition().x();
		planeTransform[13] = groundNode.getLocalPosition().y();
		planeTransform[14] = groundNode.getLocalPosition().z();
		groundPlane.setTransform(planeTransform);
		groundNode.setPhysicsObject(groundPlane);

		for (int i = 0; i < npcEntity.length; i++) {
			double[] transformNPC = toDoubleArray(npcEntity[i].getNode().getLocalTransform().toFloatArray());
			PhysicsObject npcPhysicObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transformNPC,
					0.3f, 1.0f);
			npcPhysicObject.setBounciness(0.0f);
			npcPhysicObject.setFriction(0.0f);
			npcPhysicObject.setDamping(0.99f, 0.99f);
			npcPhysicObject.setSleepThresholds(0.0f, 0.0f);
			npcEntity[i].getNode().setPhysicsObject(npcPhysicObject);
			npcEntity[i].setPhysicObject(npcPhysicObject);
			npcPhysicsObjects[i] = npcEntity[i].getPhysicObject();
		}
	}

	private void setupNetwork() {
		objectsToRemove = new Vector<UUID>();
		isClientConnected = false;
		try {
			protocolClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (protocolClient == null)
			System.out.println("Missing protocol host");
		else {
			protocolClient.sendJoinMessage();
			isClientConnected = true;
		}
	}

	@Override
	protected void setupWindowViewports(RenderWindow renderWindow) {
		renderWindow.addKeyListener(this);
		renderWindow.addMouseListener(this);
		renderWindow.addMouseMotionListener(this);
		renderWindow.addMouseWheelListener(this);

		Viewport playerOneViewport = renderWindow.getViewport(0);
		playerOneViewport.setDimensions(0.0f, 0.0f, 1.0f, 1.0f);
		playerOneViewport.setClearColor(new Color(0.5f, 1.0f, 0.5f));
	}

	/**
	 * Initializes 3P Camera controls for both players as well as their controls.
	 * 
	 * @param engine
	 * @param sceneManager
	 */
	protected void setupOrbitCameras(Engine engine, SceneManager sceneManager) {
		SceneNode cameraOneNode = sceneManager.getSceneNode("cameraOneNode");
		orbitCameraOne = new Camera3PController(cameraOneNode, targetNode,
				com.dsgames.game.myGameEngine.controller.InputType.MOUSE, inputManager);
	}

	protected void setupInputs(SceneManager sceneManager) {

		// String gamepadName = inputManager.getFirstGamepadName();

		moveLeftAction = new NetworkMoveLeftAction(this, dolphinNodeOne, protocolClient);
		moveRightAction = new NetworkMoveRightAction(this, dolphinNodeOne, protocolClient);
		moveForwardAction = new NetworkMoveForwardAction(this, dolphinNodeOne, protocolClient);
		moveBackwardAction = new NetworkMoveBackwardAction(this, dolphinNodeOne, protocolClient);
		exitGameAction = new ExitGameAction(this);
		skipSongAction = new SkipSongAction(this);
		avatarJumpAction = new AvatarJumpAction(this, dolphinNodeOne.getName());
		avatarChargeAction = new AvatarChargeAction(this, dolphinNodeOne.getName());
		closeConnectionAction = new CloseConnectionAction(protocolClient, this, isClientConnected);
		fireAction = new NetworkFireAction(this, dolphinNodeOne, protocolClient);

		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.A, moveLeftAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.D,
						moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.W,
						moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.S,
						moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.P, skipSongAction,
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.ESCAPE,
						exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.SPACE,
						avatarJumpAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.LSHIFT,
						avatarChargeAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.K,
						closeConnectionAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			}
			if (keyboards.getType() == Controller.Type.MOUSE) {
				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Button.LEFT,
						fireAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			}
		}

	}

	public Vector<SceneNode> getPlayers() {
		Vector<SceneNode> players = new Vector<SceneNode>();
		players.add(dolphinNodeOne);
		if (!ghostAvatars.isEmpty()) {
			ghostAvatars.forEach((key, val) -> {
				players.add((SceneNode) val.getNode());
			});
		}
		return players;
	}

	public static float distanceFrom(Vector3 p1, Vector3 p2) {
		return (float) Math
				.sqrt(Math.pow(p1.x() - p2.x(), 2) + Math.pow(p1.y() - p2.y(), 2) + Math.pow(p1.z() - p2.z(), 2));
	}

	protected void processNetworking(float elapsedTime) {
		if (protocolClient != null) {
			protocolClient.processPackets();
		}

		// remove ghost avatars of players who have left the game
		Iterator<UUID> i = objectsToRemove.iterator();
		while (i.hasNext()) {
			this.getEngine().getSceneManager().destroySceneNode(i.next().toString());
		}
		objectsToRemove.clear();
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
		targetNode = (SceneNode) orbitCameraOne.getCameraTarget();
		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		playerOneLivesString = Integer.toString(playerOneLives);
		playerOneScoreString = Integer.toString(playerOneScore);

		displayString = "Player One Time: " + elapsedTimeString;
		displayString += " | Lives = " + playerOneLivesString;
		displayString += " | Score = " + playerOneScoreString;
		displayString += " | Position: (" + formatFloat.format(dolphinNodeOne.getWorldPosition().x()) + ", "
				+ formatFloat.format(dolphinNodeOne.getWorldPosition().y()) + ", "
				+ formatFloat.format(dolphinNodeOne.getWorldPosition().z()) + ")";
		if (cooldownP1 - elapsedTimeSeconds < 0) {
			displayString += " | Charge Ready!";
			playerStretchController.removeNode(dolphinNodeOne);
		} else if (cooldownP1 - elapsedTimeSeconds > 10) {
			displayString += " | Charge active!";
		} else {
			displayString += " | Charge cooldown: " + (cooldownP1 - elapsedTimeSeconds);
		}
		displayString += " | Current song: ";
		switch (currentSong % 3) {
		case 0:
			displayString += "Claude Debussy - Suite bergamasque ~ Clair de lune";
			break;
		case 1:
			displayString += "Claude Debussy - Arabesque No. 1";
			break;
		case 2:
			displayString += "Claude Debussy - Reverie";
			break;
		}
		renderSystem.setHUD(displayString, 15, (renderSystem.getRenderWindow().getViewport(0).getActualBottom()) + 2);
		updateVerticalPosition();
		processNetworking(elapsedTime);
		inputManager.update(elapsedTime);

		if (hostStatus == 0) {
			npcController = new NPCController(this, protocolClient, npcs, npcPhysicsObjects, hostStatus);
			hostStatus = -1;
		}

		if (hostStatus == -1) {
			npcController.runNpcLoop();
		}

		mouseInit = true;
		recenterMouse();
		checkChargeTime();

		if (jumpP1) {
			velocityP1 -= 1.0f;
			if (Math.abs(velocityP1) > TERMINAL_VELOCITY) {
				velocityP1 = TERMINAL_VELOCITY * -1;
			}
			dolphinNodeOne.getPhysicsObject().applyForce(0.0f, velocityP1, 0.0f, 0.0f, 0.0f, 0.0f);
		} else if (dolphinNodeOne.getWorldPosition().y() <= 0.5f && jumpP1) {
			velocityP1 = 0.0f;
			jumpP1 = false;
		}

		SkeletalEntity x = (SkeletalEntity) getEngine().getSceneManager().getEntity("dolphinEntityOne");
		x.update();
		for(int i = 20; i< 40;i++)
		{
			SkeletalEntity monsterSkeletal = (SkeletalEntity) getEngine().getSceneManager().getEntity("npcEntityOne" + Integer.toString(i));
			monsterSkeletal.update();
		}
		orbitCameraOne.updateCameraPosition();
		if (!ghostAvatars.isEmpty()) {
			ghostAvatars.forEach((k, v) -> {
				synchronizeAvatarPhysics(v.getNode());
			});
		}

		targetNode.lookAt(dolphinNodeOne);
		updateBullets();
	}

	public Vector3 getPlayerPosition() {
		SceneNode player = this.getEngine().getSceneManager().getSceneNode("dolphinEntityOneNode");
		return player.getWorldPosition();
	}

	public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException {
		SceneManager sceneManager = this.getEngine().getSceneManager();
		if (avatar != null && (!sceneManager.hasEntity("ghostEntity" + avatar.getId().toString()))) {
			Entity ghostEntity = sceneManager.createEntity("ghostEntity" + avatar.getId().toString(), "modelGame.obj");
			ghostEntity.setPrimitive(Primitive.TRIANGLES);
			SceneNode ghostNode = sceneManager.getRootSceneNode().createChildSceneNode(avatar.getId().toString());
			ghostNode.attachObject(ghostEntity);
			ghostNode.scale(0.04f, 0.04f, 0.04f);
			ghostNode.moveLeft(3.0f);

			Texture dolphinTexture = textureManager.getAssetByPath("modelGame.jpg");
			TextureState dolphinTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
			dolphinTextureState.setTexture(dolphinTexture);
			ghostEntity.setRenderState(dolphinTextureState);

			float mass = 1.0f;
			double[] transform;

			transform = toDoubleArray(ghostNode.getLocalTransform().toFloatArray());
			PhysicsObject ghostNodePhyiscsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass,
					transform, 0.3f, 1.0f);

			ghostNodePhyiscsObject.setBounciness(0.0f);
			ghostNodePhyiscsObject.setFriction(0.0f);
			ghostNodePhyiscsObject.setDamping(0.99f, 0.99f);
			ghostNodePhyiscsObject.setSleepThresholds(0.0f, 0.0f);
			ghostNode.setPhysicsObject(ghostNodePhyiscsObject);

			avatar.setNode(ghostNode);
			avatar.setEntity(ghostEntity);
			ghostAvatars.put(avatar.getId(), avatar);
		}
	}

	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar) {
		if (avatar != null) {
			objectsToRemove.add(avatar.getId());
			ghostAvatars.remove(avatar.getId());
		}
	}

	public void moveGhostAvatar(UUID id, Vector3 position) {
		if (ghostAvatars.get(id) != null) {
			ghostAvatars.get(id).getNode().setLocalPosition(position);
			synchronizeAvatarPhysics(ghostAvatars.get(id).getNode());
		}
	}

	/**
	 * Ensures that the player's vertical position traverses along the terrain
	 */
	public void updateVerticalPosition() {
		Vector3 avatarWorldPositionP1 = dolphinNodeOne.getWorldPosition();
		Vector3 avatarLocalPositionP1 = dolphinNodeOne.getLocalPosition();
		Vector3 terrainPositionP1 = (Vector3) Vector3f.createFrom(avatarLocalPositionP1.x(),
				tessellationEntity.getWorldHeight(avatarWorldPositionP1.x(), avatarWorldPositionP1.z()) + 0.3f,
				avatarLocalPositionP1.z());
		Vector3 groundPositionP1 = (Vector3) Vector3f.createFrom(avatarLocalPositionP1.x(),
				groundTessellation.getWorldHeight(avatarWorldPositionP1.x(), avatarWorldPositionP1.z() - 0.3f),
				avatarLocalPositionP1.z());
		if (avatarLocalPositionP1.y() <= terrainPositionP1.y() + 0.3f
				|| avatarLocalPositionP1.y() <= groundPositionP1.y()) {
			Vector3 avatarPositionP1 = terrainPositionP1;
			if (avatarPositionP1.y() < groundPositionP1.y()) {
				avatarPositionP1 = groundPositionP1;
			}
			dolphinNodeOne.setLocalPosition(avatarPositionP1);
			synchronizeAvatarPhysics(dolphinNodeOne);
			if (jumpP1) {
				dolphinNodeOne.getPhysicsObject().applyForce(0.0f, 1500.0f, 0.0f, 0.0f, 0.0f, 0.0f);
				jumpP1 = false;
			}
		} else if (avatarLocalPositionP1.y() > terrainPositionP1.y() + 1.0f) {
			jumpP1 = true;
		}

		for (int i = 0; i < npcEntity.length; i++) {
			Vector3 npcWorldPosition = npcEntity[i].getNode().getWorldPosition();
			Vector3 npcLocalPosition = npcEntity[i].getNode().getLocalPosition();
			Vector3 npcTerrainPosition = (Vector3) Vector3f.createFrom(npcLocalPosition.x(),
					tessellationEntity.getWorldHeight(npcWorldPosition.x(), npcWorldPosition.z() + 0.3f),
					npcLocalPosition.z());
			Vector3 npcGroundPlanePosition = (Vector3) Vector3f.createFrom(npcLocalPosition.x(),
					groundTessellation.getWorldHeight(npcWorldPosition.x(), npcWorldPosition.z() + 0.2f),
					npcLocalPosition.z());

			if (npcLocalPosition.y() <= npcTerrainPosition.y() + 0.3f
					|| npcLocalPosition.y() <= npcGroundPlanePosition.y()) {
				Vector3 npcPosition = npcTerrainPosition;
				if (npcLocalPosition.y() < npcGroundPlanePosition.y()) {
					npcPosition = npcGroundPlanePosition;
				}
				npcEntity[i].getNode().setLocalPosition(npcPosition);
				synchronizeAvatarPhysics(npcEntity[i].getNode());
			}

		}
	}

	/**
	 * Invoked by SkipAudioAction. Stops the current soundtrack and plays the next
	 * one.
	 */
	public void playAudio() {
		music[currentSong].stop();
		currentSong++;
		currentSong %= music.length;
		music[currentSong].play();
	}

	public void playSoundEffect() {
		sfx[1].play();
	}

	public void playJumpSound() {
		sfx[2].play();
	}

	public void npcFireBullet(SceneNode sn, Vector3 target) throws IOException {
		Engine engine = this.getEngine();
		SceneManager sceneManager = engine.getSceneManager();

		Entity bulletEntity = sceneManager.createEntity("bullet" + UUID.randomUUID().toString(), "sphere.obj");
		bulletEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode bulletNode = sceneManager.getRootSceneNode().createChildSceneNode(bulletEntity.getName() + "Node");
		bulletNode.attachObject(bulletEntity);

		Vector3 origin = Vector3f.createFrom(sn.getLocalPosition().x(), sn.getLocalPosition().y(),
				sn.getLocalPosition().z() + 2.0f);

		bulletNode.setLocalPosition(origin);
		bulletNode.scale(0.2f, 0.2f, 0.2f);

		Vector3[] positions = { origin, Vector3f.createFrom(bulletNode.getLocalPosition().x(),
				bulletNode.getLocalPosition().y(), bulletNode.getLocalPosition().z()) };

		double[] transform = toDoubleArray(bulletNode.getLocalTransform().toFloatArray());
		PhysicsObject bulletPhysicObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), 0.1f, transform,
				0.3f, 1.0f);
		bulletPhysicObject.setSleepThresholds(0.0f, 0.0f);
		bulletNode.setPhysicsObject(bulletPhysicObject);

		bulletNode.lookAt(Vector3f.createFrom(target.x(), target.y(), target.z()));

		bullets.put(bulletNode, positions);

		Texture bulletTexture = textureManager.getAssetByPath("Fire_A_2.png");
		TextureState bulletTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		bulletTextureState.setTexture(bulletTexture);

		bulletEntity.setRenderState(bulletTextureState);
		// System.out.println("Firing " + bulletNode.getName() +
		// bulletNode.getLocalPosition() + " at " + this.targetNode.getWorldPosition());

	}

	public void fireBullet() throws IOException {
		Engine engine = this.getEngine();
		SceneManager sceneManager = engine.getSceneManager();
		SceneNode player = this.dolphinNodeOne;
		Entity bulletEntity = sceneManager.createEntity("bullet" + UUID.randomUUID().toString(), "sphere.obj");
		bulletEntity.setPrimitive(Primitive.TRIANGLES);
		SceneNode bulletNode = sceneManager.getRootSceneNode().createChildSceneNode(bulletEntity.getName() + "Node");

		bulletNode.attachObject(bulletEntity);

		/*
		 * Light bulletLight = sceneManager.createLight(bulletEntity.getName() +
		 * "Light", Light.Type.POINT); bulletLight.setAmbient(new Color(1.0f, 0.6353f,
		 * 0.6118f)); // 255, 165, 155 bulletLight.setDiffuse(new Color(0.7f, 0.7f,
		 * 0.7f)); bulletLight.setSpecular(new Color(0.5f, 0.5f, 0.5f));
		 * bulletLight.setConstantAttenuation(10.3f);
		 * bulletLight.setLinearAttenuation(10.06f);
		 * bulletLight.setQuadraticAttenuation(10.001f);
		 * bulletLight.setFalloffExponent(100.0f); bulletLight.setRange(0.01f);
		 * bulletNode.attachObject(bulletLight);
		 */
		// doesnt currently set it to be ahead of the player
		// only sets the bullet to be +3 in relation to their Z position
		Vector3 origin = Vector3f.createFrom(player.getLocalPosition().x() + player.getLocalForwardAxis().x() * 2.0f,
				player.getLocalPosition().y(), player.getLocalPosition().z() + player.getLocalForwardAxis().z() * 2.0f);

		bulletNode.setLocalPosition(origin);
		bulletNode.scale(0.5f, 0.5f, 0.5f);

		// bulletNode -> [origin, currentPosition]
		Vector3[] positions = { origin, Vector3f.createFrom(bulletNode.getLocalPosition().x(),
				bulletNode.getLocalPosition().y(), bulletNode.getLocalPosition().z()) };

		double[] transform = toDoubleArray(bulletNode.getLocalTransform().toFloatArray());
		PhysicsObject bulletPhysicObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), 0.1f, transform,
				0.3f, 1.0f);
		bulletPhysicObject.setSleepThresholds(0.0f, 0.0f);
		bulletNode.setPhysicsObject(bulletPhysicObject);

		bulletNode
				.lookAt(Vector3f.createFrom(player.getLocalPosition().x() + player.getLocalForwardAxis().x() * 3000.0f,
						player.getLocalForwardAxis().y(),
						player.getLocalPosition().z() + player.getLocalForwardAxis().z() * 3000.0f));

		bullets.put(bulletNode, positions);

		Texture bulletTexture = textureManager.getAssetByPath("bullet.png");
		TextureState bulletTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		bulletTextureState.setTexture(bulletTexture);

		bulletEntity.setRenderState(bulletTextureState);
		// System.out.println("Firing " + bulletNode.getName() +
		// bulletNode.getLocalPosition() + " at " + this.targetNode.getWorldPosition()+
		// " at " + player.getLocalForwardAxis().x()+ " at " +
		// player.getLocalForwardAxis().y()+ " at " +player.getLocalForwardAxis().z());
	}

	/**
	 * Checks if the player can perform a charge or not
	 */
	private void checkChargeTime() {
		if (elapsedTimeSeconds > chargeTimeP1) {
			playerCharge.put(dolphinNodeOne, false);
		}
	}

	public void setIsConnected(boolean connected) {
		isClientConnected = connected;

	}

	private void updateBullets() {
		bullets.forEach((bullet, position) -> {
			float[] playerDirection = { bullet.getLocalForwardAxis().x() * 12.0f, 0.0f,
					bullet.getLocalForwardAxis().z() * 12.0f };
			bullet.getPhysicsObject().setLinearVelocity(playerDirection);
			Vector3 slope = bullet.getLocalPosition().sub(position[1]);
			bullet.setLocalPosition(bullet.getLocalPosition().add(slope));
			// System.out.println("New local position: " + bullet.getLocalPosition());
			// System.out.println("New world position: " + bullet.getWorldPosition());
			// synchronizeAvatarPhysics(bullet);
			if (Math.abs(Math.abs(bullet.getLocalPosition().x()) - Math.abs(position[0].x())) > 50.0f
					|| Math.abs(Math.abs(bullet.getLocalPosition().y()) - Math.abs(position[0].y())) > 50.0f
					|| Math.abs(Math.abs(bullet.getLocalPosition().z()) - Math.abs(position[0].z())) > 50.0f) {
				bullet.setLocalPosition(0.0f, -100.0f, 0.0f);
				synchronizeAvatarPhysics(bullet);
				this.getEngine().getSceneManager().destroySceneNode(bullet);
				bullets.remove(bullet);
				
			}
		});
	}

	private void initializeMouse(RenderSystem renderSystem, RenderWindow render) {
		Viewport view = renderWindow.getViewport(0);
		int left = renderWindow.getLocationLeft();
		int top = renderWindow.getLocationTop();
		int height = view.getActualScissorHeight();
		int width = view.getActualScissorWidth();
		centerX = left + width / 2;
		centerY = top + height / 2;
		isRecentering = false;

		try {
			mouseRobot = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException("Couldn't initialize robot");
		}

		recenterMouse();
		lastMouseX = centerX;
		lastMouseY = centerY;

		render.addMouseMotionListener(this);
	}

	public void mouseMoveAction(MouseEvent e) {
		if (mouseInit) {
			if (isRecentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
				isRecentering = false;
			}
			mouseX = e.getXOnScreen();
			mouseY = e.getYOnScreen();

			float dx = lastMouseX - mouseX;
			float dy = lastMouseY - mouseY;

			dolphinNodeOne.rotate(Degreef.createFrom((float) (dx * sensitivity)),
					Vector3f.createFrom(0.0f, 1.0f, 0.0f));
			orbitCameraOne.updateCameraPosition();

			lastMouseX = mouseX;
			lastMouseY = mouseY;
			recenterMouse();
			lastMouseX = centerX;
			lastMouseY = centerY;
		}
	}

	private void recenterMouse() {
		Viewport view = renderWindow.getViewport(0);
		int left = renderWindow.getLocationLeft();
		int top = renderWindow.getLocationTop();
		int height = view.getActualScissorHeight();
		int width = view.getActualScissorWidth();
		centerX = left + width / 2;
		centerY = top + height / 2;
		isRecentering = true;
		mouseRobot.mouseMove((int) centerX, (int) centerY);
	}

	public void setIsConnected(boolean connected, int clientStatus) {
		isClientConnected = connected;
		if (clientStatus == 0)
			hostStatus = clientStatus;
		else {
			hostStatus = clientStatus;
			npcController = new NPCController(this, protocolClient, npcs, npcPhysicsObjects, hostStatus);
		}
	}

	public void moveNpc(int npcId, float[] npcTransform, Vector3 vectorPosition) {
		if (npcController != null) {
			if (npcController.getNpc(npcId) != null) {
				double[] transform = { npcTransform[0], npcTransform[1], npcTransform[2], npcTransform[3],
						npcTransform[4], npcTransform[5], npcTransform[6], npcTransform[7], npcTransform[8],
						npcTransform[9], npcTransform[10], npcTransform[11], npcTransform[12], npcTransform[13],
						npcTransform[14], npcTransform[15] };
				npcController.getNpc(npcId).setTransform(transform);
				npcController.getNpc(npcId).getNpcSceneNode().lookAt(vectorPosition);
			}
		}

	}

	/**
	 * Adds the scene node the the stretch controller
	 * 
	 * @param player
	 */
	public void addToStretchController(SceneNode player) {
		playerStretchController.addNode(player);
	}

	public void synchronizeAvatarPhysics(Node avatarNode) {
		if (running) {
			double[] transform = avatarNode.getPhysicsObject().getTransform();
			transform[12] = avatarNode.getLocalPosition().x();
			transform[13] = avatarNode.getLocalPosition().y();
			transform[14] = avatarNode.getLocalPosition().z();
			avatarNode.getPhysicsObject().setTransform(transform);
		} else {
			avatarNode.getPhysicsObject().setTransform(toDoubleArray(avatarNode.getWorldTransform().toFloatArray()));
		}
	}

	private void runScript(ScriptEngine engine, File scriptFile) {
		try {
			FileReader fileReader = new FileReader(scriptFile);
			engine.eval(fileReader);
			fileReader.close();
		} catch (FileNotFoundException e1) {
			System.out.println(scriptFile + " not found " + e1);
		} catch (IOException e2) {
			System.out.println("IO problem with " + scriptFile + e2);
		} catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFile + e3);
		} catch (NullPointerException e4) {
			System.out.println("Null pointer exception in " + scriptFile + e4);
		}
	}

	/**
	 * Returns a random float array of size 3
	 * 
	 * @param args
	 */
	private float[] randomFloatArray(float upperBound) {
		float[] randomFloat = { (RAND.nextFloat() * upperBound), Math.abs((RAND.nextFloat() * upperBound)),
				(RAND.nextFloat() * upperBound) };
		for (int i = 0; i < randomFloat.length; i++) {
			if (RAND.nextBoolean()) {
				randomFloat[i] += RAND.nextFloat();
			} else {
				randomFloat[i] -= RAND.nextFloat();
			}
		}
		return randomFloat;
	}

	/**
	 * Converts a double array into a float array
	 * 
	 * @param arr
	 * @return
	 */
	private float[] toFloatArray(double[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		float[] outputFloat = new float[n];

		for (int i = 0; i < n; i++) {
			outputFloat[i] = (float) arr[i];
		}

		return outputFloat;
	}

	/**
	 * Converts a float array into a double array
	 * 
	 * @param arr
	 * @return
	 */
	private double[] toDoubleArray(float[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		double[] outputFloat = new double[n];

		for (int i = 0; i < n; i++) {
			outputFloat[i] = (double) arr[i];

		}
		return outputFloat;
	}

	@Override
	protected void loadConfiguration(Configuration config) throws IOException {
		config.load("assets/config/hunt.properties");
	}

	public static void main(String[] args) {
		Game game = null;
		System.out.println("dsgames.HuntingGame.main() running!");
		if (BUILD_STATE.equalsIgnoreCase("test")) {
			// "192.168.1.9", "1234"
			game = new HuntingGame(args[0], Integer.parseInt(args[1]));
		} else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				System.out.print("Please enter the server's IP address: ");
				String ip = reader.readLine();
				System.out.println("Please enter the port to connect to: ");
				String port = reader.readLine();
				game = new HuntingGame(ip, Integer.parseInt(port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("NetworkServer instantiated!");
		System.out.println("Using IP: " + ((HuntingGame) game).getAddress());
		System.out.println("Serving over port: " + ((HuntingGame) game).getPort());

		try {
			game.startup();
			game.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			protocolClient.sendByeMessage();
			game.shutdown();
			game.exit();
		}
	}

	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_V:
			SkeletalEntity x = (SkeletalEntity) getEngine().getSceneManager().getEntity("dolphinEntityOne");
			x.stopAnimation();
			x.playAnimation("wave", 0.5f, LOOP, 0);
			break;
		case KeyEvent.VK_B:
			SkeletalEntity z = (SkeletalEntity) getEngine().getSceneManager().getEntity("dolphinEntityOne");
			z.stopAnimation();
			z.playAnimation("walk", 2f, LOOP, 0);
			break;

		case KeyEvent.VK_C:
			SkeletalEntity y = (SkeletalEntity) getEngine().getSceneManager().getEntity("dolphinEntityOne");
			y.stopAnimation();
			break;
		}
	}
}