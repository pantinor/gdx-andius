/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmap;

import java.util.Random;

/**
 *
 * @author Paul
 */
public class WorldMapGen {

    private static Random RAND = new Random(System.currentTimeMillis());

    private static final double PI = 3.141593;

    private static final int[] Red = {0, 0, 0, 0, 0, 0, 0, 0, 34, 68, 102, 119, 136, 153, 170, 187,
        0, 34, 34, 119, 187, 255, 238, 221, 204, 187, 170, 153,
        136, 119, 85, 68,
        255, 250, 245, 240, 235, 230, 225, 220, 215, 210, 205, 200,
        195, 190, 185, 180, 175};

    private static final int[] Green = {0, 0, 17, 51, 85, 119, 153, 204, 221, 238, 255, 255, 255,
        255, 255, 255, 68, 102, 136, 170, 221, 187, 170, 136,
        136, 102, 85, 85, 68, 51, 51, 34,
        255, 250, 245, 240, 235, 230, 225, 220, 215, 210, 205, 200,
        195, 190, 185, 180, 175};

    private static final int[] Blue = {0, 68, 102, 136, 170, 187, 221, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 0, 0, 0, 0, 0, 34, 34, 34, 34, 34, 34,
        34, 34, 34, 17, 0,
        255, 250, 245, 240, 235, 230, 225, 220, 215, 210, 205, 200,
        195, 190, 185, 180, 175};

    private static int[] WorldMapArray;
    private static final int XRange = 320;
    private static final int YRange = 160;
    private static final int[] Histogram = new int[256];
    private static int FilledPixels;
    private static double YRangeDiv2, YRangeDivPI;
    private static double[] SinIterPhi;

    public static void main(String[] args) throws Exception {
        {
            int a, j, i, Color, MaxZ = 1, MinZ = -1;
            int row = 0;
            boolean TwoColorMode = false;
            int index2;
            int Threshold, Count;
            int PercentWater = 0, PercentIce = 0, Cur = 0;

            WorldMapArray = new int[XRange * YRange];
            SinIterPhi = new double[2 * XRange];

            for (i = 0; i < XRange; i++) {
                SinIterPhi[i] = SinIterPhi[i + XRange] = (float) Math.sin(i * 2 * PI / XRange);
            }

            for (j = 0, row = 0; j < XRange; j++) {
                WorldMapArray[row] = 0;
                for (i = 1; i < YRange; i++) {
                    WorldMapArray[i + row] = Integer.MIN_VALUE;
                }
                row += YRange;
            }

            YRangeDiv2 = YRange / 2;
            YRangeDivPI = YRange / PI;

            generateWorldMap();

            index2 = (XRange / 2) * YRange;
            for (j = 0, row = 0; j < XRange / 2; j++) {
                for (i = 1; i < YRange; i++) {
                    WorldMapArray[row + index2 + YRange - i] = WorldMapArray[row + i];
                }
                row += YRange;
            }

            for (j = 0, row = 0; j < XRange; j++) {
                Color = WorldMapArray[row];
                for (i = 1; i < YRange; i++) {
                    Cur = WorldMapArray[row + i];
                    if (Cur != Integer.MIN_VALUE) {
                        Color += Cur;
                    }
                    WorldMapArray[row + i] = Color;
                }
                row += YRange;
            }

            for (j = 0; j < XRange * YRange; j++) {
                Color = WorldMapArray[j];
                if (Color > MaxZ) {
                    MaxZ = Color;
                }
                if (Color < MinZ) {
                    MinZ = Color;
                }
            }

            for (j = 0, row = 0; j < XRange; j++) {
                for (i = 0; i < YRange; i++) {
                    Color = WorldMapArray[row + i];
                    Color = (int) (((float) (Color - MinZ + 1) / (float) (MaxZ - MinZ + 1)) * 30) + 1;
                    Histogram[Color]++;
                }
                row += YRange;
            }

            Threshold = PercentWater * XRange * YRange / 100;

            for (j = 0, Count = 0; j < 256; j++) {
                Count += Histogram[j];
                if (Count > Threshold) {
                    break;
                }
            }

            /* Threshold now holds where sea-level is */
            Threshold = j * (MaxZ - MinZ + 1) / 30 + MinZ;

            if (TwoColorMode) {
                for (j = 0, row = 0; j < XRange; j++) {
                    for (i = 0; i < YRange; i++) {
                        Color = WorldMapArray[row + i];
                        if (Color < Threshold) {
                            WorldMapArray[row + i] = 3;
                        } else {
                            WorldMapArray[row + i] = 20;
                        }
                    }
                    row += YRange;
                }
            } else {

                for (j = 0, row = 0; j < XRange; j++) {
                    for (i = 0; i < YRange; i++) {
                        Color = WorldMapArray[row + i];

                        if (Color < Threshold) {
                            Color = (int) (((float) (Color - MinZ) / (float) (Threshold - MinZ)) * 15) + 1;
                        } else {
                            Color = (int) (((float) (Color - Threshold) / (float) (MaxZ - Threshold)) * 15) + 16;
                        }

                        if (Color < 1) {
                            Color = 1;
                        }
                        if (Color > 255) {
                            Color = 31;
                        }

                        WorldMapArray[row + i] = Color;
                    }
                    row += YRange;
                }

                Threshold = PercentIce * XRange * YRange / 100;

                if ((Threshold <= 0) || (Threshold > XRange * YRange)) {
                    return;
                }

                FilledPixels = 0;
                for (i = 0; i < YRange; i++) {
                    for (j = 0, row = 0; j < XRange; j++) {
                        Color = WorldMapArray[row + i];
                        if (Color < 32) {
                            floodFill4(j, i, Color);
                        }
                        if (FilledPixels > Threshold) {
                            return;
                        }
                        row += YRange;
                    }
                }

                FilledPixels = 0;

                for (i = (YRange - 1); i > 0; i--) {
                    for (j = 0, row = 0; j < XRange; j++) {
                        Color = WorldMapArray[row + i];
                        if (Color < 32) {
                            floodFill4(j, i, Color);
                        }
                        if (FilledPixels > Threshold) {
                            return;
                        }
                        row += YRange;
                    }
                }
            }

            //GIFEncode(Save, XRange, YRange, 1, 0, 8, Red, Green, Blue);
        }

    }

