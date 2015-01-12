package it.hackcaffebabe.jdrive.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that manage the base path of {@link it.hackcaffebabe.jdrive.fs.Watcher}
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
