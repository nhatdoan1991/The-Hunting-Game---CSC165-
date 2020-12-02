package com.dsgames.game.hunt;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.io.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;

import com.dsgames.game.myGameEngine.action.AvatarLeftStickXAction;
import com.dsgames.game.myGameEngine.action.AvatarLeftStickYAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveBackwardAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveForwardAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveLeftAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveRightAction;
import com.dsgames.game.myGameEngine.action.ExitGameAction;
import com.dsgames.game.myGameEngine.action.SkipSongAction;
import com.dsgames.game.myGameEngine.action.huntinggame.CloseConnectionAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveBackwardAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveForwardAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveLeftAction;
import com.dsgames.game.myGameEngine.action.huntinggame.network.NetworkMoveRightAction;
import com.dsgames.game.myGameEngine.entities.GhostAvatar;
import com.dsgames.game.myGameEngine.network.ProtocolClient;
import com.dsgames.game.myGameEngine.node.controller.StretchController;
import com.dsgames.game.myGameEngine.node.controller.VerticalOrbitController;
import com.dsgames.game.myGameEngine.action.AvatarChargeAction;
import com.dsgames.game.myGameEngine.action.AvatarJumpAction;
import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.display.DisplaySettingsDialog;
import com.saechaol.game.myGameEngine.object.manual.ManualAxisLineObject;
import com.saechaol.game.myGameEngine.object.manual.ManualFloorObject;

