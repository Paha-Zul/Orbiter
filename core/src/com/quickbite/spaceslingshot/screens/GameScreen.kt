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
import com.quickbite.spaceslingshot.Predictor
import com.quickbite.spaceslingshot.data.GameLevels
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.guis.GameScreenGUI
import com.quickbite.spaceslingshot.objects.Obstacle
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.Ship
import com.quickbite.spaceslingshot.util.LineDraw

/**
 * Created by Paha on 8/7/2016.
 */
class GameScreen(val game:MyGame) : Screen {
    val data = GameScreenData()
    lateinit var gui:GameScreenGUI
    val lineDrawer = LineDraw()

    var paused = true
    lateinit var points:List<Vector2>

    lateinit var starryBackground: TextureRegion

    override fun show() {
        data.ship = Ship()
        runPredictor()
        gui = GameScreenGUI(this)

        Gdx.input.inputProcessor = InputMultiplexer(MyGame.stage, GameScreenInputListener(this))

        val background = MyGame.manager["starry", Texture::class.java]
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        starryBackground = TextureRegion(background)
        starryBackground.setRegion(0,0,480, 800)
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        update(delta)
        draw(MyGame.batch)

        MyGame.stage.act()
        MyGame.stage.draw()
    }

    private fun update(delta:Float){
        //Not paused update...
        if(!paused) {
            runPredictor()

            applyGravity(data.planetList, data.ship)
            data.ship.update(delta)
            data.planetList.forEach { obj ->
                obj.update(delta)
            }

            gui.fuelBar.setAmounts(data.ship.fuel, data.ship.burnTime * data.ship.burnPerTick)

            val result = checkHitPlanet(data.planetList, data.obstacleList, data.ship)
            if(result == 0){
                gui.showGameOver(true)
                setGamePaused(true)
            }else if(result == 1){
                gui.showGameOver(false)
                setGamePaused(true)
            }

        //Paused update...
        }else{
            lineDrawer.setStartAndEnd(data.ship.burnBallBasePosition, data.ship.burnBallPosition)
        }
    }

    //Broken
    private fun fixedUpdate(){
        if(paused) return

//        applyGravity(data.planetList, data.ship)
        data.ship.fixedUpdate()
        data.planetList.forEach { obj ->
            obj.fixedUpdate()
        }
    }

    private fun draw(batch: SpriteBatch){
        batch.projectionMatrix = MyGame.camera.combined
        batch.begin()

        batch.draw(starryBackground, MyGame.camera.position.x - MyGame.camera.viewportWidth/2f, MyGame.camera.position.y- MyGame.camera.viewportHeight/2f,
                MyGame.camera.viewportWidth, MyGame.camera.viewportHeight)

        if(paused)
            lineDrawer.draw(batch)

//        batch.shader = MyGame.shaderProgram
        data.planetList.forEach { obj ->
            obj.draw(batch)
        }
//        batch.shader = null

        data.obstacleList.forEach { obj ->
            obj.draw(batch)
        }

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

    private fun applyGravity(planets: Array<Planet>, ship: Ship){
        planets.forEach { planet ->
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

    fun toggleShipBurn(){
        data.ship.toggleDoubleBurn()
        gui.fuelBar.setAmounts(data.ship.fuel, data.ship.burnAmount)
    }

    fun reloadLevel():Boolean{
        val success = GameLevels.loadLevel(data.currLevel, data)
        runPredictor()
        gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        return success
    }

    fun loadLevel(level:Int):Boolean{
        val success = GameLevels.loadLevel(level, data)
        data.currLevel = level
        runPredictor()
        gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        return success
    }

    fun loadNextLevel():Boolean{
        val success = GameLevels.loadLevel(++data.currLevel, data)
        runPredictor()
        gui.fuelBar.setAmounts(data.ship.fuel, 0f, data.ship.fuel)
        return success
    }

    fun runPredictor(){
        Predictor.runPrediction(data.planetList, data.ship, { planets, ship ->
            applyGravity(planets, ship)
            val hit = checkHitPlanet(planets, data.obstacleList, ship)
            if(hit == 0 || hit == 1)
                return@runPrediction

        })

        points = Predictor.pointsList
    }

    fun toggleGamePause(){
        setGamePaused(!this.paused)
    }

    fun setGamePaused(paused:Boolean){
        this.paused = paused

        if(paused) {
            runPredictor()
            gui.bottomPauseButton.setText("Resume")
        }else{
//            points = listOf()
        }
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
        data.obstacleList.clear()
        data.planetList.clear()
    }
}