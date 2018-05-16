package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.*

class PlayerShip(position: Vector2, fuel:Float) : ShipBase(position, fuel) {

    private class DockingData(val position: Vector2, val rotation:Float, val callback:()->Unit)

    private val sprite: Sprite = Sprite(MyGame.gameScreenAtlas.findRegion("spaceship"))
    private lateinit var ring: Sprite
    private lateinit var thrustFireSprite: Sprite
    private val thrustFirePositionPercent = Vector2(0.68f, 0f)

    private val thrusterLineDrawers:List<LineDraw> = List(3, { LineDraw(Vector2(), Vector2(), MyGame.manager["dash", Texture::class.java]) })


    private var hideShipSprite = false
    var hideControls = false

    private val easeAlpha: Interpolation = Interpolation.linear

    var docking = false
    var dockingElapsed = 0f
    val dockingTime = 2f

    var exploding = false
    var explodingElapsed = 0f
    private var explodingEffect = ParticleEffect()
    var timeCounter = 0f

    val explodingTime = 2f

    val tmpVector = Vector2()

    private lateinit var dockingData: DockingData

    override var rotation = 0f

    val burnHandles:Array<BurnHandle> = arrayOf(
            BurnHandle(this, ShipLocation.Rear, 0f),
            BurnHandle(this, ShipLocation.Left, 90f),
            BurnHandle(this, ShipLocation.Right, -90f)
    )

    constructor():this(Vector2(0f, 0f), 0f)

