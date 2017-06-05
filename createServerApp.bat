javac -encoding UTF-8 ServerGUI.java
jar cvfm ServerGUI.jar manifestServer.txt *.class Models/*.class
del *.class Models\*.class