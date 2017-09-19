package it.hackcaffebabe.jdrive;


public class TestStuff {
    public static void main(String...args){
        String p = "/home/andrea/Google Drive/file1.txt";

        System.out.println(p);
        String g = "/home/andrea/";
        if( p.startsWith(g) ){
            String neww = p.replaceFirst(g, "");
            System.out.println( neww );
        }
    }
}
