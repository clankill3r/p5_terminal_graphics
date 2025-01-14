package p5_terminal_graphics;

import processing.core.*;
import processing.event.KeyEvent;
import processing.opengl.*;
import java.io.*;
import static p5_terminal_graphics.Terminal_Helper.*;
import static java.awt.event.KeyEvent.*;

public class P5_Terminal_Graphics extends PGraphics3D {


    // This is used in size(600, 600, P5_Terminal_Graphics.TERMINAL);
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    static public final String TERMINAL = "p5_terminal_graphics.P5_Terminal_Graphics";
    public boolean did_init = false;
    public Terminal_Screen terminal_screen;
    public PGraphics resized_g;
    public PrintStream default_out = System.out;
    public PrintStream default_err = System.err;
    public PrintStream std_out_file;
    public PrintStream std_err_file;
    static public String std_out_file_name = "std_out.txt";
    static public String std_err_file_name = "std_err.txt";
    static public String sequence = " .`-_':,;^=+/\"|)\\<>)iv%xclrs{*}I?!][1taeo7zjLunT#JCwfy325Fp6mqSghVd4EgXPGZbYkOA&8U$@KHDBWNMR0Q";
    static public char[] brightness_to_char_lookup = new char[256];
    public int draw_terminal_screen_time_ms;
    public int background_color = 0;
    static public boolean display_stats = false;
    static public Terminal_Post terminal_post;

    public interface Terminal_Post {
        void terminal_post(Terminal_Screen terminal_screen);
    }


    @Override
    public void background(int rgb) {
        super.background(rgb);
        background_color = rgb;
    }

    @Override
    public void background(float v1, float v2, float v3) {
        super.background(v1, v2, v3);
        background_color = color(v1, v2, v3);
    }

    @Override
    public void background(float gray) {
        super.background(gray);
        background_color = color(gray);
    }

    @Override
    public void setParent(PApplet parent) {
        super.setParent(parent);

        // We make the output for System.out.println and System.err.println
        // go to a file as soon as possible, else the user might never
        // see the output. Lines printed in settings will not reach the file,
        // but lines from setup will, which is good enough.
        
        String sketch_path = parent.sketchPath();
        
        try {

            // With the FileOutputStream there is the append option, if we set it to false we never get more
            // then one line of output cause it keeps writing to the beginning of that file.
            // If we set it to true, then also the text of previous times remains, and there does not seem
            // a clear option to have it append, but clear it when first creating the stream.
            // So we will just delete the file. (Oracle why have things to be so hard?)

            File out_file = new File(sketch_path+"/"+std_out_file_name);
            File err_file = new File(sketch_path+"/"+std_err_file_name);

            if (out_file.exists()) {
                out_file.delete();
            }
            if (err_file.exists()) {
                err_file.delete();
            }
            std_out_file = new PrintStream(new FileOutputStream(sketch_path+"/"+std_out_file_name, true));
            std_err_file = new PrintStream(new FileOutputStream(sketch_path+"/"+std_err_file_name, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.setErr(std_err_file);
        System.setOut(std_out_file);
    }

    
    public void endDraw() {
        
        if (!did_init) {
            // Setting the surface to not visible also stops the rendering...
            // PSurfaceJOGL surface = (PSurfaceJOGL) parent.getSurface();
            // surface.setVisible(false);
        
            terminal_screen = Terminal_Helper.create_and_start_terminal_screen();
            parent.registerMethod("post", this);

            // populate the brightness_to_char_lookup array
            float m = (float) sequence.length() / brightness_to_char_lookup.length;
            for (int i = 0; i < brightness_to_char_lookup.length; i++) {
                brightness_to_char_lookup[i] = sequence.charAt((int)Math.floor(i*m));
            }

            did_init = true;
        }
        super.endDraw();
    }


    public void post() {

        Terminal_Helper.terminal_read_input_vk_keys((vk_key)-> {

            // in the processing source this is event.getModifiersEx();
            // Not sure if 0 is always fine in our case, but for now I don't need more.
            int modiefiers = 0; 
            char c_key = (char)vk_key;

            // This can probably done better, but the KeyEvent class
            // that oracle provides lacks some really usefull methods.
            // For example a static method to get the char of a VK_key...
            // https://stackoverflow.com/questions/62967633/get-char-of-vk-key
            switch (vk_key) {
                case VK_UP: 
                case VK_LEFT:
                case VK_RIGHT:
                case VK_DOWN:
                c_key = CHAR_UNDEFINED;
                break;
            }

            parent.postEvent(new KeyEvent(null, parent.millis(), KeyEvent.TYPE, modiefiers, c_key, vk_key));

        });
        
        
        if (resize_terminal_screen_if_required(terminal_screen)) {
            resized_g = parent.createGraphics(terminal_screen.cols, terminal_screen.rows, P3D);
        }

        resized_g.beginDraw();
        resized_g.image(parent.g, 0, 0, resized_g.width, resized_g.height);
        resized_g.endDraw();
        resized_g.loadPixels();

        colorMode(RGB, 255);

        for (int i = 0, y = 0; y < terminal_screen.rows; y++) {
            for (int x = 0; x < terminal_screen.cols; x++) {
                
                int clr = resized_g.pixels[i];
                int brightness = (int) brightness(clr);
                char c = brightness_to_char_lookup[brightness];
                // play around with changing the style here
                set_text(terminal_screen, ""+c, x, y, clr, background_color);
                // set_text(terminal_screen, " ", x, y, 0, clr);
                // set_text(terminal_screen, ""+c, x, y, -1, background_color);
                i++;
            }
        }
        
        if (display_stats) {
            set_text(terminal_screen, "fps: "+parent.frameRate, 0, 0, color(255), color(0));
            set_text(terminal_screen, "draw_terminal_screen_time_ms: "+draw_terminal_screen_time_ms, 0, 1, color(255), color(0));
        }

        if (terminal_post != null) {
            terminal_post.terminal_post(terminal_screen);
        }
        
        int start = parent.millis();
        System.setOut(default_out);
        draw_terminal_screen(terminal_screen);
        System.setOut(std_out_file);
        draw_terminal_screen_time_ms = parent.millis() - start;
        
    }
    
    
}