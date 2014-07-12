using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.backends.lwjgl;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.math;
using lwjglgl = org.lwjgl.opengl;
using Nibb = java.nio.ByteBuffer;
namespace CU
{
    class GameGDX : ApplicationListener
    {
        public static OrthographicCamera camera;
        SpriteBatch batch;
        TextureAtlas atlas;
        int width, height;
        Logic brain;
        Texture palette;
        Texture[] pieces;
        TextureRegion[,] terrains;
        TextureAtlas.AtlasRegion[][][][] units;
        Animation[][][] animations;
        public static float stateTime, attackTime, explodeTime, receiveTime;
        TextureAtlas.AtlasRegion currentFrame;
        public static float updateStep = 0.33F;
        public ShaderProgram shader;
        private static Random r = new Random();
        public void create()
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
            Timer.instance().scheduleTask(new NilTask(brain.ProcessStep), 0F, updateStep);
        }

        public void render()
        {
            Timer.instance().start();
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);

            camera.update();
            stateTime += Gdx.graphics.getDeltaTime();
            attackTime += Gdx.graphics.getDeltaTime();
            explodeTime += Gdx.graphics.getDeltaTime();
            receiveTime += Gdx.graphics.getDeltaTime();
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
                            highlighter += brain.ActiveUnit.color + 1; //3 + ((brain.gradient[w + 1, h + 1]) % 6);
                            break;
                        case HighlightType.Dim:
                            boldness = 0;
                            highlighter = 10;
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

                    faction.r = highlighter / 32.0F;
                    batch.setColor(faction);
                    batch.draw(terrains[brain.FieldMap.Land[w, h], boldness], w * 64 + h * 64, w * 32 - h * 32);
                }
            }

            for (int row = 0; row < width + height; row++)
            {
                for (int col = 0; col <= ((row < width) ? row : (width + height -1) - row); col++)
                {
                    int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                    int h = (row < width) ? col : row - width + col;
                    if (brain.UnitGrid[w, h] != null)
                    {
                        faction.r = (brain.UnitGrid[w, h].color+1) / 32.0F;
                        batch.setColor(faction);
                        switch(brain.UnitGrid[w, h].visual)
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
                        faction.r = (brain.ActiveUnit.color+1) / 32.0F;
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
                                    currentFrame = (TextureAtlas.AtlasRegion)animations[brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber][2+brain.currentlyFiring].getKeyFrame(attackTime, false);
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

            //            worldX = 20 + x * 64 + y * 64;
            //            worldY = 8 + x * 32 - y * 32;

            for (int h = 0; h < height; h++)
            {
                for (int w = width - 1; w >= 0; w--)
                {
                    //if (brain.UnitGrid[w, h] != null)
                    //{
                    //    currentFrame = animations[brain.ReverseColors[brain.UnitGrid[w, h].color]][brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber].getKeyFrame(stateTime, true);
                    //    batch.draw(currentFrame, brain.UnitGrid[w, h].worldX, brain.UnitGrid[w, h].worldY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                    //}
                    //if (w == brain.ActiveUnit.x && h == brain.ActiveUnit.y)
                    //{
                    //    currentFrame = animations[brain.ReverseColors[brain.ActiveUnit.color]][brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber].getKeyFrame(fastTime, true);
                    //    batch.draw(currentFrame, brain.ActiveUnit.worldX, brain.ActiveUnit.worldY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                    //}
                }
            }
            batch.end();

            //shader.end();
        }
        public void resume()
        {
            Timer.instance().start();
        }
        public void pause()
        {
        }
        public void dispose()
        {
            brain.dispose();
            Timer.instance().kill();
            batch.dispose();
            atlas.dispose();
        }
        public void resize(int wide, int high)
        {
            camera.setToOrtho(false, wide, high);
            camera.update();
        }
        public void win()
        {
            Timer.instance().kill();
            Console.WriteLine("Y O U   W I N !!!");
        }
        public void lose()
        {
            Timer.instance().kill();
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

    class Launcher
    {
        public static void Main(string[] args)
        { //"Commanders Unite", 800, 800
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
