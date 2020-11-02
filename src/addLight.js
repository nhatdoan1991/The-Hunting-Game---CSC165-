var JavaPackages = new JavaImporter( 
	Packages.ray.rage.scene.SceneManager, 
	Packages.ray.rage.scene.SceneNode, 
	Packages.ray.rage.scene.Light, 
 	Packages.ray.rage.scene.Light.Type, 
 	Packages.ray.rage.scene.Light.Type.POINT, 
 	Packages.java.awt.Color  
);

with(JavaPackages)
{
	function addKeyLight(keyLight,keyLightNode)
	{
		keyLight.setAmbient(new Color(0.5, 0.5, 0.5));
		keyLight.setDiffuse(new Color(0.7, 0.7, 0.7));
		keyLight.setSpecular(new Color(0.5, 0.5, 0.5));
		keyLight.setRange(500.0);
		keyLightNode.moveUp(500.0);
		keyLightNode.attachObject(keyLight);
	}
	function addLightFlashOne(lightFlashOne, lightFlashOneNode)
	{
		lightFlashOne.setAmbient(new Color(0.25, 0.25, 0.25));
		lightFlashOne.setDiffuse(new Color(0.7, 0.7, 0.7));
		lightFlashOne.setSpecular(new Color(0.5, 0.5, 0.5));
		//lightFlashOne.setConeCutoffAngle(.3);
		lightFlashOne.setConstantAttenuation(0.3);
		lightFlashOne.setLinearAttenuation(0.06);
		lightFlashOne.setQuadraticAttenuation(0.001);
		lightFlashOne.setFalloffExponent(40.0);
		lightFlashOne.setRange(30.0);
		lightFlashOneNode.attachObject(lightFlashOne);
	}
}