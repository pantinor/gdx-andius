package utils;

import andius.Constants.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

public class WrappingTileMapRenderer extends BatchTiledMapRenderer {

    private final Map map;
    private final SpreadFOV fov;
    float stateTime = 0;

    public WrappingTileMapRenderer(Map map, TiledMap tiledMap, SpreadFOV fov, float unitScale) {
        super(tiledMap, unitScale);
        this.map = map;
        this.fov = fov;
    }

    public SpreadFOV getFOV() {
        return this.fov;
    }

    @Override
    public void render() {
        beginRender();
        for (MapLayer layer : map.getTiledMap().getLayers()) {
            if (layer instanceof TiledMapTileLayer && layer.isVisible()) {
                renderTileLayer((TiledMapTileLayer) layer);
            }
        }
        endRender();
    }

    @Override
    public void renderTileLayer(TiledMapTileLayer layer) {

        stateTime += Gdx.graphics.getDeltaTime();

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        int layerTileWidth = (int) (layer.getTileWidth() * unitScale);
        int layerTileHeight = (int) (layer.getTileHeight() * unitScale);

        int col1 = (int) (viewBounds.x / layerTileWidth);
        int col2 = (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth);
        int row1 = (int) (viewBounds.y / layerTileHeight);
        int row2 = (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight);

        float y = row2 * layerTileHeight;
        float startX = col1 * layerTileWidth;

        for (int row = row2; row >= row1; row--) {

            float x = startX;
            for (int col = col1; col < col2; col++) {

                int cx = col;
                if (col < 0) {
                    cx = layerWidth + col;
                } else if (col >= layerWidth) {
                    cx = col - layerWidth;
                }

                int cy = row;
                if (row < 0) {
                    cy = layerHeight + row;
                } else if (row >= layerHeight) {
                    cy = row - layerHeight;
                }

                TiledMapTileLayer.Cell cell = layer.getCell(cx, cy);
                float color = getColor(layer, col, layerHeight - 1 - row);

                if (cell == null) {
                    x += layerTileWidth;
                    continue;
                }

                TiledMapTile tile = cell.getTile();
                if (tile != null) {

                    TextureRegion region = tile.getTextureRegion();

                    float x1 = x + tile.getOffsetX() * unitScale;
                    float y1 = y + tile.getOffsetY() * unitScale;
                    float x2 = x1 + region.getRegionWidth() * unitScale;
                    float y2 = y1 + region.getRegionHeight() * unitScale;

                    float u1 = region.getU();
                    float v1 = region.getV2();
                    float u2 = region.getU2();
                    float v2 = region.getV();

                    vertices[X1] = x1;
                    vertices[Y1] = y1;
                    vertices[C1] = color;
                    vertices[U1] = u1;
                    vertices[V1] = v1;

                    vertices[X2] = x1;
                    vertices[Y2] = y2;
                    vertices[C2] = color;
                    vertices[U2] = u1;
                    vertices[V2] = v2;

                    vertices[X3] = x2;
                    vertices[Y3] = y2;
                    vertices[C3] = color;
                    vertices[U3] = u2;
                    vertices[V3] = v2;

                    vertices[X4] = x2;
                    vertices[Y4] = y1;
                    vertices[C4] = color;
                    vertices[U4] = u2;
                    vertices[V4] = v1;

                    batch.draw(region.getTexture(), vertices, 0, 20);
                }
                x += layerTileWidth;
            }
            y -= layerTileHeight;
        }

    }

    public float getColor(TiledMapTileLayer layer, int col, int row) {

        Color batchColor = this.batch.getColor();
        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        int cx = layerWidth + col;
        int cy = layerHeight + row;

        float[][] lightMap = fov.lightMap();

        if (!(cx >= 0 && cx < lightMap.length && cy >= 0 && cy < lightMap[0].length)) {
            return Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, 1f);
        }

        float val = lightMap[cx][cy];

        if (val <= 0) {
            return Color.BLACK.toFloatBits();
        } else {
            return Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, 1f);
        }

    }
    
    private final Rectangle box = new Rectangle();

    public void draw(TextureRegion region, float x, float y, float color) {

        this.box.set(x, y, 5, 5);

        if (this.viewBounds.contains(this.box)) {

            float x1 = x;
            float y1 = y;
            float x2 = x1 + region.getRegionWidth() * unitScale;
            float y2 = y1 + region.getRegionHeight() * unitScale;

            float u1 = region.getU();
            float v1 = region.getV2();
            float u2 = region.getU2();
            float v2 = region.getV();

            vertices[X1] = x1;
            vertices[Y1] = y1;
            vertices[C1] = color;
            vertices[U1] = u1;
            vertices[V1] = v1;

            vertices[X2] = x1;
            vertices[Y2] = y2;
            vertices[C2] = color;
            vertices[U2] = u1;
            vertices[V2] = v2;

            vertices[X3] = x2;
            vertices[Y3] = y2;
            vertices[C3] = color;
            vertices[U3] = u2;
            vertices[V3] = v2;

            vertices[X4] = x2;
            vertices[Y4] = y1;
            vertices[C4] = color;
            vertices[U4] = u2;
            vertices[V4] = v1;
            
            this.batch.draw(region.getTexture(), vertices, 0, 20);
        }
    }

}
