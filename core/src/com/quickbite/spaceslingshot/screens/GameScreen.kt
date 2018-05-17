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
import com.quickbite.spaceslingshot.LevelManager
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.data.json.JsonLevelData
import com.quickbite.spaceslingshot.guis.GameScreenGUI
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Obstacle
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Planet
import com.quickbite.spaceslingshot.objects.gamescreenobjects.PlayerShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.ShipBase
import com.quickbite.spaceslingshot.util.*

/**
 * Created by Paha on 8/7/2016.
 * Handles the Game Screen functionality.
 */
class GameScreen(val game:MyGame, val levelToLoad:Int, val isEndlessGame:Boolean = false) : Screen {

    lateinit var starryBackground: TextureRegion

    var physicsAccumulator = 0f

    companion object{
        lateinit var gameScreenData:GameScreenData
        val predictorLineDrawer = LineDraw(Vector2(), Vector2(), GH.createPixel(Color.WHITE))
        lateinit var gui:GameScreenGUI

        var finished = false
        var lost = false

        var paused = true
            set(value){
                field = value
                if(!value){
                    Tests.clearShipList()
                }
            }

        var pauseTimer = false

        fun setGameOver(lost:Boolean){
            GameScreen.finished = true
            GameScreen.lost = lost
        }

        fun applyGravity(planet: Planet, ship: ShipBase){
            val dst = planet.position.dst(ship.position)
            if(dst <= planet.gravityRange){
                val pull = planet.getPull(dst)
                val angle = MathUtils.atan2(planet.position.y - ship.position.y, planet.position.x - ship.position.x )
                val x = MathUtils.cos(angle)*pull
                val y = MathUtils.sin(angle)*pull
                ship.addVelocity(x, y)
            }
        }

        /**
         * Pauses all physics except for the test ship predictor which is enabled
         */
        fun pauseAllPhysicsExceptPredictorShip(data:GameScreenData){
            data.planetList.forEach { p -> p.setPhysicsPaused(true) }
            data.asteroidList.forEach { a -> a.setPhysicsPaused(true) }
            data.fuelContainerList.forEach { a -> a.setPhysicsPaused(true) }
            data.ship.setPhysicsPaused(true)
        }

        /**
         * Resumes all physics except for the test ship predictor which is disabled
         */
        fun resumeAllPhysicsExceptPredictorShip(data:GameScreenData){
            data.planetList.forEach { p -> p.setPhysicsPaused(false) }
            data.asteroidList.forEach { a -> a.setPhysicsPaused(false) }
            data.fuelContainerList.forEach { a -> a.setPhysicsPaused(false) }
            data.ship.setPhysicsPaused(false)
        }
    }

    init{
        gameScreenData = GameScreenData(this, isEndlessGame)
    }

    override fun show() {
        //This has to be loaded here because the endless game needs the game screen gameScreenData to work
        gameScreenData.endlessGame = if(isEndlessGame) EndlessGame(this) else null

        finished = false
        lost = false
        paused = true
        pauseTimer = false

        gameScreenData.ship = PlayerShip()
        Predictor.queuePrediction = true
        gui = GameScreenGUI(this)

        Gdx.input.inputProcessor = InputMultiplexer(MyGame.stage, GameScreenInputListener(this))

        val background = MyGame.manager["starry", Texture::class.java]
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        starryBackground = TextureRegion(background)
        starryBackground.setRegion(0, 0, 480, 800)

        //If we aren't doing endless (which is -1), load the level.
        if(levelToLoad != -1) {
            LevelManager.loadLevel(levelToLoad, this)
        }else
            gameScreenData.endlessGame?.start()

        GameScreen.gui.fuelBar.setAmounts(gameScreenData.ship.fuel, 0f, gameScreenData.ship.fuel)

        Predictor.init(gameScreenData.ship, {pauseAllPhysicsExceptPredictorShip(gameScreenData)}, { resumeAllPhysicsExceptPredictorShip(gameScreenData)},
                {doPhysicsStep(Constants.PHYSICS_TIME_STEP)})
        Predictor.queuePrediction = true

        predictorLineDrawer.size = 3
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        //General update of things
        update(delta)
        //This is the physics update
        if(!paused)
            doPhysicsStep(delta)
        //Then we draw
        draw(MyGame.batch)

        //Debug render AFTER we draw
//        MyGame.debugRenderer.render(MyGame.world, MyGame.Box2dCamera.combined)

        //Update stage
        MyGame.stage.act()
        MyGame.stage.draw()
    }

