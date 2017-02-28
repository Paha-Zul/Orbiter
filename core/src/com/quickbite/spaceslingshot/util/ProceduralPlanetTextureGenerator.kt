package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.PlanetData
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

        val planetType = PlanetData.PlanetType.values()[MathUtils.random(3)]
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
                val color = getColor(planetType, adjustedValue, adjustedSecondaryValue, shadowPixels[x][y])
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }

        return pixmap
    }

    private fun createTextureFromPixmap(pixmap: Pixmap): Texture {
        val texture = Texture(pixmap)

        if (writeToFile) {
            PixmapIO.writePNG(Gdx.files.local("planet_${textureCounter}.png"), pixmap)
            textureCounter++
        }

        pixmap.dispose()

        textureCounter = 0
        return texture
    }

    fun generatePlanetTexturesFromData(){
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

            val noiseTexture = MyGame.manager["height_${i+1}", Texture::class.java]
            noiseTexture.textureData.prepare()
            val noisePixmap = noiseTexture.textureData.consumePixmap()

            val planetType = PlanetData.PlanetType.values()[MathUtils.random(PlanetData.PlanetType.values().size-1)]

            for (x in 0..pixmap.width - 1) {
                for (y in 0..pixmap.height - 1) {
                    if (pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                        continue

                    val value = noisePixmap.getPixel(x, y)
//                        Gdx.app.log(tag, "Value: $value")
                    val adjustedValue = MathUtils.clamp(value.toFloat(), 0f, 1f) //Adjust by 1 and clamp at 0 to 1
                    val adjustedSecondaryValue = 0f
                    val color = getColor(planetType, adjustedValue, adjustedSecondaryValue, shadowPixels[x][y])
                    pixmap.drawPixel(x, y, Color.rgba8888(color))
                }
            }

            noisePixmap.dispose()

            val texture = Texture(pixmap)
            pixmap.dispose()

            this.textureArray[i] = texture
        }

        shadowPixmap.dispose()
    }

    fun storeTexture(texture: Texture, number:Int){
        textureArray[number] = texture
        Gdx.app.log(tag, "Loaded texture #$number")
    }

//    fun generatePlanetTextures(planetType:PlanetData.PlanetType):Texture{
//        var num = 5
//        var name = "height_"
//
//        if(planetType == PlanetData.PlanetType.Lava){
//            num = 1
//            name = "height_special_"
//        }
//
//        val referenceTexture = MyGame.manager["$name${MathUtils.random(1, num)}", Texture::class.java]
//        referenceTexture.textureData.prepare()
//        val heightMap = referenceTexture.textureData.consumePixmap()
//
//        val pixmap:Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
//        pixmap.setColor(Color.RED)
//        pixmap.fillCircle(128, 128, 128)
//
//        for (x in 0..pixmap.width-1){
//            for(y in 0..pixmap.height-1){
//                if(pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
//                    continue
//
//                val pixel = _tmpCol1.set(heightMap.getPixel(x, y))
//
//                val adjustedValue = pixel.r
//                val adjustedSecondaryValue = 0f
//                val color = getColor(planetType, adjustedValue, adjustedSecondaryValue)
//                pixmap.drawPixel(x, y, Color.rgba8888(color))
//            }
//        }
//
//        val texture = Texture(pixmap)
//        pixmap.dispose()
//        heightMap.dispose()
//
//        return texture
//    }

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

    private fun getColor(planetType: PlanetData.PlanetType, value: Float, secondaryValue: Float, shadowValue:Float): Color {
        if(planetType == PlanetData.PlanetType.Earth)
            return getColor(earthData, value, secondaryValue, shadowValue)
        else if(planetType == PlanetData.PlanetType.Desert)
            return getColor(desertData, value, secondaryValue, shadowValue)
        else if(planetType == PlanetData.PlanetType.Ice)
            return getColor(iceData, value, secondaryValue, shadowValue)
        else if(planetType == PlanetData.PlanetType.Lava)
            return getColor(lavaData, value, secondaryValue, shadowValue)
        else
            return getColor(lavaData, value, secondaryValue, shadowValue)
    }

    private fun getColor(data: PlanetTypeData, value: Float, secondaryValue: Float, shadowValue:Float): Color {
        _tmpColor3.set(0f, 0f, 0f, 1f)

        if(value < data.min){
            _tmpCol1.set(data.col1.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpCol2.set(_tmpCol1.lerp(data.col1.second, value/data.min))
//            System.out.println("data.col1.second, value/data.min: ${data.col1.second}, ${value/data.min}")
        }else if (value < data.mid){
            _tmpCol1.set(data.col2.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpCol2.set(_tmpCol1.lerp(data.col2.second, (value - data.min)/data.mid))
//            System.out.println("data.col2.second, (value - data.min)/data.mid: ${data.col2.second}, ${(value - data.min)/data.mid}")

        }else if(value <= data.max){
            _tmpCol1.set(data.col3.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpCol2.set(_tmpCol1.lerp(data.col3.second, (value - data.mid)/data.max))
//            System.out.println("data.col2.second, (value - data.mid)/data.max: ${data.col3.second}, ${(value - data.mid)/data.max}")

        }

        //System.out.println("Shadowvalue : $shadowValue")
        return _tmpCol2.lerp(_tmpColor3, shadowValue)
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