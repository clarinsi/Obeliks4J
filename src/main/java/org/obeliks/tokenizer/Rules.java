/*==========================================================================;
 *
 *  Projekt Sporazumevanje v slovenskem jeziku:
 *    http://www.slovenscina.eu/Vsebine/Sl/Domov/Domov.aspx
 *  Project Communication in Slovene:
 *    http://www.slovenscina.eu/Vsebine/En/Domov/Domov.aspx
 *
 *  Avtorske pravice za to izdajo programske opreme ureja licenca
 *    Creative Commons Priznanje avtorstva-Nekomercialno-Brez predelav 2.5
 *  This work is licenced under the Creative Commons
 *    Attribution-NonCommercial-NoDerivs 2.5 licence
 *
 *  File:     Rules.java
 *  Original: Rules.cs (Dec-2010)
 *  Desc:     Segmentation and tokenization rules for Slovene
 *  Created:  Jul-2016
 *
 *  Authors: Miha Grcar, Simon Krek, Kaja Dobrovoljc
 *
 ***************************************************************************/

package org.obeliks.tokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class Rules
{
    private static class TokenizerRegex
    {
        public Pattern regex;
        public boolean val;
        public boolean txt;
        public String rhs;
    }

    private static ArrayList<TokenizerRegex> tokRulesPart1
        = loadRules("TokRulesPart1.txt");
    private static ArrayList<TokenizerRegex> tokRulesPart2
        = loadRules("TokRulesPart2.txt");

    private static HashSet<String> abbrvSeq
        = loadList("ListOSeq.txt");
    private static HashSet<String> abbrvSegSeq
        = loadList("ListOSegSeq.txt");
    private static HashSet<String> abbrvNoSegSeq
        = loadList("ListONoSegSeq.txt");
    private static HashSet<String> abbrvExcl
        = loadList("ListOExcl.txt");
    private static HashSet<String> abbrvExclCS
        = loadList("ListOExclCS.txt");
    private static HashSet<String> abbrvSeg
        = loadList("ListOSeg.txt");
    private static HashSet<String> abbrvAll
        = loadList("ListOAll.txt");
    private static HashSet<String> abbrvAllCS
        = loadList("ListOAllCS.txt");

    private static ArrayList<Integer> abbrvSeqLen
        = new ArrayList<Integer>();

    private static Pattern tagRegex
        = Pattern.compile("\\</?[^>]+\\>");
    private static Pattern abbrvRegex
        = Pattern.compile("<w>(\\p{L}+)</w><c>\\.</c>");
    private static Pattern endOfSentenceRegex
        = Pattern.compile("^<[wc]>[\\p{Lu}\"»“‘'0-9]$");
    private static Pattern abbrvExclRegex
        = Pattern.compile("(?<step><w>(?<word>\\p{L}+)</w><c>\\.</c>(?<tail><S/>)?)(?<ctx>(</[ps]>)|(<[wc]>.))");
    private static Pattern abbrvOtherRegex
        = Pattern.compile("(?<step><w>(?<word>\\p{L}+)</w><c>\\.</c>(?<tail><S/>)?)<[wc]>[:,;0-9\\p{Ll}]");

    static {
        HashSet<Integer> lengths = new HashSet<Integer>();
        for (String abbrv : abbrvSeq) {
            int len = 0;
            for (char ch : abbrv.toCharArray()) {
                if (ch == '.') { len++; }
            }
            lengths.add(len);
        }
        abbrvSeqLen = new ArrayList<Integer>(lengths);
        Collections.sort(abbrvSeqLen, Collections.reverseOrder());
    }

    public static String tokenize(String text) {
        String xml = execRules(text, tokRulesPart1);
        for (int len : abbrvSeqLen) {
            xml = processAbbrvSeq(xml, len);
        }
        xml = processAbbrvExcl(xml);
        xml = processAbbrvOther(xml);
        xml = execRules(xml, tokRulesPart2);
        xml = xml.replace("<!s/>", "");
        return "<text>" + xml + "</text>";
    }

    private static String processAbbrvExcl(String txt) {
        int idx = 0;
        StringBuilder s = new StringBuilder();
        Matcher m = abbrvExclRegex.matcher(txt);
        while (m.find(idx)) {
            s.append(txt.substring(idx, m.start() - idx));
            String xml;
            String word = m.group("word");
            String wordLower = word.toLowerCase();
            if (word.length() == 1 || abbrvExcl.contains(wordLower) || abbrvExclCS.contains(word)) {
                xml = "<w>" + m.group("word") + ".</w>" + m.group("tail");
                idx = m.start() + m.group("step").length();
                if (abbrvSeg.contains(wordLower) && endOfSentenceRegex.matcher(m.group("ctx")).find()) {
                    xml += "</s><s>";
                }
            } else {
                xml = m.group("step");
                idx = m.start() + xml.length();
            }
            s.append(xml);
        }
        s.append(txt.substring(idx, txt.length() - idx));
        return s.toString();
    }

    private static String processAbbrvSeq(String txt, int seqLen) {
        int idx = 0;
        StringBuilder s = new StringBuilder();
        Pattern regex = Pattern.compile("(?<jump>(?<step><w>\\p{L}+</w><c>\\.</c>(<S/>)?)(<w>\\p{L}+</w><c>\\.</c>(<S/>)?){" + (seqLen - 1) + "})(?<ctx>(</[ps]>)|(<[wc]>.))");
        Matcher m = regex.matcher(txt);
        while (m.find(idx)) {
            s.append(txt.substring(idx, m.start() - idx));
            String xml = m.group("jump");
            String abbrvLower = tagRegex.matcher(xml).replaceAll("").replace(" ", "").toLowerCase();
            if (abbrvSeq.contains(abbrvLower)) {
                idx = m.start() + xml.length();
                xml = abbrvRegex.matcher(xml).replaceAll("<w>$1.</w>");
                if (endOfSentenceRegex.matcher(m.group("ctx")).find()) {
                    if (abbrvSegSeq.contains(abbrvLower)) {
                        xml = xml + "</s><s>";
                    } else if (abbrvNoSegSeq.contains(abbrvLower)) {
                        xml += "<!s/>";
                    }
                }
            } else {
                xml = m.group("step");
                idx = m.start() + xml.length();
            }
            s.append(xml);
        }
        s.append(txt.substring(idx, txt.length() - idx));
        return s.toString();
    }

    private static HashSet<String> loadList(String name) {
        HashSet<String> set = new HashSet<String>();
        try {
            File file = new File(Rules.class.getClassLoader().getResource(name).getFile());
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String token = scanner.nextLine().trim();
                if (!token.equals("") && !token.startsWith("#")) {
                    set.add(token);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    private static String processAbbrvOther(String txt) {
        int idx = 0;
        StringBuilder s = new StringBuilder();
        Matcher m = abbrvOtherRegex.matcher(txt);
        while (m.find(idx)) {
            s.append(txt.substring(idx, m.start() - idx));
            String xml;
            String word = m.group("word");
            String wordLower = word.toLowerCase();
            if (abbrvAll.contains(wordLower) || abbrvAllCS.contains(word)) {
                xml = "<w>" + m.group("word") + ".</w>" + m.group("tail");
                idx = m.start() + m.group("step").length();
            } else {
                xml = m.group("step");
                idx = m.start() + xml.length();
            }
            s.append(xml);
        }
        s.append(txt.substring(idx, txt.length() - idx));
        return s.toString();
    }

    private static ArrayList<TokenizerRegex> loadRules(String resName) {
        Pattern splitRegex = Pattern.compile("^(?<regex>.*)((--)|(==))\\>(?<rhs>.*)$");
        ArrayList<TokenizerRegex> rules = new ArrayList<TokenizerRegex>();
        ClassLoader classLoader = Rules.class.getClassLoader();
        try {
            File file = new File(classLoader.getResource(resName).getFile());
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.startsWith("#") && !line.isEmpty()) {
                    int opt = Pattern.MULTILINE;
                    if (line.contains("-->")) { opt |= Pattern.CASE_INSENSITIVE; }
                    TokenizerRegex tknRegex = new TokenizerRegex();
                    tknRegex.val = line.contains("$val");
                    tknRegex.txt = line.contains("$txt");
                    Matcher matcher = splitRegex.matcher(line);
                    if (matcher.find()) {
                        try {
                            tknRegex.regex = Pattern.compile(matcher.group("regex").trim(), opt);
                            tknRegex.rhs = matcher.group("rhs").trim();
                            rules.add(tknRegex);
                        } catch (Exception e) {
                            System.err.println("Warning: Cannot parse line \"" + line + "\"");
                        }
                    } else {
                        System.err.println("Warning: Cannot parse line \"" + line + "\"");
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rules;
    }

    private static String execRules(String text, ArrayList<TokenizerRegex> rules) {
        for (TokenizerRegex tknRegex : rules) {
            if (!tknRegex.val && !tknRegex.txt) {
                text = tknRegex.regex.matcher(text).replaceAll(tknRegex.rhs);
            } else {
                Matcher m = tknRegex.regex.matcher(text);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String replTxt = tknRegex.rhs;
                    if (tknRegex.val) {
                        replTxt = replTxt.replace("$val", m.group());
                    }
                    if (tknRegex.txt) {
                        replTxt = replTxt.replace("$txt", tagRegex.matcher(m.group()).replaceAll(""));
                    }
                    m.appendReplacement(sb, replTxt);
                }
                m.appendTail(sb);
                text = sb.toString();
            }
        }
        return text;
    }
}
