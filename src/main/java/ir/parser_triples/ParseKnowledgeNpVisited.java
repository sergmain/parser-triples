package ir.parser_triples;

import opennlp.tools.parser.Parse;

/**
 * @author Serge
 * Date: 5/24/2022
 * Time: 4:49 PM
 */
public interface ParseKnowledgeNpVisited {
    default void showCodeTreeKnowledge(Parse parse) {
        subjectNounPhrase(parse, 0, parse);
    }

    void subjectNounPhrase(Parse p, int levels, Parse sent);

}
