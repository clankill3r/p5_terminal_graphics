package doeke_p5_terminal_graphics;

import static java.awt.event.KeyEvent.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class Terminal_IO {

private Terminal_IO() {}


static public final String ANSI_BACKSPACE = "\b \b";
static final public String ANSI_RESET     = "\u001b[0m";
static final public String ANSI_BOLD      = "\u001b[1m";
static final public String ANSI_FAINT     = "\u001b[2m";
static final public String ANSI_INVERSE   = "\u001b[3m"; // not widely supported, sometimes treated as inverse
static final public String ANSI_UNDERLINE = "\u001b[4m";
static final public String ANSI_REVERSED  = "\u001b[7m";
static final public String ANSI_CONCEAL   = "\u001b[8m";

static final public String ANSI_BLACK   = "\u001b[30m";
static final public String ANSI_RED     = "\u001b[31m";
static final public String ANSI_GREEN   = "\u001b[32m";
static final public String ANSI_YELLOW  = "\u001b[33m";
static final public String ANSI_BLUE    = "\u001b[34m";
static final public String ANSI_MAGENTA = "\u001b[35m";
static final public String ANSI_CYAN    = "\u001b[36m";
static final public String ANSI_WHITE   = "\u001b[37m";

static final public String ANSI_BG_BLACK   = "\u001b[40m";
static final public String ANSI_BG_RED     = "\u001b[41m";
static final public String ANSI_BG_GREEN   = "\u001b[42m";
static final public String ANSI_BG_YELLOW  = "\u001b[43m";
static final public String ANSI_BG_BLUE    = "\u001b[44m";
static final public String ANSI_BG_MAGENTA = "\u001b[45m";
static final public String ANSI_BG_CYAN    = "\u001b[46m";
static final public String ANSI_BG_WHITE   = "\u001b[47m";

static final public String ANSI_CLEAR_FROM_CURSOR_TILL_END_SCREEN   = "\u001b[0J";
static final public String ANSI_CLEAR_FROM_CURSOR_TILL_BEGIN_SCREEN = "\u001b[1J";
static final public String ANSI_CLEAR_ENTIRE_SCREEN = "\u001b[2J";
static final public String ANSI_CLEAR_ENTIRE_SCREEN_INCLUDING_SCROLLBACK = "\u001b[3J";

static final public String ANSI_CLEAR_FROM_CURSOR_TILL_END_LINE = "\u001b[0K";
static final public String ANSI_CLEAR_FROM_CURSOR_TILL_BEGIN_LINE = "\u001b[1K";
static final public String ANSI_CLEAR_ENTIRE_LINE = "\u001b[2K";


static final public String ansi_cursor_up(int n) {
    return "\u001b["+n+"A";
}

static final public String ansi_cursor_down(int n) {
    return "\u001b["+n+"B";
}

static final public String ansi_cursor_right(int n) {
    return "\u001b["+n+"C";
}

static final public String ansi_cursor_left(int n) {
    return "\u001b["+n+"D";
}

static final public String ansi_bg_color(int r, int g, int b) {
    return "\u001b[48;2;"+r+";"+g+";"+b+"m"; 
}

static final public String ansi_fill_color(int r, int g, int b) {
    return "\u001b[38;2;"+r+";"+g+";"+b+"m"; 
}

static final public String ansi_bg_color(int rgb) {
    int r  = (rgb >> 16) & 0xFF;
    int g  = (rgb >> 8) & 0xFF;
    int b  = rgb & 0xFF;
    return "\u001b[48;2;"+r+";"+g+";"+b+"m"; 
}

static final public String ansi_fill_color(int rgb) {
    int r  = (rgb >> 16) & 0xFF;
    int g  = (rgb >> 8) & 0xFF;
    int b  = rgb & 0xFF;
    return "\u001b[38;2;"+r+";"+g+";"+b+"m"; 
}

static public class Terminal_Screen {
    public String tty_restore_config;
    public int rows;
    public int cols;
    public Terminal_Screen_Buffer front_buffer = new Terminal_Screen_Buffer();
    public Terminal_Screen_Buffer back_buffer  = new Terminal_Screen_Buffer();
}

static public class Terminal_Screen_Buffer {
    public char[][] data;
    public Cell_Style[][] style; // todo optmise allocation for slower devices like raspberry pi
}

static public class Cell_Style {
    public int background = 0;
    public int fill = 0xffffffff;
    public int style_flags;
}

static public Terminal_Screen create_and_start_terminal_screen() {

    Terminal_Screen screen = new Terminal_Screen();

    screen.tty_restore_config = stty("-g");

    stty("-icanon min 1"); // only 1 char recuired for a complete read
    stty("-echo"); // disable echo
    // stty("115200");
    // stty("256000");
    cmd("tput civis 2");

    Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
            stop_terminal_screen(screen);
        }
    });

    return screen;
}

