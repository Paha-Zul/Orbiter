package com.quickbite.spaceslingshot.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.GameScreenInputListener
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.guis.GameScreenGUI
import com.quickbite.spaceslingshot.objects.Obstacle
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.Ship
import com.quickbite.spaceslingshot.util.*

/**
 * Created by Paha on 8/7/2016.
 * Handles the Game Screen functionality.
 */
class GameScreen(val game:MyGame, val levelToLoad:Int, endlessGame:Boolean = false) : Screen {
    val data = GameScreenData()
    lateinit var gui:GameScreenGUI
    val lineDrawer = LineDraw()

    lateinit var points:List<Vector2>

    lateinit var starryBackground: TextureRegion

    var physicsAccumulator = 0f
    var updateAccumulator = 0f

    var pauseLimit = 100f
    var pauseAmtPerTick = 0.1f

    val endlessGame:EndlessGame? = if(endlessGame) EndlessGame(data) else null

    companion object{
        var finished = false
        var lost = false
        var paused = true

        fun applyGravity(planet:Planet, ship:Ship){
            val dst = planet.position.dst(ship.position)
            if(dst <= planet.gravityRange){
                val pull = planet.getPull(dst)
                val angle = MathUtils.atan2(planet.position.y - ship.position.y, planet.position.x - ship.position.x )
                val x = MathUtils.cos(angle)*pull
                val y = MathUtils.sin(angle)*pull
                ship.addVelocity(x, y)
            }
        }
    }

    override fun show() {
        data.ship = Ship()
        runPredictor()
        gui = GameScreenGUI(this)

        Gdx.input.inputProcessor = InputMultiplexer(MyGame.stage, GameScreenInputListener(this))

        val background = MyGame.manager["starry", Texture::class.java]
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        starryBackground = TextureRegion(background)
        starryBackground.setRegion(0,0,480, 800)

        if(levelToLoad != -1)
            loadLevel(levelToLoad)
        else
            endlessGame?.start()

        this.gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)

        runPredictor()

//        data.asteroidSpawnerList.add(AsteroidSpawner(Vector2(100f, 100f), Vector2(1f, 0f), Pair(1f, 5f), Pair(1f, 2f), data))
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        update(delta)
        if(!paused) doPhysicsStep(delta)
        draw(MyGame.batch)

        MyGame.debugRenderer.render(MyGame.world, MyGame.camera.combined)

