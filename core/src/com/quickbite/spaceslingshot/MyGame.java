package com.quickbite.spaceslingshot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quickbite.spaceslingshot.data.json.PlanetDataManager;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;
import com.quickbite.spaceslingshot.screens.MainMenuScreen;
import com.quickbite.spaceslingshot.util.ContactListenerClass;
import com.quickbite.spaceslingshot.util.EasyAssetManager;
import com.quickbite.spaceslingshot.util.JsonLevelLoader;
import com.quickbite.spaceslingshot.util.Loader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyGame extends Game {
	public static SpriteBatch batch;
	public static ShapeRenderer shapeRenderer;
    public static OrthographicCamera camera, UICamera;
    public static Viewport viewport, UIViewport;
    public static Stage stage, worldStage;
	public static BitmapFont font;
	public static ShaderProgram shaderProgram;
	public static EasyAssetManager manager;
	public static World world;

	public static TextureAtlas GUIAtlas;
	public static Box2DDebugRenderer debugRenderer;

	private static ExecutorService threadPool;

	public static AdInterface ads;
	public static Transactions transactions;
	public static ActionResolver actionResolver;

	public MyGame(AdInterface ads, Transactions transactions, ActionResolver actionResolver){

		MyGame.ads = ads;
		MyGame.transactions = transactions;
		MyGame.actionResolver = actionResolver;
	}

	@Override
	public void create () {
		world = new World(new Vector2(0f, 0f), true);
//		testWorld = new World(new Vector2(0f, 0f), true);

		world.setContactListener(new ContactListenerClass());
//        testWorld.setContactListener(new ContactListenerClass());

		debugRenderer = new Box2DDebugRenderer();

		font = new BitmapFont(Gdx.files.internal("fonts/mainFont.fnt"));

        camera = new OrthographicCamera(480, 800);
        UICamera = new OrthographicCamera(480, 800);

        viewport = new FillViewport(480, 800, camera);
        UIViewport = new FillViewport(480, 800, UICamera);

		manager = new EasyAssetManager();

        stage = new Stage(UIViewport);
        worldStage = new Stage(viewport);

		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);

        String FRAG = Gdx.files.internal("shaders/blackhole.frag").readString();
        String VERT = Gdx.files.internal("shaders/blackhole.vert").readString();

		shaderProgram = new ShaderProgram(VERT,FRAG);

		Loader.INSTANCE.loadAllImgs(manager, Gdx.files.internal("img"), false);
		Loader.INSTANCE.loadMusic(manager, Gdx.files.internal("music"));
		Loader.INSTANCE.loadAtlas(manager, Gdx.files.internal("atlas"), false);
        manager.finishLoading();

		MyGame.GUIAtlas = manager.get("GUI", TextureAtlas.class);

		int cores = Runtime.getRuntime().availableProcessors();
		if(cores > 1){
			threadPool = Executors.newFixedThreadPool(10);
		}

		JsonLevelLoader.INSTANCE.loadLevels();
		PlanetDataManager.INSTANCE.readDefinitionsJson();

		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		UICamera.update();

		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.setToOrtho(false, width, height);
		UICamera.setToOrtho(false, width, height);

		viewport.update(width, height, true);
//		stage.getViewport().update(width, height, true);
	}

	public static void postRunnable(Runnable runnable, boolean forceMain){
		if(threadPool != null && !forceMain)
			threadPool.submit(runnable);
		else {
		    System.out.println("Posting runnable on rendering thread");
            Gdx.app.postRunnable(runnable);
        }
	}
}
