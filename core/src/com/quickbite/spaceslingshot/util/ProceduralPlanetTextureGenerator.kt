package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.PlanetData
import com.quickbite.spaceslingshot.data.json.PlanetDataManager
import net.dermetfan.utils.math.Noise

/**
 * Created by Paha on 8/20/2016.
 */
object ProceduralPlanetTextureGenerator {
    val range = 1f

    private val _tmpCol1: Color = Color()
    private val _tmpCol2: Color = Color()
    private val _tmpColor3: Color = Color()

    var textureCounter = 0
    var writeToFile = false

    private val tag = "PPTG"

    private lateinit var earthData: PlanetTypeData
    private lateinit var iceData: PlanetTypeData
    private lateinit var lavaData: PlanetTypeData
    private lateinit var desertData: PlanetTypeData

    val textureArray:Array<Texture?> = Array(10, {null})

    init{
        earthData = PlanetTypeData(
                Pair(EasyColor(40, 80, 237), EasyColor(67, 104, 250) ),
                Pair(EasyColor(147, 219, 156), EasyColor(73, 143, 82)),
                Pair(EasyColor(135, 129, 118), EasyColor(181, 162, 130)),
                0.5f, 0.8f, 1f)

        desertData = PlanetTypeData(
                Pair(EasyColor(237, 201, 175), EasyColor(207, 173, 128)),
                Pair(EasyColor(194, 135, 52), EasyColor(247, 171, 64)),
                Pair(EasyColor(219, 177, 99), EasyColor(189, 139, 47)),
                0.5f, 0.8f, 1f)

        iceData = PlanetTypeData(
                Pair(EasyColor(203, 235, 245), EasyColor(173, 230, 247)),
                Pair(EasyColor(129, 169, 181), EasyColor(73, 148, 171)),
                Pair(EasyColor(92, 153, 237), EasyColor(40, 127, 250)),
                0.5f, 0.7f, 1f)

        lavaData = PlanetTypeData(
                Pair(EasyColor(232, 58, 0), EasyColor(230, 138, 46)) ,
                Pair(EasyColor(140, 121, 98), EasyColor(99, 83, 65)),
                Pair(EasyColor(173, 138, 73), EasyColor(140, 117, 74)),
                0.3f, 0.6f, 1f)

//        lavaData = PlanetTypeData(
//                Pair(Color(Color.GREEN), Color(Color.GREEN)) ,
//                Pair(Color(Color.RED), Color(Color.RED)),
//                Pair(Color(Color.BLUE), Color(Color.BLUE)),
//                0.2f, 0.7f, 1f)
    }

    fun generatePlanetTexturesThreaded(amount:Int, seeds:Array<Long> = arrayOf(), cloudSeeds:Array<Long> = arrayOf()){
        var counter:Int = 0

        val shadowTexture = MyGame.manager["shadow", Texture::class.java]
        shadowTexture.textureData.prepare()
        val shadowPixmap = shadowTexture.textureData.consumePixmap()
        val shadowPixels = Array(256, {Array(256, {0f})})

        for (x in 0..shadowPixels.size - 1) {
            for (y in 0..shadowPixels.size - 1) {
                shadowPixels[x][y] = Color(shadowPixmap.getPixel(x, y)).a
            }
        }

        for(i in 1..amount) {
            MyGame.postRunnable({
                val pixmap = generatePixMap(seeds, cloudSeeds, shadowPixels, i-1) //Execute the main function

                //Call this on the main thread. This will create the texture.
                MyGame.postRunnable({
                    val texture = createTextureFromPixmap(pixmap)
                    storeTexture(texture, counter++)
                }, true)

            }, false)
        }

        shadowPixmap.dispose()
    }

