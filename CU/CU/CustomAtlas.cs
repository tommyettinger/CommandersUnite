/* This file is licensed under
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Thomas Ettinger, modifying code originally from LibGDX
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using ut = com.badlogic.gdx.utils;
using com.badlogic.gdx.files;
namespace CU
{
    public class CustomPage : TextureAtlas.TextureAtlasData.Page
    {
        public new FileHandle textureFile;
        public new Texture texture;
        public new float width, height;
        public new bool useMipMaps;
        public new Pixmap.Format format;
        public new Texture.TextureFilter minFilter;
        public new Texture.TextureFilter magFilter;
        public new Texture.TextureWrap uWrap;
        public new Texture.TextureWrap vWrap;

        public CustomPage()
            : base(new FileHandle(""), 0.0F, 0.0F, false, Pixmap.Format.LuminanceAlpha, Texture.TextureFilter.Nearest,
                Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
        {
            this.width = base.width;
            this.height = base.height;
            this.textureFile = base.textureFile;
            this.useMipMaps = base.useMipMaps;
            this.format = base.format;
            this.minFilter = base.minFilter;
            this.magFilter = base.magFilter;
            this.uWrap = base.uWrap;
            this.vWrap = base.vWrap;
        }
        public CustomPage(FileHandle file, float width, float height, bool mipmap, Pixmap.Format format, Texture.TextureFilter min, Texture.TextureFilter max, Texture.TextureWrap repeatX, Texture.TextureWrap repeatY)
            : base(file, width, height, mipmap, Pixmap.Format.LuminanceAlpha, Texture.TextureFilter.Nearest,
                Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
        {
            this.width = base.width;
            this.height = base.height;
            this.textureFile = base.textureFile;
            this.useMipMaps = base.useMipMaps;
            this.format = base.format;
            this.minFilter = base.minFilter;
            this.magFilter = base.magFilter;
            this.uWrap = base.uWrap;
            this.vWrap = base.vWrap;
        }
    }

    public class CustomRegion : TextureAtlas.TextureAtlasData.Region
    {
        public new CustomPage page;
        public new int index;
        public new String name;
        public new float offsetX;
        public new float offsetY;
        public new int originalWidth;
        public new int originalHeight;
        public new bool rotate;
        public new int left;
        public new int top;
        public new int width;
        public new int height;
        public new bool flip;
        public new int[] splits;
        public new int[] pads;
    }
    class CustomAtlasData : TextureAtlas.TextureAtlasData
    {
        public ut.Array pages = new ut.Array(new CustomPage().getClass());
        public ut.Array regions = new ut.Array(new CustomRegion().getClass());
        static String[] tuple = new String[4];

        public CustomAtlasData(FileHandle packFile, FileHandle imagesDir, bool flip)
            : base(packFile, imagesDir, flip)
        {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(packFile.read()), 64);
            try
            {
                CustomPage pageImage = null;
                while (true)
                {
                    String line = reader.readLine();
                    if (line == null) break;
                    if (line.Trim().Length == 0)
                        pageImage = null;
                    else if (pageImage == null)
                    {
                        FileHandle file = imagesDir.child(line);

                        float width = 0, height = 0;
                        if (readTuple(reader) == 2)
                        { // size is only optional for an atlas packed with an old TexturePacker.
                            width = int.Parse(tuple[0]);
                            height = int.Parse(tuple[1]);
                            readTuple(reader);
                        }
                        Pixmap.Format format = Pixmap.Format.valueOf(tuple[0]);

                        readTuple(reader);
                        Texture.TextureFilter min = Texture.TextureFilter.valueOf(tuple[0]);
                        Texture.TextureFilter max = Texture.TextureFilter.valueOf(tuple[1]);

                        String direction = readValue(reader);
                        Texture.TextureWrap repeatX = Texture.TextureWrap.ClampToEdge;
                        Texture.TextureWrap repeatY = Texture.TextureWrap.ClampToEdge;
                        if (direction.Equals("x"))
                            repeatX = Texture.TextureWrap.Repeat;
                        else if (direction.Equals("y"))
                            repeatY = Texture.TextureWrap.Repeat;
                        else if (direction.Equals("xy"))
                        {
                            repeatX = Texture.TextureWrap.Repeat;
                            repeatY = Texture.TextureWrap.Repeat;
                        }

                        pageImage = new CustomPage(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
                        pages.add(pageImage);
                    }
                    else
                    {
                        bool rotate = Boolean.Parse(readValue(reader));

                        readTuple(reader);
                        int left = int.Parse(tuple[0]);
                        int top = int.Parse(tuple[1]);

                        readTuple(reader);
                        int width = int.Parse(tuple[0]);
                        int height = int.Parse(tuple[1]);

                        CustomRegion region = new CustomRegion();
                        region.page = pageImage;
                        region.left = left;
                        region.top = top;
                        region.width = width;
                        region.height = height;
                        region.name = line;
                        region.rotate = rotate;

                        if (readTuple(reader) == 4)
                        { // split is optional
                            region.splits = new int[] {int.Parse(tuple[0]), int.Parse(tuple[1]),
								int.Parse(tuple[2]), int.Parse(tuple[3])};

                            if (readTuple(reader) == 4)
                            { // pad is optional, but only present with splits
                                region.pads = new int[] {int.Parse(tuple[0]), int.Parse(tuple[1]),
									int.Parse(tuple[2]), int.Parse(tuple[3])};

                                readTuple(reader);
                            }
                        }

                        region.originalWidth = int.Parse(tuple[0]);
                        region.originalHeight = int.Parse(tuple[1]);

                        readTuple(reader);
                        region.offsetX = int.Parse(tuple[0]);
                        region.offsetY = int.Parse(tuple[1]);

                        region.index = int.Parse(readValue(reader));

                        if (flip) region.flip = true;

                        regions.add(region);
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ut.GdxRuntimeException("Error reading pack file: " + packFile, ex);
            }
            finally
            {
                ut.StreamUtils.closeQuietly(reader);
            }

            new ut.Sort().sort((Object[])regions.items, indexComparator, 0, regions.size);

        }
        static int readTuple(java.io.BufferedReader reader)
        {
            String line = reader.readLine();
            int colon = line.IndexOf(':');
            if (colon == -1) throw new ut.GdxRuntimeException("Invalid line: " + line);
            int i = 0, lastMatch = colon + 1;
            for (i = 0; i < 3; i++)
            {
                int comma = line.IndexOf(',', lastMatch);
                if (comma == -1) break;
                tuple[i] = line.Substring(lastMatch, comma - lastMatch).Trim();
                lastMatch = comma + 1;
            }
            tuple[i] = line.Substring(lastMatch).Trim();
            return i + 1;
        }

        static String readValue(java.io.BufferedReader reader)
        {
            String line = reader.readLine();
            int colon = line.IndexOf(':');
            if (colon == -1) throw new ut.GdxRuntimeException("Invalid line: " + line);
            return line.Substring(colon + 1).Trim();
        }

        public override ut.Array getPages()
        {
            return pages;
        }

        public override ut.Array getRegions()
        {
            return regions;
        }


        static java.util.Comparator indexComparator = new JUComparator<TextureAtlas.TextureAtlasData.Region>((region1, region2) =>
        {
            int i1 = region1.index;
            if (i1 == -1) i1 = Int32.MaxValue;
            int i2 = region2.index;
            if (i2 == -1) i2 = Int32.MaxValue;
            return i1 - i2;
        });
    }
}