    init {
        //The ship sprite
        sprite.setSize(shipHeight, shipWidth)
        sprite.setPosition(position.x - shipWidth / 2, position.y - shipHeight / 2)
        sprite.setOrigin(shipHeight/2f, shipWidth/2f)

        //The ring around the ship with handles and stuff, indicates it can be rotated
        ring = Sprite(MyGame.gameScreenAtlas.findRegion("arrowCircle"))
        ring.setPosition(position.x - ringRadius, position.y - ringRadius)
        ring.setSize(ringRadius * 2f, ringRadius * 2f)
        ring.color = Color.WHITE
        ring.setOrigin(ringRadius, ringRadius)

        //The fire for thrust
        thrustFireSprite = Sprite(MyGame.gameScreenAtlas.findRegion("thrustFire"))
        thrustFireSprite.setSize(24f, 24f)
        thrustFireSprite.setOrigin(thrustFireSprite.width/2f, thrustFireSprite.height/2f)

        explodingEffect.load(Gdx.files.internal("particles/Explosion.p"), MyGame.gameScreenAtlas)
        explodingEffect.allowCompletion()

        //Initially set the rotation
        setRotationTowardsMouse(0f, 0f)

        //When we collide with something
        EventSystem.onEvent("collide_begin", { args ->
            val other = args[0] as Fixture
            val otherData = other.body.userData as BodyData

            //If the other thing was a planet
            if(otherData.type == BodyData.ObjectType.Planet) {
                //If it wasn't a sensor, game over man!
                if (!other.isSensor) {
                    setExploding()
                }

                //If the other fixture is a sensor and it's body belongs to a planet, we are in the gravity well
                else if (other.isSensor) {
                    val planet = otherData.bodyOwner as Planet
                }

                //If we collided with a station...
            }else if(otherData.type == BodyData.ObjectType.Station){
                //Call this event but delay it. Since this event will be called during a physics step, we can not
                //change anything physics related, so instead, delay it!
                EventSystem.callEvent("hit_station", listOf(this), otherData.id, true)

                //If we collided with a fuel container...
            }else if(otherData.type == BodyData.ObjectType.FuelContainer){
                val container = otherData.dataObject as FuelContainer
                this.fuel = Math.min(this.fuel + container.fuel, this.maxFuel)
                container.dead = true

                //If we collided with an asteroid....
            }else if(otherData.type == BodyData.ObjectType.Asteroid){
                val asteroidVelocity = other.body.linearVelocity.cpy().scl(Constants.BOX2D_SCALE * 1/2f) //TODO Magic Number!!!
                val shipVelocity = velocity //Grab the ships velocity
                Predictor.setShipVelocityAsCurrentPredictorVelocity() //Get the current velocity we need to be at
                shipVelocity.add(asteroidVelocity)
                Predictor.queuePrediction = true
            }

        }, this.uniqueID)

        //On collide_end, remove the planet from the list
        EventSystem.onEvent("collide_end", { args ->
            val other = args[0] as Fixture
            val otherData = other.body.userData as BodyData

            if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                val planet = otherData.bodyOwner as Planet
            }

        }, this.uniqueID)
    }

    override fun update(delta: Float) {
        super.update(delta)
    }

    override fun fixedUpdate(delta: Float) {
        super.fixedUpdate(delta)

        //If the physics are not paused, burn the fuel and let gravity pull us
        if(!physicsArePaused)
            realShipSimulation(delta)

        //If we are docking, perform the docking options
        if(docking){
            dockingElapsed += delta
            val progress = Math.min(1f, dockingElapsed/dockingTime)

            val rotation = easeAlpha.apply(this.rotation, dockingData.rotation, progress)
            tmpVector.interpolate(dockingData.position, progress, Interpolation.linear)

            setPosition(tmpVector)
            setShipRotation(rotation)

            if(progress >= 1){
                docking = false
                dockingElapsed = 0f
                dockingData.callback()
            }
        }

        //If we are exploding, do exploding stuff
        if(exploding){
            explodingElapsed += delta

            if(explodingElapsed >= explodingTime || explodingEffect.isComplete){
                exploding = false
                explodingElapsed = 0f
                GameScreen.setGameOver(true)
            }
        }
    }

    override fun draw(batch: SpriteBatch) {
        super.draw(batch)

        setAllDoubleBurnGraphics()
        sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
        ring.setPosition(position.x - ringRadius, position.y - ringRadius)
        burnHandles.forEach(BurnHandle::setPosition)
        thrusterLineDrawers.forEachIndexed { i, drawer ->
            val handle = burnHandles[i]
            drawer.setStartAndEnd(handle.burnHandleBasePosition, handle.burnHandlePosition)
        }

        //Only draw the handles and thruster lines if we are paused
        if(GameScreen.paused && !hideControls) {
            drawHandles(batch)
            thrusterLineDrawers.forEach { drawer -> drawer.draw(batch) }
        }

        if (!hideShipSprite) {
            sprite.rotation = rotation
            sprite.draw(batch)
            drawThrusters(batch)
        }

        //If we're exploding, explode!
        if (exploding)
            explodingEffect.draw(batch, Gdx.graphics.deltaTime)
    }

    override fun draw2(batch: SpriteBatch) {
        super.draw2(batch)
    }

    override fun dispose() {
        super.dispose()
    }

    override fun clickedOn(x: Float, y: Float): Boolean {
        return super.clickedOn(x, y)
    }

    override fun createBody() {
        val bodyDef = BodyDef()
//        bodyDef.type = if(testShip) BodyDef.BodyType.DynamicBody else BodyDef.BodyType.KinematicBody
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x* Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE)
        bodyDef.allowSleep = false

        val world = MyGame.world
        this.body = world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val shape = PolygonShape()

        shape.setAsBox((shipWidth/2f)*Constants.BOX2D_SCALE, (shipHeight/2f)*Constants.BOX2D_SCALE)

        mainFixture.shape = shape

        this.body.createFixture(mainFixture)

        shape.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this, this)
    }

    fun drawHandles(batch: SpriteBatch){
        setBurnHandlePosition()

        ring.draw(batch)
        burnHandles.forEach { handle -> handle.draw(batch) }
    }

    private fun drawThrusters(batch: SpriteBatch){
        thrusters.forEach { thruster ->
            if(thruster.burnTime > 0){
                setThrustFirePosition()
                thrustFireSprite.draw(batch)
            }
        }
    }

    private fun realShipSimulation(delta:Float){
        if(exploding) return

        body.setLinearVelocity(0f, 0f)
        timeCounter += delta //Increment out counter
        if(delta >= Constants.PHYSICS_TIME_STEP){ //If we pass over the time step amount
            timeCounter -= Constants.PHYSICS_TIME_STEP //Subtract that amount from our counter
            Predictor.currPointIndex++ //increase the point index
            if(Predictor.currPointIndex >= Predictor.points.size - 2){ //If we are past the last point, predict again!
                Predictor.setShipVelocityAsCurrentPredictorVelocity()
                Predictor.queuePrediction = true
            }
            val newPosition = Vector2(Predictor.points[Predictor.currPointIndex].position) //Make a copy so we don't change the predictor ship
            position.set(newPosition.x, newPosition.y)
            newPosition.set(newPosition.x* Constants.BOX2D_SCALE, newPosition.y* Constants.BOX2D_SCALE)
            body.setTransform(newPosition, rotation)
            burnFuel()
        }
    }

    override fun setShipRotation(rotation: Float) {
        super.setShipRotation(rotation)
        sprite.rotation = this.rotation
        burnHandles.forEach { handle -> handle.burnHandle.rotation = this.rotation + handle.rotationOffset }
        ring.rotation = this.rotation - 90 //Give an offset of 90 so the arrow doesn't sit under the burn ball
    }

    private fun setPosition(position:Vector2){
        this.setPosition(position.x, position.y)
    }

    /**
     * Sets the position of the ship. This sets both the rendering position and the physics body position.
     * @param x The X position (real value)
     * @param y The Y position (real value)
     */
    override fun setPosition(x:Float, y:Float){
        super.setPosition(x, y)

        sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
        ring.setPosition(position.x - ringRadius, position.y - ringRadius)
        burnHandles.forEach(BurnHandle::setPosition)
        setBurnHandlePosition()
    }

    private fun setBurnHandlePosition(){
        burnHandles.forEach { handle ->
            val thruster = getThruster(handle.burnHandleLocation)

            val angle = (rotation + thruster.rotationOffset)*MathUtils.degreesToRadians
            val x = ringRadius * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position of the burn ball
            val y = ringRadius * MathUtils.sin(angle) + MathUtils.cos(angle) //Original Y position of the burn ball
            val x2 = (ringRadius + thruster.burnTime) * MathUtils.cos(angle) - MathUtils.sin(angle) //Adjusted X position using the burn time.
            val y2 = (ringRadius + thruster.burnTime) * MathUtils.sin(angle) + MathUtils.cos(angle) //Adjusted Y position using the burn time.

            handle.burnHandleBasePosition.set(position.x + x, position.y + y)
            handle.burnHandlePosition.set(position.x + x2, position.y + y2 )
            handle.burnHandle.setPosition(handle.burnHandlePosition.x - BurnHandle.burnHandleSize, handle.burnHandlePosition.y - BurnHandle.burnHandleSize)
        }
    }



    /**
     * Sets the thrust fire's position in relation to the ship.
     */
    private fun setThrustFirePosition(){
        val angle = rotation*MathUtils.degreesToRadians
        val xOffset = sprite.width*thrustFirePositionPercent.x
        val x = (-xOffset) * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position of the burn ball
        val y = (-xOffset) * MathUtils.sin(angle) + MathUtils.cos(angle) //Original Y position of the burn ball

        thrustFireSprite.setPosition(this.position.x - thrustFireSprite.width/2f + x, this.position.y - thrustFireSprite.height/2f + y)
        thrustFireSprite.rotation = angle*MathUtils.radiansToDegrees
    }

    fun setDocking(position: Vector2, rotation:Float, callback:()->Unit){
        dockingElapsed = 0f
        docking = true
        dockingData = DockingData(position, rotation, callback)
        tmpVector.set(this.position) //We will use this to interpolate

        setVelocity(0f, 0f)
        thrusters.forEach { thruster -> thruster.burnTime = 0 }
    }

    /**
     * Sets the rotation of the ship towards the mouse. Also handles all graphics related to the ship.
     */
    fun setRotationTowardsMouse(mouseX:Float, mouseY:Float){
        val rot = MathUtils.atan2(mouseY - position.y, mouseX - position.x)*MathUtils.radiansToDegrees
        println(rot)
        setShipRotation(rot)

        setBurnHandlePosition()
    }

    fun setExploding(){
        exploding = true
        explodingEffect.setPosition(position.x, position.y)
        explodingEffect.start()
        hideShipSprite = true
    }

    /**
     * Determines what on the ship was clicked.
     * @param mouseX The mouse's X location
     * @param mouseY The mouse's Y location
     * @return An integer representing what was clicked. 2 = burn handle, 1 = rotation ring, 0 = nothing
     */
    fun clickOnShip(mouseX:Float, mouseY:Float):Pair<Int, ShipLocation>{
        val dst = position.dst(mouseX, mouseY)
        val result = findClickedOn(mouseX, mouseY, dst)
        return Pair(result.first, result.second)
    }

    /**
     *
     */
    private fun findClickedOn(mouseX:Float, mouseY: Float, dst:Float):MutablePair<Int, ShipLocation>{
        val hit:MutablePair<Int, ShipLocation> = MutablePair(0, ShipLocation.Rear)

        burnHandles.forEach { handle ->
            val dstToHandle = handle.burnHandlePosition.dst(mouseX, mouseY)
            if(dstToHandle <= BurnHandle.burnHandleSize){
                hit.set(2, handle.burnHandleLocation)
                return hit
            }
        }

        if(dst <= ringRadius){
            hit.set(1, ShipLocation.Rear)
            return hit
        }

        return hit
    }

    /** Toggles the double burn of the ship
     * @return True if the double burn is activated, false otherwise.
     */
    fun toggleDoubleBurn(shipLocation: ShipLocation):Boolean{
        val thruster = getThruster(shipLocation) //Get the thruster
        val result = thruster.toggleDoubleBurn(fuel) //Toggle the burn
        return result //Return the result
    }

    private fun setDoubleBurnGraphic(handle:BurnHandle, value:Boolean){
        when(value){
        //If we are double burning, set the texture and apply the bonus burn.
            true -> {
                handle.burnHandle.setRegion(BurnHandle.doubleBurnTexture)
                handle.burnHandle.color = Color.RED
            }

        //If we are not double burning, set the texture and reduce our burn.
            false ->{
                handle.burnHandle.setRegion(BurnHandle.normalBurnTexture)
                handle.burnHandle.color = Color.WHITE
            }
        }
    }

    private fun setAllDoubleBurnGraphics(){
        burnHandles.forEachIndexed { i, handle ->
            setDoubleBurnGraphic(handle, thrusters[i].doubleBurn)
        }
    }

    private fun getBurnHandle(shipLocation: ShipLocation):BurnHandle{
        return burnHandles.filter { it.burnHandleLocation == shipLocation }[0]
    }

    /**
     * Drags the burn handle to the location of the mouse
     * @param mouseX The mouse's X position
     * @param mouseY The mouse's Y position
     */
    fun dragBurn(mouseX: Float, mouseY: Float, shipLocation: ShipLocation) {
        val rotationOffset = GH.getRotationFromLocation(shipLocation)

        var dst = position.dst(mouseX, mouseY) - ringRadius
        if (dst <= 0) dst = 0f

        val thruster = getThruster(shipLocation)

        //The burn time is equal to the distance. If the burn amount is greater than the fuel we have, set it to the max
        thruster.burnTime = dst.toInt()
        if (thruster.burnAmount > fuel) {
            thruster.burnTime = (fuel / thruster.fuelBurnedPerTick).toInt()
        }

        this.setRotationTowardsMouse(mouseX, mouseY)
    }

    override fun reset(position: Vector2, fuel: Float, initialVelocity: Vector2) {
        super.reset(position, fuel, initialVelocity)
        this.hideShipSprite = false

        sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
        ring.setPosition(position.x - ringRadius, position.y - ringRadius)
        burnHandles.forEach(BurnHandle::setPosition)
        setBurnHandlePosition()
    }

    class BurnHandle(val ship: PlayerShip, val burnHandleLocation: ShipLocation, val rotationOffset:Float){
        companion object{
            var normalBurnTexture: TextureRegion = MyGame.gameScreenAtlas.findRegion("arrow")
            var doubleBurnTexture: TextureRegion = MyGame.gameScreenAtlas.findRegion("doubleArrow")
            const val burnHandleSize = 30f

        }

        val burnHandlePosition = Vector2()
        val burnHandleBasePosition = Vector2()
        var burnHandle:Sprite

        init{
            burnHandle = Sprite(normalBurnTexture)
            burnHandle.setPosition(ship.position.x - burnHandleSize, ship.position.y - burnHandleSize)
            burnHandle.setSize(burnHandleSize * 2f, burnHandleSize * 2f)
            burnHandle.setOrigin(burnHandle.width/2f, burnHandle.height/2f)
            burnHandle.color = Color.WHITE
        }

        fun setPosition(){
            burnHandle.setPosition(ship.position.x - burnHandleSize, ship.position.y - burnHandleSize)
        }

        fun draw(batch: SpriteBatch){
            burnHandle.draw(batch)
        }

        fun reset(){
            burnHandlePosition.set(0f, 0f)
        }
    }


}