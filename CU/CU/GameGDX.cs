using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.backends.lwjgl;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.utils;
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
        public static Timer timer;
        private static Random r = new Random();
        public void create()
        {
            brain = new Logic(15,15);
            width = brain.FieldMap.Width;
            height = brain.FieldMap.Height;
            brain.PlaceUnits();
            atlas = new TextureAtlas(Gdx.files.@internal("Assets/pack.atlas"));
            terrains = new TextureRegion[10,7];
            for (int i = 0; i < 10; i++ )
            {
                terrains[i, 0] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i]);
                terrains[i, 1] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_bright");
                terrains[i, 2] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_dim");
                terrains[i, 3] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_spectrum0");
                terrains[i, 4] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_spectrum1");
                terrains[i, 5] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_spectrum2");
                terrains[i, 6] = atlas.findRegion("Terrain/" + LocalMap.Terrains[i] + "_spectrum3");
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
                        animations[i][Unit.UnitLookup[name]][j] = new Animation(((Unit.CurrentMobilities[Unit.UnitLookup[name]] == MovementType.Immobile) ? 0.4F : 0.2F),
                            units[i][Unit.UnitLookup[name]][j]);
                    }
                }
            }
            camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 800);
            
            batch = new SpriteBatch();
            stateTime = 0;
            fastTime = 0;
            timer = new Timer();
            timer.scheduleTask(new NilTask(brain.ProcessStep), 2F, 0.4F);
        }

        public void render()
        {
            Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1);
            Gdx.gl.glClear(GL20.__Fields.GL_COLOR_BUFFER_BIT);

            camera.update();
            stateTime += Gdx.graphics.getDeltaTime();
            fastTime += Gdx.graphics.getDeltaTime()*2;

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            //            for (int h = 0; h < height; h++)

            for (int h = 0; h < height; h++)
            {
                //for (int w = 0; w < width; w++)
            
                for (int w = width - 1; w >= 0; w--) 
                {
                    int highlighter = 0;
                    switch(brain.FieldMap.Highlight[w,h])
                    {
                        case HighlightType.Bright:
                            highlighter = 1;
                            break;
                        case HighlightType.Dim:
                            highlighter = 2;
                            break;
                        case HighlightType.Plain:
                            highlighter = 0;
                            break;
                        case HighlightType.Spectrum:
                            highlighter= 3+ ((int)((stateTime * 10)%4));
                            break;
                        default:
                            break;
                    }
                    batch.draw(terrains[brain.FieldMap.Land[w, h], highlighter], w * 64 + h * 64, w * 32 - h * 32);
                    if (brain.UnitGrid[w, h] != null)
                    {
                        currentFrame = animations[brain.ReverseColors[brain.UnitGrid[w, h].color]][brain.UnitGrid[w, h].unitIndex][brain.UnitGrid[w, h].facingNumber].getKeyFrame(stateTime, true);
                        batch.draw(currentFrame, 20 + w * 64 + h * 64, 8 + w * 32 - h * 32 + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                    }
                    if (w == brain.ActiveUnit.x && h == brain.ActiveUnit.y)
                    {
                        currentFrame = animations[brain.ReverseColors[brain.ActiveUnit.color]][brain.ActiveUnit.unitIndex][brain.ActiveUnit.facingNumber].getKeyFrame(fastTime, true);
                        batch.draw(currentFrame, 20 + w * 64 + h * 64, 8 + w * 32 - h * 32 + LocalMap.Depths[brain.FieldMap.Land[w, h]] * 3);
                    }
                }
            }
            batch.end();
        }
        public void resume()
        {
            timer.start();
        }
        public void pause()
        {
            timer.stop();
        }
        public void dispose()
        {
            timer.stop();
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
            timer.stop();
            Console.WriteLine("Y O U   W I N !!!");
        }
        public void lose()
        {
            timer.stop();
            Console.WriteLine("YOU LOSE...");
        }
    }

    class Launcher
    {
        public static void Main(string[] args)
        {
            LwjglApplication app = new LwjglApplication(new GameGDX(), "Commanders Unite", 800, 800);
        }
    }
}
