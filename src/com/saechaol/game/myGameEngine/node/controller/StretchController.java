package com.saechaol.game.myGameEngine.node.controller;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class StretchController extends AbstractController {
	   private float growthRate = .003f;
	   private float cycleTime = 2000.0f;
	   private float totalTime = 0.0f;
	   private float direction = 1.0f;
	   
	   @Override
	   protected void updateImpl(float elapsedTimeMillis) {
		   totalTime += elapsedTimeMillis;
		   float growthAmount = 1.0f + direction * growthRate;
		   
		   if(totalTime > cycleTime) {
			   direction = -direction;
			   totalTime = 0.0f;
		   }
		   for(Node node : super.controlledNodesList) {
			   Vector3 nodeScale = node.getLocalScale();
			   nodeScale = Vector3f.createFrom(nodeScale.x() * growthAmount, nodeScale.y(), nodeScale.z() * growthAmount);
			   node.setLocalScale(nodeScale);
		   }
	   }
}