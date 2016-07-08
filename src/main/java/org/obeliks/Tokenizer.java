package org.obeliks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

public class Tokenizer
{
    private static String locate(String path, String[] searchPattern) {
        path = path.trim();
        if (path.isEmpty()) { return null; }
        if (!path.contains(File.separator)) { path = "." + File.separator + path; }
        // is it a folder?
        if (new File(path).isDirectory()) { searchPattern[0] = "*.*"; return path; }
        // must be a search pattern
        int splitAt = path.lastIndexOf(File.separator);
        searchPattern[0] = path.substring(splitAt + 1);
        path = path.substring(0, splitAt);
        if (new File(path).isDirectory()) { return path; }
        return null;
    }

    private static void processFiles(String path) {
        System.out.println("Process files " + path);
        String[] searchPattern = new String[1];
        path = locate(path, searchPattern);
        //System.out.println("Path: " + path + " ; Pattern: " + searchPattern[0]);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern[0]);
        if (path == null) {
            System.err.println("Warning: Option -if used without an argument.");
            return;
        }
        File[] files = new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return matcher.matches(Paths.get(name));
            }
        });
        System.out.println(files.length);
        for (File file : files) {
            System.out.println(file.toString());
        }
    }

    public static void main(String [] args) {
        Hashtable<String, ArrayList<String>> params = new Hashtable<String, ArrayList<String>>();
        ArrayList<String> texts = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                String val = "";
                if (i + 1 < args.length && !arg.equals("sif")) {
                    val = args[i++ + 1];
                }
                if (params.containsKey(arg)) {
                    params.get(arg).add(val);
                } else {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(val);
                    params.put(arg, list);
                }
            } else {
                texts.add(arg);
            }
        }
        boolean readFromStdIn = true;
        for (String text : texts) {
            System.out.println("Process text " + text);
            readFromStdIn = false;
        }
        if (params.containsKey("if")) {
            for (String pattern : params.get("if")) {
                processFiles(pattern);
                readFromStdIn = false;
            }
        }
        if (readFromStdIn) {
            boolean readFileNames = params.containsKey("sif");
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String inputStr;
            try {
                while ((inputStr = bufReader.readLine()) != null) {
                    if (readFileNames) {
                        processFiles(inputStr);
                    } else {
                        System.out.println("Process text " + inputStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(Rules.tokenize("To je stavek miha@sowalabs.com."));
    }
}
