/*
package client.projections;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.util.StringTokenizer;

public class PythonProjection {

    public void run() {

        String property = System.getProperty("java.library.path");
        StringTokenizer parser = new StringTokenizer(property, ":");

        while (parser.hasMoreTokens()) {
            System.out.println(parser.nextToken());
        }

te

//        System.loadLibrary("JyNI");
//        System.loadLibrary("JyNI-Loader");

        try (PythonInterpreter python = new PythonInterpreter()) {

            python.exec("import sys");
            python.exec("print sys");

//            python.exec("pip");

            python.exec("import numpy as py");
        }
//        python.exec("execfile('src/main/python/equirectangular-toolbox/nfov.py')");
    }

}
*/
