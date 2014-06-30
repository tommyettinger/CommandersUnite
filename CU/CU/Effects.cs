using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.math;
using com.badlogic.gdx.utils;

namespace CU
{
    public enum HighlightType
    {
        Plain, Bright, Dim, Spectrum
    }
    /*
    public class ShaderTools
    {
        public static ShaderProgram AlterChannels(float r, float g, float b)
        {
            string colors = "vec4(" + r + ", " + g + ", " + b + ", 1)";
            string vertexShader =
    @"
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    v_color = " + colors + @";
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}";
            string fragmentShader =
@"
#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
}";
            return new ShaderProgram(vertexShader, fragmentShader);
        }
        //public static ShaderProgram Plain = AlterChannels(1, 1, 1);
        //public static ShaderProgram Bright = AlterChannels(1.35f, 1.35f, 1.35f);
        //public static ShaderProgram[] Spectrum = { AlterChannels(1.4f, 0.8f, 0.8f), AlterChannels(1.4f, 1.4f, 0.7f), AlterChannels(0.8f, 1.4f, 0.8f), AlterChannels(0.85f, 0.85f, 1.4f)};
    }*/
    public delegate void Nilly ();
    public class NilTask : Timer.Task
    {
        Nilly runner;
        public NilTask(Nilly runner)
        {
            this.runner = runner;
        }
        public override void run()
        {
            runner();
        }
    }
    public class Effects
    {

        public static void CenterCamera(float gridX, float gridY)
        {
            GameGDX.camera.position.set(new Vector3(64 * (gridX + gridY), 32 * (gridX - gridY)+32, 0));
            GameGDX.camera.update();
        }
        public static void CenterCamera(Position pos)
        {
            GameGDX.camera.position.set(new Vector3(64 * (pos.x + pos.y), 32 * (pos.x - pos.y) + 32, 0));
            GameGDX.camera.update();
        }
    }
}
