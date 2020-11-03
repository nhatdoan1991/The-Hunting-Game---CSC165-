package com.saechaol.game.myGameEngine.node.Controller;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Degreef;
import ray.rml.Vector3;

public class VerticalOrbitController extends AbstractController implements Throttleable {

	private float speed;
	private Node target;
	private float distance;
	private float horizontalDistance;
	private boolean alwaysFacingTarget;

	public VerticalOrbitController(Node orbitTarget, float orbitSpeed, float verticalDistance, float offset,
			boolean faceTarget) {
		super();
		target = orbitTarget;
		speed = orbitSpeed;
		distance = verticalDistance;
		horizontalDistance = offset;
		alwaysFacingTarget = faceTarget;
	}

	public VerticalOrbitController(Node orbitTarget, float orbitSpeed, float verticalDistance, float offset) {
		this(orbitTarget, orbitSpeed, verticalDistance, offset, false);
	}

	public VerticalOrbitController(Node orbitTarget, float orbitSpeed, float verticalDistance) {
		this(orbitTarget, orbitSpeed, verticalDistance, 0.0f);
	}

	public VerticalOrbitController(Node orbitTarget, float orbitSpeed) {
		this(orbitTarget, orbitSpeed, 1.0f);
	}

	public VerticalOrbitController(Node orbitTarget) {
		this(orbitTarget, 1.0f);
	}

	@Override
	public void setSpeed(float s) {
		speed = s;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	public Node getTarget() {
		return target;
	}

	public float getDistance() {
		return distance;
	}

	public float getHorizontalDistance() {
		return horizontalDistance;
	}

	public boolean isAlwaysFacingTarget() {
		return alwaysFacingTarget;
	}

	public void setTarget(Node orbitTarget) {
		target = orbitTarget;
	}

	public void setDistance(float d) {
		distance = d;
	}

	public void setHorizontalOffset(float offset) {
		horizontalDistance = offset;
	}

	public void setFacingTarget(boolean target) {
		alwaysFacingTarget = target;
	}

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		int nodes = super.controlledNodesList.size();

		float azimuthFromTarget = Degreef.createFrom(360.0f / nodes).valueRadians();
		double timeDelta = (System.currentTimeMillis() / 1000.0) * speed;

		Vector3 nodePosition = target.getWorldPosition();
		for (int i = 0; i < nodes; ++i) {
			float dx = (float) (Math.sin(timeDelta + azimuthFromTarget * i) * distance);
			float dy = (float) (Math.cos(timeDelta + azimuthFromTarget * i) * distance);
			Node node = super.controlledNodesList.get(i);
			node.setLocalPosition(nodePosition.x() + dx, nodePosition.y() + dy, nodePosition.z() + horizontalDistance);
			if (alwaysFacingTarget)
				node.lookAt(target);
		}
	}

}
