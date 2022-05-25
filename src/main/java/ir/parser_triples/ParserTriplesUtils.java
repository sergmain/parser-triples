package ir.parser_triples;

import opennlp.tools.parser.HeadRules;
import opennlp.tools.parser.Parse;

import java.io.*;
import java.util.function.Supplier;

/**
 * @author Serge
 * Date: 5/24/2022
 * Time: 4:41 PM
 */
public class ParserTriplesUtils {

    public static void parse(HeadRules rules, boolean useFunctionTag, boolean fixPossesives, InputStream is, Supplier<ParseKnowledgeNpVisited> parseKnowledgeNpVisitedFunc) throws IOException {
        Parse.useFunctionTags(useFunctionTag);

        int ki = 1;
        try (InputStreamReader isr = new InputStreamReader(is); BufferedReader in = new BufferedReader(isr)) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                System.out.println("start parsing sentense #"+(ki++));
                Parse p = Parse.parseParse(line, null);
                ParseKnowledgeNpVisited pKnowledge = parseKnowledgeNpVisitedFunc.get();
                Parse.pruneParse(p);
                if (fixPossesives) {
                    Parse.fixPossesives(p);
                }
                if (rules!=null) {
                     p.updateHeads(rules);
                     p.show();
                }
                System.out.println(p.getCoveredText());
                pKnowledge.showCodeTreeKnowledge(p);
            }
        }
    }

    public static HeadRules loadRules(String ruleFile) throws IOException {
        if (ruleFile==null) {
            return null;
        }
        try (InputStream is = new FileInputStream(ruleFile);) {
            return loadRules(is);
        }
    }

    public static HeadRules loadRules(InputStream is) throws IOException {
        if (is==null) {
            return null;
        }
        try (InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)) {
            return new opennlp.tools.parser.lang.en.HeadRules(br);
        }
    }
}
