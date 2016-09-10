package com.quickbite.spaceslingshot.util;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;

import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class EasyAssetManager extends AssetManager {
    HashMap<String, DataReference> dataMap = new HashMap<String, DataReference>(20);

    public synchronized <T> T get(String commonName, Class<T> type) {
        //Get the reference from the data map.
        DataReference ref = dataMap.get(commonName);
        if(ref == null) {
            //If it's null, let's try to find it in the underlying AssetManager by the common name. This really only is useful in cases where the underlying AssetManager loads its own file,
            //for instance, when you load an Atlas file and it loads the images for you.
            if(this.isLoaded(commonName)) {
                return super.get(commonName, type);
            }

            return null;
        }

        if(this.isLoaded(ref.path))
            return super.get(ref.path, type);

        return null;
    }

    @Override
    public synchronized <T> T get(String commonName){
        //Get the reference from the data map.
        DataReference ref = dataMap.get(commonName);
        if(ref == null) {
            //If it's null, let's try to find it in the underlying AssetManager by the common name. This really only is useful in cases where the underlying AssetManager loads its own file,
            //for instance, when you load an Atlas file and it loads the images for you.
            if(this.isLoaded(commonName)) {
                return super.get(commonName);
            }

            return null;
        }

        if(this.isLoaded(ref.path))
            return super.get(ref.path);

        return null;
    }

    /**
     * Loads an asset.
     * @param fileName The filename, relative to the assets folder.
     * @param commonName The common/simplified name of the asset for retrieving later.
     * @param type The type of file (ex: Texture.class)
     * @param <T> The type.
     */
    public synchronized <T> void load(String fileName, String commonName, Class<T> type) {
        super.load(fileName, type);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    /**
     * Loads an asset, using the fileName to generate a commonName, thus 'art/Something.png' will end up as 'Something'
     * @param fileName The file name relative to the assets folder.
     * @param type The type of file, ex: Texture.class
     * @param <T> The type.
     */
    @Override
    public synchronized <T> void load(String fileName, Class<T> type) {
        super.load(fileName, type);
        String commonName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.indexOf("."));
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    @Override
    public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> param) {
        super.load(fileName, type, param);
        String commonName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.indexOf("."));
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    public synchronized <T> void load(String fileName, String commonName, Class<T> type, AssetLoaderParameters<T> param) {
        super.load(fileName, type, param);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    @Override
    public synchronized void unload(String commonName) {
        DataReference ref = dataMap.get(commonName);
        String dataName = "";
        if(ref != null)
            dataName = ref.path;
        else
            dataName = commonName;
        super.unload(dataName);
    }

    /**
     * Basically holds a link from a simple name to the actual path. This makes it
     * a lot easier to load an asset from the manager ("somePicture" vs "img/misc/buttons/somePicture.png")
     */
    private class DataReference{
        private String name, path;

        public DataReference(String name, String path){
            this.name = name;
            this.path = path;
        }
    }
}


