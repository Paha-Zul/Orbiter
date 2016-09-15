package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.util.EasyColor
import com.quickbite.spaceslingshot.util.OpenSimplexNoise
import net.dermetfan.utils.math.Noise

/**
 * Created by Paha on 8/20/2016.
 */
object ProceduralPlanetTextureGenerator {
    val range = 1f

    private val _tmpCol1:Color = Color()
    private val _tmpCol2:Color = Color()
    private val _tmpColor3:Color = Color()

    var textureCounter = 0
    var writeToFile = false

    private val tag = "PPTG"

    private lateinit var earthData:PlanetTypeData
    private lateinit var iceData:PlanetTypeData
    private lateinit var lavaData:PlanetTypeData
    private lateinit var desertData:PlanetTypeData

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
        var startTime = TimeUtils.millis()

        var counter:Int = 0

        var diff = (TimeUtils.millis() - startTime)/1000f
        Gdx.app.log(tag, "Starting generation: $diff")
        startTime = TimeUtils.millis()

        for(i in 1..amount) {
            val createPixmapFromNoise: () -> Pixmap = {

                Gdx.app.log(tag, "Setting Seed")
                startTime = TimeUtils.millis()

                val seed = if(seeds.size > i - 1) seeds[i - 1] else MathUtils.random(Long.MAX_VALUE)
                Noise.setSeedEnabled(true)
                Noise.setSeed(seed)

                val noise = OpenSimplexNoise(seed)

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "Setting seed took: $diff")
                startTime = TimeUtils.millis()

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

                Gdx.app.log(tag, "Generating noise")
                startTime = TimeUtils.millis()

//                val map = Noise.diamondSquare(8, smooth, range, true, true, true, 1f, 4, 2)

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "Noise took: $diff")

                Gdx.app.log(tag, "Generating cloud noise")
                startTime = TimeUtils.millis()

