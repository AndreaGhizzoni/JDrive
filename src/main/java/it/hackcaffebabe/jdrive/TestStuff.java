package it.hackcaffebabe.jdrive;


import java.util.HashMap;
import java.util.Optional;

public class TestStuff {

    public static class ComplexObj {
        String a;
        String b;

        ComplexObj( String a, String b ){ this.a = a; this.b = b; }
        public String toString(){ return "[ "+a+" "+b+" ]";}
    }

    public static void main(String...args) {
        HashMap<ComplexObj, String> map = new HashMap<>();
        map.put(new ComplexObj("a1", "b1"), "value1");
        map.put(new ComplexObj("a2", "b2"), "value2");

        System.out.println( map.toString() );

        Optional<ComplexObj> optional = map.keySet()
            .stream()
            .filter( complexObj -> complexObj.a.equals("a1") )
            .findAny();

        if( optional.isPresent() ){
            System.out.println("find!");
            ComplexObj complexObj = optional.get();
            String oldValue = map.get( complexObj );
            complexObj.a = "lol";


            System.out.println( map.put(complexObj, oldValue) );
        }else{
            System.out.println("not find...");
        }

        System.out.println( map.toString() );
    }
}