    private fun update(delta:Float){
        EventSystem.executeEventQueue()
        gameScreenData.endlessGame?.update(delta)
        Predictor.update()

        //Not paused update
        if(!paused) {
            //If the timer isn't paused, subtract time from it
            if(!pauseTimer)
                gameScreenData.levelTimer += delta

            //Clamp the pause limit
            gameScreenData.pauseLimit = Math.min(gameScreenData.pauseLimit + Constants.PAUSE_AMTPERTICK, 100f)

//            runPredictor()
           //Update ship, planets, asteroids, stations, fuel containers here
            gameScreenData.ship.update(delta)
            gameScreenData.planetList.forEach { obj -> obj.update(delta)}
            gameScreenData.asteroidSpawnerList.forEach { spawner -> spawner.update(delta) }
            gameScreenData.stationList.forEach { station -> station.update(delta) }
            gameScreenData.fuelContainerList.forEach { station -> station.update(delta) }

            //Kill off asteroids here if dead
            for(i in (gameScreenData.asteroidList.size-1).downTo(0)){
                gameScreenData.asteroidList[i].update(delta)
                if(gameScreenData.asteroidList[i].dead) {
                    gameScreenData.asteroidList[i].dispose()
                    gameScreenData.asteroidList.removeIndex(i)
                }
            }

            //Kill off fuel containers here if dead
            for(i in (gameScreenData.fuelContainerList.size-1).downTo(0)){
                gameScreenData.fuelContainerList[i].update(delta)
                if(gameScreenData.fuelContainerList[i].dead) {
                    gameScreenData.fuelContainerList[i].dispose()
                    gameScreenData.fuelContainerList.removeIndex(i)
                }
            }

            //Sync up the fuel bar (GUI) with the ship's fuel
            gui.fuelBar.setAmounts(gameScreenData.ship.fuel, gameScreenData.ship.fuelTaken)

            //TODO Wtf is this
//            Tests.addToShipList(Tests.MovementData(Vector2(gameScreenData.ship.position), Vector2(gameScreenData.ship.velocity), gameScreenData.ship.rotation,
//                    gameScreenData.ship.planetList.size, gameScreenData.ship.fuel, delta))

            Tests.update()

            //If the game is over, pause!
            if(GameScreen.finished){
                gui.showGameOver(lost)
                setGamePaused(true)
                gameOver() //Run the game over logic
            }

        //Paused update...
        }else{
            //Don't allow the pause counter to go down if we are on the game over screen
            if(!GameScreen.finished) {
                gameScreenData.pauseLimit -= Constants.PAUSE_AMTPERTICK
                if (gameScreenData.pauseLimit <= 0)
                    setGamePaused(false)
            }
        }

        gui.bottomPauseProgressBar.value = gameScreenData.pauseLimit
        gui.update(delta)
    }

    private fun doPhysicsStep(deltaTime: Float) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        val frameTime = Math.min(deltaTime, 0.25f)
        physicsAccumulator += frameTime

