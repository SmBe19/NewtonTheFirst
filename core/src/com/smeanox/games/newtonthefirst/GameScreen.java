package com.smeanox.games.newtonthefirst;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
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
	Body heroBody;
	Body groundBody;
	Body enemyBody;

	private int numHeroFootContacts = 0;

	private float accumulator = 0;

	public GameScreen(){
		batch = new SpriteBatch();
		camera = new OrthographicCamera(40, 30);
		//camera = new OrthographicCamera(30, 20);
		img = new Texture("hero.png");

		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		debugRenderer = new Box2DDebugRenderer();

		initPhysics();
	}

	private void initPhysics(){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(0, 0);

		heroBody = world.createBody(bodyDef);

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(0.5f, 1);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1;
		fixtureDef.friction = 0.25f;
		fixtureDef.restitution = 0.25f;
		heroBody.createFixture(fixtureDef);

		FixtureDef sensorFixtureDef = new FixtureDef();
		polygonShape.setAsBox(0.4f, 0.25f, new Vector2(0, -1.25f), 0);
		sensorFixtureDef.shape = polygonShape;
		sensorFixtureDef.isSensor = true;
		Fixture footFixture = heroBody.createFixture(sensorFixtureDef);
		footFixture.setUserData(1);

		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		groundBody = world.createBody(bodyDef);

		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1;
		fixtureDef.friction = 0.5f;
		fixtureDef.restitution = 1;
		polygonShape.setAsBox(0.5f, 10, new Vector2(-15, 0), 0);
		groundBody.createFixture(fixtureDef);
		polygonShape.setAsBox(0.5f, 10, new Vector2(15, 0), 0);
		groundBody.createFixture(fixtureDef);
		fixtureDef.restitution = 0;
		polygonShape.setAsBox(15, 0.5f, new Vector2(0, -10), 0);
		groundBody.createFixture(fixtureDef);

		enemyBody = world.createBody(bodyDef);
		polygonShape.setAsBox(0.5f, 0.5f, new Vector2(10, 0), 0);
		enemyBody.createFixture(fixtureDef);

		polygonShape.dispose();

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				Object uA = contact.getFixtureA().getUserData();
				Object uB = contact.getFixtureB().getUserData();
				if(uA != null && 1 == ((Integer) uA) || uB != null && 1 == ((Integer) uB)){
					numHeroFootContacts++;
				}
			}

			@Override
			public void endContact(Contact contact) {
				Object uA = contact.getFixtureA().getUserData();
				Object uB = contact.getFixtureB().getUserData();
				if(uA != null && 1 == ((Integer) uA) || uB != null && 1 == ((Integer) uB)){
					numHeroFootContacts--;
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		});
	}

	private void initPhysicsOld(){
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
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(img, heroBody.getPosition().x - 0.5f, heroBody.getPosition().y - 1, 0.5f, 1, 1, 2, 1, 1,
				heroBody.getAngle() * MathUtils.radiansToDegrees, 0, 0, img.getWidth(), img.getHeight(), false, false);
		batch.end();

		updateInput(delta);
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

	private void updateInput(float delta){
		float destX = heroBody.getLinearVelocity().x;
		if(Gdx.input.isKeyPressed(Input.Keys.D)){
			destX = 10;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.A)){
			destX = -10;
		}
		heroBody.applyLinearImpulse(new Vector2(heroBody.getMass() * (destX - heroBody.getLinearVelocity().x), 0), heroBody.getPosition(), true);
		if(Gdx.input.isKeyPressed(Input.Keys.W) && numHeroFootContacts > 0){
			heroBody.applyLinearImpulse(new Vector2(0, heroBody.getMass() * 2), heroBody.getPosition(), true);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
			heroBody.applyTorque(((0 - heroBody.getAngle())%MathUtils.PI2) * 20, true);
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
