package com.example.jpiet;

/**
 * Supported colors in Piet Language
 */
public enum CodelColor {
	
    BLACK("Black", 0x000000, -10, -10),
    LIGHT_YELLOW("Light Yellow", 0xffffc0, 1, 0),
    GREEN("Green", 0x00ff00, 2, 1),
    DARK_YELLOW("Dark Yellow", 0xc0c000, 1, 2),
    LIGHT_MAGENTA("Light Magenta", 0xffc0ff, 5, 0),
    DARK_GREEN("Dark Green", 0x00c000, 2, 2),
    LIGHT_BLUE("Light Blue", 0xc0c0ff, 4, 0),
    DARK_BLUE("Dark Blue", 0x0000c0, 4, 2),
    LIGHT_RED("Light Red", 0xffc0c0, 0, 0),
    BLUE("Blue", 0x0000ff, 4, 1),
    LIGHT_GREEN("Light Green", 0xc0ffc0, 2, 0),
    CYAN("Cyan", 0x00ffff, 3, 1),
    MAGENTA("Magenta", 0xff00ff, 5, 1),
    RED("Red", 0xff0000, 0, 1),
    DARK_MAGENTA("Dark Magenta", 0xc000c0, 5, 2),
    DARK_RED("Dark Red", 0xc00000, 0, 2),
    YELLOW("Yellow", 0xffff00, 1, 1),
    LIGHT_CYAN("Light Cyan", 0xc0ffff, 3, 0),
    DARK_CYAN("Dark Cyan", 0x00c0c0, 3, 2),
    WHITE("White", 0xffffff, -100, -100);
    String name;
    
    public int value;
    int hue;
    int dark;
    int R;
    int G;
    int B;

    public static CodelColor findColor(int pixel) {
    	int r = (pixel & 0xFF0000) >> 16;
        int g = (pixel & 0xFF00) >> 8;
        int b = (pixel & 0xFF);

        return findColor(r, g, b);
    }
    
    public int getARGB(){
        int argb = 255;
        argb = (argb << 8) + R;
        argb = (argb << 8) + G;
        argb = (argb << 8) + B;
        
        return argb;
    }
    
    public int getRGBA() {
        int rgba = R;
        rgba = (rgba) + G;
        rgba = (rgba << 8) + B;
        rgba = (rgba << 8) + 255;
  
        return rgba;
    }
    
    public static CodelColor findColor(int r, int g, int b) {
        CodelColor result = BLACK;
        for (CodelColor c: CodelColor.values()) {
            if( c.R == r && c.G == g && c.B == b ){
                result = c;
            }
        }

        return result;
    }
    //FIXME ONE USE ARGB NOT RGB
    private CodelColor(String _name, int _value, int _hue, int _dark) {
        name = _name;
        value = _value;
        hue = _hue;
        dark = _dark;
        R = (value & 0xFF0000) >> 16;
        G = (value & 0xFF00) >> 8;
        B = (value & 0xFF);
        //System.out.println("CC: " +r + " " + g + " " + b);
    }
}
