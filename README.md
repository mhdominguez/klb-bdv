# Keller Lab Block file type (.klb) - JNI Library #

## Java Native Interface (JNI) ##

The KLB API is exposed on the Java-side through a JNI wrapper, included in the "javaWrapper" subfolder. It can be build with maven, includes compiled native libraries for Windows and Linux (both 64-bit) and will eventually be available as an artifact on a Maven repository. ImageJ users on supported platforms can simply install KLB support by following the update site (see below).


## Install KLB for Fiji via Release binaries

Download ALL .jar files in the most recent Release published to this repository (see **Releases** on the right).  Copy these files to your `Fiji.app/jars` folder.


## Build JNI library from source
  - install Maven
  - navigate to the javaWrapper subfolder
  - run "mvn clean package"
  - the JAR file will be built at "javaWrapper/target/klb-[version].jar"
  - you will also need klb-[version].jar, available through the ImageJ update site at http://sites.imagej.net/SiMView/
