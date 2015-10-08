package it.hackcaffebabe.jdrive.tree;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * My Personal implementation of tree data structure.
 */
public class FSTree {
    private Node root;

    public FSTree( Path p ){
        if( p != null ){
            this.root = new Node(p);
        }
    }

    public FSTree(){}

//==============================================================================
// INNER CLASS
//==============================================================================
    public class Node{
        private Path filePath;
        private ArrayList<Node> children;

        public Node( Path p ){
            this.filePath = p;
            this.children = new ArrayList<>();
        }
    }
}
