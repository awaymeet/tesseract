package tess4j;
import java.io.File;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * @author Viayie
 * Use tess4j to do OCR
 */
public class Ocr {
	public static String ocr(String filePath) throws TesseractException{
        Tesseract instance = new Tesseract();
        instance.setDatapath(System.getProperty("user.dir") +File.separator+"src" +File.separator+"main" +File.separator+"resources" +File.separator+"tessdata" +File.separator);
        instance.setLanguage("chi_sim");
        File f=new File(filePath);
        return instance.doOCR(f);
    }
}
