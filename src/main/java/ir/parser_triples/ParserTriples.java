package ir.parser_triples;

import opennlp.tools.parser.HeadRules;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Serge
 * Date: 5/24/2022
 * Time: 4:36 PM
 */
public class ParserTriples {

    /**
	 * Reads training parses (one-sentence-per-line) and displays
     * ParseKnowledgeNpVisitedSet structure.
     *
     * @param args
     *            The head rules files.
     *
     * @throws IOException
     *             If the head rules file can not be opened and read.
	 */
    @Deprecated
    public static void main(String[] args) throws java.io.IOException {
        if (args.length == 0) {
            System.err.println("Usage: ParserTriples -fun -pos head_rules < train_parses");
            System.err.println("Reads training parses (one-sentence-per-line) and displays ParseKnowledgeNpVisitedSet structure.");
            System.exit(1);
        }

        boolean fixPossesives = false;
        boolean useFunctionTag = false;
        Supplier<ParseKnowledgeNpVisited> parseKnowledgeNpVisitedFunc = ParseKnowledgeNpVisitedSet::new;
        String ruleFile = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-func" -> useFunctionTag = true;
                case "-pos" -> fixPossesives = true;
                case "-map" -> parseKnowledgeNpVisitedFunc = ParseKnowledgeNpVisitedMap::new;
                case "-rule" -> ruleFile = args[i++];
            }
        }

        HeadRules rules = ParserTriplesUtils.loadRules(ruleFile);

        ParserTriplesUtils.parse(rules, useFunctionTag, fixPossesives, System.in, parseKnowledgeNpVisitedFunc);
    }

}