    private static void generateWorldMap() {

        double Alpha, Beta;
        double TanB;
        int row = 0;
        int Theta, Phi, Xsi;

        Alpha = (((float) RAND.nextInt(Integer.MAX_VALUE)) / Integer.MAX_VALUE - 0.5) * PI;
        Beta = (((float) RAND.nextInt(Integer.MAX_VALUE)) / Integer.MAX_VALUE - 0.5) * PI;
        TanB = Math.tan(Math.acos(Math.cos(Alpha) * Math.cos(Beta)));
        Xsi = (int) (XRange / 2 - (XRange / PI) * Beta);

        for (Phi = 0; Phi < XRange / 2; Phi++) {
            double tmp = (SinIterPhi[Phi] + Xsi - Phi + XRange) * TanB;
            Theta = (int) (YRangeDivPI * Math.atan(tmp) + YRangeDiv2);
            if ((RAND.nextInt(Integer.MAX_VALUE) & 1) != 0) {
                if (WorldMapArray[row + Theta] != Integer.MIN_VALUE) {
                    WorldMapArray[row + Theta]--;
                } else {
                    WorldMapArray[row + Theta] = -1;
                }
            } else if (WorldMapArray[row + Theta] != Integer.MIN_VALUE) {
                WorldMapArray[row + Theta]++;
            } else {
                WorldMapArray[row + Theta] = 1;
            }
            row += YRange;
        }
    }

    private static void floodFill4(int x, int y, int OldColor) {

        if (WorldMapArray[x * YRange + y] == OldColor) {

            if (WorldMapArray[x * YRange + y] < 16) {
                WorldMapArray[x * YRange + y] = 32;
            } else {
                WorldMapArray[x * YRange + y] += 17;
            }

            FilledPixels++;
            if (y - 1 > 0) {
                floodFill4(x, y - 1, OldColor);
            }
            if (y + 1 < YRange) {
                floodFill4(x, y + 1, OldColor);
            }
            if (x - 1 < 0) {
                floodFill4(XRange - 1, y, OldColor);
            } else {
                floodFill4(x - 1, y, OldColor);
            }

            if (x + 1 >= XRange) {
                floodFill4(0, y, OldColor);
            } else {
                floodFill4(x + 1, y, OldColor);
            }
        }
    }

    private static int nextPixel() {
        int r;
        if (CountDown == 0) {
            return -1;
        }
        --CountDown;
        r = WorldMapArray[curx * YRange + cury];
        bumpPixel();
        return r;
    }

    private static int Width, Height;
    private static int curx, cury;
    private static long CountDown;
    private static int Pass = 0;
    private static boolean Interlace = false;

    private static void bumpPixel() {
        /*
         * Bump the current X position
         */
        ++curx;

        /*
         * If we are at the end of a scan line, set curx back to the beginning
         * If we are interlaced, bump the cury to the appropriate spot,
         * otherwise, just increment it.
         */
        if (curx == Width) {
            curx = 0;

            if (!Interlace) {
                ++cury;
            } else {
                switch (Pass) {

                    case 0:
                        cury += 8;
                        if (cury >= Height) {
                            ++Pass;
                            cury = 4;
                        }
                        break;

                    case 1:
                        cury += 8;
                        if (cury >= Height) {
                            ++Pass;
                            cury = 2;
                        }
                        break;

                    case 2:
                        cury += 4;
                        if (cury >= Height) {
                            ++Pass;
                            cury = 1;
                        }
                        break;

                    case 3:
                        cury += 2;
                        break;
                }
            }
        }
    }

}
