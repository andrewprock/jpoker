package utils;
import java.io.*;

public class Debug {
    public static boolean debug = false;
    public static final int ALL        =  9;
    public static final int LOOP       =  8;
    public static final int HEADERS    =  7;
    public static final int EXCEPTIONS =  6;
    public static final int FIVE          =  5;
    public static final int FOUR          =  4;
    public static final int TEST       =  3;
    public static final int TWO           =  2;
    public static final int ONE           =  1;
    public static final int ZERO          =  0;
    
    public static int level = ALL;

    public static void out (Object s) {
	if (debug && level >= ALL)
	    System.out.print("D(" + level + "):" + s + "\n");
    }
    public static void out (int s) {
	if (debug && level >= ALL)
	    System.out.print("D(" + level + "):" + s + "\n");
    }  
    public static void out (double s) {
	if (debug && level >= ALL)
	    System.out.print("D(" + level + "):" + s + "\n");
    }

    public static void hout (Object s) {
	if (debug && level >= HEADERS)
	    System.out.print("H(" + HEADERS + "): " + s + "\n");
    }
    public static void hout (int s) {
	if (debug && level >= HEADERS)
	    System.out.print("H(" + HEADERS + "): " + s + "\n");
    }  
    public static void hout (double s) {
	if (debug && level >= HEADERS)
	    System.out.print("H(" + HEADERS + "): " + s + "\n");
    }

    public static void eout (Object s) {
	if (debug && level >= EXCEPTIONS)
	    System.out.print("E(" + EXCEPTIONS + "): " + s + "\n");
    }
    public static void eout (int s) {
	if (debug && level >= EXCEPTIONS)
	    System.out.print("E(" + EXCEPTIONS + "): " + s + "\n");
    }  
    public static void eout (double s) {
	if (debug && level >= EXCEPTIONS)
	    System.out.print("E(" + EXCEPTIONS + "): " + s + "\n");
    }

    public static void tout (Object s) {
	if (debug && level >= TEST)
	    System.out.print("T(" + TEST + "): " + s + "\n");
    }
    public static void tout (int s) {
	if (debug && level >= TEST)
	    System.out.print("T(" + TEST + "): " + s + "\n");
    }  
    public static void tout (double s) {
	if (debug && level >= TEST)
	    System.out.print("T(" + TEST + "): " + s + "\n");
    }

    public static void out (Object s, int level) {
	if (debug)
	    System.out.print(s+"\n");
    }
    public static void out (int s, int level) {
	if (debug)
	    System.out.print(s+"\n");
    }  
    public static void out (double s, int level) {
	if (debug)
	    System.out.print(s+"\n");
    }
}

