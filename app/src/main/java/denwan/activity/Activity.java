package denwan.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by tanne on 17.07.2017.
 */

public class Activity {
    static final String FILENAME = "activity.data";

    public static Data DATA = new Data();

    static File getPath(android.app.Activity activity)
    {
        return activity.getExternalFilesDir(null);
    }

    public static boolean save(android.app.Activity activity)
    {
        File path = getPath(activity);

        File file = new File(path, FILENAME);

        try {
            FileOutputStream outfile = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(outfile);
            out.writeObject(DATA);
            out.close();
            outfile.close();
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
        catch (java.io.IOException e)
        {
            return false;
        }

        return true;
    }

    public static boolean load(android.app.Activity activity)
    {
        boolean result = true;
        try {
            File file = new File(getPath(activity), FILENAME);
            FileInputStream fileInput = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileInput);

            DATA = (Data)in.readObject();

            in.close();
        }
        catch(FileNotFoundException e)
        {
            result = false;
        }
        catch(IOException e)
        {
            result = false;
        }
        catch(ClassNotFoundException e)
        {
            result = false;
        }

        return result;
    }
}
