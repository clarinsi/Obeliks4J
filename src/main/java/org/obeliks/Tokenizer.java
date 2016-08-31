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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer
{
    private static void processFile(String fileName, int[] np, OutputStream os) throws Exception {
        String contents = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        processText(contents, np, os);
    }

    private static Pattern ampPattern
        = Pattern.compile("&(amp;)?");
    private static Pattern ltPattern
        = Pattern.compile("<|(&lt;)");
    private static Pattern gtPattern
        = Pattern.compile(">|(&gt;)");

    private static int indexOf(String str, String substr, int fromIdx, String[] val) {
        val[0] = "";
        Pattern pattern = null;
        if (substr.equals("&amp;")) {
            pattern = ampPattern;
        } else if (substr.equals("&lt;")) {
            pattern = ltPattern;
        } else if (substr.equals("&gt;")) {
            pattern = gtPattern;
        }
        if (pattern != null) {
            Matcher m = pattern.matcher(str);
            if (m.find(fromIdx)) {
                val[0] = m.group();
                return m.start();
            } else {
                return -1;
            }
        }
        val[0] = substr;
        return str.indexOf(substr, fromIdx);
    }

    private static void processPara(String para, int startIdx, int np, OutputStream os) throws Exception {
        String tokensXml = Rules.tokenize(para);
        Pattern token = Pattern.compile("<s>|<[wc]>([^<]+)</[wc]>");
        Matcher m = token.matcher(tokensXml);
        int idx = 0;
        int ns = 1, nt = 0;
        while (m.find()) {
            String val = m.group();
            if (val.equals("<s>")) {
                ns++; nt = 0;
            } else {
                val = m.group(1);
                String[] actualVal = new String[1];
                int idxOfToken = indexOf(para, val, idx, actualVal);
                if (idxOfToken == -1) {
                    System.err.println("Warning: Cannot compute token index.");
                }
                idx = Math.max(idx, idxOfToken + actualVal[0].length());
                idxOfToken += /*startIdx +*/ 1;
                String line = np + "." + ns + "." + (++nt) + "." + idxOfToken + "-" + (idxOfToken + actualVal[0].length() - 1) + "\t" + actualVal[0] + System.lineSeparator();
                os.write(line.getBytes(Charset.forName("UTF-8")));
            }
        }
    }

    private static void processText(String text, int[] np, OutputStream os) throws Exception {
        Pattern para = Pattern.compile("[^\\n]+", Pattern.MULTILINE);
        Matcher m = para.matcher(text);
        while (m.find()) {
            processPara(m.group(), m.start(), ++np[0], os);
        }
    }

    public static void main(String[] args) {
        try {
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
            int[] np = new int[1];
            OutputStream os = System.out;
            if (params.containsKey("o")) { // output file name
                String fileName = params.get("o").get(0);
                os = new FileOutputStream(fileName);
            }
            for (String text : texts) {
                processText(text, np, os);
                readFromStdIn = false;
            }
            if (params.containsKey("if")) {
                for (String fileName : params.get("if")) {
                    processFile(fileName, np, os);
                    readFromStdIn = false;
                }
            }
            if (readFromStdIn) {
                boolean readFileNames = params.containsKey("sif");
                InputStreamReader isReader = new InputStreamReader(System.in);
                BufferedReader bufReader = new BufferedReader(isReader);
                String inputStr;
                while ((inputStr = bufReader.readLine()) != null) {
                    if (readFileNames) {
                        processFile(inputStr, np, os);
                    } else {
                        processText(inputStr, np, os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
