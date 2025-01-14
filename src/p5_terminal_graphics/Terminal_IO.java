package p5_terminal_graphics;

import static java.awt.event.KeyEvent.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//-----------Terminal_IO 
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


static public String _terminal_restore_settings;


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




// ------------------------------------------------------------

static public String curses_version() {
    return cmd("tput -V");
}

static public String terminal_type() {
    return cmd("echo $TERM");
}

static public int cols() {
    return Integer.parseInt(cmd("tput cols 2>/dev/tty"));
}

static public int rows() {
    return Integer.parseInt(cmd("tput lines 2>/dev/tty"));
}

static public int[] size() {

    String[] tokens = cmd("stty size < /dev/tty").split(" ");

    return new int[] {
        Integer.parseInt(tokens[0]), 
        Integer.parseInt(tokens[1])
    };
}

static public int initial_tab_width() {
    return Integer.parseInt(cmd("tput it 2>/dev/tty"));
}

static public int n_colors() {
    return Integer.parseInt(cmd("tput colors 2>/dev/tty"));
}

static public void disable_echo() {
    cmd("stty -echo < /dev/tty");
}

static public void enable_echo() {
    cmd("stty echo < /dev/tty");
}

static public void do_not_try_to_clear_scrollback() {
    cmd("tput -x");
}

static public void init() {
    cmd("tput init");
}

static public void reset() {
    cmd("reset 2> /dev/tty");
}

static public void clear() {
    cmd("tput clear 2> /dev/tty");
}

static public void hide_cursor() {
    cmd("tput civis > /dev/tty");
}

static public void show_cursor() {
    cmd("tput cnorm > /dev/tty");
}

// or printf("\u001b[1;1H"); // we set the cursor to 0, 0 for now to clear
static public void set_cursor(int x, int y) {
    // Note(Doeke): works but ansi might be better for windows
    // cmd("tput cup "+x+" "+y+" > /dev/tty");
    // order y x is correct!
    printf("\u001b["+y+";"+x+"H");
}

static public void delete_n_lines_below_cursor_inclusive(int n) {
    cmd("tput dl "+n+" > /dev/tty");
}

static public void put_into_character_mode() {
    cmd("stty -icanon min 1 < /dev/tty");
}

static public void store_terminal_settings() {
    _terminal_restore_settings = cmd("stty -g < /dev/tty");
}

static public void restore_terminal_settings() {
    assert _terminal_restore_settings != null;
    // restoring it seems to be some nasty job
    show_cursor();
    clear();
    turn_off_all_attributes();
    reset(); // clear does not work well, so we use reset as well
    enable_echo();
    cmd("stty "+_terminal_restore_settings+" < /dev/tty");
}

static public void turn_off_all_attributes() {
    cmd("tput sgr0 > /dev/tty");
}

static public void carriage_return() {
    cmd("tput cr > /dev/tty");
}

// csr     cs      change scrolling region to lines #1 through #2 (p)
// cub     le      move cursor left #1 spaces (p)
// cub1    le      move cursor left one space
// cud     do      move cursor down #1 lines (p*)
// cud1    do      move cursor down one line
// cuf     ri      move cursor right #1 spaces (p*)
// cuf1    nd      move cursor right one space
// cup     cm      move cursor to row #1, column #2 of screen (p)
// cuu     up      move cursor up #1 lines (p*)
// cuu1    up      move cursor up one line
// cvvis   vs      make cursor very visible
// dch     dc      delete #1 characters (p*)
// dch1    dc      delete one character (*)
// dim     mh      begin half intensity mode
// dl      dl      delete #1 lines (p*)
// dl1     dl      delete one line (*)
// dsl     ds      disable status line
// ech     ec      erase #1 characters (p)
// ed      cd      clear to end of display (*)
// el      ce      clear to end of line
// el1     cb      clear to beginning of line, inclusive
// enacs   ea      enable alternate character set
// ff      ff      form feed for hardcopy terminal (*)
// flash   vb      visible bell (must not move cursor)
// fsl     fs      return from status line
// hd      hd      move cursor down one-half line
// home    ho      home cursor (if no `cup')
// hpa     ch      move cursor to column #1 (p)
// ht      ta      tab to next 8 space hardware tab stop
// hts     st      set a tab in all rows, current column
// hu      hu      move cursor up one-half line
// ich     ic      insert #1 blank characters (p*)
// ich1    ic      insert one blank character
// if      if      name of file containing initialization string
// il      al      add #1 new blank lines (p*)
// il1     al      add one new blank line (*)
// ind     sf      scroll forward (up) one line
// indn    sf      scroll forward #1 lines (p)
// invis   mk      begin invisible text mode
// ip      ip      insert pad after character inserted (*)
// iprog   ip      path of program for initialization
// is1     i1      terminal initialization string
// is2     is      terminal initialization string
// is3     i3      terminal initialization string
// kbeg    &9      shifted beginning key
// kcan    &0      shifted cancel key
// kcmd    *1      shifted command key
// kcpy    *2      shifted copy key
// kcrt    *3      shifted create key
// kdc     *4      shifted delete char key
// kdl     *5      shifted delete line key
// kend    *7      shifted end key
// keol    *8      shifted clear line key
// kext    *9      shifted exit key
// kfnd    *0      shifted find key
// khlp    #1      shifted help key
// khom    #2      shifted home key
// kic     #3      shifted input key
// klft    #4      shifted left arrow key
// kmov    %b      shifted move key
// kmsg    %a      shifted message key
// knxt    %c      shifted next key
// kopt    %d      shifted options key
// kprt    %f      shifted print key
// kprv    %e      shifted prev key
// krdo    %g      shifted redo key
// kres    %j      shifted resume key
// krit    %i      shifted right arrow
// krpl    %h      shifted replace key
// ksav    !1      shifted save key
// kspd    !2      shifted suspend key
// kund    !3      shifted undo key
// ka1     k1      upper left of keypad
// ka3     k3      upper right of keypad
// kb2     k2      center of keypad
// kbeg    @1      beginning key
// kbs     kb      backspace key
// kc1     k4      lower left of keypad
// kc3     k5      lower right of keypad
// kcan    @2      cancel key
// kcbt    kb      back tab key
// kclo    @3      close key
// kclr    kc      clear screen or erase key
// kcmd    @4      command key
// kcpy    @5      copy key
// kcrt    @6      create key
// kctab   kt      clear tab key
// kcub1   kl      left arrow key
// kcud1   kd      down arrow key
// kcuf1   kr      right arrow key
// kcuu1   ku      up arrow key
// kdch1   kd      delete character key
// kdl1    kl      delete line key
// ked     ks      clear to end of screen key
// kel     ke      clear to end of line key
// kend    @7      end key
// kext    @9      exit key


static public String cmd(String args) {
    return exec("sh", "-c", args);
}

static public String exec(String... cmd) {

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

static public void printf(String s) {
    System.console().printf("%s", s);
}

    
}