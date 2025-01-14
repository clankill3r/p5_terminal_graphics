package p5_terminal_graphics_examples;

import processing.core.PApplet;
import processing.core.PShape;
import java.lang.invoke.MethodHandles;
import p5_terminal_graphics.P5_Terminal_Graphics;
import p5_terminal_graphics.Terminal_Helper;
import p5_terminal_graphics.Terminal_Helper.Terminal_Screen;

/*
CONTROLS:

[UP]   - increase rotation speed
[DOWN] - decrease rotation speed
[S]    - toggle stats on / off
[R]    - random background color
*/

public class Terminal_Fox extends PApplet {

    PShape model;

    public static void main(String[] args) {
        PApplet.main(MethodHandles.lookup().lookupClass().getName());
    }

    
    @Override
    public void settings() {
        size(1120, 850, P5_Terminal_Graphics.TERMINAL);
    }


    @Override
    public void setup() {        
        frameRate(60);
        model = loadShape("fox.obj");

        // terminal_post will be called after processing did the rendering
        // but before things are being send to the terminal.
        // It allows to draw text for example
        P5_Terminal_Graphics.terminal_post = this::terminal_post;
    }

    public void terminal_post(Terminal_Screen terminal_screen) {
        int fps = (int)frameRate;
        Terminal_Helper.set_text(terminal_screen, "fps: "+fps, terminal_screen.cols-7, 0, color(255), color(0));
    }


    float last_frame_time;
    float current_frame_time;
    float delta_time = 1f / 60f;
    
    float rot_y = 0;
    float rot_speed = HALF_PI; // in seconds

    int background_color = 0;


    @Override
    public void draw() {

        last_frame_time = current_frame_time;
        current_frame_time = millis();
        delta_time = (current_frame_time - last_frame_time) / 1000;
        
        background(background_color);

        pointLight(255, 100, 0, 0, 0, 800);
        pointLight(0, 0, 255, width, 0, 0);
        ambientLight(50, 50, 50);

        translate(width/2, height*0.6f);
        rotateZ(PI);
        rot_y += rot_speed * delta_time;
        rotateY(-rot_y);
        scale(35);
        shape(model);

        surface.setTitle("fps: "+frameRate);
    }



    @Override
    public void keyTyped() {
        if (key == CODED) {
            if (keyCode == UP) {
                rot_speed += radians(15);
            }
            if (keyCode == DOWN) {
                rot_speed -= radians(15);
            }
        }
        else if (key == 's' || key == 'S') {
            P5_Terminal_Graphics.display_stats = !P5_Terminal_Graphics.display_stats;
        }
        else if (key == 'r' || key == 'R') {
            colorMode(HSB, 360, 100, 100);
            background_color = color(random(360), random(100), random(0, 25));
        }
    }

}
