package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont

/**
 * Created by Paha on 6/19/2016.
 */
object Loader {
    fun loadAllImgs(manager: EasyAssetManager, dir: FileHandle, recursive:Boolean = false){
        for(file in dir.list()){
            if(file.isDirectory && recursive)
                loadAllImgs(manager, file, true)
            else if(file.extension() == "png"){
                manager.load(file.path(), Texture::class.java)
            }
        }
    }

    fun loadFonts(manager: EasyAssetManager, dir: FileHandle){
        for(file in dir.list()){
            if(file.extension() == "fnt"){
                manager.load(file.path(), BitmapFont::class.java)
            }
        }
    }
}