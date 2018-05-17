package com.quickbite.spaceslingshot.objects.gamescreenobjects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.util.Constants

abstract class ShipBase(position: Vector2, var fuel:Float) : SpaceBody(position, 50, 0f)
, IUniqueID, IPhysicsBody, Disposable {

    enum class ShipLocation {Rear, Left, Right, Front}

    var maxFuel = fuel

    val fuelTaken:Float
        get() {
            var amount = 0f
            thrusters.forEach { amount += it.burnAmount }
            return amount
        }

    protected val ringRadius = 100f
    protected val shipWidth = 50f
    protected val shipHeight = 50f

    protected val velocityHolder = Vector2()

    /** This velocity is the 'unscaled' value of velocity. The Box2D body holds the 'scaled' value for the physics world **/
    val velocity:Vector2 = Vector2()

    val thrusters:Array<Thruster> = arrayOf(
            Thruster(0.005f, 0.1f, Vector2(1f, 0f), ShipLocation.Rear, 0f), //Rear
            Thruster(0.005f, 0.1f, Vector2(0f, -1f), ShipLocation.Left, -90f), //Left
            Thruster(0.005f, 0.1f, Vector2(0f, 1f), ShipLocation.Right, 90f) //Right
    )

    override var dead: Boolean = false

    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)

    override lateinit var body: Body

    override var physicsArePaused: Boolean = false

    constructor():this(Vector2(0f, 0f), 0f)

    init{
        this.createBody()
    }

    override fun update(delta: Float) {
        super.update(delta)
    }

    override fun fixedUpdate(delta: Float) {
        super.fixedUpdate(delta)
    }

    override fun draw2(batch: SpriteBatch) {
        super.draw2(batch)
    }

    override fun dispose() {
        val world = MyGame.world
        world.destroyBody(this.body)
    }

    override fun clickedOn(x: Float, y: Float): Boolean {
        return super.clickedOn(x, y)
    }

    /**
     * Sets the ships rotation along with the graphics of the ship
     * @param rotation The rotation in degrees.
     */
    open fun setShipRotation(rotation:Float){
        this.rotation = rotation
        this.body.setTransform(position.x* Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE, rotation*MathUtils.degreesToRadians)
    }

    /**
     * Burns a tick of fuel.
     */
    protected fun burnFuel(){
        thrusters.forEach { thruster ->
            val result = thruster.burnFuel(fuel)
            fuel -= result.first
            addVelocityDirection(result.second, thruster.burnDirection)
        }
    }

    protected fun applyVelocity(){
        position.set(body.position.x*Constants.BOX2D_INVERSESCALE, body.position.y*Constants.BOX2D_INVERSESCALE)
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

    private fun addVelocityDirection(force:Float, facing:Vector2){
        val angle = (rotation + facing.angle())*MathUtils.degreesToRadians
        val x = MathUtils.cos(angle)*force
        val y = MathUtils.sin(angle)*force
        addVelocity(x, y)
    }

    fun setAllFuel(fuel:Float, maxFuel:Float = fuel){
        this.fuel = fuel
        this.maxFuel = maxFuel
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
     * Sets the position of the ship. This sets both the rendering position and the physics body position.
     * @param x The X position (real value)
     * @param y The Y position (real value)
     */
    open fun setPosition(x: Float, y: Float) {
        this.position.set(x, y)
        this.body.setTransform(x * Constants.BOX2D_SCALE, y * Constants.BOX2D_SCALE, 0f)
    }

    /**
     * Gets a thruster based on the ship location
     */
    protected fun getThruster(shipLocation: ShipLocation): Thruster {
//        return when(shipLocation){
//            Ship.ShipLocation.Rear -> thrusters.filter { it.location == Ship.ShipLocation.Rear }[0]
//            Ship.ShipLocation.Left -> thrusters.filter { it.location == Ship.ShipLocation.Right }[0] //Get the opposite
//            Ship.ShipLocation.Right -> thrusters.filter { it.location == Ship.ShipLocation.Left }[0] //Get the opposite
//            else -> thrusters.filter { it.location == Ship.ShipLocation.Rear }[0]
//        }

        return thrusters.filter { it.location == shipLocation }[0]
    }

    /**
     * Copies the thrusters passed in into this ship.
     */
    fun copyThrusters(thrustersToCopy: Array<Thruster>) {
        this.thrusters.forEachIndexed { i, thruster ->
            thruster.setBurnForceAndPerTick(thrustersToCopy[i].burnForce, thrustersToCopy[i].fuelBurnedPerTick)
            thruster.burnTime = thrustersToCopy[i].burnTime
            thruster.doubleBurn = thrusters[i].doubleBurn
        }
    }

    /**
     * Used mostly for copying the burn force and burn per tick to another ship.
     */
    fun setBurnForceAndPerTick(force: Float, amount: Float, ship: ShipLocation) {
        val thruster = getThruster(ship)
        thruster.setBurnForceAndPerTick(force, amount)
    }

    /**
     * Adds an amount of fuel to the ship
     * @param amount The amount to add
     */
    fun addFuel(amount: Float): Boolean {
        fuel += amount
        val result = fuel >= maxFuel
        fuel = Math.min(fuel, maxFuel) //Clamp the value to max fuel
        return result
    }

    /**
     * Sets the double burn to a certain value
     * @param burnValue The value to set the double burn as
     * @return What the burn value is.
     */
    fun setDoubleBurn(burnValue:Boolean, shipLocation: ShipLocation):Boolean{
        val thruster = getThruster(shipLocation) //Get the thruster
        val result = thruster.setDoubleBurn(burnValue, this.fuel) //Set the burn
        return result //Return the result
    }

    open fun reset(position: Vector2, fuel: Float, initialVelocity: Vector2) {
        this.position.set(position.x, position.y)
        this.fuel = fuel
        this.maxFuel = fuel
        this.thrusters.forEachIndexed { i, thruster ->
            thruster.reset()
            this.setDoubleBurn(false, thruster.location)
        }

        this.setShipRotation(0f)
        this.body.setTransform(Vector2(position.x * Constants.BOX2D_SCALE, position.y * Constants.BOX2D_SCALE), 0f)
        this.body.setLinearVelocity(initialVelocity.x * Constants.VELOCITY_SCALE, initialVelocity.y * Constants.VELOCITY_SCALE)
        this.velocity.set(initialVelocity)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        if (physicsArePaused == pausePhysics) return //If we are not changing the state, simply return

        when (pausePhysics) {
        //If we are pausing, remove velocity and store it away.
            true -> {
                velocityHolder.set(body.linearVelocity.x, body.linearVelocity.y)
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
}