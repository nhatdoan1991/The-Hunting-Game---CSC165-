var JavaPackages = new JavaImporter( 
	Packages.ray.rage.scene.SceneManager, 
	Packages.ray.rage.scene.SceneNode, 
	Packages.ray.rage.scene.SkyBox,
	Packages.ray.rage.util.Configuration,
	Packages.ray.rage.asset.texture.Texture,
	Packages.java.awt.geom.AffineTransform
);

with(JavaPackages)
{
	function setupSkybox(sceneManager,engine,textureManager)
	{
		worldSkybox = sceneManager.createSkyBox("OceanSkybox");
		configuration = engine.getConfiguration();

		// initialize skybox textures
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path.test"));
		skyboxFrontTexture = textureManager.getAssetByPath("front.jpg");
		skyboxBackTexture = textureManager.getAssetByPath("back.jpg");
		skyboxLeftTexture = textureManager.getAssetByPath("left.jpg");
		skyboxRightTexture = textureManager.getAssetByPath("right.jpg");
		skyboxTopTexture = textureManager.getAssetByPath("top.jpg");
		skyboxBottomTexture = textureManager.getAssetByPath("bottom.jpg");

		// transform skybox textures
		skyboxAffineTransform = new AffineTransform();
		skyboxAffineTransform.translate(skyboxFrontTexture.getImage().getHeight(), skyboxFrontTexture.getImage().getHeight());
		skyboxAffineTransform.scale(-1, -1);
		//skyboxAffineTransform.rotate(180);
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
		
}