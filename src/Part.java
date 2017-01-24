import java.net.URL;

/**
 * Created by Sai Pranav on 1/24/2017.
 */
public class Part {
  URL url;
  int partNumber;
  String filePath;
  boolean finished;
  int startBytes, endBytes;

  public Part(URL url, int partNumber, String filePath, int startBytes, int endBytes){
    this.url = url;
    this.partNumber = partNumber;
    this.startBytes = startBytes;
    this.filePath = filePath;
    this.endBytes = endBytes;
    this.finished = false;
  }
}
