/*==========================================================================;
 *
 *  Projekt Sporazumevanje v slovenskem jeziku:
 *    http://www.slovenscina.eu/Vsebine/Sl/Domov/Domov.aspx
 *  Project Communication in Slovene:
 *    http://www.slovenscina.eu/Vsebine/En/Domov/Domov.aspx
 *
 *  Avtorske pravice za to izdajo programske opreme ureja licenca MIT
 *  This work is licenced under The MIT License
 *    http://opensource.org/licenses/MIT
 *
 *  File:     Tokenizer.java
 *  Desc:     Executes segmentation and tokenization from command line
 *  Created:  Jul-2016
 *
 *  Authors:  Miha Grcar
 *
 ***************************************************************************/

package org.obeliks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

public class Tokenizer
{
    private static void processFile(String fileName) {
        try {
            String contents = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            processText(contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processText(String text) {
        System.out.println(Rules.tokenize(text));
    }

    public static void main(String[] args) {
        Hashtable<String, ArrayList<String>> params = new Hashtable<String, ArrayList<String>>();
        ArrayList<String> texts = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                ArrayList<String> vals = new ArrayList<String>();
                if (!arg.equals("sif") && !arg.equals("-")) {
                    int j = i + 1;
                    while (j < args.length && !args[j].startsWith("-")) {
                        vals.add(args[j++]);
                    }
                    i = j - 1;
                }
                if (params.containsKey(arg)) {
                    params.get(arg).addAll(vals);
                } else {
                    params.put(arg, vals);
                }
            } else {
                texts.add(arg);
            }
        }
        boolean readFromStdIn = true;
        for (String text : texts) {
            processText(text);
            readFromStdIn = false;
        }
        if (params.containsKey("if")) {
            for (String fileName : params.get("if")) {
                processFile(fileName);
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
                        processFile(inputStr);
                    } else {
                        processText(inputStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
