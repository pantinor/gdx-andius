"""Build the low-poly Ultima I overworld OBJ using only the Python standard library.

Run: python tools/generate_overworld.py
One unit is one TMX tile. X runs east, Z runs south, and Y is up.
Tile (column, row) has its center at (column + .5, height, row + .5).
Natural scenery is batched in small objects; buildings and entrances are separate.
"""

import argparse
import base64
from collections import Counter, defaultdict
import gzip
import math
from pathlib import Path
import random
import re
import struct
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
MATERIALS = {
    "grass": (.30, .46, .17), "grass_light": (.34, .49, .19),
    "grass_dark": (.27, .42, .15), "forest_floor": (.19, .31, .12),
    "shore": (.57, .51, .32), "earth": (.30, .25, .16),
    "rock": (.43, .45, .40), "rock_light": (.53, .55, .49),
    "summit": (.75, .76, .68),
    "ocean": (.035, .19, .29), "ocean_light": (.04, .21, .32),
    "shallows": (.065, .34, .39), "surf": (.30, .57, .56),
    "trunk": (.28, .17, .085), "pine": (.10, .26, .12),
    "pine_light": (.16, .34, .14), "leaves": (.24, .40, .12),
    "leaves_light": (.34, .48, .16), "bush": (.36, .43, .12),
    "stone": (.66, .63, .51), "stone_dark": (.39, .40, .35),
    "plaster": (.84, .75, .55), "roof": (.42, .19, .11),
    "roof_dark": (.27, .24, .20), "door": (.20, .12, .065),
    "entrance": (.035, .045, .035), "flag": (.68, .16, .10),
    "window": (.20, .30, .29), "sign": (.54, .34, .14),
}


class Mesh:
    def __init__(self, name):
        self.name = name
        self.vertices = []
        self.vertex_ids = {}
        self.faces = defaultdict(list)

    def face(self, points, material):
        # Export triangles with explicit outward normals (also readable by LibGDX).
        for i in range(1, len(points) - 1):
            triangle = [points[0], points[i], points[i + 1]]
            indices = []
            for point in triangle:
                key = tuple(round(v, 6) for v in point)
                if key not in self.vertex_ids:
                    self.vertex_ids[key] = len(self.vertices)
                    self.vertices.append(key)
                indices.append(self.vertex_ids[key])
            self.faces[material].append(indices)


def box(mesh, x, y, z, width, height, depth, material):
    a, b = x - width / 2, x + width / 2
    c, d = z - depth / 2, z + depth / 2
    t = y + height
    for points in (
        [(a, t, c), (a, t, d), (b, t, d), (b, t, c)],
        [(a, y, d), (a, y, c), (b, y, c), (b, y, d)],
        [(a, y, c), (a, t, c), (b, t, c), (b, y, c)],
        [(b, y, d), (b, t, d), (a, t, d), (a, y, d)],
        [(a, y, d), (a, t, d), (a, t, c), (a, y, c)],
        [(b, y, c), (b, t, c), (b, t, d), (b, y, d)],
    ):
        mesh.face(points, material)


def cone(mesh, x, y, z, radius, height, material, sides=6, phase=0, top_radius=0):
    ring = [(x + radius * math.cos(phase + i * math.tau / sides), y,
             z + radius * math.sin(phase + i * math.tau / sides)) for i in range(sides)]
    upper = [(x + top_radius * math.cos(phase + i * math.tau / sides), y + height,
              z + top_radius * math.sin(phase + i * math.tau / sides)) for i in range(sides)]
    for i in range(sides):
        j = (i + 1) % sides
        if top_radius:
            mesh.face([ring[j], ring[i], upper[i], upper[j]], material)
        else:
            mesh.face([ring[j], ring[i], (x, y + height, z)], material)
    mesh.face(ring, material)
    if top_radius:
        mesh.face(list(reversed(upper)), material)


def bush(mesh, x, y, z, radius, material="bush"):
    cone(mesh, x, y, z, radius, radius * .65, material, 5, top_radius=radius * .62)
    cone(mesh, x, y + radius * .65, z, radius * .62, radius * .55, material, 5)


def tree(mesh, x, y, z, rng):
    height = rng.uniform(.61, .95)
    radius = rng.uniform(.19, .26)
    cone(mesh, x, y, z, .043, height * .72, "trunk", 5, top_radius=.025)
    if rng.random() < .65:
        cone(mesh, x, y + height * .20, z, radius, height * .65, "pine", 6)
        cone(mesh, x, y + height * .48, z, radius * .74, height * .55, "pine_light", 6)
    else:
        bush(mesh, x, y + height * .36, z, radius, "leaves")
        bush(mesh, x + .035, y + height * .60, z, radius * .77, "leaves_light")


