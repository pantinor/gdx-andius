package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class ObjLoader {

    public static Model loadModel(String fname, String name, float scale) {
        ObjData data;
        try {
            data = parseObj(fname);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Try to load the .mtl with the same base name as the .obj
        HashMap<String, Material> mtlMaterials = new HashMap<>();
        try {
            int dot = fname.lastIndexOf('.');
            String mtlName = (dot >= 0)
                    ? fname.substring(0, dot) + ".mtl"
                    : fname + ".mtl";
            mtlMaterials.putAll(parseMtl(mtlName));
        } catch (Exception e) {
            // If no MTL or parse error, we just fall back to default material
            e.printStackTrace();
        }

        final VertexAttributes solidVA = new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)
        );

        // Fallback material when a face has no or unknown material
        final Material defaultSolidMaterial = new Material(ColorAttribute.createDiffuse(Color.CHARTREUSE));

        for (ObjObject obj : data.objects) {
            if (!obj.name.equals(name)) {
                continue;
            }

            // --- compute vertical offset so the model sits on the ground ---
            float minY = Float.POSITIVE_INFINITY;

            for (int[] chain : obj.lines) {
                for (int vi : chain) {
                    float y = data.vertices.get(vi).y;
                    if (y < minY) {
                        minY = y;
                    }
                }
            }
            for (int[] poly : obj.faces) {
                for (int vi : poly) {
                    float y = data.vertices.get(vi).y;
                    if (y < minY) {
                        minY = y;
                    }
                }
            }

            final float yOffset = (minY == Float.POSITIVE_INFINITY) ? 0f : -minY;

            ModelBuilder mb = new ModelBuilder();
            mb.begin();

            Vector3 tmp0 = new Vector3();
            Vector3 tmp1 = new Vector3();
            Vector3 tmp2 = new Vector3();
            Vector3 edge0 = new Vector3();
            Vector3 edge1 = new Vector3();
            Vector3 normal = new Vector3();

            HashMap<String, MeshPartBuilder> solidBuilders = new HashMap<>();

            for (int faceIndex = 0; faceIndex < obj.faces.size(); faceIndex++) {
                int[] poly = obj.faces.get(faceIndex);
                if (poly.length < 3) {
                    continue;
                }

                String mtlName = (faceIndex < obj.faceMaterials.size())
                        ? obj.faceMaterials.get(faceIndex)
                        : null;

                String key = (mtlName != null) ? mtlName : "__default__";

                MeshPartBuilder solid = solidBuilders.get(key);
                if (solid == null) {
                    Material mat = (mtlName != null) ? mtlMaterials.get(mtlName) : null;
                    if (mat == null) {
                        mat = defaultSolidMaterial;
                    }
                    solid = mb.part(
                            obj.name + "_solid_" + key,
                            GL20.GL_TRIANGLES,
                            solidVA,
                            mat
                    );
                    solidBuilders.put(key, solid);
                }

                int v0Index = poly[0];
                Vector3 v0 = tmp0.set(data.vertices.get(v0Index)).add(0f, yOffset, 0f);

                for (int i = 1; i < poly.length - 1; i++) {
                    int v1Index = poly[i];
                    int v2Index = poly[i + 1];

                    Vector3 v1 = tmp1.set(data.vertices.get(v1Index)).add(0f, yOffset, 0f);
                    Vector3 v2 = tmp2.set(data.vertices.get(v2Index)).add(0f, yOffset, 0f);

                    edge0.set(v1).sub(v0);
                    edge1.set(v2).sub(v0);
                    normal.set(edge0).crs(edge1).nor();
                    if (normal.isZero(1e-6f)) {
                        continue;
                    }

                    VertexInfo i0 = new VertexInfo().setPos(v0).setNor(normal);
                    VertexInfo i1 = new VertexInfo().setPos(v1).setNor(normal);
                    VertexInfo i2 = new VertexInfo().setPos(v2).setNor(normal);

                    short s0 = solid.vertex(i0);
                    short s1 = solid.vertex(i1);
                    short s2 = solid.vertex(i2);
                    solid.triangle(s0, s1, s2);
                }
            }

            Model model = mb.end();
            model.nodes.get(0).scale.set(scale, scale, scale);
            return model;
        }

        // no object with that name found
        return null;
    }

    private static ObjData parseObj(String fname) throws IOException {
        FileHandle fh = Gdx.files.classpath(fname);
        try (BufferedReader br = fh.reader(64 * 1024)) {
            ArrayList<Vector3> verts = new ArrayList<>();
            ArrayList<ObjObject> objects = new ArrayList<>();
            ObjObject current = null;
            String currentMtl = null; // current material name from `usemtl`

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("v ")) {
                    // vertex
                    String[] tok = line.split("\\s+");
                    float x = Float.parseFloat(tok[1]);
                    float y = Float.parseFloat(tok[2]);
                    float z = Float.parseFloat(tok[3]);
                    verts.add(new Vector3(x, y, z));

                } else if (line.startsWith("o ") || line.startsWith("g ")) {
                    // new object / group
                    String name = line.substring(2).trim();
                    current = new ObjObject(name);
                    objects.add(current);

                } else if (line.startsWith("usemtl ")) {
                    // set current material
                    currentMtl = line.substring(7).trim();

                } else if (line.startsWith("l ")) {
                    // polyline (edges)
                    if (current == null) {
                        current = new ObjObject("default");
                        objects.add(current);
                    }

                    String[] tok = line.split("\\s+");
                    int n = tok.length - 1;
                    if (n >= 2) {
                        int[] poly = new int[n];
                        for (int i = 0; i < n; i++) {
                            poly[i] = parseIndex(tok[i + 1], verts.size());
                        }
                        current.lines.add(poly);
                    }

                } else if (line.startsWith("f ")) {
                    // polygon / face
                    if (current == null) {
                        current = new ObjObject("default");
                        objects.add(current);
                    }

                    String[] tok = line.split("\\s+");
                    int n = tok.length - 1;
                    if (n >= 3) {
                        int[] poly = new int[n];
                        for (int i = 0; i < n; i++) {
                            poly[i] = parseIndex(tok[i + 1], verts.size());
                        }
                        current.faces.add(poly);
                        // remember which material was active for this face
                        current.faceMaterials.add(currentMtl);
                    }
                }
                // other OBJ directives (vn, vt, mtllib, s, etc.) are ignored
            }

            return new ObjData(verts, objects);
        }
    }

    private static HashMap<String, Material> parseMtl(String fname) throws IOException {
        HashMap<String, Material> out = new HashMap<>();

        FileHandle fh = Gdx.files.classpath(fname);
        if (!fh.exists()) {
            return out; // no mtl, use defaults
        }

        try (BufferedReader br = fh.reader(8 * 1024)) {
            String line;
            String currentName = null;
            Color diffuse = new Color(Color.WHITE);

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("newmtl ")) {
                    // flush previous material
                    if (currentName != null) {
                        out.put(currentName, new Material(ColorAttribute.createDiffuse(diffuse)));
                    }
                    currentName = line.substring(7).trim();
                    diffuse.set(Color.WHITE);

                } else if (line.startsWith("Kd ")) {
                    // diffuse RGB
                    String[] tok = line.split("\\s+");
                    if (tok.length >= 4) {
                        float r = Float.parseFloat(tok[1]);
                        float g = Float.parseFloat(tok[2]);
                        float b = Float.parseFloat(tok[3]);
                        diffuse.set(r, g, b, 1f);
                    }

                } else if (line.startsWith("d ")) {
                    // opacity
                    String[] tok = line.split("\\s+");
                    if (tok.length >= 2) {
                        float a = Float.parseFloat(tok[1]);
                        diffuse.a = a;
                    }
                }
                // Ka, Ks, Ns, etc. ignored for now
            }

            // flush last material
            if (currentName != null) {
                out.put(currentName, new Material(ColorAttribute.createDiffuse(diffuse)));
            }
        }

        return out;
    }

    private static int parseIndex(String token, int vertCount) {
        int slash = token.indexOf('/');
        String viStr = (slash < 0) ? token : token.substring(0, slash);
        int vi = Integer.parseInt(viStr);
        if (vi > 0) {
            return vi - 1;
        } else {
            return vertCount + vi;
        }
    }

    private static void addEdge(HashSet<Long> set, int a, int b) {
        int i0 = Math.min(a, b);
        int i1 = Math.max(a, b);
        long key = ((long) i0 << 32) | (i1 & 0xFFFFFFFFL);
        set.add(key);
    }

    private static class ObjData {

        final ArrayList<Vector3> vertices;
        final ArrayList<ObjObject> objects;

        ObjData(ArrayList<Vector3> vertices, ArrayList<ObjObject> objects) {
            this.vertices = vertices;
            this.objects = objects;
        }
    }

    private static class ObjObject {

        final String name;
        final ArrayList<int[]> lines = new ArrayList<>();
        final ArrayList<int[]> faces = new ArrayList<>();
        final ArrayList<String> faceMaterials = new ArrayList<>();

        ObjObject(String name) {
            this.name = name;
        }
    }

}
