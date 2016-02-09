package com.smeanox.games.newtonthefirst;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

/**
 * Comment
 */
public class GameScreen implements Screen {

	public static final float PHYSICS_TIME_STEP = 1/60f;
	public static final int VELOCITY_ITERATIONS = 6;
	public static final int POSITION_ITERATIONS = 2;

	SpriteBatch batch;
	Camera camera;
	Texture img;

	World world;
	Box2DDebugRenderer debugRenderer;

	private float accumulator = 0;

	public GameScreen(){
		batch = new SpriteBatch();
		camera = new OrthographicCamera(1000, 700);
		img = new Texture("hero.png");

		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		debugRenderer = new Box2DDebugRenderer();

		initPhysics();
	}

	private void initPhysics(){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(100, 300);

		CircleShape circle = new CircleShape();
		circle.setRadius(4);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.8f;

		Body lastBody = null;
		for(int i = 0; i < 20; i++) {
			bodyDef.position.set(10 + 20*i, 300);
			Body body = world.createBody(bodyDef);
			Fixture fixture = body.createFixture(fixtureDef);

			if(lastBody != null){
				DistanceJointDef distanceJointDef = new DistanceJointDef();
				distanceJointDef.initialize(lastBody, body, lastBody.getPosition(), body.getPosition());
				distanceJointDef.dampingRatio = 0.5f;
				distanceJointDef.frequencyHz = 4f;
				DistanceJoint joint = (DistanceJoint) world.createJoint(distanceJointDef);
			}
			lastBody = body;
		}

		circle.dispose();


		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0, 10);

		Body groundBody = world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(camera.viewportWidth, 10f);
		groundBody.createFixture(groundBox, 0f);
		groundBox.dispose();

		BodyDef plattformBodyDef = new BodyDef();
		plattformBodyDef.type = BodyDef.BodyType.KinematicBody;
		plattformBodyDef.position.set(50, 0);

		Body plattformBody = world.createBody(plattformBodyDef);

		PolygonShape plattformBox = new PolygonShape();
		plattformBox.setAsBox(25, 10);
		plattformBody.createFixture(plattformBox, 0);
		plattformBox.dispose();

		plattformBody.setLinearVelocity(5, 10);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		debugRenderer.render(world, camera.combined);

		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		doPhysicsStep(delta);
	}

	private void doPhysicsStep(float deltaTime) {
		// fixed time step
		// max frame time to avoid spiral of death (on slow devices)
		float frameTime = Math.min(deltaTime, 0.25f);
		accumulator += frameTime;
		while (accumulator >= PHYSICS_TIME_STEP) {
			world.step(PHYSICS_TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
			accumulator -= PHYSICS_TIME_STEP;
		}
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}
}
