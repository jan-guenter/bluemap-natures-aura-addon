/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Emits bounded cubes and planes using only admitted installed textures. */
final class PrimitiveEmitter {

    private static final float[][] ATLAS_UV = {
            {32F, 0F, 48F, 16F},  // down
            {16F, 0F, 32F, 16F},  // up
            {16F, 16F, 32F, 32F}, // north
            {48F, 16F, 64F, 32F}, // south
            {0F, 16F, 16F, 32F},  // west
            {32F, 16F, 48F, 32F}  // east
    };
    private static final Direction[] DIRECTIONS = {
            Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    PrimitiveEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean cube(
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            Bounds bounds,
            int color,
            boolean atlas,
            boolean fullbright,
            int quarterTurns
    ) {
        if (resourcePack.getTextures().get(texture) == null
                || quarterTurns < 0 || quarterTurns > 3) {
            return false;
        }
        int material = textures.get(texture);
        for (int face = 0; face < DIRECTIONS.length; face++) {
            Direction direction = rotate(DIRECTIONS[face], quarterTurns);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
            }
            FaceLighting.Sample light = FaceLighting.sample(block, direction);
            int visible = settings.isCaveDetectionUsesBlockLight()
                    ? Math.max(light.sunlight(), light.blocklight()) : light.sunlight();
            if (block.isRemoveIfCave() && visible == 0) {
                continue;
            }
            Vertex[] vertices = rotate(vertices(bounds, DIRECTIONS[face]), quarterTurns);
            float[] uv = atlas ? ATLAS_UV[face] : new float[]{0F, 0F, 1F, 1F};
            float divisor = atlas ? 64F : 1F;
            emitQuad(
                    target, material, vertices,
                    uv[0] / divisor, uv[1] / divisor,
                    uv[2] / divisor, uv[3] / divisor,
                    color, fullbright ? 15 : light.sunlight(),
                    fullbright ? 15 : light.blocklight(), false
            );
        }
        return true;
    }

    boolean horizontalPlane(
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            float minimumX,
            float maximumX,
            float y,
            float minimumZ,
            float maximumZ,
            int color,
            boolean fullbright
    ) {
        if (resourcePack.getTextures().get(texture) == null) {
            return false;
        }
        FaceLighting.Sample light = FaceLighting.sample(block, Direction.UP);
        emitQuad(
                target, textures.get(texture),
                new Vertex[]{
                        new Vertex(minimumX, y, maximumZ),
                        new Vertex(maximumX, y, maximumZ),
                        new Vertex(maximumX, y, minimumZ),
                        new Vertex(minimumX, y, minimumZ)
                },
                0F, 0F, 1F, 1F, color,
                fullbright ? 15 : light.sunlight(),
                fullbright ? 15 : light.blocklight(), true
        );
        return true;
    }

    boolean verticalPlane(
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            float minimumX,
            float maximumX,
            float minimumY,
            float maximumY,
            float z,
            int color
    ) {
        if (resourcePack.getTextures().get(texture) == null) {
            return false;
        }
        FaceLighting.Sample light = FaceLighting.sample(block, Direction.SOUTH);
        emitQuad(
                target, textures.get(texture),
                new Vertex[]{
                        new Vertex(minimumX, minimumY, z),
                        new Vertex(maximumX, minimumY, z),
                        new Vertex(maximumX, maximumY, z),
                        new Vertex(minimumX, maximumY, z)
                },
                0F, 1F, 1F, 0F, color,
                light.sunlight(), light.blocklight(), true
        );
        return true;
    }

