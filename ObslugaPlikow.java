import java.io.*;
import java.util.*;
import java.lang.*;

class ObslugaPlikow {

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else
            return "";
    }

    public static void stringToFile(String fileName, String content) {

        File file = new File(fileName);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("Texts\\" + file));
            out.write(content);
            out.close();
        } catch (IOException e) {
            System.out.println("Blad odczytu pliku.");
            System.exit(2);
        }
    }

    public static ArrayList<File> getFiles() {
        ArrayList<File> files = new ArrayList<File>();
        try {
            File folder = new File("Texts");
            for (File file : folder.listFiles()) {
                if (!file.isDirectory() && ObslugaPlikow.getFileExtension(file).equals("txt")) {
                    // System.out.println(file.getName());
                    // try {
                    //     String fileText = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                    //     System.out.println(fileText);
                    // } catch (IOException e) {
                    //     System.out.println("Blad odczytu pliku.");
                    //     System.exit(2);
                    // }
                    files.add(file);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Nie ma katalogu (Texts).");
        }
        return files;
    }
}