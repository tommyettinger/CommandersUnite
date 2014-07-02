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
namespace CU
{
    class GameGDX : ApplicationListener
    {
        public static OrthographicCamera camera;
        SpriteBatch batch;
        TextureAtlas atlas;
        int width, height;
        Logic brain;
        TextureRegion[,] terrains;
        TextureRegion[][][][] units;
        Animation[][][] animations;
        float stateTime, fastTime;
        TextureRegion currentFrame;
        public static float updateStep = 1/3F;
        private static Random r = new Random();
        public void create()
        {
            brain = new Logic(25,25);
            width = brain.FieldMap.Width;
            height = brain.FieldMap.Height;
            brain.PlaceUnits();
            atlas = new TextureAtlas(Gdx.files.@internal("Assets/pack.atlas"));
            terrains = new TextureRegion[18,18];
            for (int i = 0; i < 10; i++ )
            {
                terrains[i, 0] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i]);
                terrains[i, 1] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_bold");
                for (int j = 0; j < 8; j++)
                {
                    terrains[i, 2 + j*2] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_color" + j);
                    terrains[i, 3 + j*2] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_bold_color" + j);
                }
            }
            for (int i = 0; i < 8; i++)
            {
                terrains[10 + i, 0] = atlas.findRegion("Terrain/" + LocalMap.Terrains[10] + "_color" + i);
                terrains[10 + i, 1] = atlas.findRegion("Terrain/" + LocalMap.Terrains[10] + "_bold_color" + i);
            for (int j = 0; j < 8; j++)
            {
                terrains[10 + i, 2 + j * 2] = atlas.findRegion("Terrain/" + LocalMap.Terrains[10] + "_color" + i); ////ALL THE SAME COLOR
                terrains[10 + i, 3 + j * 2] = atlas.findRegion("Terrain/" + LocalMap.Terrains[10] + "_bold_color" + i);
            }
            }
            units = new TextureRegion[4][][][];
            animations = new Animation[4][][];
            for (int i = 0; i < 4; i++ )
            {
                units[i] = new TextureRegion[Unit.CurrentUnits.Length][][];
                animations[i] = new Animation[Unit.CurrentUnits.Length][];

                foreach (string name in Unit.CurrentUnits)
                {
                    units[i][Unit.UnitLookup[name]] = new TextureRegion[4][];
                    animations[i][Unit.UnitLookup[name]] = new Animation[4];

                    for (int j = 0; j < 4; j++)
                    {
                        units[i][Unit.UnitLookup[name]][j] = new TextureRegion[((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4)];
                        for (int k = 0; k < ((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 2 : 4); k++)
                        {
                            units[i][Unit.UnitLookup[name]][j][k] = atlas.findRegion("color" + brain.Colors[i] + "/" + name + "_face" + j, k);
                        }
                        animations[i][Unit.UnitLookup[name]][j] = new Animation(((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 0.4F : 0.15F),
                            units[i][Unit.UnitLookup[name]][j]);
                    }
                }
            }
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 1280, 720);
            
            batch = new SpriteBatch();
            stateTime = 0;
            fastTime = 0;
            Timer.instance().scheduleTask(new NilTask(brain.ProcessStep), 0F, updateStep);
        }

        public void render()
        {
            Timer.instance().start();
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);

            camera.update();
            stateTime += Gdx.graphics.getDeltaTime();
            fastTime += Gdx.graphics.getDeltaTime()*1.5F;

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            //            for (int h = 0; h < height; h++)

            for (int row = 0; row < width+height; row++)
            {
                for (int col = 0; col <= ((row < width) ? row : (width+height-1) - row); col++ )
                {

                    int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                    int h = (row < width) ?  col : row - width + col;
                    int highlighter = 0;
                    switch (brain.FieldMap.Highlight[w, h])
                    {
                        case HighlightType.Bright:
                            highlighter = 3 + (2 * brain.ActiveUnit.color);
                            break;
                        case HighlightType.Dim:
                            highlighter = 2;
                            break;
                        case HighlightType.Plain:
                            highlighter = 0;
                            break;
                        case HighlightType.Spectrum:
                            highlighter = 7 + (2 * ((int)((stateTime * 9) % 6)));
                            break;
                        default:
                            break;
                    }
                    batch.draw(terrains[brain.FieldMap.Land[w, h], highlighter], w * 64 + h * 64, w * 32 - h * 32);
                }
            }

            for (int row = 0; row < width + height - 1; row++)
            {
                for (int col = 0; col <= ((row < width) ? row : (width + height - 1) - row); col++)
                {

                    int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                    int h = (row < width) ? col : row - width + col;
                    if (brain.UnitGrid[w, h] != null)
                    {
                        currentFrame = animations[brain.ReverseColors[brain.UnitGrid[w, h].color]][brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber].getKeyFrame(stateTime, true);
                        batch.draw(currentFrame, brain.UnitGrid[w, h].worldX, brain.UnitGrid[w, h].worldY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                    }
                    if (brain.ActiveUnit.x == w && brain.ActiveUnit.y == h)
                    {
                        currentFrame = animations[brain.ReverseColors[brain.ActiveUnit.color]][brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber].getKeyFrame(fastTime, true);
                        batch.draw(currentFrame, brain.ActiveUnit.worldX, brain.ActiveUnit.worldY + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
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
            LwjglApplication app = new LwjglApplication(new GameGDX(), cfg);
        }
    }
}
