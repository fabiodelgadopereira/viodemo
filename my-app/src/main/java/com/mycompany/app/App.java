package com.mycompany.app;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


public class App {
    public static void main(String[] args) throws IOException, NotFoundException {

        String qrcodePath = "cnh-demo.png";
        String qrcodeContentPath = "teste.bin";
        
        InputStream is = App.class.getClassLoader().getResourceAsStream(qrcodePath);
        if (is == null) {
             throw new FileNotFoundException("Arquivo não encontrado!");
        }
        byte[] imageQRCode = is.readAllBytes();

        ByteArrayInputStream imageIS = new ByteArrayInputStream(imageQRCode);
        final BufferedImage bufferedImage = ImageIO.read(imageIS);
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

        //Sobre HINTs https://zxing.github.io/zxing/apidocs/com/google/zxing/DecodeHintType.html
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, Charset.forName("ISO-8859-1"));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        ArrayList<BarcodeFormat> formats = new ArrayList<BarcodeFormat>(1);
        formats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

        // DECODE https://zxing.github.io/zxing/apidocs/com/google/zxing/MultiFormatReader.html
        MultiFormatReader qrCodeReader = new MultiFormatReader();
        qrCodeReader.setHints(hints);
        // Result https://zxing.github.io/zxing/apidocs/com/google/zxing/Result.html
        Result result = qrCodeReader.decodeWithState(bitmap);

        //Pegar os bytes do Result
        byte[] raw = result.getText().getBytes(Charset.forName("ISO-8859-1"));

        Files.write(Paths.get(qrcodeContentPath), raw);
    
    }
}
