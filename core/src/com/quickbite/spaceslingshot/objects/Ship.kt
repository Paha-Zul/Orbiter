package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.*
import java.util.*

/**
 * Created by Paha on 8/6/2016.
 * @param position The real 'world' coordinates of the ship. These will be used for everything (including rendering) except Box2D which is scaled.
 */
class Ship(val position:Vector2, var fuel:Float, initialVelocity:Vector2, val testShip:Boolean = false): IUpdateable, IDrawable, IUniqueID, IPhysicsBody, Disposable {
    enum class ShipLocation {Rear, Left, Right, Front}

    override var dead:Boolean  = false

    var maxFuel = fuel
        private set

    val fuelTaken:Float
        get() {
            var amount:Float = 0f
            thrusters.forEach { amount += it.burnAmount }
            return amount
        }

    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body

    private val tmpVector:Vector2 = Vector2()

    //Docking stuff.
    private lateinit var dockingData:DockingData
    var docking = false
    val dockingTime = 2f
    var dockingElapsed = 0f

    private val planetList:LinkedList<Planet> = LinkedList()

    private val velocityHolder = Vector2()

    var rotation = 0f
    get
    private set

    val thrusters:Array<Thruster> = arrayOf(
            Thruster(0.01f, 0.1f, Vector2(1f, 0f), ShipLocation.Rear, 0f),
            Thruster(0.005f, 0.1f, Vector2(0f, -1f), ShipLocation.Left, -90f),
            Thruster(0.005f, 0.1f, Vector2(0f, 1f), ShipLocation.Right, 90f)
    )

    val burnHandles:Array<BurnHandle> = arrayOf(
            BurnHandle(this, ShipLocation.Rear, 0f),
            BurnHandle(this, ShipLocation.Left, 90f),
            BurnHandle(this, ShipLocation.Right, -90f)
    )

    val velocity:Vector2
        get() = Vector2(body.linearVelocity.x*Constants.VELOCITY_INVERSESCALE, body.linearVelocity.y*Constants.VELOCITY_INVERSESCALE)

    override var physicsArePaused = false

    private lateinit var sprite:Sprite
    private lateinit var ring:Sprite
    private lateinit var thrustFireSprite:Sprite

    private val ringRadius = 100f
    private val shipWidth = 50f
    private val shipHeight = 50f

    private val thrustFirePositionPercent = Vector2(0.68f, 0f)

    private val easeAlpha: Interpolation = Interpolation.linear

    init{
        this.createBody()

        if(!testShip) {
            sprite = Sprite(MyGame.manager["spaceship", Texture::class.java])
            sprite.setSize(shipHeight, shipWidth)
            sprite.setPosition(position.x - shipWidth / 2, position.y - shipHeight / 2)
            sprite.setOrigin(shipHeight/2f, shipWidth/2f)

            val ringTexture = MyGame.manager["arrowCircle", Texture::class.java]
            ringTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

            ring = Sprite(ringTexture)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            ring.setSize(ringRadius * 2f, ringRadius * 2f)
            ring.color = Color.WHITE
            ring.setOrigin(ringRadius, ringRadius)

            thrustFireSprite = Sprite(MyGame.manager["thrustFire", Texture::class.java])
            thrustFireSprite.setSize(24f, 24f)
            thrustFireSprite.setOrigin(thrustFireSprite.width/2f, thrustFireSprite.height/2f)

            setRotationTowardsMouse(0f, 0f)

            //When we collide with something
            EventSystem.onEvent("collide_begin", { args ->
                val other = args[0] as Fixture
                val otherData = other.body.userData as BodyData

                //If the other thing was a planet
                if(otherData.type == BodyData.ObjectType.Planet) {
                    //If it wasn't a sensor, game over man!
                    if (!other.isSensor) {
                        val planet = otherData.bodyOwner as Planet
                        GameScreen.setGameOver(!planet.homePlanet)
                    }

                    //If the other fixture is a sensor and it's body belongs to a planet, we are in the gravity well
                    else if (other.isSensor) {
                        val planet = otherData.bodyOwner as Planet
                        planetList.add(planet)
                    }
                }else if(otherData.type == BodyData.ObjectType.Station){
                    //Call this event but delay it. Since this event will be called during a physics step, we can not
                    //change anything physics related, so instead, delay it!
                    EventSystem.callEvent("hit_station", listOf(this), otherData.id, true)
                }

            }, this.uniqueID)

            //On collide_end, remove the planet from the list
            EventSystem.onEvent("collide_end", { args ->
                val other = args[0] as Fixture
                val otherData = other.body.userData as BodyData

                if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                    val planet = otherData.bodyOwner as Planet
                    planetList.remove(planet)
                }

            }, this.uniqueID)

        //If we are the test ship
        }else{
            EventSystem.onEvent("collide_begin", { args ->
                val other = args[0] as Fixture
                val otherData = other.body.userData as BodyData

                //If the other fixture is a sensor and it's body belongs to a planet, we are in the gravity well
                if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                    val planet = otherData.bodyOwner as Planet
                    planetList.add(planet)
                }

            }, this.uniqueID)

