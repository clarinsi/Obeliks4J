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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
    private static void processFile(String fileName, int[] np, OutputStream os, Document teiDoc) throws Exception {
        String contents = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        processText(contents, np, os, teiDoc);
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

    private static Document CreateTeiDoc() throws Exception {
        Document teiDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = teiDoc.createElement("TEI");
        root.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
        root.setAttribute("xml:lang", "sl");
        root.appendChild(teiDoc.createElement("text"));
        teiDoc.appendChild(root);
        return teiDoc;
    }

    private static void processPara(String para, int startIdx, int np, OutputStream os, Document teiDoc) throws Exception {
        if (para.startsWith("\uFEFF")) { para = para.substring(1); }
        Element node;
        String tokensXml = Rules.tokenize(para);
        Node parentNode = null;
        if (teiDoc != null) {
            parentNode = teiDoc.getElementsByTagName("text").item(0);
            node = teiDoc.createElement("p");
            parentNode = parentNode.appendChild(node);
            node.setAttribute("xml:id", "" + np);
        }
        Pattern token = Pattern.compile("<S/>|</?s>|<([wc])>([^<]+)</[wc]>");
        Matcher m = token.matcher(tokensXml);
        int idx = 0;
        int ns = 1, nt = 0;
        int oldNs = 1;
        boolean hasOutput = false;
        while (m.find()) {
            String val = m.group();
            if (val.equals("<s>")) {
                nt = 0;
                if (teiDoc != null) {
                    node = teiDoc.createElement("s");
                    node.setAttribute("xml:id", np + "." + ns);
                    parentNode.appendChild(node);
                    parentNode = parentNode.getLastChild();
                }
            } else if (val.equals("</s>")) {
                if (teiDoc != null) {
                    parentNode = parentNode.getParentNode();
                }
                ns++;
            } else if (val.equals("<S/>")) {
                if (teiDoc != null) {
                    node = teiDoc.createElement("c");
                    node.setTextContent(" ");
                    parentNode.appendChild(node);
                }
            } else {
                val = m.group(2);
                String[] actualVal = new String[1];
                int idxOfToken = indexOf(para, val, idx, actualVal);
                if (idxOfToken == -1) {
                    System.err.println("Warning: Cannot compute token index.");
                }
                idx = Math.max(idx, idxOfToken + actualVal[0].length());
                idxOfToken++;
                nt++;
                String line = np + "." + ns + "." + nt + "." + idxOfToken + "-" + (idxOfToken + actualVal[0].length() - 1) + "\t" + actualVal[0] + System.lineSeparator();
                String tagName = m.group(1).equals("c") ? "pc" : "w";
                if (teiDoc != null) {
                    node = teiDoc.createElement(tagName);
                    parentNode.appendChild(node);
                    node.setTextContent(actualVal[0]);
                    node.setAttribute("xml:id", np + "." + ns + ".t" + nt);
                } else {
                    if (ns != oldNs) {
                        os.write(System.lineSeparator().getBytes(Charset.forName("UTF-8")));
                        oldNs = ns;
                    }
                    os.write(line.getBytes(Charset.forName("UTF-8")));
                    hasOutput = true;
                }
            }
        }
        if (teiDoc == null && hasOutput) {
            os.write(System.lineSeparator().getBytes(Charset.forName("UTF-8")));
        }
    }

    private static void processText(String text, int[] np, OutputStream os, Document teiDoc) throws Exception {
        Pattern para = Pattern.compile("[^\\n]+", Pattern.MULTILINE);
        Matcher m = para.matcher(text);
        while (m.find()) {
            processPara(m.group(), m.start(), ++np[0], os, teiDoc);
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
                    if (!arg.equals("tei") && !arg.equals("sif") && !arg.equals("-")) {
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
            Document teiDoc = params.containsKey("tei") ? CreateTeiDoc() : null;
            boolean readFromStdIn = true;
            int[] np = new int[1];
            OutputStream os = System.out;
            if (params.containsKey("o")) { // output file name
                String fileName = params.get("o").get(0);
                os = new FileOutputStream(fileName);
            }
            for (String text : texts) {
                processText(text, np, os, teiDoc);
                readFromStdIn = false;
            }
            if (params.containsKey("if")) {
                for (String fileName : params.get("if")) {
                    processFile(fileName, np, os, teiDoc);
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
                        processFile(inputStr, np, os, teiDoc);
                    } else {
                        processText(inputStr, np, os, teiDoc);
                    }
                }
            }
            if (teiDoc != null) {
                StreamResult consoleResult = new StreamResult(os);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(teiDoc);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, consoleResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
