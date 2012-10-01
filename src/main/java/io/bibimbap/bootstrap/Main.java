package io.bibimbap.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;

public class Main {
    private static String home    = System.getProperty("user.home");
    private static String sep     = System.getProperty("file.separator");
    private static String bibHome = home + sep + ".bibimbap";
    private static String bibInst = bibHome + sep + "install";
    private static String bibJar  = bibInst + sep + "bibimbap.jar";
    private static File   bibFile = new File(bibJar);

    public static void main(String[] args) throws Exception {
        try {
            String localMD5 = getLocalMD5();
            if(localMD5 == null) {
                // The file does not exist or something along those lines.
                System.out.println("bibimbap jar not found. Downloading latest...");
                File dl = downloadLatest();
                (new File(bibInst)).mkdirs();
                moveOrCopy(dl, bibFile);
                System.out.println("Installation complete.");
            } else {
                String remoteMD5 = getRemoteMD5();
                if(!localMD5.equals(remoteMD5)) {
                    System.out.println("New version found. Downloading latest...");
                    File dl = downloadLatest();
                    moveOrCopy(dl, bibFile);
                    System.out.println("Upgrade complete.");
                }
            }

            boot(args);

        } catch (Exception e) {
            System.err.println("An error occurred : " + e.getLocalizedMessage());
            throw(e);
        }
    }

    private static void boot(String[] args) throws Exception {
        URLClassLoader cl = URLClassLoader.newInstance(
            new URL[] { bibFile.toURI().toURL() },
            Main.class.getClassLoader());

        Class<?>  clazz = Class.forName("bibimbap.Main", true, cl);
        Method method   = clazz.getDeclaredMethod("boot", String[].class, ClassLoader.class);
        // Object instance = clazz.newInstance();
        Object result   = method.invoke(null, (Object)(args), (Object)cl);
    }

    // MD5 of locally installed bibimbap.jar.
    private static String getLocalMD5() throws Exception {
        if(bibFile.exists()) {
            return md5(bibFile);
        } else {
            return null;
        }
    }

    // Subject to change if we ever return it as JSON or whatever.
    private static String getRemoteMD5() throws Exception {
        String response = getText("http://bibimbap.io/files/bibimbap-latest.jar.md5");
        String md5     = response.substring(0,32).toLowerCase();
        if(md5.length() != 32) {
            throw new Exception("Retrieved MD5 is too short : \"" + md5 + "\"");
        }
        for(int i = 0; i < md5.length(); i++) {
            char c = md5.charAt(i);
            if(!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'f')) {
                throw new Exception("Invalid MD5 string : \"" + md5 + "\"");
            }
        }
        return md5;
    }

    private static File downloadLatest() throws Exception {
        File tmp = File.createTempFile("bibimbap-", ".jar");
        downloadFile("http://bibimbap.io/files/bibimbap-latest.jar", tmp);
        return tmp;
    }

    // Reinventing one wheel
    private static String getText(String url) throws Exception {
        URLConnection c = (new URL(url)).openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream())); 
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    // Reinventing another wheel.
    private static void downloadFile(String url, File dest) throws Exception {
        ReadableByteChannel rbc = Channels.newChannel((new URL(url)).openStream());
        FileOutputStream fos = new FileOutputStream(dest);
        fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
    }

    // Reinventing yet another wheel.
    private static String md5(File file) throws Exception {
        FileInputStream fis =  new FileInputStream(file);
        byte[] bytes = new byte[1024];
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int read = 0;

        do {
            read = fis.read(bytes);
            if(read > 0) {
                digest.update(bytes, 0, read);
            }
        } while (read != -1);

        fis.close();

        StringBuilder sb = new StringBuilder();

        for(byte b : digest.digest()) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    // How many wheels already?
    private static void moveOrCopy(File src, File dst) throws Exception {
        if(src.renameTo(dst)) return;
        if(!dst.exists()) {
            dst.createNewFile();
        }
        FileChannel srcChan = new FileInputStream(src).getChannel();
        FileChannel dstChan = new FileOutputStream(dst).getChannel();
        long count = 0;
        long size  = srcChan.size();

        while((count += dstChan.transferFrom(srcChan, count, size - count)) < size) {
        }

        if(srcChan != null) {
            srcChan.close();
        }

        if(dstChan != null) {
            dstChan.close();
        }
    }
}
