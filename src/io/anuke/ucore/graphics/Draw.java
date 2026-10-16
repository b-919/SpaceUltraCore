package io.anuke.ucore.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.function.Consumer;
import io.anuke.ucore.scene.style.Drawable;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Pooling;
import io.anuke.ucore.util.Tmp;

import static io.anuke.ucore.core.Core.batch;

public class Draw{
    private static Color[] carr = new Color[3];
    private static TextureRegion blankRegion;
    private static float scl = 1f;
    private static float[] vertices = new float[20];

    public static void scale(float scaling){
        Draw.scl = scaling;
    }

    public static float scale(){
        return scl;
    }

    public static TextureRegion getBlankRegion(){
        if(blankRegion == null){
            if(Core.atlas != null && Core.atlas.hasRegion("blank")){
                blankRegion = Core.atlas.getRegion("blank");
                return blankRegion;
            }
            Log.err("WARNING: No blank texture region found. Falling back to manually-created region.");
            blankRegion = Pixmaps.blankTextureRegion();
        }
        return blankRegion;
    }
    // added if you already have "error" sprite when sprites are missing this only used if you want to a feature actually have a blank region instead of error one
    /** Returns the fully transparent 'clear' region, used as a fallback for missing optional regions. */
    public static TextureRegion getClearRegion(){
        return Core.atlas == null ? getBlankRegion() : Core.atlas.getRegion("clear", getBlankRegion());
    }

    public static void sprite(Sprite sprite){
        sprite.draw(batch);
    }

    public static void patch(String name, float x, float y, float width, float height){
        getPatch(name).draw(batch, x, y, width, height);
    }

    public static Drawable getPatch(String name){
        return Core.skin.getDrawable(name);
    }

    /** Sets the batch color to this color AND the previous alpha. */
    public static void tint(Color color){
        color(color.r, color.g, color.b, batch.getColor().a);
    }

    public static void tint(Color a, Color b, float s){
        Hue.mix(a, b, s, Tmp.c1);
        color(Tmp.c1.r, Tmp.c1.g, Tmp.c1.b, batch.getColor().a);
    }

    public static void color(Color color){
        batch.setColor(color);
    }

    public static void color(Color[] colors, float progress){
        batch.setColor(Hue.mix(colors, Tmp.c1, progress));
    }

    public static void color(Color a, Color b, Color c, float progress){
        carr[0] = a;
        carr[1] = b;
        carr[2] = c;
        batch.setColor(Hue.mix(carr, Tmp.c1, progress));
    }

    /** Sets the color to the provided color multiplied by the factor. */
    public static void colorl(Color color, float multiply){
        batch.setColor(Tmp.c1.set(color).mul(multiply, multiply, multiply, 1f));
    }

    /** Automatically mixes colors. */
    public static void color(Color a, Color b, float s){
        batch.setColor(Hue.mix(a, b, s, Tmp.c1));
    }

    public static void color(int color){
        batch.setPackedColor(NumberUtils.intBitsToFloat(color));
    }

    public static void color(){
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
    }

    public static void color(float r, float g, float b){
        batch.setColor(r, g, b, 1f);
    }

    public static void color(float r, float g, float b, float a){
        batch.setColor(r, g, b, a);
    }

    /** Lightness color. */
    public static void colorl(float l){
        color(l, l, l);
    }

    /** Lightness color, alpha. */
    public static void colorl(float l, float a){
        color(l, l, l, a);
    }