    private fun generatePixMap(seeds:Array<Long>, cloudSeeds:Array<Long>, shadowPixels:Array<Array<Float>>, index:Int): Pixmap {
        val seed = if(seeds.size > index) seeds[index] else MathUtils.random(Long.MAX_VALUE)
        Noise.setSeedEnabled(true)
        Noise.setSeed(seed)

        val noise = OpenSimplexNoise(seed)

        val values = PlanetData.PlanetType.values()
        val planetType = values[MathUtils.random(values.size-1)]
        System.out.println("planetType : $planetType")

        val smooth:Float
        when (planetType) {
            PlanetData.PlanetType.Earth -> smooth = 1.5f
            PlanetData.PlanetType.Ice -> smooth = 1.5f
            PlanetData.PlanetType.Desert -> smooth = 1.8f
            PlanetData.PlanetType.Lava -> smooth = 1.2f
            else -> smooth = 1.5f
        }

//                val map = Noise.diamondSquare(8, smooth, range, true, true, true, 1f, 4, 2)

        val cloudSeed = if(cloudSeeds.size > index) cloudSeeds[index] else MathUtils.random(Long.MAX_VALUE)
        Noise.setSeed(cloudSeed)
        val cloudMap = Noise.diamondSquare(8, 1.4f, range, false, false, true, 1f, 1, 1)

        val pixmap: Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED)

        pixmap.fillCircle(128, 128, 128)

