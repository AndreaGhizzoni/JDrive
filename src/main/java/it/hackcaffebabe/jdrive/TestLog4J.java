package it.hackcaffebabe.jdrive;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TestLog4J
{
    static final Logger logger = LogManager.getLogger(TestLog4J.class.getName());

    public boolean doIt( int a) {
        logger.entry(a);
        logger.error("Logger error message");
        logger.info("asd");
        return logger.exit(false);
    }

    public static void main(String[] args){
        TestLog4J b = new TestLog4J();
        b.doIt(2);
    }
}
