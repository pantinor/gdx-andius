package utils;

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
import andius.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import java.util.ArrayList;
import java.util.List;

public class TmxMapRenderer extends BatchTiledMapRenderer implements Constants {

    private final Map map;
    float stateTime = 0;
    List<CreatureLayer> creatureLayers = new ArrayList<>();

    public interface CreatureLayer {

        public void render(float time);
    }

    public void registerCreatureLayer(CreatureLayer layer) {
        creatureLayers.add(layer);
    }

    public void unregisterCreatureLayer(CreatureLayer layer) {
        creatureLayers.remove(layer);
    }

    public TmxMapRenderer(Map map, TiledMap tiledMap, float unitScale) {
        super(tiledMap, unitScale);
        this.map = map;
    }

    public boolean shouldRenderCell(int roomId, int x, int y) {
        if (this.map.getRoomIds() == null || roomId <= 0) {
            return true;
        }
        if (this.map.getRoomIds()[x][y][0] == roomId || this.map.getRoomIds()[x][y][1] == roomId || this.map.getRoomIds()[x][y][2] == roomId) {
            return true;
        }
        return false;
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

        stateTime += Gdx.graphics.getDeltaTime() / 4;

        if (layer.getName().equals("creature")) {
            for (CreatureLayer cl : creatureLayers) {
                cl.render(stateTime);
            }
            return;
        }

        final Color batchColor = batch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        int layerTileWidth = (int) (layer.getTileWidth() * unitScale);
        int layerTileHeight = (int) (layer.getTileHeight() * unitScale);

        int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
        int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));
        int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
        int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

        float y = row2 * layerTileHeight;
        float startX = col1 * layerTileWidth;

        for (int row = row2; row >= row1; row--) {

            float x = startX;
            for (int col = col1; col < col2; col++) {

                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                if (cell == null || !shouldRenderCell(this.map.getScreen().currentRoomId(), col, layerHeight - row - 1)) {
                    x += layerTileWidth;
                    continue;
                }

                TiledMapTile tile = cell.getTile();

                if (tile != null) {
                    final boolean flipX = cell.getFlipHorizontally();
                    final boolean flipY = cell.getFlipVertically();
                    final int rotations = cell.getRotation();

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

                    if (flipX) {
                        float temp = vertices[U1];
                        vertices[U1] = vertices[U3];
                        vertices[U3] = temp;
                        temp = vertices[U2];
                        vertices[U2] = vertices[U4];
                        vertices[U4] = temp;
                    }
                    if (flipY) {
                        float temp = vertices[V1];
                        vertices[V1] = vertices[V3];
                        vertices[V3] = temp;
                        temp = vertices[V2];
                        vertices[V2] = vertices[V4];
                        vertices[V4] = temp;
                    }
                    if (rotations != 0) {
                        switch (rotations) {
                            case TiledMapTileLayer.Cell.ROTATE_90: {
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V2];
                                vertices[V2] = vertices[V3];
                                vertices[V3] = vertices[V4];
                                vertices[V4] = tempV;

                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U2];
                                vertices[U2] = vertices[U3];
                                vertices[U3] = vertices[U4];
                                vertices[U4] = tempU;
                                break;
                            }
                            case TiledMapTileLayer.Cell.ROTATE_180: {
                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U3];
                                vertices[U3] = tempU;
                                tempU = vertices[U2];
                                vertices[U2] = vertices[U4];
                                vertices[U4] = tempU;
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V3];
                                vertices[V3] = tempV;
                                tempV = vertices[V2];
                                vertices[V2] = vertices[V4];
                                vertices[V4] = tempV;
                                break;
                            }
                            case TiledMapTileLayer.Cell.ROTATE_270: {
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V4];
                                vertices[V4] = vertices[V3];
                                vertices[V3] = vertices[V2];
                                vertices[V2] = tempV;

                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U4];
                                vertices[U4] = vertices[U3];
                                vertices[U3] = vertices[U2];
                                vertices[U2] = tempU;
                                break;
                            }
                        }
                    }
                    batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
                }
                x += layerTileWidth;
            }
            y -= layerTileHeight;
        }
    }
}
