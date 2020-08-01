package p5_terminal_graphics;

import java.io.IOException;


//-----------Terminal_Helper
public class Terminal_Helper {
     private Terminal_Helper(){}

//
// Data:
//
static public class Terminal_Screen {
    public String tty_restore_config;
    public int rows;
    public int cols;
    public Terminal_Screen_Buffer front_buffer = new Terminal_Screen_Buffer();
    public Terminal_Screen_Buffer back_buffer  = new Terminal_Screen_Buffer();
}

static public class Terminal_Screen_Buffer {
    public char[][] data;
    public Cell_Style[][] style; // TODO optmise allocation for slower devices like raspberry pi
}

static public class Cell_Style {
    public int background = 0;
    public int fill = 0xffffffff;
    public int style_flags;
}


public interface Key_Pressed {
    void key_pressed(int key);
}

//
// Functions:
//
static public Terminal_Screen create_and_start_terminal_screen() {

    Terminal_IO.store_terminal_settings();

    Terminal_Screen screen = new Terminal_Screen();

    Terminal_IO.put_into_character_mode();
    Terminal_IO.disable_echo();
    Terminal_IO.hide_cursor();

    Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
            Terminal_IO.restore_terminal_settings();
        }
    });

    return screen;
}


static public boolean resize_terminal_screen_if_required(Terminal_Screen terminal) {

    boolean change = false;

    int rows = Terminal_IO.rows();
    int cols = Terminal_IO.cols();

    if (cols != terminal.cols || rows != terminal.rows) {
        change = true;
        terminal.cols = cols;
        terminal.rows = rows;
        terminal.front_buffer.data = new char[rows][cols];
        terminal.front_buffer.style = new Cell_Style[rows][cols];
        terminal.back_buffer.data = new char[rows][cols];
        terminal.back_buffer.style = new Cell_Style[rows][cols];
        for (int y = 0; y < terminal.rows; y++) {
            for (int x = 0; x < terminal.cols; x++) {
                terminal.front_buffer.style[y][x] = new Cell_Style();
                terminal.back_buffer.style[y][x] = new Cell_Style();
            }
        }
    }
    return change;
}





// static public int set_text(Terminal_Screen terminal, String text, int x, int y) {
//     return set_text(terminal, text, x, y, 0, 0);
// }

// returns how many lines the text took, if y was already > then the rows 0 is returned
static public int set_text(Terminal_Screen terminal, String text, int x, int y, int fill, int background) {

    if (y > terminal.rows) {
        return 0;
    }


    String[] lines = text.replace("\t", "  ").split("\n");
    for (String line : lines) {
        if (y < 0)
            continue;
        if (y >= terminal.rows)
            break;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (x + i >= terminal.cols)
                break;
            if (x + i < 0)
                break;
            terminal.back_buffer.data[y][x + i] = c;
            terminal.back_buffer.style[y][x + i].fill = fill;
            terminal.back_buffer.style[y][x + i].background = background;
        }
        y++;
    }

    return lines.length;
}

static public boolean cell_style_equals(Cell_Style a, Cell_Style b) {
    return a.style_flags == b.style_flags && a.background == b.background && a.fill == b.fill;
}


static public void draw_terminal_screen(Terminal_Screen terminal) {

    Terminal_IO.set_cursor(1, 1);

    int index = 0;
    int last_written_index = -1;

    for (int y = 0; y < terminal.rows; y++) {
        for (int x = 0; x < terminal.cols; x++) {

            index++;

            Cell_Style f_style = terminal.front_buffer.style[y][x];
            Cell_Style b_style = terminal.back_buffer.style[y][x];

            char f_char = terminal.front_buffer.data[y][x];
            char b_char = terminal.back_buffer.data[y][x];

            if (f_char != b_char || !cell_style_equals(f_style, b_style)) {

                if (last_written_index == index - 1) {

                    Cell_Style last_style = null;
                    
                    if (x > 0) {
                        last_style = terminal.back_buffer.style[y][x - 1];
                    } 
                    else {
                        last_style = terminal.back_buffer.style[y - 1][terminal.cols - 1];
                    }
                    if (last_style.background != b_style.background) {
                        Terminal_IO.printf(Terminal_IO.ansi_bg_color(b_style.background));
                    }

                    Terminal_IO.printf(Terminal_IO.ansi_fill_color(b_style.fill));
                    Terminal_IO.printf(""+b_char);

                } else {
                    Terminal_IO.printf("\u001b["+(y+1)+";"+(x+1)+"H");
                    Terminal_IO.printf(Terminal_IO.ANSI_RESET);
                    Terminal_IO.printf(Terminal_IO.ansi_bg_color(b_style.background));
                    Terminal_IO.printf(Terminal_IO.ansi_fill_color(b_style.fill));
                    Terminal_IO.printf(""+b_char);
                }

                last_written_index = index;
            }

        }
    }

    // swap front and back buffer
    Terminal_Screen_Buffer tmp = terminal.back_buffer;
    terminal.back_buffer = terminal.front_buffer;
    terminal.front_buffer = tmp;


}


static public void clear_terminal_backbuffer(Terminal_Screen terminal, int fill, int background) {

    Terminal_Screen_Buffer back_buffer = terminal.back_buffer;

    for (int y = 0; y < terminal.rows; y++) {
        for (int x = 0; x < terminal.cols; x++) {
            back_buffer.data[y][x] = ' ';
            back_buffer.style[y][x].fill = fill;
            back_buffer.style[y][x].background = background;
        }
    }
}


// // TODO name read_next_input_key? return '\0' when nothing is left
static public void terminal_read_input_vk_keys(Key_Pressed key_pressed) {

    try {
        while (System.in.available() != 0) {
            int vk_key = Terminal_IO.read_input_key();
            key_pressed.key_pressed(vk_key);
        }
    } catch (IOException e) {
        // restore terminal?
        e.printStackTrace();
    }
}


}