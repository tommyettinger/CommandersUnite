/* This file is licensed under
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Thomas Ettinger
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
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.backends.lwjgl;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.math;
using Nibb = java.nio.ByteBuffer;

namespace CU
{
    enum GameState
    {
        Paused, NPC_Play, PC_Play, PC_Select
    }
    class GameGDX : Game
    {
        public static OrthographicCamera camera;
        SpriteBatch batch;
        TextureAtlas atlas;
        int width, height;
        public static Logic brain;
        Texture palette;
        Texture[] pieces;
        TextureRegion[,] terrains;
        TextureAtlas.AtlasRegion[][][][] units;
        Animation[][][] animations;
        public BitmapFont font, largeFont;
        public static float stateTime, attackTime, explodeTime, receiveTime;
        TextureAtlas.AtlasRegion currentFrame;
        public static float updateStep = 0.33F;
        public ShaderProgram shader;
        private static Random r = new Random();
        InputProc inp;
        public static Position cursor = null;
        public static GameState state = GameState.PC_Select;
        public static GameState previousState = GameState.PC_Select;
        public override void create()
        {
            pieces = new Texture[] {new Texture(Gdx.files.local("Assets/pack.png"), Pixmap.Format.RGBA8888, false),
                new Texture(Gdx.files.local("Assets/pack2.png"), Pixmap.Format.RGBA8888, false)};

            brain = new Logic(25, 25);
            width = brain.FieldMap.Width;
            height = brain.FieldMap.Height;
            brain.PlaceUnits();
            atlas = new TextureAtlas(new CustomAtlasData(Gdx.files.local("Assets/pack.atlas"), Gdx.files.local("Assets"), false));

            palette = new Texture(Gdx.files.local("Assets/PaletteDark.png"), Pixmap.Format.RGBA8888, false);
            palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            font = new BitmapFont(Gdx.files.@internal("Assets/Monology.fnt"));
            largeFont = new BitmapFont(Gdx.files.@internal("Assets/MonologyLarge.fnt"));

            terrains = new TextureRegion[11, 2];
            for (int i = 0; i < 11; i++)
            {
                terrains[i, 0] = atlas.findRegion(LocalMap.Terrains[i]); //"Terrain/" + 
                terrains[i, 1] = atlas.findRegion(LocalMap.Terrains[i] + "_bold");
                //for (int j = 0; j < 8; j++)
                //{
                //    terrains[i, 2 + j * 2] = atlas.findRegion(LocalMap.Terrains[i] + "_color" + j); //"Terrain/" + 
                //    terrains[i, 3 + j*2] = atlas.findRegion(LocalMap.Terrains[i] + "_bold_color" + j);
                //}
            }
            //for (int i = 0; i < 8; i++)
            //{
            //    terrains[10 + i, 0] = atlas.findRegion(LocalMap.Terrains[10] + "_color" + i);
            //    terrains[10 + i, 1] = atlas.findRegion(LocalMap.Terrains[10] + "_bold_color" + i);
            //for (int j = 0; j < 8; j++)
            //{
            //    terrains[10 + i, 2 + j * 2] = atlas.findRegion(LocalMap.Terrains[10] + "_color" + i); ////ALL THE SAME COLOR
            //    terrains[10 + i, 3 + j * 2] = atlas.findRegion(LocalMap.Terrains[10] + "_bold_color" + i);
            //}

            units = new TextureAtlas.AtlasRegion[Unit.CurrentUnits.Length][][][];
            animations = new Animation[Unit.CurrentUnits.Length][][];
            TextureAtlas.AtlasRegion clear = atlas.findRegion("clear");
            TextureAtlas.AtlasRegion clearLarge = atlas.findRegion("clear_large");
            foreach (string name in Unit.CurrentUnits)
            {
                units[Unit.UnitLookup[name]] = new TextureAtlas.AtlasRegion[4][][];
                animations[Unit.UnitLookup[name]] = new Animation[4][];

                for (int dir = 0; dir < 4; dir++)
                {
                    units[Unit.UnitLookup[name]][dir] = new TextureAtlas.AtlasRegion[((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 6)][];
                    for (int j = 0; j < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 6); j++)
                    {
                        switch (j)
                        {
                            case 0:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[(Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4];
                                for (int k = 0; k < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4); k++)
                                {
                                    units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_face" + dir, k);
                                }
                                break;
                            case 1:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[9];
                                for (int k = 0; k < 8; k++)
                                {
                                    units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_Explode_face" + dir, k);
                                }
                                units[Unit.UnitLookup[name]][dir][j][8] = clearLarge;
                                break;
                            case 2:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[(Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16];
                                for (int k = 0; k < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16); k++)
                                {
                                    if (Unit.WeaponDisplays[Unit.UnitLookup[name]][0] > -1)
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_Attack_0_face" + dir, k);
                                    }
                                    else
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_face" + dir, k % 4);
                                    }
                                }
                                break;
                            case 3:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[(Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16];
                                for (int k = 0; k < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16); k++)
                                {
                                    if (Unit.WeaponDisplays[Unit.UnitLookup[name]][1] > -1)
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_Attack_1_face" + dir, k);
                                    }
                                    else
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_face" + dir, k % 4);
                                    }
                                }
                                break;
                            case 4:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[(Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16];
                                for (int k = 0; k < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16); k++)
                                {
                                    if (Unit.WeaponDisplays[Unit.UnitLookup[name]][0] > -1)
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_Receive_0_face" + dir, k);
                                    }
                                    else
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = clearLarge;
                                    }
                                }
                                break;
                            case 5:
                                units[Unit.UnitLookup[name]][dir][j] = new TextureAtlas.AtlasRegion[(Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16];
                                for (int k = 0; k < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 16); k++)
                                {
                                    if (Unit.WeaponDisplays[Unit.UnitLookup[name]][1] > -1)
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = atlas.findRegion(name + "_Receive_1_face" + dir, k);
                                    }
                                    else
                                    {
                                        units[Unit.UnitLookup[name]][dir][j][k] = clearLarge;
                                    }
                                }
                                break;
                        }
                    }
                    animations[Unit.UnitLookup[name]][dir] = new Animation[((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 6)];
                    for (int j = 0; j < ((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 6); j++)
                    {
                        switch (j)
                        {
                            case 0: animations[Unit.UnitLookup[name]][dir][j] = new Animation(((Unit.AllMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 0.4F : 0.15F),
                            units[Unit.UnitLookup[name]][dir][j]);
                                break;
                            case 1: animations[Unit.UnitLookup[name]][dir][j] = new Animation(0.11F,
                                    units[Unit.UnitLookup[name]][dir][j]);
                                //                                units[Unit.UnitLookup[name]][i][j].Concat(new TextureRegion[] {units[Unit.UnitLookup[name]][i][j][7],  }).ToArray());
                                break;
                            case 2: if (Unit.WeaponDisplays[Unit.UnitLookup[name]][0] > -1)
                                {
                                    animations[Unit.UnitLookup[name]][dir][j] = new Animation(0.11F,
                                        units[Unit.UnitLookup[name]][dir][j]);
                                }
                                break;
                            case 3: if (Unit.WeaponDisplays[Unit.UnitLookup[name]][1] > -1)
                                {
                                    animations[Unit.UnitLookup[name]][dir][j] = new Animation(0.11F,
                                        units[Unit.UnitLookup[name]][dir][j]);
                                }
                                break;
                            case 4: if (Unit.WeaponDisplays[Unit.UnitLookup[name]][0] > -1)
                                {
                                    animations[Unit.UnitLookup[name]][dir][j] = new Animation(0.11F,
                                        units[Unit.UnitLookup[name]][dir][j]);
                                }
                                break;
                            case 5: if (Unit.WeaponDisplays[Unit.UnitLookup[name]][1] > -1)
                                {
                                    animations[Unit.UnitLookup[name]][dir][j] = new Animation(0.11F,
                                        units[Unit.UnitLookup[name]][dir][j]);
                                }
                                break;
                        }
                    }
                }

            }

            camera = new OrthographicCamera();
            camera.setToOrtho(false, 1280, 720);

            batch = new SpriteBatch();

            shader = createChannelShader();
            stateTime = 0;
            attackTime = 0;
            explodeTime = 0;
            receiveTime = 0;
            cursor = new Position(brain.ActiveUnit.x, brain.ActiveUnit.y);
            inp = new InputProc();
            Gdx.input.setInputProcessor(inp);
            Timer.instance().scheduleTask(new NilTask(brain.ProcessStep), 0F, updateStep);
        }

        public override void render()
        {
            Timer.instance().start();
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);

            camera.update();
            if (state == GameState.Paused)
            { }
            else if (state == GameState.NPC_Play || state == GameState.PC_Play)
            {
                stateTime += Gdx.graphics.getDeltaTime();
                attackTime += Gdx.graphics.getDeltaTime();
                explodeTime += Gdx.graphics.getDeltaTime();
                receiveTime += Gdx.graphics.getDeltaTime();
            }
            else if (state == GameState.PC_Select)
            {
                stateTime += Gdx.graphics.getDeltaTime();
            }
            //            fastTime += Gdx.graphics.getDeltaTime() * 1.5F;

            batch.setProjectionMatrix(camera.combined);

            //shader.begin();

            Color faction = new Color();
            faction.a = 1;
            faction.b = 0.5F;
            faction.g = 0.9F;
            faction.r = 9 / 32;

            batch.setShader(shader);
            Gdx.gl.glActiveTexture(GL20.__Fields.GL_TEXTURE0);
            batch.begin();

            palette.bind(3);

            shader.setUniformi("u_texPalette", 3);
            pieces[0].bind(2);
            shader.setUniformi("u_texture", 2);

            //            shader.setUniformf("u_paletteIndex", 0.5F);

            faction.r = 10 / 32.0F;
            batch.setColor(faction);
            //            for (int h = 0; h < height; h++)
            for (int row = 0; row < width + height; row++)
            {
                for (int col = 0; col <= ((row < width) ? row : (width + height - 1) - row); col++)
                {

                    int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                    int h = (row < width) ? col : row - width + col;
                    int boldness = 0;
                    float highlighter = 10;
                    switch (brain.FieldMap.Highlight[w, h])
                    {
                        case HighlightType.Bright:
                            boldness = 1;
                            highlighter += 0;//(((int)((stateTime) % 2)) == 0) ? brain.ActiveUnit.color + 1 : 0; //3 + ((brain.gradient[w + 1, h + 1]) % 6);
                            break;
                        case HighlightType.Dim:
                            boldness = 0;
                            highlighter = 19;//(((int)((stateTime) % 2)) == 0) ? 19 : 10;
                            break;
                        case HighlightType.Plain:
                            boldness = 0;
                            highlighter = 10;
                            break;
                        case HighlightType.Spectrum:
                            boldness = 1;
                            highlighter += 3 + ((int)((stateTime * 9) % 6));
                            break;
                        default:
                            break;
                    }
                    if (state == GameState.PC_Select && brain.FieldMap.Highlight[w, h] != HighlightType.Dim && cursor.x == w && cursor.y == h)
                    {
                        boldness = 1;
                        highlighter = 13 + ((int)((stateTime * 9) % 6));
                    }
                    faction.r = highlighter / 32.0F;
                    batch.setColor(faction);
                    batch.draw(terrains[brain.FieldMap.Land[w, h], boldness], w * 64 + h * 64, w * 32 - h * 32);
                }
            }

            for (int row = 0; row < width + height; row++)
            {
                for (int col = 0; col <= ((row < width) ? row : (width + height - 1) - row); col++)
                {
                    int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                    int h = (row < width) ? col : row - width + col;
                    if (brain.UnitGrid[w, h] != null)
                    {
                        faction.r = (brain.UnitGrid[w, h].color + 1) / 32.0F;
                        batch.setColor(faction);
                        switch (brain.UnitGrid[w, h].visual)
                        {
                            case VisualAction.Normal:
                                currentFrame = (TextureAtlas.AtlasRegion)animations[brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber][0].getKeyFrame(stateTime, true);
                                batch.draw(currentFrame, (int)(brain.UnitGrid[w, h].worldX) + currentFrame.offsetX, (int)(brain.UnitGrid[w, h].worldY) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                break;
                            case VisualAction.Exploding:
                                currentFrame = (TextureAtlas.AtlasRegion)animations[brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber][1].getKeyFrame(explodeTime, false);
                                batch.draw(currentFrame, (int)(brain.UnitGrid[w, h].worldX - 80) + currentFrame.offsetX, (int)(brain.UnitGrid[w, h].worldY - 40) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                break;
                            case VisualAction.Firing:
                                if (Unit.WeaponDisplays[brain.UnitGrid[w, h].unitIndex][0] > -1)
                                {
                                    currentFrame = (TextureAtlas.AtlasRegion)animations[brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber][2].getKeyFrame(attackTime, false);
                                    batch.draw(currentFrame, (int)(brain.UnitGrid[w, h].worldX - 80) + currentFrame.offsetX, (int)(brain.UnitGrid[w, h].worldY - 40) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                }
                                else
                                {
                                    currentFrame = (TextureAtlas.AtlasRegion)animations[brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber][0].getKeyFrame(attackTime, true);
                                    batch.draw(currentFrame, (int)(brain.UnitGrid[w, h].worldX) + currentFrame.offsetX, (int)(brain.UnitGrid[w, h].worldY) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                }
                                break;
                        }

                        foreach (DirectedPosition dp in brain.ActiveUnit.targeting)
                        {
                            if (brain.currentlyFiring > -1 && dp.x == w && dp.y == h)
                            {

                                int tx = 20 - 80 + dp.x * 64 + dp.y * 64;
                                int ty = 6 - 40 + dp.x * 32 - dp.y * 32;
                                currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][Logic.ConvertDirection(dp.dir)][4 + brain.currentlyFiring].getKeyFrame(receiveTime, false);
                                batch.draw(currentFrame, tx + currentFrame.offsetX, ty + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[dp.x, dp.y]] * 3);
                            }
                        }
                    }
                    if (brain.ActiveUnit.x == w && brain.ActiveUnit.y == h)
                    {
                        faction.r = (brain.ActiveUnit.color + 1) / 32.0F;
                        batch.setColor(faction);
                        //[brain.ReverseColors[brain.ActiveUnit.color]]

                        switch (brain.ActiveUnit.visual)
                        {
                            case VisualAction.Normal:
                                currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber][0].getKeyFrame(stateTime, true);
                                batch.draw(currentFrame, (int)(brain.ActiveUnit.worldX) + currentFrame.offsetX, (int)(brain.ActiveUnit.worldY) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                break;
                            case VisualAction.Exploding:
                                currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber][1].getKeyFrame(explodeTime, false);
                                batch.draw(currentFrame, (int)(brain.ActiveUnit.worldX - 80) + currentFrame.offsetX, (int)(brain.ActiveUnit.worldY - 40) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                break;
                            case VisualAction.Firing:
                                if (brain.currentlyFiring > -1)
                                {
                                    currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber][2 + brain.currentlyFiring].getKeyFrame(attackTime, false);
                                    batch.draw(currentFrame, (int)(brain.ActiveUnit.worldX - 80) + currentFrame.offsetX, (int)(brain.ActiveUnit.worldY - 40) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                }
                                else
                                {
                                    currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber][0].getKeyFrame(attackTime, true);
                                    batch.draw(currentFrame, (int)(brain.ActiveUnit.worldX) + currentFrame.offsetX, (int)(brain.ActiveUnit.worldY) + currentFrame.offsetY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                                }
                                break;
                        }
                    }
                }
            }
            if (state == GameState.Paused)
            {
                batch.setColor(Color.BLACK);
                largeFont.setColor(Color.BLACK); //(sp.large ? largeFont : font).
                font.setColor(Color.BLACK); //(sp.large ? largeFont : font).
                largeFont.draw(batch, "PAUSED", camera.position.x + 12, camera.position.y + 50);
                font.draw(batch, "Press Space to continue", camera.position.x - 23 * 2, camera.position.y + 0);
            }
            foreach (Speech sp in brain.speaking)
            {
                batch.setColor(Color.BLACK);
                largeFont.setColor(Color.BLACK); //(sp.large ? largeFont : font).
                (sp.large ? largeFont : font).draw(batch, sp.text, sp.worldX - (sp.text.Length * (sp.large ? 8 : 4)), sp.worldY);
            }
            //            worldX = 20 + x * 64 + y * 64;
            //            worldY = 8 + x * 32 - y * 32;

            batch.end();

            //shader.end();
        }
        public override void resume()
        {
            //            Timer.instance().start();
            resumeGame();
        }
        public static void resumeGame()
        {
            if (state == GameState.Paused)
            {
                Timer.instance().resume();
                GameGDX.state = previousState;
            }
        }
        public override void pause()
        {
            pauseGame();
        }
        public static void pauseGame()
        {

            if (state != GameState.Paused)
            {
                Timer.instance().pause();
                previousState = GameGDX.state;
                GameGDX.state = GameState.Paused;
            }
        }
        public override void dispose()
        {
            try
            {
                brain.dispose();
                Timer.instance().stop();
                Timer.instance().dispose();

            }
            catch (Exception e)
            {
#if DEBUG
                Console.WriteLine(e.StackTrace);
#endif
            }
            batch.dispose();
            atlas.dispose();
        }
        public override void resize(int wide, int high)
        {
            camera.setToOrtho(false, wide, high);
            camera.update();
        }
        public void win()
        {
            Timer.instance().stop();
            Console.WriteLine("Y O U   W I N !!!");
        }
        public void lose()
        {
            Timer.instance().stop();
            Console.WriteLine("YOU LOSE...");
        }

        static public ShaderProgram createChannelShader()
        {
            String vertex = "attribute vec4 a_position;    \n" +
                      "attribute vec4 a_color;\n" +
                      "attribute vec2 a_texCoord0;\n" +
                      "uniform mat4 u_projTrans;\n" +
                      "varying vec4 v_color;" +
                      "varying vec2 v_texCoords;" +
                      "void main()                  \n" +
                      "{                            \n" +
                      "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                      "   v_color.a = v_color.a * (256.0/255.0);\n" +
                      "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0; \n" +
                      "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                      "}                            \n";
            string fragment = @"
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texPalette;

void main()
{
        vec4 color = texture2D(u_texture, v_texCoords);
        vec2 index = vec2(color.r, v_color.r);
        gl_FragColor = vec4(texture2D(u_texPalette, index).rgb, color.a);
}";

            ShaderProgram shader = new ShaderProgram(vertex, fragment);
            if (shader.isCompiled() == false) throw new ArgumentException("Error compiling shader: " + shader.getLog());
            return shader;
        }
    }

    class InputProc : InputProcessor
    {
        public InputProc()
        {
        }
        public bool keyDown(int keycode)
        {
            if (keycode == Input.Keys.SPACE)
            {

                if (GameGDX.state != GameState.Paused)
                {
                    GameGDX.pauseGame();
                }
                else
                {
                    GameGDX.resumeGame();
                }
            }
            return false;
        }

        public bool keyUp(int keycode)
        {
            return false;
        }

        public bool keyTyped(char character)
        {
            return false;
        }

        public bool touchDown(int x, int y, int pointer, int button)
        {
            return false;
        }

        public bool touchUp(int x, int y, int pointer, int button)
        {
            return false;
        }

        public bool touchDragged(int x, int y, int pointer)
        {
            return false;
        }

        public bool mouseMoved(int x, int y)
        {
            if (GameGDX.state == GameState.PC_Select)
            {
                Vector3 v3 = GameGDX.camera.unproject(new Vector3(x, y, 0));

                float worldX = v3.x;
                float worldY = v3.y;
                
                //Console.WriteLine("screenX: " + screenX + ", screenY: " + screenY);
                worldY -= 32;
                //screenX /= 64;
                //screenY /= 32;

                int gridX = (int)((worldX / 128 + worldY / 64));
                int gridY = (int)((worldX / 128 - worldY / 64));

                GameGDX.cursor.x = gridX;
                GameGDX.cursor.y = gridY;
                /*if(GameGDX.brain.FieldMap.ValidatePosition(GameGDX.cursor))
                {
                    screenY = (int)v3.y;
                    screenY -= 32 + (LocalMap.Depths[GameGDX.brain.FieldMap.Land[worldX, worldY]] *3);
                    //Console.WriteLine("X: " + worldX + ", Y: " + worldY + ", Depth" + LocalMap.Depths[GameGDX.brain.FieldMap.Land[worldX, worldY]]);
                    screenY /= 32;

                    worldX = ((screenX + screenY) / 2);
                    worldY = ((screenX - screenY) / 2);
                    GameGDX.cursor.x = worldX;
                    GameGDX.cursor.y = worldY;
                }*/
            }
            return false;
        }

        public bool scrolled(int amount)
        {
            return false;
        }

    }

    class Launcher
    {
        public static void Main(string[] args)
        { //"Commanders Unite", 800, 800
            if (Environment.OSVersion.Platform == PlatformID.Unix)
            {
                if (IntPtr.Size == 8 && File.Exists("libikvm-native.so") == false)
                {
                    File.Copy("libikvm-native64.so", "libikvm-native.so");
                }
                else if (File.Exists("libikvm-native.so") == false)
                {
                    File.Copy("libikvm-native32.so", "libikvm-native.so");
                }
            }
            com.badlogic.gdx.utils.GdxNativesLoader.disableNativesLoading = true;
            com.badlogic.gdx.utils.SharedLibraryLoader loader = new com.badlogic.gdx.utils.SharedLibraryLoader();
            // java.io.File nativesDir = null;
            /*try
            {
                nativesDir = new java.io.File("./");
                //if (com.badlogic.gdx.utils.SharedLibraryLoader.isWindows)
                //{
                //    nativesDir = loader.extractFile(com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? "lwjgl64.dll" : "lwjgl.dll", null).getParentFile();
                //    if (!LwjglApplicationConfiguration.disableAudio)
                //        loader.extractFile(com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? "OpenAL64.dll" : "OpenAL32.dll", nativesDir.getName());
                //}
                //else if (com.badlogic.gdx.utils.SharedLibraryLoader.isMac)
                //{
                //    java.io.File extractedFile = loader.extractFile("liblwjgl.jnilib", null);
                //    nativesDir = extractedFile.getParentFile();
                //    new com.badlogic.gdx.files.FileHandle(extractedFile).copyTo(new com.badlogic.gdx.files.FileHandle(new java.io.File(nativesDir, "liblwjgl.dylib")));
                //    if (!LwjglApplicationConfiguration.disableAudio) loader.extractFile("openal.dylib", nativesDir.getName());
                //}
                //else if (com.badlogic.gdx.utils.SharedLibraryLoader.isLinux)
                //{
                //    nativesDir = loader.extractFile(com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? "liblwjgl64.so" : "liblwjgl.so", null).getParentFile();
                //    if (!LwjglApplicationConfiguration.disableAudio)
                //        loader.extractFile(com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? "libopenal64.so" : "libopenal.so", nativesDir.getName());
                //}
            }
            catch (Exception ex)
            {
                throw new com.badlogic.gdx.utils.GdxRuntimeException("Unable to find LWJGL natives.", ex);
            }
            java.lang.System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
            Console.WriteLine(java.lang.System.getProperty("org.lwjgl.librarypath"));
            Console.WriteLine(java.lang.System.getProperty("java.library.path"));*/
            //Console.ReadKey();
            /*if (com.badlogic.gdx.utils.SharedLibraryLoader.isLinux)
            {
                java.lang.System.setProperty("java.library.path", java.lang.System.getProperty("java.library.path") + (com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? ":linux-x64" : ":linux-x86"));
//                java.lang.System.load(com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit ? "linux-x64/libjawt.so" : "linux-x86/libjawt.so");
            }*/
            loader.load("gdx");

            LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
            cfg.title = "Commanders Unite";
            cfg.width = 1280;
            cfg.height = 720;
            cfg.backgroundFPS = 45;
            cfg.foregroundFPS = 45;
            cfg.fullscreen = false;




            //            cfg.addIcon("Assets/CU32.png", Files.FileType.Local);
            //cfg.addIcon("Assets/CU16.png", Files.FileType.Local);
            //cfg.addIcon("Assets/CU128.png", Files.FileType.Local);
            LwjglApplication app = new LwjglApplication(new GameGDX(), cfg);

            /*
            string[] iconPaths = { "Assets/CU32.png", "Assets/CU16.png", "Assets/CU128.png" };
            int[] sizes = { 32, 16, 128 };
            Nibb[] icons = new Nibb[3];
            for (int i = 0, n = 3; i < n; i++)
            {
                Pixmap rgba = new Pixmap(sizes[i], sizes[i], Pixmap.Format.RGBA8888);
                rgba.drawPixmap(new Pixmap(Gdx.files.getFileHandle(iconPaths[i], Files.FileType.Internal)), 0, 0);

                icons[i] = rgba.getPixels();
                rgba.dispose();
            }
            lwjglgl.Display.setIcon(icons);
            */
        }
    }
}
