package it.hackcaffebabe.jdrive.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO add doc
 */
final class WatcherUtil
{
    private static Path BASE = Paths.get("/home/andrea/test");

//==============================================================================
//  SETTER
//==============================================================================
    public static void setBase(Path base){
        BASE = base;
    }

//==============================================================================
//  GETTER
//==============================================================================
    public static Path getBase(){
        return BASE;
    }

}
