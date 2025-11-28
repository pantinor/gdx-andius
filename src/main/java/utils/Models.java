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
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Models {

    public static Model loadModel(String fname, String name, Color color, float scale) {
        ObjData data = null;
        try {
            data = parseObj(fname);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Material material = new Material(
                ColorAttribute.createDiffuse(color),
                ColorAttribute.createSpecular(Color.WHITE)
        );

        final VertexAttributes edgeVA = new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );

        final VertexAttributes solidVA = new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)
        );

        for (ObjObject obj : data.objects) {
            if (!obj.name.equals(name)) {
                continue;
            }

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

            MeshPartBuilder edges = mb.part(obj.name + "_edges", GL20.GL_LINES, edgeVA, material);
            MeshPartBuilder solid = mb.part(obj.name + "_solid", GL20.GL_TRIANGLES, solidVA, material);

            final HashSet<Long> edgeSet = new HashSet<>();

            for (int[] chain : obj.lines) {
                for (int i = 0; i < chain.length - 1; i++) {
                    addEdge(edgeSet, chain[i], chain[i + 1]);
                }
            }

            for (int[] poly : obj.faces) {
                int n = poly.length;
                if (n < 2) {
                    continue;
                }
                for (int i = 0; i < n; i++) {
                    int a = poly[i];
                    int b = poly[(i + 1) % n];
                    addEdge(edgeSet, a, b);
                }
            }

            Vector3 tmp0 = new Vector3();
            Vector3 tmp1 = new Vector3();
            Vector3 tmp2 = new Vector3();
            Vector3 edge0 = new Vector3();
            Vector3 edge1 = new Vector3();
            Vector3 normal = new Vector3();

            for (long key : edgeSet) {
                int i0 = (int) (key >>> 32), i1 = (int) key;
                Vector3 p0 = tmp0.set(data.vertices.get(i0)).add(0f, yOffset, 0f);
                Vector3 p1 = tmp1.set(data.vertices.get(i1)).add(0f, yOffset, 0f);
                short s0 = edges.vertex(new VertexInfo().setPos(p0).setCol(color));
                short s1 = edges.vertex(new VertexInfo().setPos(p1).setCol(color));
                edges.line(s0, s1);
            }

            for (int[] poly : obj.faces) {
                if (poly.length < 3) {
                    continue;
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

        return null;
    }

    private static ObjData parseObj(String fname) throws IOException {
        FileHandle fh = Gdx.files.classpath(fname);
        try (BufferedReader br = fh.reader(64 * 1024)) {
            ArrayList<Vector3> verts = new ArrayList<>();
            ArrayList<ObjObject> objects = new ArrayList<>();
            ObjObject current = null;

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("v ")) {
                    String[] tok = splitWS(line, 4);
                    float x = Float.parseFloat(tok[1]);
                    float y = Float.parseFloat(tok[2]);
                    float z = Float.parseFloat(tok[3]);
                    verts.add(new Vector3(x, y, z));
                } else if (line.startsWith("o ")) {
                    String name = line.substring(2).trim();
                    current = new ObjObject(name);
                    objects.add(current);

                } else if (line.startsWith("l ")) {
                    // make sure we have some object to attach to
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
                    // faces: f v1 v2 v3 [v4 ...]
                    // vertex indices can be like "3", "3/2", "3/2/1" or negative
                    if (current == null) {
                        current = new ObjObject("default");
                        objects.add(current);
                    }

                    String[] tok = line.split("\\s+");
                    int n = tok.length - 1;
                    if (n >= 3) { // at least a triangle
                        int[] poly = new int[n];
                        for (int i = 0; i < n; i++) {
                            poly[i] = parseIndex(tok[i + 1], verts.size());
                        }
                        current.faces.add(poly);
                    }
                }
            }

            return new ObjData(verts, objects);
        }
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

    private static String[] splitWS(String s, int expected) {
        String[] out = new String[expected];
        int idx = 0;
        int i = 0, len = s.length();
        while (i < len && idx < expected) {
            while (i < len && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            int start = i;
            while (i < len && !Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            if (start < i) {
                out[idx++] = s.substring(start, i);
            }
        }
        if (idx != expected) {
            return s.trim().split("\\s+"); // fallback
        }
        return out;

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

        ObjObject(String name) {
            this.name = name;
        }
    }

}
