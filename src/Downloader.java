import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sai Pranav on 1/24/2017.
 */
public class Downloader extends Thread {
  Part part;

  public Downloader(Part part){
    this.part = part;
  }

  public void run(){
    InputStream inputStream = null;
    OutputStream outStream = null;
    try {
      File targetFile = new File(this.part.filePath);
      if(targetFile.exists()){
        System.out.println(this.part.filePath + " Already Exists");
        return;
      }
      HttpURLConnection urlConnection = (HttpURLConnection) this.part.url.openConnection();
      urlConnection.setRequestProperty("Range", "bytes=" + this.part.startBytes + "-" + this.part.endBytes);
      urlConnection.connect();
      inputStream = urlConnection.getInputStream();

      outStream = new FileOutputStream(targetFile);
      byte[] buffer = new byte[inputStream.available()];
      inputStream.read(buffer);
      outStream.write(buffer);
      this.part.finished = true;
    } catch (IOException e) {
      System.out.println(e);
    } finally {
      close(inputStream);
      close(outStream);
    }
    return;
  }

  private void close(Closeable stream) {
    try {
      if (stream != null) {
        stream.close();
      }
    } catch(IOException e) {
      System.out.println(e);
    }
  }
}
