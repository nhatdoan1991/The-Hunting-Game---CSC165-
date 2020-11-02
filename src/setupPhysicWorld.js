var JavaPackages = new JavaImporter( 
	Packages.ray.rage.scene.SceneManager, 
	Packages.ray.rage.scene.SceneNode,
	Packages.ray.rage.util.Configuration,
	Packages.ray.physics.PhysicsEngine,
	Packages.ray.physics.PhysicsEngineFactory,
	Packages.ray.physics.PhysicsObject
);

with(JavaPackages)
{
	function setupPhysicsWorld() {
		engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		gravity = [ 0.0,-9.8, 0.0 ];

		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
		
		mass = 1.0;
		up = [0.0, 1.0, 0.0 ];
		var transform;

		transform = toDoubleArray(dolphinNodeOne.getLocalTransform().toFloatArray());
		dolphinOnePhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transform, 0.3, 1.0);

		dolphinOnePhysicsObject.setBounciness(0.0);
		dolphinOnePhysicsObject.setFriction(0.0);
		dolphinOnePhysicsObject.setDamping(0.99, 0.99);
		dolphinOnePhysicsObject.setSleepThresholds(0.0, 0.0);
		dolphinNodeOne.setPhysicsObject(dolphinOnePhysicsObject);

		
		transform = toDoubleArray(groundNode.getLocalTransform().toFloatArray());
		groundPlane = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), transform, up, 0.0);

		groundPlane.setBounciness(0.5);
		groundNode.scale(500.0, 1.0, 500.0);
		groundNode.setLocalPosition(0.0, -0.8 ,0.0);
		planeTransform = groundPlane.getTransform();
		planeTransform[12] = groundNode.getLocalPosition().x();
		planeTransform[13] = groundNode.getLocalPosition().y();
		planeTransform[14] = groundNode.getLocalPosition().z();
		groundPlane.setTransform(planeTransform);
		groundNode.setPhysicsObject(groundPlane);

	}

	
}