    public static void alpha(float alpha){
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, Mathf.clamp(alpha));
    }

    //region textures

    public static void rect(Texture texture, float x, float y){
        batch.draw(texture, x - texture.getWidth() / 2, y - texture.getHeight() / 2);
    }

    public static void rect(Texture texture, float x, float y, float w, float h){
        batch.draw(texture, x - w / 2, y - h / 2, w, h);
    }

    //endregion textures

    public static void rect(TextureRegion region, float x, float y){
        rect(region, x, y, region.getRegionWidth() * scl, region.getRegionHeight() * scl);
    }

    public static void rect(TextureRegion region, float x, float y, float width, float height){
        batch.draw(region, x - width / 2, y - height / 2, width, height);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h, float rotation){
        batch.draw(region, x - w / 2, y - h / 2, w / 2, h / 2, w, h, 1, 1, rotation);
    }

    public static void rect(String name, float x, float y){
        rect(region(name), x, y);
    }

    public static void rect(String name, float x, float y, float rotation){
        rect(region(name), x, y, rotation);
    }

    public static void rect(TextureRegion region, float x, float y, float rotation){
        rect(region, x, y, region.getRegionWidth() * scl, region.getRegionHeight() * scl, rotation);
    }

    public static void rect(String name, float x, float y, float w, float h, float rotation){
        rect(region(name), x, y, w, h, rotation);
    }

    public static void rect(String name, float x, float y, float w, float h){
        rect(region(name), x, y, w, h);
    }

    /** Rectangle centered and rotated around its bottom middle point. */
    public static void grect(String name, float x, float y, float rotation){
        grect(region(name), x, y, rotation);
    }

    /** Rectangle centered and rotated around its bottom middle point. */
    public static void grect(TextureRegion region, float x, float y, float rotation){
        grect(region, x, y, region.getRegionWidth() * scl, region.getRegionHeight() * scl, rotation);
    }

    /** Rectangle centered and rotated around its bottom middle point. */
    public static void grect(TextureRegion region, float x, float y, float w, float h, float rotation){
        batch.draw(region, x - w / 2f, y, w / 2f, 0, w, h, 1, 1, rotation);
    }

    /** Grounded rect. */
    public static void grect(String name, float x, float y){
        TextureRegion region = region(name);
        batch.draw(region, x - region.getRegionWidth() / 2, y);
    }

    /** Grounded rect with rotation and origin. */
    public static void grect(String name, float x, float y, float originx, float originy, float rotation, boolean flip){
        TextureRegion region = region(name);
        batch.draw(region, x - region.getRegionWidth() / 2 * -Mathf.sign(flip), y, originx, originy,
                region.getRegionWidth() * -Mathf.sign(flip), region.getRegionHeight(), 1f, 1f, rotation);
    }

    /** Grounded rect. */
    public static void grect(String name, float x, float y, boolean flipx){
        TextureRegion region = region(name);
        if(flipx){
            batch.draw(region, x + region.getRegionWidth() / 2, y, -region.getRegionWidth(), region.getRegionHeight());
        }else{
            batch.draw(region, x - region.getRegionWidth() / 2, y);
        }
    }

    /** Grounded rect. */
    public static void grect(String name, float x, float y, float w, float h){
        TextureRegion region = region(name);
        batch.draw(region, x - w / 2, y, w, h);
    }

    public static void crect(String name, float x, float y, float w, float h){
       crect(region(name), x, y, w, h);
    }

    public static void crect(TextureRegion texture, float x, float y, float w, float h){
        batch.draw(texture, x, y, w, h);
    }

    public static void crect(TextureRegion texture, float x, float y){
        crect(texture, x, y, texture.getRegionWidth(), texture.getRegionHeight());
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, Consumer<Vector2> tweaker){
        rectv(region, x, y, width, height, width / 2, height / 2, 0, tweaker);
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, float rotation, Consumer<Vector2> tweaker){
        rectv(region, x, y, width, height, width / 2, height / 2, rotation, tweaker);
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, float originX, float originY, float rotation, Consumer<Vector2> tweaker){
        x -= width / 2f;
        y -= height / 2f;

        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        float cos = MathUtils.cosDeg(rotation);
        float sin = MathUtils.sinDeg(rotation);

        float x1 = cos * fx - sin * fy + worldOriginX;
        float y1 = sin * fx + cos * fy + worldOriginY;
        float x2 = cos * fx - sin * fy2 + worldOriginX;
        float y2 = sin * fx + cos * fy2 + worldOriginY;
        float x3 = cos * fx2 - sin * fy2 + worldOriginX;
        float y3 = sin * fx2 + cos * fy2 + worldOriginY;
        float x4 = x1 + (x3 - x2);
        float y4 = y3 - (y2 - y1);

        tweaker.accept(Tmp.v1.set(x1, y1));
        x1 = Tmp.v1.x;
        y1 = Tmp.v1.y;

        tweaker.accept(Tmp.v1.set(x2, y2));
        x2 = Tmp.v1.x;
        y2 = Tmp.v1.y;

        tweaker.accept(Tmp.v1.set(x3, y3));
        x3 = Tmp.v1.x;
        y3 = Tmp.v1.y;

        tweaker.accept(Tmp.v1.set(x4, y4));
        x4 = Tmp.v1.x;
        y4 = Tmp.v1.y;

        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();

        float color = Core.batch.getPackedColor();

        int i = 0;
        vertices[i++] = x1;
        vertices[i++] = y1;
        vertices[i++] = color;
        vertices[i++] = u;
        vertices[i++] = v;

        vertices[i++] = x2;
        vertices[i++] = y2;
        vertices[i++] = color;
        vertices[i++] = u;
        vertices[i++] = v2;

        vertices[i++] = x3;
        vertices[i++] = y3;
        vertices[i++] = color;
        vertices[i++] = u2;
        vertices[i++] = v2;

        vertices[i++] = x4;
        vertices[i++] = y4;
        vertices[i++] = color;
        vertices[i++] = u2;
        vertices[i++] = v;

        batch.draw(region.getTexture(), vertices, 0, vertices.length);
    }

    public static void tscl(float scl){
        Core.font.getData().setScale(scl);
    }

    public static void text(String text, float x, float y){
        text(text, x, y, Align.center);
    }

    public static Vector2 textc(String text, float x, float y, Vector2 outsize){
        GlyphLayout lay = Pooling.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(Core.font, text);
        Core.font.draw(batch, text, x - lay.width / 2, y + lay.height / 2);
        outsize.set(lay.width, lay.height);
        Pooling.free(lay);
        return outsize;
    }

    public static void text(String text, float x, float y, int align){
        Core.font.draw(batch, text, x, y, 0, align, false);
    }

    public static void tcolor(Color color){
        Core.font.setColor(color);
    }

    public static void tcolor(float r, float g, float b, float a){
        Core.font.setColor(r, g, b, a);
    }

    public static void tcolor(float r, float g, float b){
        Core.font.setColor(r, g, b, 1f);
    }

    public static void tcolor(float alpha){
        Core.font.setColor(1f, 1f, 1f, alpha);
    }

    public static void tcolor(){
        Core.font.setColor(Color.WHITE);
    }

    /** Resets thickness, color and text color */
    public static void reset(){

        Lines.stroke(1f);
        color();
        if(Core.font != null)
            tcolor();
    }

    public static TextureRegion region(String name){
        return Core.atlas == null ? null : Core.atlas.getRegion(name);
    }

    public static TextureRegion region(String name, TextureRegion def){
        return Core.atlas == null ? def : Core.atlas.getRegion(name, def);
    }

    public static TextureRegion optional(String name){
        return Core.atlas == null ? null : Core.atlas.getRegion(name, null);
    }

    public static boolean hasRegion(String name){
        return Core.atlas != null && Core.atlas.hasRegion(name);
    }

}
