package it.hackcaffebabe.jdrive;


public class TestStuff {

    private static class T {
        private Thread rT;

        public T(){
            this.rT = new Thread( () -> {
                while (!Thread.interrupted()) {
                    System.out.println("alive");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            } , "name" );
        }

        public void start(){
            this.rT.start();
        }

        public void startClosing(){
            System.out.println("closing");
            if( this.rT != null )
                this.rT.interrupt();
        }
    }

    public static void main(String...args) {
//        String p = "/home/andrea/Google Drive";
//        System.out.println(p);
//        String g = "Google Drive";
//            System.out.println( p.replaceFirst(g, "") );

        T r = new T();
        r.start();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        r.startClosing();
    }
}
