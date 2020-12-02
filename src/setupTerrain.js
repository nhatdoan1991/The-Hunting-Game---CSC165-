var JavaPackages = new JavaImporter( 
	Packages.ray.rage.scene.SceneManager, 
	Packages.ray.rage.scene.SceneNode,
	Packages.ray.rage.scene.Tessellation
);

with(JavaPackages)
{
	var tessellationEntity,tessellationNode;
	function setupTessellation(mygame) {
		tessellationEntity = mygame.getEngine().getSceneManager().createTessellation("tessellationEntity", 8);
		tessellationEntity.setSubdivisions(32.0);

		tessellationNode = mygame.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(tessellationEntity.getName() + "Node");
		tessellationNode.attachObject(tessellationEntity);

		tessellationNode.translate(0.0, -10, 0.0);
		tessellationNode.scale(1000.0, 1000.0, 1000.0);
		tessellationEntity.setHeightMap(mygame.getEngine(), "noisemap.jpg");
		tessellationEntity.setNormalMap(mygame.getEngine(), "noisemapnormal.png");
		tessellationEntity.setTexture(mygame.getEngine(), "grass.jpg");
		tessellationEntity.setQuality(8);

	}
}