                val cloudSeed = if(cloudSeeds.size > i - 1) cloudSeeds[i - 1] else MathUtils.random(Long.MAX_VALUE)
                Noise.setSeed(cloudSeed)
                val cloudMap = Noise.diamondSquare(8, 1.4f, range, false, false, true, 1f, 1, 1)

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "cloud noise took: $diff")

                Gdx.app.log(tag, "Generating pixmap")
                startTime = TimeUtils.millis()

                val pixmap: Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
                pixmap.setColor(Color.RED)

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "pixmap took: $diff")

                Gdx.app.log(tag, "filling circle")
                startTime = TimeUtils.millis()

                pixmap.fillCircle(128, 128, 128)

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "circle took: $diff")

                Gdx.app.log(tag, "Writing color to pixmap")
                startTime = TimeUtils.millis()

                for (x in 0..pixmap.width - 1) {
                    for (y in 0..pixmap.height - 1) {
                        if (pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                            continue

                        val value = noise.eval(x.toDouble()/100.0, y.toDouble()/100.0) + 1
//                        Gdx.app.log(tag, "Value: $value")
                        val adjustedValue = MathUtils.clamp(value.toFloat(), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                        val adjustedSecondaryValue = MathUtils.clamp((cloudMap[x][y] + range / 2f), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                        val color = getColor(planetType, adjustedValue, adjustedSecondaryValue)
                        pixmap.drawPixel(x, y, Color.rgba8888(color))
                    }
                }

                diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "writing colors took: $diff")

                pixmap
            }

            val createTextureFromPixmap: (Pixmap) -> Texture = { pixmap: Pixmap ->
                Gdx.app.log(tag, "Creating texture from pixmap")
                var startTime = TimeUtils.millis()

                val texture = Texture(pixmap)

                var diff = (TimeUtils.millis() - startTime)/1000f
                Gdx.app.log(tag, "Crating texture took: $diff")

                if (writeToFile) {
                    PixmapIO.writePNG(Gdx.files.local("planet_$textureCounter.png"), pixmap)
                    textureCounter++
                }
                pixmap.dispose()

                textureCounter = 0
                texture
            }

            MyGame.postRunnable({
                val pixmap = createPixmapFromNoise() //Execute the main function

                //Call this on the main thread. This will create the texture.
                MyGame.postRunnable({
                    val texture = createTextureFromPixmap(pixmap)
                    storeTexture(texture, counter++)
                }, true)

            }, false)
        }
    }

    fun storeTexture(texture:Texture, number:Int){
        textureArray[number] = texture
        Gdx.app.log(tag, "Loaded texture #$number")
    }

    fun generatePlanetTextures(planetType:PlanetData.PlanetType):Texture{
        var num = 5
        var name = "height_"

        if(planetType == PlanetData.PlanetType.Lava){
            num = 1
            name = "height_special_"
        }

        val referenceTexture = MyGame.manager["$name${MathUtils.random(1, num)}", Texture::class.java]
        referenceTexture.textureData.prepare()
        val heightMap = referenceTexture.textureData.consumePixmap()

        val pixmap:Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED)
        pixmap.fillCircle(128, 128, 128)

        for (x in 0..pixmap.width-1){
            for(y in 0..pixmap.height-1){
                if(pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                    continue

                val pixel = _tmpCol1.set(heightMap.getPixel(x, y))

                val adjustedValue = pixel.r
                val adjustedSecondaryValue = 0f
                val color = getColor(planetType, adjustedValue, adjustedSecondaryValue)
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }

        val texture = Texture(pixmap)
        pixmap.dispose()
        heightMap.dispose()

        return texture
    }

    fun test(){
        Noise.setSeedEnabled(true)
        Noise.setSeed(15478)

        val map = Noise.diamondSquare(8, 1.5f, range, false, false, true, 1f, 10, 10)

        val pixmap:Pixmap = Pixmap(256, 256, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED)
        pixmap.fillCircle(128, 128, 128)

        for (x in 0..pixmap.width-1){
            for(y in 0..pixmap.height-1){
                if(pixmap.getPixel(x, y) != Color.rgba8888(1f, 0f, 0f, 1f))
                    continue

                val adjustedValue = MathUtils.clamp((map[x][y] + range/2f), 0f, 1f) //Adjust by 1 and clamp at 0 to 2
                _tmpCol1.set(1f,1f,1f,1f)
                _tmpCol2.set(0f,0f,0f,1f)
                pixmap.drawPixel(x, y, Color.rgba8888(_tmpCol1.lerp(_tmpCol2, adjustedValue/1f)))
            }
        }

        PixmapIO.writePNG(Gdx.files.local("test.png"), pixmap)
        pixmap.dispose()
    }

    private fun blackAndWhiteColor(value:Float):Color{
        val scaledValue = value*0.5f
        return Color(scaledValue, scaledValue, scaledValue, 1f)
    }

    private fun getColor(planetType: PlanetData.PlanetType, value: Float, secondaryValue: Float):Color{
        if(planetType == PlanetData.PlanetType.Earth)
            return getColor(earthData, value, secondaryValue)
        else if(planetType == PlanetData.PlanetType.Desert)
            return getColor(desertData, value, secondaryValue)
        else if(planetType == PlanetData.PlanetType.Ice)
            return getColor(iceData, value, secondaryValue)
        else if(planetType == PlanetData.PlanetType.Lava)
            return getColor(lavaData, value, secondaryValue)
        else
            return getColor(lavaData, value, secondaryValue)
    }

    fun getNextTexture():Texture{
        val texture = textureArray[textureCounter]!!
        textureCounter = (textureCounter+1)% textureArray.size
        return texture
    }

    private fun getColor(data:PlanetTypeData, value: Float, secondaryValue: Float):Color{
        if(value < data.min){
            _tmpCol1.set(data.col1.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpColor3.set(_tmpCol1.lerp(data.col1.second, value/data.min))
        }else if (value < data.mid){
            _tmpCol1.set(data.col2.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpColor3.set(_tmpCol1.lerp(data.col2.second, (value - data.min)/data.mid))
        }else if(value <= data.max){
            _tmpCol1.set(data.col3.first) //Set the tmp color to the first color. This is so when we call lerp() it doesn't mess up our class data
            _tmpColor3.set(_tmpCol1.lerp(data.col3.second, (value - data.mid)/data.max))
        }

        return _tmpColor3
    }

    private class PlanetTypeData(val col1:Pair<Color, Color>, val col2:Pair<Color, Color>, val col3:Pair<Color, Color>, val min:Float, val mid:Float, val max:Float){}
}