def house(mesh, x, y, z, width, depth, height, rng):
    box(mesh, x, y, z, width, height, depth, "plaster")
    a, b, c, d = x - width * .58, x + width * .58, z - depth * .57, z + depth * .57
    e, t = y + height, y + height + width * .43
    mesh.face([(a, e, d), (x, t, d), (x, t, c), (a, e, c)], "roof")
    mesh.face([(x, t, d), (b, e, d), (b, e, c), (x, t, c)], "roof")
    mesh.face([(x, t, c), (b, e, c), (a, e, c)], "plaster")
    mesh.face([(x, t, d), (a, e, d), (b, e, d)], "plaster")
    box(mesh, x, y, z + depth * .5 + .002, width * .22, height * .57, .008, "door")
    box(mesh, x - width * .27, y + height * .52, z + depth * .5 + .006,
        width * .14, height * .20, .009, "window")
    if rng.random() < .5:
        box(mesh, x + width * .25, e, z - depth * .20, width * .13, width * .40,
            width * .15, "stone_dark")


def castle(mesh, x, y, z, ruined=False):
    stone = "stone_dark" if ruined else "stone"
    box(mesh, x, y, z, .91, .065, .88, "stone_dark")
    # Courtyard walls and a genuine gap through the southern gate.
    for dx in (-.35, .35):
        box(mesh, x + dx, y, z, .11, .39, .72, stone)
    box(mesh, x, y, z - .35, .72, .39, .11, stone)
    for dx in (-.245, .245):
        box(mesh, x + dx, y, z + .35, .23, .33, .11, stone)
    box(mesh, x, y + .25, z + .35, .27, .14, .11, stone)
    box(mesh, x, y, z + .285, .25, .25, .015, "entrance")
    for dx in (-.35, .35):
        for dz in (-.35, .35):
            h = .44 if ruined and dx > 0 else .56
            box(mesh, x + dx, y, z + dz, .20, h, .20, stone)
            for ox in (-.066, .066):
                for oz in (-.066, .066):
                    box(mesh, x + dx + ox, y + h, z + dz + oz, .068, .075, .068, stone)
    box(mesh, x, y, z - .09, .30, .66, .30, stone)
    for dx in (-.11, .11):
        for dz in (-.20, .02):
            box(mesh, x + dx, y + .66, z + dz, .08, .08, .08, stone)
    box(mesh, x, y + .66, z - .09, .018, .28, .018, "trunk")
    # A thick pennant remains visible with backface culling enabled.
    mesh.face([(x, y + .91, z - .101), (x + .19, y + .87, z - .101),
               (x, y + .82, z - .101)], "flag")
    mesh.face([(x, y + .82, z - .079), (x + .19, y + .87, z - .079),
               (x, y + .91, z - .079)], "flag")


def dungeon(mesh, x, y, z, rng):
    # Stone arch, recessed black interior, and a short approach: no flat icon.
    box(mesh, x, y, z, .79, .035, .77, "rock")
    for dx in (-.28, .28):
        box(mesh, x + dx, y, z, .19, .35, .42, "rock")
    center_y = y + .34
    for i in range(7):
        a, b = i * math.pi / 7, (i + 1) * math.pi / 7
        inner_a = (x + .185 * math.cos(a), center_y + .185 * math.sin(a))
        inner_b = (x + .185 * math.cos(b), center_y + .185 * math.sin(b))
        outer_a = (x + .375 * math.cos(a), center_y + .375 * math.sin(a))
        outer_b = (x + .375 * math.cos(b), center_y + .375 * math.sin(b))
        # Each voussoir is closed, with front facing south (+Z).
        front = [(px, py, z + .21) for px, py in (inner_a, outer_a, outer_b, inner_b)]
        back = [(px, py, z - .21) for px, py in (inner_a, outer_a, outer_b, inner_b)]
        mesh.face(front, "rock_light" if i % 2 else "rock")
        mesh.face(list(reversed(back)), "rock")
        for k in range(4):
            j = (k + 1) % 4
            mesh.face([front[k], back[k], back[j], front[j]], "rock")
    opening = [(x - .185, y, z - .18), (x + .185, y, z - .18)]
    opening += [(x + .185 * math.cos(i * math.pi / 7),
                 center_y + .185 * math.sin(i * math.pi / 7), z - .18) for i in range(8)]
    mesh.face(opening, "entrance")
    box(mesh, x, y, z + .29, .34, .02, .17, "stone_dark")


