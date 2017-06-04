javac ClientGUI.java
jar cvfm ClientGUI.jar manifestClient.txt *.class Models/*.class
del *.class Models\*.class