import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Sai Pranav on 1/24/2017.
 */
public class DownloadManager {
  static int maxThreadWorkers = 5;
  static int fixedBytesPerWorker = 1024;

  public static void main(String args[]){
    String urlToDownload = null;
    String fileName = null;
    String pathToSave = null;
    URL url = null;
    ArrayList<Part> parts;
    ArrayList<Thread> threads = new ArrayList<Thread>();
    int threadWorkers = 0;
    Scanner scanner = new Scanner(System.in);
    FilenameFilter tempFilter;

    // Get program arguments
    if(args.length != 2){
      System.out.println("Url from which file to be downloaded is needed\nPath to save the downloaded file is needed\nDownloadManager http://example.com/data.txt C:\\Users\\Sai\\Desktop");
    } else {
      urlToDownload = args[0];
      fileName = urlToDownload.substring(urlToDownload.lastIndexOf("/")+1, urlToDownload.length());
      pathToSave = args[1];
    }

    try{
      url = new URL(urlToDownload);
    } catch(MalformedURLException e){
      System.out.println(e);
      System.exit(0);
    }

    String finalFileName = fileName;
    tempFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.contains(finalFileName) && name.endsWith("tmp")) {
          return true;
        } else {
          return false;
        }
      }
    };

    // Start download procedure
    File folder = new File(".");
    File[] listOfFiles = folder.listFiles();
    boolean fileAlreadyExisting = false;

    for (int i = 0; i < listOfFiles.length; i++) {
      File file = listOfFiles[i];
      if (file.isFile() && file.getName().contains(fileName) && file.getName().endsWith("tmp")) {
        fileAlreadyExisting = true;
        //String content = FileUtils.readFileToString(file);
      }
    }
    if(fileAlreadyExisting){
      System.out.println("Resume");
    } else {
      System.out.println("New");
      int fileSize = getFileSize(url);
      parts = generateParts(url, fileName, fileSize);

      for(int i = 0; i < parts.size(); i++){
        //if(threadWorkers != maxThreadWorkers){
        Thread newWorker = new Downloader(parts.get(i));
        //threadWorkers++;
        threads.add(newWorker);
        newWorker.start();
        //}
        // User can press CTRL + D
        /*if(scanner.hasNext()) {
          // Perform all other stuffs
          System.exit(0);
        }*/
      }

      // All temp files downloaded execute Merger
      for(Thread thread : threads){
        try {
          thread.join(8000);
        } catch (InterruptedException e) {
          System.out.println(e);
          System.exit(0);
        }
      }
      merger(pathToSave, fileName, tempFilter);
    }



  }

  private static int getFileSize(URL url) {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("HEAD");
      conn.getInputStream();
      return conn.getContentLength();
    } catch (IOException e) {
      System.out.println(e);
    } finally {
      if(conn != null){
        conn.disconnect();
      }
    }
    return -1;
  }

  private static ArrayList<Part> generateParts(URL url, String fileName, int fileSize){
    ArrayList<Part> toBeReturned = new ArrayList<Part>();
    int numberOfParts = fileSize / fixedBytesPerWorker;
    for(int i = 0; i <= numberOfParts; i++){
      String partPath = ".\\" + fileName + "." + i + ".tmp";
      Part part = new Part(url, i, partPath, (i) * fixedBytesPerWorker,(i+1) * fixedBytesPerWorker);
      toBeReturned.add(part);
    }
    return toBeReturned;
  }

  private static void merger(String destination, String fileName, FilenameFilter tempFilter){
    FileReader fileReader = null;
    FileWriter targetFile = null;
    try {
      targetFile = new FileWriter(destination + "\\" + fileName);
      File folder = new File(".");
      File[] listOfFiles = folder.listFiles(tempFilter);

      Arrays.sort(listOfFiles, new Comparator<File>(){
        @Override
        public int compare(File f1, File f2) {
          String fileName1 = f1.getName();
          String fileName2 = f2.getName();

          int fileId1 = Integer.parseInt(fileName1.split("\\.")[2]);
          int fileId2 = Integer.parseInt(fileName2.split("\\.")[2]);

          return fileId1 - fileId2;
        }
      });

      for (int i = 0; i < listOfFiles.length; i++) {
        File file = listOfFiles[i];
        if (file.isFile()) {
          fileReader = new FileReader(file);
          int character = fileReader.read();
          while(character != -1) {
            targetFile.write(character);
            character = fileReader.read();
          }
          file.delete();
        }
      }
    } catch (IOException e){
      System.out.println(e);
    } finally {
      close(fileReader);
      close(targetFile);
    }
  }

  private static void close(Closeable stream) {
    try {
      if (stream != null) {
        stream.close();
      }
    } catch(IOException e) {
      System.out.println(e);
    }
  }

}