static public boolean resize_terminal_screen_if_required(Terminal_Screen terminal) {

    boolean change = false;

    String size = stty("size").trim();
    String[] tokens = size.split(" ");
    int rows = Integer.parseInt(tokens[0]);
    int cols = Integer.parseInt(tokens[1]);
    // int cols = Integer.parseInt(exec(new String[] {"bash", "-c", "tput cols 2>
    // /dev/tty"}));
    // int rows = Integer.parseInt(exec(new String[] {"bash", "-c", "tput lines 2>
    // /dev/tty"}));

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




public interface Key_Pressed {
    void key_pressed(int key);
}


static public void terminal_read_input_vk_keys(Key_Pressed key_pressed) {

    try {
        while (System.in.available() != 0) {
            int vk_key = read_input_key();
            key_pressed.key_pressed(vk_key);
        }
    } catch (IOException e) {
        // restore terminal?
        e.printStackTrace();
    }
}

static public int read_input_key() {
    
    //    key     | key sequence  | ascii index sequence
    //    --------+---------------+---------------------
    //    F1        ESC O P        [27 79 80] 
    //    F2        ESC O Q        [27 79 81] 
    //    F3        ESC O R        [27 79 82] 
    //    F4        ESC O S        [27 79 83]
    //    Up        ESC [ A        [27 91 65] 
    //    Down      ESC [ B        [27 91 66] 
    //    Left      ESC [ D        [27 91 68] 
    //    Right     ESC [ C        [27 91 67]
    //    Home      ESC [ 1 ~      [27 91 49 126] 
    //    F5        ESC [ 1 5 ~    [27 91 49 53 126] 
    //    F6        ESC [ 1 7 ~    [27 91 49 55 126] 
    //    F7        ESC [ 1 8 ~    [27 91 49 56 126] 
    //    F8        ESC [ 1 9 ~    [27 91 49 57 126]
    //    End       ESC [ 4 ~      [27 91 52 126] 
    //    Page-up   ESC [ 5 ~      [27 91 53 126] 
    //    Page-down ESC [ 6 ~      [27 91 54 126]
    //    Insert    ESC [ 2 ~      [27 91 50 126]
    //    F9        ESC [ 2 0 ~    [27 91 50 48 126] 
    //    F10       ESC [ 2 1 ~    [27 91 50 49 126] 
    //    F11       ESC [ 2 2 ~    [27 91 50 50 126] 
    //    F12       ESC [ 2 3 ~    [27 91 50 51 126]
    
    try {

        if (System.in.available() != 0) {

            int c = System.in.read();
            if (c == 27) {

                switch (System.in.read()) {
                case 79:
                    switch (System.in.read()) {
                    case 80:
                        return VK_F1;
                    case 81:
                        return VK_F2;
                    case 82:
                        return VK_F3;
                    case 83:
                        return VK_F4;
                    }
                case 91:
                    switch (System.in.read()) {
                    case 65:
                        return VK_UP;
                    case 66:
                        return VK_DOWN;
                    case 68:
                        return VK_LEFT;
                    case 67:
                        return VK_RIGHT;
                    case 49: {
                        switch (System.in.read()) {
                        case 126:
                            return VK_HOME;
                        case 53:
                            return System.in.read() == 126 ? VK_F5 : 0;
                        case 55:
                            return System.in.read() == 126 ? VK_F6 : 0;
                        case 56:
                            return System.in.read() == 126 ? VK_F7 : 0;
                        case 57:
                            return System.in.read() == 126 ? VK_F8 : 0;
                        }
                    }
                    case 52:
                        return System.in.read() == 126 ? VK_END : 0;
                    case 53:
                        return System.in.read() == 126 ? VK_PAGE_UP : 0;
                    case 54:
                        return System.in.read() == 126 ? VK_PAGE_DOWN : 0;
                    case 50: {
                        switch (System.in.read()) {
                        case 126:
                            return VK_INSERT;
                        case 48:
                            return System.in.read() == 126 ? VK_F9 : 0;
                        case 49:
                            return System.in.read() == 126 ? VK_F10 : 0;
                        case 50:
                            return System.in.read() == 126 ? VK_F11 : 0;
                        case 51:
                            return System.in.read() == 126 ? VK_F12 : 0;
                        }
                    }
                    }
                }
            } else {
                return c;
            }
        }
    } catch (IOException e) {
        // restore terminal?
        e.printStackTrace();
    }

    return 0;
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



static public void printf(String s) {
    System.console().printf("%s", s);
}



static public void draw_terminal_screen(Terminal_Screen terminal) {

    // clear
    printf("\u001b[1;1H"); // we set the cursor to 0, 0 for now to clear

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
                        printf(ansi_bg_color(b_style.background));
                    }

                    printf(ansi_fill_color(b_style.fill));
                    printf(""+b_char);

                } else {
                    printf("\u001b["+(y+1)+";"+(x+1)+"H");
                    printf(ANSI_RESET);
                    printf(ansi_bg_color(b_style.background));
                    printf(ansi_fill_color(b_style.fill));
                    printf(""+b_char);
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






static public void stop_terminal_screen(Terminal_Screen terminal) {
    stty("echo");
    stty(terminal.tty_restore_config);
    cmd("tput cvvis 2");
}


static public String stty(String args) {
    String cmd = "stty " + args + " < /dev/tty";
    return exec(new String[] { "sh", "-c", cmd });
}


static public String cmd(String args) {
    String cmd = args + " > /dev/tty";

    return exec(new String[] {
                "sh",
                "-c",
                cmd
            });
}


static public String exec(String[] cmd) {

    try {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        Process p;
        
        p = Runtime.getRuntime().exec(cmd);
        
        int c;
        InputStream in = p.getInputStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        in = p.getErrorStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        p.waitFor();

        String result = new String(bout.toByteArray());
        return result.trim();
    } 
    catch (IOException | InterruptedException e) {
        return null;
    }
    
}


    
}