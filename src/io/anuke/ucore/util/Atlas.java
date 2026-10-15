package io.anuke.ucore.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


/**
 * Utility class that improves on TextureAtlas -
 * faster texture lookups with an ObjectMap and automatic error textures.
 */
public class Atlas extends TextureAtlas{
    ObjectMap<String, AtlasRegion> regionmap = new ObjectMap<>();
    ObjectMap<Texture, Pixmap> pixmaps = new ObjectMap<>();
    AtlasRegion error;

    public Atlas(String filename){
        this(Gdx.files.internal("sprites/" + filename));
    }

    public Atlas(FileHandle file){
        super(file);
        for(AtlasRegion r : super.getRegions()){
            regionmap.put(r.name, r);
        }
        error = findRegion("error");
    }

    /** Empty constructor. Does not load resources. Internal use only. */
    public Atlas(){

    }

    /**
     * Merges pack file data into this atlas.
     * Sprite pages: regions are built straight from the pack file's integer
     * pixel bounds (no UV round trips), page textures are registered with the
     * atlas so {@link #dispose()} frees them, every new region is indexed in
     * the lookup map and the error region is refreshed so missing names fall
     * back to it instead of throwing.
     */
    @Override
    public void load(TextureAtlasData data){
        Array<AtlasRegion> regions = getRegions();
        int before = regions.size;
        super.load(data);
        // during super(file) construction regionmap is not initialized yet;
        // the constructor repopulates it from getRegions() afterwards
        if(regionmap == null) return;
        for(int i = before; i < regions.size; i++){
            AtlasRegion region = regions.get(i);
            regionmap.put(region.name, region);
        }
        error = regionmap.get("error", error);
    }

    /** Merges every region of another pack file into this atlas. */
    public void add(FileHandle packFile){
        load(new TextureAtlasData(packFile, packFile.parent(), false));
    }

    /**
     * Used for getting the pixmap of a certain region.
     * Note that it is internally stored and disposed when needed.
     *
     * @param name the name of the region to use.
     * @return the pixmap of the region.
     */
    public Pixmap getPixmapOf(String name){
        return getPixmapOf(findRegion(name));
    }

    /**
     * Used for getting the pixmap of a certain region.
     * Note that it is internally stored and disposed when needed.
     *
     * @param region the region to use.
     * @return the pixmap of the region.
     */
    public Pixmap getPixmapOf(TextureRegion region){
        Texture texture = region.getTexture();
        if(pixmaps.containsKey(texture)) return pixmaps.get(texture);
        if(!texture.getTextureData().isPrepared()) texture.getTextureData().prepare();
        Pixmap pixmap = texture.getTextureData().consumePixmap();
        pixmaps.put(texture, pixmap);
        return pixmap;
    }

    /** Disposes of all pixmaps obtained from {@link #getPixmapOf}. */
    public void disposePixmaps(){
        for(Pixmap pixmap : pixmaps.values()){
            pixmap.dispose();
        }
        pixmaps.clear();
    }

    @Override
    public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
        AtlasRegion aregion = super.addRegion(name, texture, x, y, width, height);
        regionmap.put(name, aregion);
        return aregion;
    }

    /** Sets the error region to the specified region. */
    public void setErrorRegion(String name){
        error = findRegion(name);
    }

    /** Note that this returns the 'error' region if this region is not found. */
    @Override
    public AtlasRegion findRegion(String name){
        AtlasRegion r = regionmap.get(name);
        if(r == null && error == null && !(name.equals("error")))
            throw new IllegalArgumentException("The region \"" + name + "\" does not exist!");
        if(r == null) return error;
        return r;
    }

    public float regionHeight(String name){
        return findRegion(name).getRegionHeight();
    }

    public float regionWidth(String name){
        return findRegion(name).getRegionWidth();
    }

    /** @return whether or not the specified region is found. */
    public boolean hasRegion(String s){
        return regionmap.containsKey(s);
    }

    public void dispose(){
        super.dispose();
        for(Pixmap pixmap : pixmaps.values()){
            pixmap.dispose();
        }
    }

    public TextureRegion getRegion(String name){
        return findRegion(name);
    }

    public TextureRegion getRegion(String name, TextureRegion def){
        TextureRegion region = regionmap.get(name);
        return region == null ? def : region;
    }
}
