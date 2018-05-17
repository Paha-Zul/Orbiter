package com.quickbite.spaceslingshot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.*;
import com.quickbite.spaceslingshot.data.json.PlanetDataManager;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;
import com.quickbite.spaceslingshot.screens.MainMenuScreen;
import com.quickbite.spaceslingshot.util.Constants;
import com.quickbite.spaceslingshot.util.ContactListenerClass;
import com.quickbite.spaceslingshot.util.EasyAssetManager;
import com.quickbite.spaceslingshot.json.JsonLevelLoader;
import com.quickbite.spaceslingshot.util.Loader;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyGame extends Game {
	public static SpriteBatch batch;
	public static ShapeRenderer shapeRenderer;
    public static OrthographicCamera camera, UICamera, Box2dCamera;
    public static Viewport viewport, UIViewport;
    public static Stage stage;
	public static BitmapFont font;
	public static BitmapFont scaledFont;
	public static EasyAssetManager manager;
	public static World world;

	public static TextureAtlas GUIAtlas;
	public static TextureAtlas gameScreenAtlas;
	public static Box2DDebugRenderer debugRenderer;

	private static ThreadPoolExecutor threadPool;

	public static AdInterface ads;
	public static Transactions transactions;
	public static ActionResolver actionResolver;

	public MyGame(AdInterface ads, Transactions transactions, ActionResolver actionResolver){

		MyGame.ads = ads;
		MyGame.transactions = transactions;
		MyGame.actionResolver = actionResolver;

//		GoogleApiAvailability
	}

	@Override
	public void create () {
//		gsClient =

		world = new World(new Vector2(0f, 0f), true);
//		testWorld = new World(new Vector2(0f, 0f), true);

		world.setContactListener(new ContactListenerClass());
//        testWorld.setContactListener(new ContactListenerClass());

		debugRenderer = new Box2DDebugRenderer();

		font = new BitmapFont(Gdx.files.internal("fonts/secFont.fnt"));
		scaledFont = new BitmapFont(Gdx.files.internal("fonts/secFont.fnt"));
		scaledFont.getData().setScale(0.15f);

        camera = new OrthographicCamera(480, 800);
        UICamera = new OrthographicCamera(480, 800);
		Box2dCamera = new OrthographicCamera(480 * Constants.BOX2D_SCALE, 800 * Constants.BOX2D_SCALE);

        viewport = new FitViewport(480, 800, camera);
        UIViewport = new FitViewport(480, 800, UICamera);

		manager = new EasyAssetManager();

        stage = new Stage(UIViewport);

		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);

		Loader.INSTANCE.loadAllImgs(manager, Gdx.files.internal("img"), false);
		Loader.INSTANCE.loadMusic(manager, Gdx.files.internal("music"));
		Loader.INSTANCE.loadAtlas(manager, Gdx.files.internal("atlas"), false);
        manager.finishLoading();

		MyGame.GUIAtlas = manager.get("GUI", TextureAtlas.class);
		MyGame.gameScreenAtlas = manager.get("gameScreenAtlas", TextureAtlas.class);

		int cores = Runtime.getRuntime().availableProcessors();
		if(cores > 1){
			threadPool = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
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
		Box2dCamera.update();
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
		UIViewport.update(width, height, true);
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