            //On collide_end, remove the planet from the list
            EventSystem.onEvent("collide_end", { args ->
                val other = args[0] as Fixture
                val otherData = other.body.userData as BodyData

                if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                    val planet = otherData.bodyOwner as Planet
                    planetList.remove(planet)
                }

            }, this.uniqueID)
        }
    }

    //Empty constructor
    constructor():this(Vector2(0f, 0f), 0f, Vector2(0f, 0f), false)

    override fun update(delta: Float) {

    }

    override fun fixedUpdate(delta: Float) {
        if(!physicsArePaused) {
            burnFuel()
            planetList.forEach { planet ->
                val dst = planet.position.dst(this.position)
                if(dst <= planet.gravityRange)
                    GameScreen.applyGravity(planet, this)
            }
            position.set(body.position.x*Constants.BOX2D_INVERSESCALE, body.position.y*Constants.BOX2D_INVERSESCALE)
        }

        if(docking){
            dockingElapsed += delta
            val progress = Math.min(1f, dockingElapsed/dockingTime)

            val rotation = easeAlpha.apply(this.rotation, dockingData.rotation, progress)
            tmpVector.interpolate(dockingData.position, progress, Interpolation.linear)

            setPosition(tmpVector)
            setShipRotation(rotation)

            if(dockingElapsed >= dockingTime){
                docking = false
                dockingElapsed = 0f
                dockingData.callback()
            }
        }
    }

    override fun draw(batch: SpriteBatch) {

        //Adjust all sprites
        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandles.forEach(BurnHandle::setPosition)
        }

        sprite.draw(batch)
        drawThrusters(batch)
    }

    fun setAllFuel(fuel:Float, maxFuel:Float = fuel){
        this.fuel = fuel
        this.maxFuel = maxFuel
    }

    fun drawThrusters(batch: SpriteBatch){
        thrusters.forEach { thruster ->
            if(thruster.burnTime > 0){
                setThrustFirePosition()
                thrustFireSprite.draw(batch)
            }
        }
    }

    fun drawHandles(batch: SpriteBatch){
        setBurnHandlePosition()

        ring.draw(batch)
        burnHandles.forEach { handle -> handle.draw(batch) }
    }

    fun addVelocity(x:Float, y:Float){
        body.setLinearVelocity(body.linearVelocity.x + x*Constants.VELOCITY_SCALE, body.linearVelocity.y + y*Constants.VELOCITY_SCALE)
}

    fun addVelocityForward(force:Float){
        val angle = rotation*MathUtils.degreesToRadians
        val x = MathUtils.cos(angle)*force
        val y = MathUtils.sin(angle)*force
        addVelocity(x, y)
    }

    fun addVelocityDirection(force:Float, facing:Vector2){
        val angle = (rotation + facing.angle())*MathUtils.degreesToRadians
        val x = MathUtils.cos(angle)*force
        val y = MathUtils.sin(angle)*force
        addVelocity(x, y)
    }

    /**
     * Sets the velocity of the ship
     * @param x The X velocity
     * @param y The Y velocity
     */
    fun setVelocity(x:Float, y:Float){
        body.setLinearVelocity(x*Constants.VELOCITY_SCALE, y*Constants.VELOCITY_SCALE)
    }

    /**
     * Sets the rotation of the ship towards the mouse. Also handles all graphics related to the ship.
     */
    fun setRotationTowardsMouse(mouseX:Float, mouseY:Float, rotationOffset:Float = 0f){
        val rot = MathUtils.atan2(mouseY - position.y, mouseX - position.x)*MathUtils.radiansToDegrees
        setShipRotation(rot, rotationOffset)

        setBurnHandlePosition()
    }

    /**
     * Sets the ships rotation along with the graphics of the ship
     * @param rotation The rotation in degrees.
     */
    fun setShipRotation(rotation:Float, rotationOffset:Float = 0f){
        this.rotation = rotation + rotationOffset
        this.body.setTransform(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE, rotation*MathUtils.degreesToRadians)
        if(!testShip) {
            sprite.rotation = this.rotation
            burnHandles.forEach { handle -> handle.burnHandle.rotation = this.rotation + handle.rotationOffset }
            ring.rotation = this.rotation - 90 //Give an offset of 90 so the arrow doesn't sit under the burn ball
        }
    }

    /**
     * Sets the burn handle's position using the ships rotation.
     */
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

    /**
     * Burns a tick of fuel.
     */
    private fun burnFuel(){
        thrusters.forEach { thruster ->
            val result = thruster.burnFuel(fuel)
            fuel -= result.first
            addVelocityDirection(result.second, thruster.burnDirection)
        }
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
        setDoubleBurnGraphic(getBurnHandle(shipLocation), result) //Set the graphic
        return result //Return the result
    }

    /**
     * Sets the double burn to a certain value
     * @param burnValue The value to set the double burn as
     * @return What the burn value is.
     */
    fun setDoubleBurn(burnValue:Boolean, shipLocation: ShipLocation):Boolean{
        val thruster = getThruster(shipLocation) //Get the thruster
        val handle = getBurnHandle(shipLocation) //Get the handle

        val result = thruster.setDoubleBurn(burnValue, this.fuel) //Set the burn

        setDoubleBurnGraphic(handle, result) //set the graphic

        return result //Return the result
    }

    private fun setDoubleBurnGraphic(handle:BurnHandle, value:Boolean){
        when(value){

            //If we are double burning, set the texture and apply the bonus burn.
            true -> {
                if(!testShip) {
                    handle.burnHandle.texture = BurnHandle.doubleBurnTexture
                    handle.burnHandle.color = Color.RED
                }
            }

            //If we are not double burning, set the texture and reduce our burn.
            false ->{
                if(!testShip) {
                    handle.burnHandle.texture = BurnHandle.normalBurnTexture
                    handle.burnHandle.color = Color.WHITE
                }
            }
        }
    }

    /**
     * Used mostly for copying the burn force and burn per tick to another ship.
     */
    fun setBurnForceAndPerTick(force:Float, amount:Float, ship: ShipLocation){
        val thruster = getThruster(ship)
        thruster.setBurnForceAndPerTick(force, amount)
    }

    /**
     * Drags the burn handle to the location of the mouse
     * @param mouseX The mouse's X position
     * @param mouseY The mouse's Y position
     */
    fun dragBurn(mouseX:Float, mouseY:Float, shipLocation: ShipLocation){
        val rotationOffset = GH.getRotationFromLocation(shipLocation)

        var dst = position.dst(mouseX, mouseY) - ringRadius
        if(dst <= 0) dst = 0f

        val thruster = getThruster(shipLocation)

        //The burn time is equal to the distance. If the burn amount is greater than the fuel we have, set it to the max
        thruster.burnTime = dst.toInt()
        if(thruster.burnAmount > fuel){
            thruster.burnTime = (fuel/thruster.fuelBurnedPerTick).toInt()
        }

        this.setRotationTowardsMouse(mouseX, mouseY, rotationOffset)
    }

    fun reset(position:Vector2, fuel:Float, initialVelocity:Vector2){
        this.position.set(position.x, position.y)
        this.fuel = fuel
        this.thrusters.forEach { thruster ->
            thruster.reset()
            thruster.setDoubleBurn(false ,this.fuel)
        }
        this.setShipRotation(0f)
        this.body.setTransform(Vector2(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE), 0f)
        this.body.setLinearVelocity(initialVelocity.x*Constants.VELOCITY_SCALE, initialVelocity.y*Constants.VELOCITY_SCALE)

        //If we are the test ship, don't do this!
        if(!testShip){
            this.planetList.clear()
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandles.forEach(BurnHandle::setPosition)
            setBurnHandlePosition()
        }
    }

    fun addFuel(amount:Float):Boolean{
        fuel += amount
        val result = fuel >= maxFuel
        fuel = Math.min(fuel, maxFuel) //Clamp the value to max fuel
        return result
    }

    override fun createBody() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x* Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE)

        val world = MyGame.world
        this.body = world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val shape = PolygonShape()

        shape.setAsBox((shipWidth/2f)*Constants.BOX2D_SCALE, (shipHeight/2f)*Constants.BOX2D_SCALE)

        mainFixture.shape = shape
        if(testShip) mainFixture.isSensor = true

        this.body.createFixture(mainFixture)

        shape.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this)
    }

    fun setPosition(position:Vector2){
        this.setPosition(position.x, position.y)
    }

    /**
     * Sets the position of the ship. This sets both the rendering position and the physics body position.
     * @param x The X position (real value)
     * @param y The Y position (real value)
     */
    fun setPosition(x:Float, y:Float){
        this.position.set(x, y)
        this.body.setTransform(x*Constants.BOX2D_SCALE, y*Constants.BOX2D_SCALE, 0f)

        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandles.forEach (BurnHandle::setPosition)
            setBurnHandlePosition()
        }
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
     * Copies the thrusters passed in into this ship.
     */
    fun copyThrusters(thrustersToCopy:Array<Thruster>){
        this.thrusters.forEachIndexed { i, thruster ->
            thruster.setBurnForceAndPerTick(thrustersToCopy[i].burnForce, thrustersToCopy[i].fuelBurnedPerTick)
            thruster.burnTime = thrustersToCopy[i].burnTime
            thruster.doubleBurn = thrusters[i].doubleBurn
        }
    }

    private fun getThruster(shipLocation: ShipLocation):Thruster{
        val thruster:Thruster

        //We do this because we want to flip left/right
        when(shipLocation){
            ShipLocation.Rear -> thruster = thrusters.filter { it.location == ShipLocation.Rear }[0]
            ShipLocation.Left -> thruster = thrusters.filter { it.location == ShipLocation.Right }[0] //Get the opposite
            ShipLocation.Right -> thruster = thrusters.filter { it.location == ShipLocation.Left }[0] //Get the opposite
            else -> thruster = thrusters.filter { it.location == ShipLocation.Rear }[0]
        }

//        return thrusters.filter { it.location == shipLocation }[0]
        return thruster
    }

    private fun getBurnHandle(shipLocation: ShipLocation):BurnHandle{
        return burnHandles.filter { it.burnHandleLocation == shipLocation }[0]
    }

    override fun dispose() {
        val world = MyGame.world
        world.destroyBody(this.body)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        if(physicsArePaused == pausePhysics) return //If we are not changing the state, simply return

        when(pausePhysics){
            //If we are pausing, remove velocity and store it away.
            true -> {
                velocityHolder.set( body.linearVelocity.x,  body.linearVelocity.y)
                body.setLinearVelocity(0f, 0f)
            }
            //Otherwise, set the velocity back
            false -> {
                body.setLinearVelocity(velocityHolder.x, velocityHolder.y)
                velocityHolder.set(0f, 0f)
            }
        }

        physicsArePaused = pausePhysics
    }

    class BurnHandle(val ship: Ship, val burnHandleLocation:ShipLocation, val rotationOffset:Float){
        companion object{
            lateinit var normalBurnTexture:Texture
            lateinit var doubleBurnTexture:Texture
            val burnHandleSize = 30f

            init{
                normalBurnTexture = MyGame.manager["arrow", Texture::class.java]
                normalBurnTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                doubleBurnTexture = MyGame.manager["doubleArrow", Texture::class.java]
                doubleBurnTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
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

    private class DockingData(val position: Vector2, val rotation:Float, val callback:()->Unit){

    }
}