package it.hackcaffebabe.jdrive.mysimpletest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TestLog4J
{
    static final Logger logger = LogManager.getLogger("primaryLog");

    public boolean doIt( int a) {
        //Trace level information, separately is to call you when you started
        //in a method or program logic, and logger.trace ("entry") basic a meaning
        logger.entry();
        //The error levels of information, information output parameter is you
        logger.error("Did it again!");
        //Info level information
        logger.info("I'm info information");
        logger.debug("I'm debug information");
        logger.warn("I'm warn information");
        logger.fatal("I'm fatal information");
        //This is the development of Level type of call: who is idle and call
        //this, not necessarily oh！
        logger.log(Level.DEBUG, "I'm debug information");
        //And the entry () method to end the correspondence, and the
        //logger.trace ("exit"); a meaning
        logger.exit();
        return false;
    }

    public static void main(String[] args){
        TestLog4J b = new TestLog4J();
        b.doIt(2);
    }
}
