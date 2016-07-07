package org.obeliks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

public class Tokenizer
{
    public static void main(String [] args) {
        Hashtable<String, ArrayList<String>> params = new Hashtable<String, ArrayList<String>>();
        ArrayList<String> texts = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                String val = "";
                if (i + 1 < args.length) {
                    val = args[i + 1];
                    i++;
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
        boolean stdInFileNames = false;
        if (params.containsKey("if")) {
            for (String pattern : params.get("if")) {
                if (pattern.equals("")) {
                    stdInFileNames = true;
                } else {
                    System.out.println("Process files " + pattern);
                    readFromStdIn = false;
                }
            }
        }
        if (readFromStdIn) {
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String inputStr;
            try {
                while ((inputStr = bufReader.readLine()) != null) {
                    if (stdInFileNames) {
                        System.out.println("Process files " + inputStr);
                    } else {
                        System.out.println("Process text " + inputStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //System.out.println(Rules.tokenize("To je stavek miha@sowalabs.com."));
    }
}
