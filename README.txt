Dependencies which won't be handled by maven:

- JyNI
- NumPy


1. JyNI



Make the binary files available to your JVM.

    On Linux, OS-X and other systems using libLD to load binaries, place libJyNI.so
    and libJyNI-loader.so (OS-X: libJyNI.dylib and libJyNI-loader.dylib) somewhere
    on your LD_LIBRARY_PATH or tell the JVM via -Djava.library.path where to find
    them or place them on the Java classpath.

    On Windows, JyNI.dll will be a folder containing python27.dll. This is due
    to technical reasons explained on the JyNI GSoC-2017 blog. Treat the folder
    JyNI.dll as if it were the binary dll file itself. It (i.e. its parent directory)
    must be available on the Java classpath.

2.

    Numpy verision 1.13.3 is required. No other version is compatibile with JyNI