        MyGame.stage.act()
        MyGame.stage.draw()
    }

    private fun update(delta:Float){
        EventSystem.executeEventQueue()
        endlessGame?.update(delta)

        //Not pausePhysics update...
        if(!paused) {
            pauseLimit = Math.min(pauseLimit + pauseAmtPerTick, 100f)

//            runPredictor()
            data.ship.update(delta)
            data.planetList.forEach { obj -> obj.update(delta)}
            data.asteroidSpawnerList.forEach { spawner -> spawner.update(delta) }
            data.stationList.forEach { station -> station.update(delta) }

            for(i in (data.asteroidList.size-1).downTo(0)){
                data.asteroidList[i].update(delta)
                if(data.asteroidList[i].dead) {
                    data.asteroidList[i].dispose()
                    data.asteroidList.removeIndex(i)
                }
            }

            gui.fuelBar.setAmounts(data.ship.fuel, data.ship.fuelTaken)

            if(GameScreen.finished){
                endlessGame?.reset()
                gui.showGameOver(lost)
                setGamePaused(true)
            }

        //Paused update...
        }else{
            pauseLimit -= pauseAmtPerTick
            if(pauseLimit <= 0)
                setGamePaused(false)
//            lineDrawer.setStartAndEnd(data.ship.burnBallBasePosition, data.ship.burnHandleLocation)
        }

        gui.bottomPauseButton.value = pauseLimit
    }

    private fun doPhysicsStep(deltaTime: Float) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        val frameTime = Math.min(deltaTime, 0.25f)
        physicsAccumulator += frameTime
        while (physicsAccumulator >= Constants.PHYSICS_TIME_STEP) {
            data.ship.fixedUpdate()
            data.planetList.forEach { p -> p.fixedUpdate() }
            MyGame.world.step(Constants.PHYSICS_TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS)
            physicsAccumulator -= Constants.PHYSICS_TIME_STEP
        }
    }

    private fun draw(batch: SpriteBatch){
        batch.projectionMatrix = MyGame.camera.combined
        batch.begin()

        batch.draw(starryBackground, MyGame.camera.position.x - MyGame.camera.viewportWidth/2f, MyGame.camera.position.y- MyGame.camera.viewportHeight/2f,
                MyGame.camera.viewportWidth, MyGame.camera.viewportHeight)

        if(paused)
            lineDrawer.draw(batch)

        //Draw planets
        data.planetList.forEach { obj -> obj.draw(batch)}

        //Draw obstacles
        data.obstacleList.forEach { obj -> obj.draw(batch)}

        //Draw asteroids
        data.asteroidList.forEach { asteroid -> asteroid.draw(batch) }

        //draw stations
        data.stationList.forEach { station -> station.draw(batch) }

        data.ship.draw(batch)

        if(paused)
            data.ship.drawHandles(batch)


        batch.end()

        val renderer = MyGame.shapeRenderer
        renderer.projectionMatrix = MyGame.camera.combined
        renderer.begin(ShapeRenderer.ShapeType.Filled)

//        drawCenters()
        drawPrediction(MyGame.shapeRenderer)

        renderer.end()

    }

    private fun drawCenters(renderer:ShapeRenderer){
        val size = 5f
        renderer.color = Color.BLUE
        data.planetList.forEach { planet ->
            renderer.circle(planet.position.x, planet.position.y, size)
        }
        renderer.circle(data.ship.position.x, data.ship.position.y, size)
    }

    private fun drawPrediction(renderer:ShapeRenderer){
        val listSize = points.size*10
        val color: Color = Color(Color.BLUE)
        points.forEachIndexed {i, point ->
            val t:Float = i/listSize.toFloat()
            color.lerp(Color.GREEN, t)
            renderer.color = color
            renderer.circle(point.x, point.y, 3f)
        }
    }

    /**
     * @return An Integer representing the result. 0 for collision, 1 for collided with target (passed), 2 for nothing
     */
    private fun checkHitPlanet(planets: Array<Planet>, obstacles:Array<Obstacle>, ship: Ship):Int{
        var dead = false
        var passed = false
        planets.forEach { planet ->
            val dst = planet.position.dst(ship.position)
            if(dst <= planet.radius){
                if(!planet.homePlanet) {
                    dead = true
                    return@forEach
                }else{
                    passed = true
                    return@forEach
                }
            }
        }

        obstacles.forEach { obstacle ->
            if (obstacle.rect.contains(ship.position)) {
                dead = true
                return@forEach
            }
        }

        return if(dead) 0 else if(passed) 1 else 2
    }

    fun toggleShipBurn(shipLocation: Ship.ShipLocation){
        data.ship.toggleDoubleBurn(shipLocation)
        gui.fuelBar.setAmounts(data.ship.fuel, data.ship.fuelTaken)
    }

    fun reloadLevel():Boolean{
        GameScreen.lost = false
        GameScreen.finished = false

        val success = loadLevel(data.currLevel)
        runPredictor()
        gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        return success
    }

    fun loadLevel(level:Int):Boolean{
        GameScreen.lost = false
        GameScreen.finished = false

        val success:Boolean
        if(endlessGame == null) {
            success = GameLevels.loadLevel(level, data)
            data.currLevel = level
            runPredictor()
            gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        }else{
            endlessGame.reset()
            success = true
        }

        return success
    }

    fun loadNextLevel():Boolean{
        GameScreen.lost = false
        GameScreen.finished = false

        val success = GameLevels.loadLevel(++data.currLevel, data)
        runPredictor()
        gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        return success
    }

    fun runPredictor(){
        Predictor.runPrediction(data.ship, {pauseAllPhysicsExceptPredictorShip()}, {resumeAllPhysicsExceptPredictorShip()}, {doPhysicsStep(Constants.PHYSICS_TIME_STEP)})
        points = Predictor.pointsList
    }

    fun toggleGamePause(){
        setGamePaused(!GameScreen.paused)
    }

    fun setGamePaused(paused:Boolean){
        GameScreen.paused = paused

        if(paused) {
            runPredictor()
            gui.bottomPauseText.setText("Resume")
//            pauseAllPhysicsExceptPredictorShip()
//            data.obstacleList.forEach { o ->  }
        }else{
//            resumeAllPhysicsExceptPredictorShip()
        }
    }

    /**
     * Pauses all physics except for the test ship predictor
     */
    fun pauseAllPhysicsExceptPredictorShip(){
        data.planetList.forEach { p -> p.setPhysicsPaused(true) }
        data.asteroidList.forEach { a -> a.setPhysicsPaused(true) }
        data.ship.setPhysicsPaused(true)
    }

    /**
     * Resumes all physics except for the test ship predictor
     */
    fun resumeAllPhysicsExceptPredictorShip(){
        data.planetList.forEach { p -> p.setPhysicsPaused(false) }
        data.asteroidList.forEach { a -> a.setPhysicsPaused(false) }
        data.ship.setPhysicsPaused(false)
    }

    fun scrollScreen(x:Float, y:Float){
        val diff = Vector2(x - MyGame.camera.position.x, MyGame.camera.position.y - y)
        MyGame.camera.position.set(x, y, 0f)
        starryBackground.scroll(diff.x/MyGame.camera.viewportWidth, diff.y/MyGame.camera.viewportHeight)
    }

    override fun resume() {

    }

    override fun dispose() {
        gui.dispose()
        data.reset()
        data.ship.dispose()
//        Predictor.dispose()
    }
}