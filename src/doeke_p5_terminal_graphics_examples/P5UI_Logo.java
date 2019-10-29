package doeke_p5_terminal_graphics_examples;


import processing.core.PApplet;
import processing.core.PShape;
import java.lang.invoke.MethodHandles;

import doeke_p5_terminal_graphics.TPGraphics;



public class P5UI_Logo extends PApplet {

    PShape model;

    public static void main(String[] args) {
        PApplet.main(MethodHandles.lookup().lookupClass().getName());
    }


    @Override
    public void settings() {
        size(1120, 850, TPGraphics.TERMINAL);
    }


    @Override
    public void setup() {        
        // frameRate(999);
        model = loadShape("P5ui.obj");  
    }


    @Override
    public void draw() {

        background(0, 0, 0);

        pointLight(255, 100, 0, 0, 0, 800);
        pointLight(0, 0, 255, width, 0, 0);
        ambientLight(50, 50, 50);

        translate(width/2, height*0.6f);
        rotateZ(PI);
        rotateY(-frameCount * 0.01f);
        scale(350);
        shape(model);

        surface.setTitle("fps: "+frameRate);
    }


}