    private static void emitQuad(
            TileModelView target,
            int material,
            Vertex[] vertices,
            float minimumU,
            float minimumV,
            float maximumU,
            float maximumV,
            int color,
            int sunlight,
            int blocklight,
            boolean doubleSided
    ) {
        int triangles = doubleSided ? 4 : 2;
        int start = target.add(triangles);
        TileModel model = target.getTileModel();
        setTriangle(model, start, vertices[0], vertices[1], vertices[2],
                minimumU, maximumV, maximumU, maximumV, maximumU, minimumV);
        setTriangle(model, start + 1, vertices[0], vertices[2], vertices[3],
                minimumU, maximumV, maximumU, minimumV, minimumU, minimumV);
        if (doubleSided) {
            setTriangle(model, start + 2, vertices[2], vertices[1], vertices[0],
                    maximumU, minimumV, maximumU, maximumV, minimumU, maximumV);
            setTriangle(model, start + 3, vertices[3], vertices[2], vertices[0],
                    minimumU, minimumV, maximumU, minimumV, minimumU, maximumV);
        }
        float red = (color >>> 16 & 0xFF) / 255F;
        float green = (color >>> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        for (int index = start; index < start + triangles; index++) {
            model.setMaterialIndex(index, material);
            model.setColor(index, red, green, blue);
            model.setAOs(index, 1F, 1F, 1F);
            model.setSunlight(index, sunlight);
            model.setBlocklight(index, blocklight);
        }
    }

    private static void setTriangle(
            TileModel model,
            int index,
            Vertex first,
            Vertex second,
            Vertex third,
            float u1,
            float v1,
            float u2,
            float v2,
            float u3,
            float v3
    ) {
        model.setPositions(index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z());
        model.setUvs(index, u1, v1, u2, v2, u3, v3);
    }

    private static Vertex[] vertices(Bounds bounds, Direction face) {
        float x0 = bounds.minimumX();
        float y0 = bounds.minimumY();
        float z0 = bounds.minimumZ();
        float x1 = bounds.maximumX();
        float y1 = bounds.maximumY();
        float z1 = bounds.maximumZ();
        return switch (face) {
            case DOWN -> vertices(x0, y0, z0, x1, y0, z0,
                    x1, y0, z1, x0, y0, z1);
            case UP -> vertices(x0, y1, z1, x1, y1, z1,
                    x1, y1, z0, x0, y1, z0);
            case NORTH -> vertices(x1, y0, z0, x0, y0, z0,
                    x0, y1, z0, x1, y1, z0);
            case SOUTH -> vertices(x0, y0, z1, x1, y0, z1,
                    x1, y1, z1, x0, y1, z1);
            case WEST -> vertices(x0, y0, z0, x0, y0, z1,
                    x0, y1, z1, x0, y1, z0);
            case EAST -> vertices(x1, y0, z1, x1, y0, z0,
                    x1, y1, z0, x1, y1, z1);
        };
    }

    private static Vertex[] vertices(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        return new Vertex[]{
                new Vertex(ax, ay, az), new Vertex(bx, by, bz),
                new Vertex(cx, cy, cz), new Vertex(dx, dy, dz)
        };
    }

    private static Vertex[] rotate(Vertex[] vertices, int quarterTurns) {
        Vertex[] result = new Vertex[vertices.length];
        for (int index = 0; index < vertices.length; index++) {
            Vertex vertex = vertices[index];
            float x = vertex.x() - 0.5F;
            float z = vertex.z() - 0.5F;
            result[index] = switch (quarterTurns) {
                case 0 -> vertex;
                case 1 -> new Vertex(0.5F - z, vertex.y(), 0.5F + x);
                case 2 -> new Vertex(0.5F - x, vertex.y(), 0.5F - z);
                case 3 -> new Vertex(0.5F + z, vertex.y(), 0.5F - x);
                default -> throw new IllegalArgumentException("invalid quarter turns");
            };
        }
        return result;
    }

    private static Direction rotate(Direction direction, int quarterTurns) {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return direction;
        }
        Direction result = direction;
        for (int count = 0; count < quarterTurns; count++) {
            result = switch (result) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
                default -> result;
            };
        }
        return result;
    }

    record Bounds(
            float minimumX,
            float minimumY,
            float minimumZ,
            float maximumX,
            float maximumY,
            float maximumZ
    ) {

        Bounds {
            if (minimumX >= maximumX || minimumY >= maximumY
                    || minimumZ >= maximumZ) {
                throw new IllegalArgumentException("empty primitive bounds");
            }
        }
    }

    private record Vertex(float x, float y, float z) {
    }
}
