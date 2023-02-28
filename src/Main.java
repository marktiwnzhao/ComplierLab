import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream input = null;
        try {
            input = new FileInputStream(args[0]);
            try {
                byte[] bytes = new byte[input.available()];
                int len;
                String s;
                while((len = input.read(bytes)) != -1) {
                    s = new String(bytes, 0, len);
                    System.out.print(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}