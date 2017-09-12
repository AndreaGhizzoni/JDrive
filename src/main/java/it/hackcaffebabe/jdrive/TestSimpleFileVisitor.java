package it.hackcaffebabe.jdrive;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * docs: https://docs.oracle.com/javase/tutorial/essential/io/walk.html
 */
public class TestSimpleFileVisitor {
    public static void main(String... args) {
        Path start = Paths.get("/home/andrea/Pictures");
        Visitor visitor = new Visitor();
        try {
            // By default, walkFileTree does not follow symbolic links.
            Files.walkFileTree( start, visitor );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Visitor extends SimpleFileVisitor<Path> {

        // Print information about each type of file.
        @Override
        public FileVisitResult visitFile( Path file,
                                          BasicFileAttributes attr) {
            if (attr.isSymbolicLink()) {
                System.out.format("Symbolic link: %s%n", file);
            } else if (attr.isRegularFile()) {
                System.out.format("Regular file: %s%n", file);
            } else {
                System.out.format("Other: %s%n", file);
            }
            System.out.println( formatAttributes(attr) );
            return FileVisitResult.CONTINUE;
        }

        // Print each directory visited.
        @Override
        public FileVisitResult postVisitDirectory( Path dir,
                                                   IOException exc) {
            System.out.format("Directory: %s%n", dir);
            return FileVisitResult.CONTINUE;
        }

        // If there is some error accessing the file, let the user know.
        // If you don't override this method and an error occurs, an IOException
        // is thrown.
        @Override
        public FileVisitResult visitFileFailed( Path file,
                                                IOException exc) {
            System.err.println(exc.getMessage());
            return FileVisitResult.CONTINUE;
        }

        private String formatAttributes( BasicFileAttributes attr ){
            return String.format(
                "{size: %d bytes, creationTime: %s, lastAccessTime: %s, lastModifiedTime: %s, isDirectory: %s, isRegularFile: %s, isSymbolicLink: %s, isOther: %s}",
                attr.size(), attr.creationTime(), attr.lastAccessTime(),
                attr.lastModifiedTime().toString(), attr.isDirectory(), attr.isRegularFile(),
                attr.isSymbolicLink(), attr.isOther()
            );
        }
    }
}