import net.java.games.input.Controller;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
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
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.Node;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.controllers.OrbitController;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
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
			
	private Action moveLeftAction, moveRightAction, moveForwardAction, moveBackwardAction, 
			 exitGameAction, skipSongAction, avatarJumpAction, avatarChargeAction,
			 closeConnectionAction;
	private boolean isClientConnected, isRecentering, mouseInit = false;
	private Camera3PController orbitCameraOne;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	private ConcurrentHashMap<UUID, GhostAvatar> ghostAvatars = new ConcurrentHashMap<UUID, GhostAvatar>();
	private double sensitivity = 0.5;
	private float lastMouseX, lastMouseY, mouseX, mouseY;
	private InputManager inputManager;
	private int starUID = 0, serverPort, ghostEntityCount = 0, centerX, centerY;
	private OrbitController playerOrbitController;
	private static ProtocolClient protocolClient;
	private ProtocolType serverProtocol;
	private RenderWindow renderWindow;
	private Robot mouseRobot;
	private SceneNode groundNode;
	private Sound[] music = new Sound[3];
	private Sound[] sfx = new Sound[3];
	private StretchController playerStretchController;
	private String serverAddress;
	private Tessellation tessellationEntity;
	private TextureManager textureManager;
	private Texture starTexture;
	private Vector<UUID> objectsToRemove;
	private VerticalOrbitController playerOrbitControllerVertical;
	private ZBufferState zState;
	
	protected ScriptEngine jsEngine;
	protected File test,addLight,setupSkybox,setupTerrain, setupAudio;

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
	int elapsedTimeSeconds, playerOneLives = 2, playerOneScore = 0, 
			currentSong = 0;
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
	 * Initializes two viewports and instantiates camera projection matrices for each.
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

		//setupSkybox(engine, sceneManager);
		
		ScriptEngineManager factory = new ScriptEngineManager();
		java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");
		
		Invocable invocableEngine = (Invocable) jsEngine ;
		setupSkybox = new File("setupSkybox.js");
		//jsEngine.put("sceneManager",sceneManager);
		//jsEngine.put("engine",engine);
		this.runScript(jsEngine,setupSkybox);
		
		try {
			invocableEngine.invokeFunction("setupSkybox", sceneManager,engine,textureManager); 
		}catch(ScriptException e1) {
			System.out.println("ScriptException in " + setupSkybox + e1); 
		}catch (NoSuchMethodException e2)
		{   
			System.out.println("No such method in " + setupSkybox + e2); 
		}catch (NullPointerException e3)
		{ 
			System.out.println ("Null pointer exception reading " + setupSkybox + e3);
		}
		
		// initialize zState
		zState = (ZBufferState) renderSystem.createRenderState(RenderState.Type.ZBUFFER);
		zState.setEnabled(true);


		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);

		// origin node for orbit controller
		originNode = sceneManager.getRootSceneNode().createChildSceneNode("originNode");
		originNode.setLocalPosition(0.0f, 5.0f, 0.0f);


		Entity dolphinEntityOne = sceneManager.createEntity("dolphinEntityOne", "playerModel.obj");

		dolphinEntityOne.setPrimitive(Primitive.TRIANGLES);

		dolphinNodeOne = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityOne.getName() + "Node");

		dolphinNodeOne.attachObject(dolphinEntityOne);

		playerCharge.put(dolphinNodeOne, false);

		playerStretchController = new StretchController();
		sceneManager.addController(playerStretchController);

		playerOrbitController = new OrbitController(dolphinNodeOne, 1.0f, 0.5f, 0.0f, false);
		sceneManager.addController(playerOrbitController);

		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));

		addLight = new File("addLight.js");
		this.runScript(jsEngine,addLight);
		Light keyLight = sceneManager.createLight("keyLightOne", Light.Type.POINT);
		SceneNode keyLightNode = sceneManager.getRootSceneNode().createChildSceneNode(keyLight.getName() + "Node");
		Light pointLightFlashOne = sceneManager.createLight("pointLightFlashOne", Light.Type.SPOT);
		SceneNode flashNodeOne = dolphinNodeOne.createChildSceneNode(pointLightFlashOne.getName() + "Node");
	
		try {
			invocableEngine.invokeFunction("addKeyLight", keyLight,keyLightNode); 
			invocableEngine.invokeFunction("addLightFlashOne", pointLightFlashOne,flashNodeOne);
		}catch(ScriptException e1) {
			System.out.println("ScriptException in " + addLight + e1); 
		}catch (NoSuchMethodException e2)
		{   
			System.out.println("No such method in " + addLight + e2); 
		}catch (NullPointerException e3)
		{ 
			System.out.println ("Null pointer exception reading " + addLight + e3);
		}



		dolphinNodeOne.moveLeft(3.0f);
		dolphinNodeOne.scale(0.04f, 0.04f, 0.04f);

		Texture dolphinOneTexture = textureManager.getAssetByPath("playerModel.png");

		TextureState dolphinOneTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);

		dolphinOneTextureState.setTexture(dolphinOneTexture);

		dolphinEntityOne.setRenderState(dolphinOneTextureState);
		setupNetwork();
		setupInputs(sceneManager);

		ManualObject groundEntity = ManualFloorObject.manualFloorObject(engine, sceneManager);
		groundNode = sceneManager.getRootSceneNode().createChildSceneNode(GROUND_NODE);
		groundNode.attachObject(groundEntity);
		groundNode.setLocalPosition(0.0f, -0.8f, 0.0f);

		setupPhysics();
		setupPhysicsWorld();
		setupOrbitCameras(engine, sceneManager);

		setupTerrain = new File("setupTerrain.js");
		this.runScript(jsEngine, setupTerrain);
		try {
			invocableEngine.invokeFunction("setupTessellation", this);
			
		}catch(ScriptException e1) {
			System.out.println("ScriptException in " + setupTerrain + e1); 
		}catch (NoSuchMethodException e2)
		{   
			System.out.println("No such method in " + setupTerrain + e2); 
		}catch (NullPointerException e3)
		{ 
			System.out.println ("Null pointer exception reading " + setupTerrain + e3);
		}
		tessellationEntity=(Tessellation) jsEngine.get("tessellationEntity");
		tessellationNode =  (SceneNode) jsEngine.get("tessellationNode");
		//setupTessellation(sceneManager);
		setupAudio = new File("setupAudio.js");
		jsEngine.put("currentSong", currentSong);
		jsEngine.put("music", music);
		jsEngine.put("sfx", sfx);
		this.runScript(jsEngine, setupAudio);
		try {
			invocableEngine.invokeFunction("setupAudio",this);
			
		}catch(ScriptException e1) {
			System.out.println("ScriptException in " + setupAudio + e1); 
		}catch (NoSuchMethodException e2)
		{   
			System.out.println("No such method in " + setupAudio + e2); 
		}catch (NullPointerException e3)
		{ 
			System.out.println ("Null pointer exception reading " + setupAudio + e3);
		}
		music = (Sound[]) jsEngine.get("music");
		sfx=(Sound[]) jsEngine.get("sfx");
		//setupAudio(sceneManager);
	}

	/**
	 * Imports and generates a height map from a noise map image, and scales it to the size of the ground plane. 
	 * A normal map is then applied to the original noise map to give the terrain surface some semblance of texture.
	 * @param sceneManager
	 */
	/*protected void setupTessellation(SceneManager sceneManager) {
		tessellationEntity = sceneManager.createTessellation("tessellationEntity", 8);
		tessellationEntity.setSubdivisions(32.0f);

		tessellationNode = sceneManager.getRootSceneNode().createChildSceneNode(tessellationEntity.getName() + "Node");
		tessellationNode.attachObject(tessellationEntity);

		tessellationNode.translate(0.0f, -10.f, 0.0f);
		tessellationNode.scale(1000.0f, 1000.0f, 1000.0f);
		tessellationEntity.setHeightMap(this.getEngine(), "noisemap.jpg");
		tessellationEntity.setNormalMap(this.getEngine(), "noisemapnormal.png");
		tessellationEntity.setTexture(this.getEngine(), "grass.jpg");
		tessellationEntity.setQuality(8);
	}*/

	/**
	 * Initializes and loads audio resources from the asset folder, and sets music[]
	 * and sfx[] to their respective resources, and plays them.
	 * 
	 * @param sceneManager
	 */
	/*protected void setupAudio(SceneManager sceneManager) {
		Configuration configuration = sceneManager.getConfiguration();
		String sfxPath = configuration.valueOf("assets.sounds.path.a1.sfx");
		String musicPath = configuration.valueOf("assets.sounds.path.a2.music");
		AudioResource clairDeLune, arabesqueNoOne, reverie, scoreSfx, destroySfx, lifeUpSfx;
		audioManager = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");

		if (!audioManager.initialize()) {
			System.out.println("The Audio Manager failed to initialize :(");
			return;
		}

		clairDeLune = audioManager.createAudioResource(musicPath + "clairdelune.wav", AudioResourceType.AUDIO_STREAM);
		arabesqueNoOne = audioManager.createAudioResource(musicPath + "arabesque_no_one.wav",
				AudioResourceType.AUDIO_STREAM);
		reverie = audioManager.createAudioResource(musicPath + "reverie.wav", AudioResourceType.AUDIO_STREAM);
		scoreSfx = audioManager.createAudioResource(sfxPath + "score.wav", AudioResourceType.AUDIO_SAMPLE);
		destroySfx = audioManager.createAudioResource(sfxPath + "destroyed.wav", AudioResourceType.AUDIO_SAMPLE);
		lifeUpSfx = audioManager.createAudioResource(sfxPath + "lifeup.wav", AudioResourceType.AUDIO_SAMPLE);

		music[0] = new Sound(clairDeLune, SoundType.SOUND_MUSIC, 100, false);
		music[1] = new Sound(arabesqueNoOne, SoundType.SOUND_MUSIC, 100, false);
		mus = new Sound(reverie, SoundType.SOUND_MUSIC, 100, false);
		sfx[0] = new Sound(scoreSfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[1] = new Sound(destroySfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[2] = new Sound(lifeUpSfx, SoundType.SOUND_EFFECT, 25, false);

		for (Sound m : music) {
			m.initialize(audioManager);
		}

		for (Sound s : sfx) {
			s.initialize(audioManager);
		}

		music[currentSong].play();
	}*/

	
	/**
	 * Initializes the JBullet physics engine and sets gravity to -9.8 units along the world Y component.
	 */
	protected void setupPhysics() {
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0.0f, -9.8f, 0.0f };

		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
	}

	/**
	 * Initializes the player physics objects and adds them to the physics world, as well as the ground plane
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

		groundPlane.setBounciness(0.5f);
		groundNode.scale(500.0f, 1.0f, 500.0f);
		groundNode.setLocalPosition(0.0f, -0.8f, 0.0f);
		double[] planeTransform = groundPlane.getTransform();
		planeTransform[12] = groundNode.getLocalPosition().x();
		planeTransform[13] = groundNode.getLocalPosition().y();
		planeTransform[14] = groundNode.getLocalPosition().z();
		groundPlane.setTransform(planeTransform);
		groundNode.setPhysicsObject(groundPlane);

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

	/*protected void setupSkybox(Engine engine, SceneManager sceneManager) throws IOException {
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
	}*/

	/**
	 * Initializes 3P Camera controls for both players as well as their controls.
	 * @param engine
	 * @param sceneManager
	 */
	protected void setupOrbitCameras(Engine engine, SceneManager sceneManager) {
		SceneNode cameraOneNode = sceneManager.getSceneNode("cameraOneNode");

		Camera cameraOne = sceneManager.getCamera("cameraOne");

		String keyboardName = inputManager.getKeyboardName();

		orbitCameraOne = new Camera3PController(cameraOne, cameraOneNode, dolphinNodeOne, keyboardName, inputManager);
		initializeMouse(renderSystem, renderWindow);
	}

	protected void setupInputs(SceneManager sceneManager) {
		
		//String gamepadName = inputManager.getFirstGamepadName();

		moveLeftAction = new NetworkMoveLeftAction(this, dolphinNodeOne, protocolClient);
		moveRightAction = new NetworkMoveRightAction(this, dolphinNodeOne, protocolClient);
		moveForwardAction = new NetworkMoveForwardAction(this, dolphinNodeOne, protocolClient);
		moveBackwardAction = new NetworkMoveBackwardAction(this, dolphinNodeOne, protocolClient);
		exitGameAction = new ExitGameAction(this);
		skipSongAction = new SkipSongAction(this);
		avatarJumpAction = new AvatarJumpAction(this, dolphinNodeOne.getName());
		avatarChargeAction = new AvatarChargeAction(this, dolphinNodeOne.getName());
		closeConnectionAction = new CloseConnectionAction(protocolClient, this, isClientConnected);
		
		/*
		 * Player One - KB
		 * - WASD 		: Move 
		 * - Arrows 	: Orbit camera 
		 * - Q/E		: Yaw dolphin left/right
		 * - SPACE 		: Jump
		 * - LSHIFT 	: Activate charge 
		 * - ESC 		: Quit 
		 * - P 			: Skip Song 
		 * - F 			: Zoom out 
		 * - R 			: Zoom in
		 */
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.A,
						moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

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
		}

		/*
		 * Player Two - Gamepad 
		 * - LStick 	: X/Y 		: Move 
		 * - RStick 	: RX/RY 	: Orbit camera 
		 * - LB			: 4			: Yaw dolphin left
		 * - RB			: 5			: Yaw dolphin right
		 * - A 			: 0 		: Jump 
		 * - B 			: 1 		: Activate charge 
		 * - Y 			: 3 		: Skip Song 
		 * - Menu 		: 7 		: Quit 
		 * - LT 		: Z+ 		: Zoom out 
		 * - RT 		: Z- 		: Zoom in
		 */
		/*if (gamepadName == null) {
			System.out.println("No gamepad detected!");
		} else {
			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Axis.X,
					leftStickXActionP2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Axis.Y,
					leftStickYActionP2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._6,
					exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._7,
					exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._0,
					avatarJumpActionP2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._1,
					avatarChargeActionP2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._3,
					skipSongAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}*/

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
		renderSystem.setHUD(displayString, 15, (renderSystem.getRenderWindow().getViewport(0).getActualBottom())+2);
		updateVerticalPosition();
		processNetworking(elapsedTime);
		inputManager.update(elapsedTime);
		mouseInit = true;
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


		orbitCameraOne.updateCameraPosition();
		if (!ghostAvatars.isEmpty()) {
			ghostAvatars.forEach((k, v)->{
				synchronizeAvatarPhysics(v.getNode());
			});
		}
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
			PhysicsObject ghostNodePhyiscsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transform, 0.3f, 1.0f);

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
				tessellationEntity.getWorldHeight(avatarWorldPositionP1.x(), avatarWorldPositionP1.z()) + 0.5f,
				avatarLocalPositionP1.z());

		if (avatarLocalPositionP1.y() <= terrainPositionP1.y() + 0.5f) {
			Vector3 avatarPositionP1 = terrainPositionP1;
			dolphinNodeOne.setLocalPosition(avatarPositionP1);
			synchronizeAvatarPhysics(dolphinNodeOne);
			if (jumpP1) {
				dolphinNodeOne.getPhysicsObject().applyForce(0.0f, 1500.0f, 0.0f, 0.0f, 0.0f, 0.0f);
				jumpP1 = false;
			}
		} else if (avatarLocalPositionP1.y() > terrainPositionP1.y() + 1.0f) {
			jumpP1 = true;
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

	/**
	 * Checks if the player can perform a charge or not
	 */
	private void checkChargeTime() {
		if (elapsedTimeSeconds > chargeTimeP1) {
			playerCharge.put(dolphinNodeOne, false);
		}
	}
	
	public void setIsConnected(boolean b) {
		isClientConnected = b;
		
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
			
			dolphinNodeOne.rotate(Degreef.createFrom((float) (dx * sensitivity)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
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
	

	/**
	 * Adds the scene node the the stretch controller
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

	private void runScript(ScriptEngine engine,File scriptFile) {
		try    
		{ 
			FileReader fileReader = new FileReader(scriptFile);
			engine.eval(fileReader);
			fileReader.close();    
		}
		catch(FileNotFoundException e1) {
			System.out.println(scriptFile + " not found " + e1); 
		} catch (IOException e2)     
		{ 
			System.out.println("IO problem with " + scriptFile + e2); 
		}catch (ScriptException e3)      
		{ 
			System.out.println("ScriptException in " + scriptFile + e3); 
		}catch (NullPointerException e4)   
		{ 
			System.out.println ("Null pointer exception in " + scriptFile + e4); 
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
			//game = new HuntingGame(args[0], Integer.parseInt(args[1]));
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

}


