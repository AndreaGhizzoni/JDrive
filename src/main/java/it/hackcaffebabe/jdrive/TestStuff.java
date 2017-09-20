package it.hackcaffebabe.jdrive;


public class TestStuff {
    public static void main(String...args){
        String p = "/home/andrea/Google Drive";

        System.out.println(p);
        String g = "Google Drive";
            System.out.println( p.replaceFirst(g, "") );
    }
}