def build(source, destination):
    root = ET.parse(source).getroot()
    width, height = int(root.get("width")), int(root.get("height"))
    if root.get("orientation") != "orthogonal" or root.get("infinite") != "0":
        raise ValueError("Expected a finite orthogonal TMX map")
    layer = root.find("layer[@name='map']")
    data = layer.find("data")
    if data.get("encoding") != "base64" or data.get("compression") != "gzip":
        raise ValueError("Expected base64/gzip tile data")
    gids = struct.unpack(f"<{width * height}I", gzip.decompress(base64.b64decode(data.text)))
    firstgid = int(root.find("tileset").get("firstgid"))
    # Normalize to the original tileset's one-based tile numbers, ignoring TMX flip flags.
    tiles = [(gid & 0x0fffffff) - firstgid + 1 for gid in gids]
    unknown = set(tiles) - {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 16, 17, 53, 54}
    if unknown:
        raise ValueError(f"Unmapped tiles: {unknown}")
    water_ids = {1, 53, 54}

    def tile(x, z):
        return tiles[z * width + x] if 0 <= x < width and 0 <= z < height else 1

    def corner(x, z):
        surrounding = [tile(x + dx, z + dz) for dx, dz in ((-1, -1), (0, -1), (-1, 0), (0, 0))]
        if any(t in water_ids for t in surrounding):
            return .065
        return .16 + max(0, surrounding.count(4) - 1) * .13

    portals = {}
    for group in root.findall("objectgroup"):
        for obj in group.findall("object"):
            x = int(float(obj.get("x", "0")) / int(root.get("tilewidth")))
            z = int(float(obj.get("y", "0")) / int(root.get("tileheight")))
            portals[x, z] = re.sub(r"[^A-Za-z0-9_]+", "_", obj.get("name", "landmark"))
    objects = {}

    def mesh(name):
        if name not in objects:
            objects[name] = Mesh(name)
        return objects[name]

    counts = Counter()
    for z in range(height):
        for x in range(width):
            t = tile(x, z)
            rng = random.Random(1000003 * z + x + 137)
            cx, cz = x + .5, z + .5
            chunk = f"{x // 8:02d}_{z // 8:02d}"
            if t in water_ids:
                m = mesh(f"water_{x // 32:02d}_{z // 32:02d}")
                coastal = any(tile(x + dx, z + dz) not in water_ids
                              for dx in range(-1, 2) for dz in range(-1, 2))
                material = "shallows" if coastal else rng.choice(("ocean", "ocean", "ocean_light"))
                m.face([(x, 0, z), (x, 0, z + 1), (x + 1, 0, z + 1), (x + 1, 0, z)], material)
                if coastal and rng.random() < .13:
                    m.face([(cx - .18, .004, cz), (cx + .14, .004, cz + .025),
                            (cx + .18, .004, cz - .004), (cx - .14, .004, cz - .025)], "surf")
                counts["water_tiles"] += 1
                continue

            m = mesh(f"terrain_{chunk}")
            ring = [(x, corner(x, z), z), (x, corner(x, z + 1), z + 1),
                    (x + 1, corner(x + 1, z + 1), z + 1), (x + 1, corner(x + 1, z), z)]
            if t == 4:
                # Shared boundary vertices join neighboring peaks into mountain ranges.
                peak = (cx + rng.uniform(-.12, .12), rng.uniform(.95, 1.65), cz + rng.uniform(-.12, .12))
                for i in range(4):
                    a, b = ring[i], ring[(i + 1) % 4]
                    sa = tuple(peak[k] * .75 + a[k] * .25 for k in range(3))
                    sb = tuple(peak[k] * .75 + b[k] * .25 for k in range(3))
                    m.face([a, b, sb, sa], "rock" if i % 2 else "rock_light")
                    m.face([sa, sb, peak], "summit" if peak[1] > 1.38 else "rock_light")
                counts["mountain_tiles"] += 1
            else:
                material = "forest_floor" if t == 3 else rng.choice(("grass", "grass", "grass_light", "grass_dark"))
                for i in range(4):
                    m.face([ring[i], ring[(i + 1) % 4], (cx, .16, cz)], material)
            for i, (dx, dz) in enumerate(((-1, 0), (0, 1), (1, 0), (0, -1))):
                if tile(x + dx, z + dz) in water_ids:
                    a, b = ring[i], ring[(i + 1) % 4]
                    m.face([b, a, (a[0], -.12, a[2]), (b[0], -.12, b[2])], "shore")
            counts["land_tiles"] += 1
            if t == 3:
                foliage = mesh(f"vegetation_{chunk}")
                for dx, dz in ((-.22, -.19), (.23, .20)):
                    tree(foliage, cx + dx + rng.uniform(-.04, .04), .16,
                         cz + dz + rng.uniform(-.04, .04), rng)
                    counts["trees"] += 1
                bush(foliage, cx - .20, .16, cz + .25, rng.uniform(.11, .16))
                counts["bushes"] += 1
            elif t == 2 and rng.random() < .13:
                foliage = mesh(f"vegetation_{chunk}")
                bush(foliage, cx + rng.uniform(-.28, .28), .16, cz + rng.uniform(-.28, .28), rng.uniform(.09, .15))
                counts["bushes"] += 1
            label = portals.get((x, z), "unnamed") + f"_{x:03d}_{z:03d}"
            if t in (5, 6, 16, 17):
                castle(mesh(f"castle_{label}"), cx, .16, cz, t == 16)
                counts["castles"] += 1
            elif t in (8, 9):
                # Every house remains independently selectable in an OBJ importer.
                for i, (dx, dz, w, d, h) in enumerate(((-.22, -.22, .34, .32, .27),
                                                       (.23, -.12, .30, .36, .32),
                                                       (-.10, .25, .35, .28, .25))):
                    house(mesh(f"building_{label}_{i + 1}"), cx + dx, .16, cz + dz, w, d, h, rng)
                    counts["buildings"] += 1
            elif t == 10:
                dungeon(mesh(f"dungeon_entrance_{label}"), cx, .16, cz, rng)
                counts["dungeon_entrances"] += 1
            elif t == 7:
                sign = mesh(f"signpost_{label}")
                box(sign, cx, .16, cz, .045, .55, .045, "trunk")
                box(sign, cx, .49, cz, .43, .18, .045, "sign")
                counts["signposts"] += 1

    destination.parent.mkdir(parents=True, exist_ok=True)
    mtl = destination.with_suffix(".mtl")
    with mtl.open("w", newline="\n") as f:
        f.write("# Ultima I overworld - solid colors, no external textures required\n")
        for name, color in MATERIALS.items():
            f.write(f"newmtl {name}\nKa {' '.join(f'{v * .35:.4f}' for v in color)}\n")
            f.write(f"Kd {' '.join(f'{v:.4f}' for v in color)}\nKs 0.0000 0.0000 0.0000\nNs 1\nd 1\nillum 1\n\n")

    vertex_offset, normal_offset = 1, 1
    total_triangles = 0
    with destination.open("w", newline="\n") as f:
        f.write(f"# Generated from {source.name} by tools/generate_overworld.py\n")
        f.write(f"# {width} x {height} tiles; 1 unit/tile; +X east, +Y up, +Z south\n")
        f.write("# Tile centers: (column + 0.5, ground height, row + 0.5); land Y=0.16, water Y=0\n")
        f.write("# TMX GIDs: 1 water, 2 grass, 3 forest, 4 mountain, 5/16/17 castle, 7 sign, 8 town, 10 dungeon\n")
        f.write("# Independent named castles, houses, dungeon entrances; natural scenery in spatial chunks\n")
        f.write("# " + ", ".join(f"{key}={value}" for key, value in sorted(counts.items())) + "\n")
        f.write(f"mtllib {mtl.name}\ns off\n")
        for obj in objects.values():
            f.write(f"o {obj.name}\n")
            for v in obj.vertices:
                f.write("v " + " ".join(f"{n:.6f}" for n in v) + "\n")
            normals, normal_ids, indexed = [], {}, {}
            for material, faces in obj.faces.items():
                indexed[material] = []
                for a, b, c in faces:
                    p, q, r = [obj.vertices[i] for i in (a, b, c)]
                    u, v = [q[k] - p[k] for k in range(3)], [r[k] - p[k] for k in range(3)]
                    n = (u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0])
                    length = math.sqrt(sum(k * k for k in n))
                    if length < 1e-10:
                        raise ValueError(f"Degenerate face in {obj.name}")
                    key = tuple(round(k / length, 6) for k in n)
                    if key not in normal_ids:
                        normal_ids[key] = len(normals)
                        normals.append(key)
                    indexed[material].append(((a, b, c), normal_ids[key]))
            for n in normals:
                f.write("vn " + " ".join(f"{k:.6f}" for k in n) + "\n")
            for material, faces in indexed.items():
                f.write(f"usemtl {material}\n")
                for triangle, normal in faces:
                    f.write("f " + " ".join(f"{v + vertex_offset}//{normal + normal_offset}" for v in triangle) + "\n")
                    total_triangles += 1
            vertex_offset += len(obj.vertices)
            normal_offset += len(normals)
    print(f"Wrote {destination}: {len(objects)} objects, {vertex_offset - 1:,} vertices, {total_triangles:,} triangles")
    print(dict(sorted(counts.items())))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tmx", type=Path, default=ROOT / "src/main/resources/assets/data/ultima1.tmx")
    parser.add_argument("--output", type=Path, default=ROOT / "src/main/resources/assets/graphics/overworld.obj")
    args = parser.parse_args()
    build(args.tmx, args.output)