        while (physicsAccumulator >= Constants.PHYSICS_TIME_STEP) {
            physicsAccumulator -= Constants.PHYSICS_TIME_STEP //Decrement the accumulator

            gameScreenData.ship.fixedUpdate(Constants.PHYSICS_TIME_STEP) //Update the ship
            gameScreenData.planetList.forEach { p -> p.fixedUpdate(Constants.PHYSICS_TIME_STEP) } //Update the planets
            gameScreenData.fuelContainerList.forEach { p -> p.fixedUpdate(Constants.PHYSICS_TIME_STEP) } //Update the planets
            MyGame.world.step(Constants.PHYSICS_TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS) //Update the physics world
        }
    }

    private fun draw(batch: SpriteBatch){
        batch.projectionMatrix = MyGame.camera.combined
        batch.begin()

        batch.draw(starryBackground, MyGame.camera.position.x - MyGame.camera.viewportWidth/2f, MyGame.camera.position.y- MyGame.camera.viewportHeight/2f,
                MyGame.camera.viewportWidth, MyGame.camera.viewportHeight)

        //When we're paused, draw the thruster handles and lines.
        if(paused) {
            drawShipControls(batch)
        }

        //Draw planets
        gameScreenData.planetList.forEach { obj -> obj.drawRing(batch)}
        gameScreenData.planetList.forEach { obj -> obj.draw(batch)}

        //Draw obstacles
        gameScreenData.obstacleList.forEach { obj -> obj.draw(batch)}

        //Draw asteroids
        gameScreenData.asteroidList.forEach { asteroid -> asteroid.draw(batch) }

        //draw stations
        gameScreenData.stationList.forEach { station -> station.draw(batch) }

        //draw fuel containers
        gameScreenData.fuelContainerList.forEach { station -> station.draw(batch) }

        //Draw the predictor line
        if(!GameScreen.finished)
            predictorLineDrawer.draw(batch)

        //Draw the ship AFTER the predictor line
        gameScreenData.ship.draw(batch)

        batch.end()

        val renderer = MyGame.shapeRenderer
        renderer.projectionMatrix = MyGame.camera.combined
        renderer.begin(ShapeRenderer.ShapeType.Filled)

        renderer.end()
    }

    private fun drawShipControls(batch:SpriteBatch){

    }

    private fun drawCenters(renderer:ShapeRenderer){
        val size = 5f
        renderer.color = Color.BLUE
        gameScreenData.planetList.forEach { planet ->
            renderer.circle(planet.position.x, planet.position.y, size)
        }
        renderer.circle(gameScreenData.ship.position.x, gameScreenData.ship.position.y, size)
    }

    /**
     * @return An Integer representing the result. 0 for collision, 1 for collided with target (passed), 2 for nothing
     */
    private fun checkHitPlanet(planets: Array<Planet>, obstacles:Array<Obstacle>, ship: PlayerShip):Int{
        var dead = false
        var passed = false
        planets.forEach { planet ->
            val dst = planet.position.dst(ship.position)
            if(dst <= planet.size){
                passed = true
                return@forEach
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

    fun toggleShipBurn(shipLocation: ShipBase.ShipLocation){
        gameScreenData.ship.toggleDoubleBurn(shipLocation)
        gui.fuelBar.setAmounts(gameScreenData.ship.fuel, gameScreenData.ship.fuelTaken)
    }

    /**
     * Called when the game is over
     */
    fun gameOver(){
        if(!lost) {
            val level = LevelManager.levels[gameScreenData.currLevel]
            checkAchievementCompletion(level)
        }
        gameScreenData.endlessGame?.finish()
    }

    private fun checkAchievementCompletion(level:JsonLevelData){
        //Loop over each achievement for the level and set our local achievement
        level.achievements.forEachIndexed { i, achievement ->
            when(achievement[0]){
                "win" -> {
                    //if we didn't lose, we're good!
                    if(!GameScreen.lost)
                        gameScreenData.achievementFlags[i] = true
                }
                "time" -> {
                    //If our total time is less than the achievement time, we're good!
                    val time = achievement[1].toFloat()
                    if(gameScreenData.levelTimer <= time)
                        gameScreenData.achievementFlags[i] = true
                }
                "fuel" -> {
                    //If the ship's fuel percentage is greater than the achievement goal, we're good!
                    val fuel = achievement[1].toFloat()
                    if(gameScreenData.ship.fuel/ gameScreenData.ship.maxFuel >= fuel/100f)
                        gameScreenData.achievementFlags[i] = true
                }
            }
        }

        //Save the achievements
        AchievementManager.saveAchievementsToPref(gameScreenData.achievementFlags, level.level.toString())
    }

    fun reset(){
        GameScreen.lost = false
        GameScreen.finished = false
        GameScreen.pauseTimer = false
        gameScreenData.levelTimer = 0f
        gameScreenData.pauseLimit = 100f
        gameScreenData.endlessGame?.reset()
    }

    fun toggleGamePause(){
        setGamePaused(!GameScreen.paused)
    }

    fun setGamePaused(paused:Boolean){
        GameScreen.paused = paused

        if(paused) {
            Predictor.setShipVelocityAsCurrentPredictorVelocity()
            Predictor.queuePrediction = true
            gui.bottomPauseText.setText("Resume")
        }else{

        }
    }

    fun scrollScreen(x:Float, y:Float){
        val diff = Vector2(x - MyGame.camera.position.x, MyGame.camera.position.y - y)
        MyGame.camera.position.set(x, y, 0f)
        val camPos = MyGame.camera.position
        MyGame.Box2dCamera.position.set(camPos.x*Constants.BOX2D_SCALE, camPos.y*Constants.BOX2D_SCALE, 0f)
        starryBackground.scroll(diff.x/MyGame.camera.viewportWidth, diff.y/MyGame.camera.viewportHeight)
    }

    override fun resume() {

    }

    override fun dispose() {
        gui.dispose()
        gameScreenData.reset()
        gameScreenData.ship.dispose()
//        Predictor.dispose()
    }
}