        for (x in 0..pixmap.width - 1) {
            for (y in 0..pixmap.height - 1) {
                if (pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                    continue

                val value = noise.eval(x.toDouble()/100.0, y.toDouble()/100.0) + 1
//                        Gdx.app.log(tag, "Value: $value")
                val adjustedValue = MathUtils.clamp(value.toFloat(), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                val adjustedSecondaryValue = MathUtils.clamp((cloudMap[x][y] + range / 2f), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                val color = colorTest(planetType.toString(), adjustedValue, adjustedSecondaryValue, shadowPixels[x][y])
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }

        return pixmap
    }

    fun generatePlanetTexturesFromDataThreaded(amount:Int){
        var counter:Int = 0

        val shadowTexture = MyGame.manager["shadow", Texture::class.java]

        shadowTexture.textureData.prepare()
        val shadowPixmap = shadowTexture.textureData.consumePixmap()
        val shadowPixels = Array(256, {Array(256, {0f})})

        //Record the shadow pixels
        for (x in 0..shadowPixels.size - 1) {
            for (y in 0..shadowPixels.size - 1) {
                shadowPixels[x][y] = Color(shadowPixmap.getPixel(x, y)).a //We gotta use the alpha from here since we're basing it on transparency
            }
        }

        for(i in 1..amount) {
            //TODO Whoa magic number, fix this
            val dataTexture = MyGame.manager["height_${1 + MathUtils.random(10)}", Texture::class.java]

            MyGame.postRunnable({
                val pixmap = generatePixMapFromData(dataTexture, shadowPixels) //Execute the main function

                //Call this on the main thread. This will create the texture.
                MyGame.postRunnable({
                    val startTime = TimeUtils.millis()

                    val texture = createTextureFromPixmap(pixmap)
                    storeTexture(texture, counter++)

                    val timeTaken = TimeUtils.millis() - startTime
                    System.out.println("[$tag] Time to generate texture from gameScreenData and store (counter): ${timeTaken}ms")

                }, true)

            }, false)
        }

        shadowPixmap.dispose()
    }

    private fun generatePixMapFromData(dataTexture: Texture, shadowPixels: Array<Array<Float>>): Pixmap {
//        var startTime = TimeUtils.millis()

        val tmpColor1 = Color()
        val tmpColor2 = Color()
        val tmpColor3 = Color()

        //First we set the entire pixmap to red. This will be our circle to fill in (to make a planet shape)
        val pixmap: Pixmap = Pixmap(128, 128, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED)
        pixmap.fillCircle(pixmap.width/2, pixmap.width/2, pixmap.width/2)

        val scale = 2

        //Grab the pixmap gameScreenData from the incoming texture
        dataTexture.textureData.prepare()
        val texturePixmap = dataTexture.textureData.consumePixmap()

        //Grab a random planet type
        val planetType = PlanetData.PlanetType.values()[MathUtils.random(PlanetData.PlanetType.values().size-1)]

//        System.out.println("[$tag] Time to fill initial texture: ${TimeUtils.millis() - startTime}ms")
//        startTime = TimeUtils.millis()

        for (x in 0..pixmap.width - 1) {
            for (y in 0..pixmap.height - 1) {
                val scaledX = x*scale
                val scaledY = y*scale

                if (pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                    continue

                val rgba = texturePixmap.getPixel(scaledX, scaledY)
                val colorFromTexture = _tmpCol1.set(rgba)
                val value = colorFromTexture.r

//                        Gdx.app.log(tag, "Value: $value")
                val adjustedValue = MathUtils.clamp(value.toFloat(), 0f, 1f) //Adjust by 1 and clamp at 0 to 1
                val adjustedSecondaryValue = 0f
                val color = colorTest(planetType.toString(), adjustedValue, adjustedSecondaryValue, shadowPixels[scaledX][scaledY], tmpColor1, tmpColor2, tmpColor3)
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }

//        System.out.println("[$tag] Time to color pixels: ${TimeUtils.millis() - startTime}ms")
//        startTime = TimeUtils.millis()

        texturePixmap.dispose()

//        val timeTaken = TimeUtils.millis() - startTime
//        System.out.println("[$tag] Time to generate pixmaps from gameScreenData: ${timeTaken}ms")

        return pixmap
    }

    private fun createTextureFromPixmap(pixmap: Pixmap): Texture {
        val texture = Texture(pixmap)

//        if (writeToFile) {
            PixmapIO.writePNG(Gdx.files.local("planet_${textureCounter}.png"), pixmap)
            textureCounter++
//        }

        pixmap.dispose()

        textureCounter = 0
        return texture
    }

    /**
     * Loads all noise maps procedurally colors them. Stores the results
     * in the texture array.
     */
    fun generatePlanetTexturesFromData(){
        val startTime = TimeUtils.millis()

        val numHeightMaps = 10

        //Shadow overlay
        val shadowTexture = MyGame.manager["shadow", Texture::class.java]
        shadowTexture.textureData.prepare()
        val shadowPixmap = shadowTexture.textureData.consumePixmap()
        val shadowPixels = Array(256, {Array(256, {0f})})

        for (x in 0..shadowPixels.size - 1) {
            for (y in 0..shadowPixels.size - 1) {
                shadowPixels[x][y] = Color(shadowPixmap.getPixel(x, y)).a
            }
        }

        for(i in 0..9) {
            val pixmap: Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
            pixmap.setColor(Color.RED)
            pixmap.fillCircle(128, 128, 128)

            val noiseTexture = MyGame.manager["height_${1 + MathUtils.random(numHeightMaps)}", Texture::class.java]
            noiseTexture.textureData.prepare()
            val noisePixmap = noiseTexture.textureData.consumePixmap()

            val planetType = PlanetData.PlanetType.values()[MathUtils.random(PlanetData.PlanetType.values().size-1)]

            for (x in 0..pixmap.width - 1) {
                for (y in 0..pixmap.height - 1) {
                    if (pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                        continue

                    val rgba = noisePixmap.getPixel(x, y)
                    val colorFromTexture = _tmpCol1.set(rgba)
                    val value = colorFromTexture.r

//                        Gdx.app.log(tag, "Value: $value")
                    val adjustedValue = MathUtils.clamp(value.toFloat(), 0f, 1f) //Adjust by 1 and clamp at 0 to 1
                    val adjustedSecondaryValue = 0f
                    val color = colorTest(planetType.toString(), adjustedValue, adjustedSecondaryValue, shadowPixels[x][y])
                    pixmap.drawPixel(x, y, Color.rgba8888(color))
                }
            }

            noisePixmap.dispose()

            val texture = Texture(pixmap)
            pixmap.dispose()

            this.textureArray[i] = texture
        }

        val result = TimeUtils.millis() - startTime
        System.out.println("[$tag] Time to generate textures from gameScreenData: ${result}ms")

        shadowPixmap.dispose()
    }

    fun storeTexture(texture: Texture, number:Int){
        textureArray[number] = texture
        Gdx.app.log(tag, "Loaded texture #$number")
    }

    fun test(){
        Noise.setSeedEnabled(true)
        Noise.setSeed(15478)

        val map = Noise.diamondSquare(8, 1.5f, range, false, false, true, 1f, 10, 10)

        val pixmap: Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED)
        pixmap.fillCircle(128, 128, 128)

        for (x in 0..pixmap.width-1){
            for(y in 0..pixmap.height-1){
                if(pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                    continue

                val adjustedValue = MathUtils.clamp((map[x][y] + range /2f), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                _tmpCol1.set(1f,1f,1f,1f)
                _tmpCol2.set(0f,0f,0f,1f)
                pixmap.drawPixel(x, y, Color.rgba8888(_tmpCol1.lerp(_tmpCol2, adjustedValue/1f)))
            }
        }

        PixmapIO.writePNG(Gdx.files.local("test.png"), pixmap)
        pixmap.dispose()
    }

    private fun blackAndWhiteColor(value:Float): Color {
        val scaledValue = value*0.5f
        return Color(scaledValue, scaledValue, scaledValue, 1f)
    }

    private fun colorTest(planetType:String, value: Float, secondaryValue: Float, shadowValue:Float):Color{
        val planetType = planetType.toLowerCase()
        val planetDataList = PlanetDataManager.definitionMap[planetType]!!
        val planetData = planetDataList[MathUtils.random(planetDataList.size - 1)]
        val colors = planetData.colors
        val transitions = planetData.transitions

        var index = 0

        //Runs through the transition ranges checking if we are in between... 0 < 0.3 < 0.5 ?
        for(i in 1..transitions.size-1){
            if(i == 1){
                //If i is 1 it's a special case.
                if(value < transitions[i]){
                    index = 0
                    break
                }

            }else{
                if(value >= transitions[i-1] && value < transitions[i]){
                    index = i
                    break
                }
            }
        }

        if(value > 0.9){
//            System.out.println("Test")
        }

        val lowerValue = if(index == 0) 0f else transitions[index-1]
        val higherValue = transitions[index] - lowerValue
        val value = value - lowerValue
        val result = value/higherValue

        _tmpColor3.set(0f, 0f, 0f, 1f)

        _tmpCol1.set(colors[index][0]) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class gameScreenData
        _tmpCol2.set(_tmpCol1.lerp(colors[index][1], result))

        return _tmpCol2.lerp(_tmpColor3, shadowValue)
    }

    private fun colorTest(planetType:String, value: Float, secondaryValue: Float, shadowValue:Float, tmpColor1:Color, tmpColor2:Color, tmpColor3:Color):Color{
        val planetType = planetType.toLowerCase()
        val planetDataList = PlanetDataManager.definitionMap[planetType]!!
        val planetData = planetDataList[MathUtils.random(planetDataList.size - 1)]
        val colors = planetData.colors
        val transitions = planetData.transitions

        var index = 0

        //Runs through the transition ranges checking if we are in between... 0 < 0.3 < 0.5 ?
        for(i in 1..transitions.size-1){
            if(i == 1){
                //If i is 1 it's a special case.
                if(value < transitions[i]){
                    index = 0
                    break
                }

            }else{
                if(value >= transitions[i-1] && value < transitions[i]){
                    index = i
                    break
                }
            }
        }

        if(value > 0.9){
//            System.out.println("Test")
        }

        val lowerValue = if(index == 0) 0f else transitions[index-1]
        val higherValue = transitions[index] - lowerValue
        val value = value - lowerValue
        val result = value/higherValue

        tmpColor3.set(0f, 0f, 0f, 1f)

        tmpColor1.set(colors[index][0]) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class gameScreenData
        tmpColor2.set(tmpColor1.lerp(colors[index][1], result))

        return tmpColor2.lerp(tmpColor3, shadowValue)
    }

    fun getNextTexture(homePlanet:Boolean = false): Texture {
        if(!homePlanet) {
            val texture = textureArray[textureCounter]!!
            textureCounter = (textureCounter + 1) % textureArray.size
            return texture
        }else{
            return MyGame.manager["wormhole", Texture::class.java]
        }
    }

    private class PlanetTypeData(val col1:Pair<Color, Color>, val col2:Pair<Color, Color>, val col3:Pair<Color, Color>, val min:Float, val mid:Float, val max:Float){}
}