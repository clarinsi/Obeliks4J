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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class Rules
{
    private static class TokenizerRegex
    {
        public Pattern mRegex;
        public boolean mVal;
        public boolean mTxt;
        public String mRhs;
    }

    private static ArrayList<TokenizerRegex> mTokRulesPart1
        = LoadRules("TokRulesPart1.txt");
    private static ArrayList<TokenizerRegex> mTokRulesPart2
        = LoadRules("TokRulesPart2.txt");

    private static Pattern mTagRegex
        = Pattern.compile("\\</?[^>]+\\>");

    public static String Tokenize(String text)
    {
        String xml = ExecRules(text, mTokRulesPart1);
        //for (int len : mAbbrvSeqLen) {
        //    xml = ProcessAbbrvSeq(xml, len);
        //}
        //xml = ProcessAbbrvExcl(xml);
        //xml = ProcessAbbrvOther(xml);
        xml = ExecRules(xml, mTokRulesPart2);
        xml = xml.replace("<!s/>", "");
        return "<text>" + xml + "</text>";
    }

    private static ArrayList<TokenizerRegex> LoadRules(String resName)
    {
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
                    tknRegex.mVal = line.contains("$val");
                    tknRegex.mTxt = line.contains("$txt");
                    Matcher matcher = splitRegex.matcher(line);
                    if (matcher.find()) {
                        try {
                            tknRegex.mRegex = Pattern.compile(matcher.group("regex").trim(), opt);
                            tknRegex.mRhs = matcher.group("rhs").trim();
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

    private static String ExecRules(String text, ArrayList<TokenizerRegex> rules)
    {
        for (TokenizerRegex tknRegex : rules) {
            if (!tknRegex.mVal && !tknRegex.mTxt) {
                text = tknRegex.mRegex.matcher(text).replaceAll(tknRegex.mRhs);
            } else {
                Matcher m = tknRegex.mRegex.matcher(text);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String replTxt = tknRegex.mRhs;
                    if (tknRegex.mVal) {
                        replTxt = replTxt.replace("$val", m.group());
                    }
                    if (tknRegex.mTxt) {
                        replTxt = replTxt.replace("$txt", mTagRegex.matcher(m.group()).replaceAll(""));
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
