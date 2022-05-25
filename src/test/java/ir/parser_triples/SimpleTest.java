package ir.parser_triples;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.*;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Serge
 * Date: 5/24/2022
 * Time: 4:36 PM
 */
public class SimpleTest {

    @Test
    public void testParserSet() throws IOException {
        try (InputStream is = SimpleTest.class.getResourceAsStream("/ie-parser.txt")) {
            assertNotNull(is);
            ParserTriplesUtils.parse(null, true, true, is, ParseKnowledgeNpVisitedSet::new);
        }
    }

    @Test
    public void testParserMap() throws IOException {
        try (InputStream is = SimpleTest.class.getResourceAsStream("/ie-parser.txt")) {
            assertNotNull(is);
            ParserTriplesUtils.parse(null, true, true, is, ParseKnowledgeNpVisitedMap::new);
        }
    }

    @Test
    public void testParserMapWithRules() throws IOException {
        HeadRules rules;
        try (InputStream is = SimpleTest.class.getResourceAsStream("/head_rules.txt")) {
            rules = ParserTriplesUtils.loadRules(is);
        }

        try (InputStream is = SimpleTest.class.getResourceAsStream("/ie-parser.txt")) {
            assertNotNull(is);
            ParserTriplesUtils.parse(rules, true, true, is, ParseKnowledgeNpVisitedMap::new);
        }
    }

    @Test
    public void testParserMap_sentence_1() throws IOException {
        ByteArrayInputStream bais = prepareSentence();
        ParserTriplesUtils.parse(null, true, true, bais, ParseKnowledgeNpVisitedMap::new);
    }

    @Test
    public void testParserMap_sentence_1_1() throws IOException {
        ByteArrayInputStream bais = prepareSentence();
        ParserTriplesUtils.parse(null, false, false, bais, ParseKnowledgeNpVisitedMap::new);
    }

    @Test
    public void testParserMapWithRules_sentence_2() throws IOException {
        ByteArrayInputStream bais = prepareSentence();
        HeadRules rules;
        try (InputStream is = SimpleTest.class.getResourceAsStream("/head_rules.txt")) {
            rules = ParserTriplesUtils.loadRules(is);
        }

        ParserTriplesUtils.parse(rules, true, true, bais, ParseKnowledgeNpVisitedMap::new);
    }

    private static ParserModel loadParderModel() throws IOException {
        try (InputStream is = SimpleTest.class.getResourceAsStream("/en-parser-chunking.bin");) {
            assertNotNull(is);
            ParserModel model = new ParserModel(is);
            return model;
        }
    }

    private static ByteArrayInputStream prepareSentence() throws IOException {
        ParserModel parserModel = loadParderModel();
        Parser parser = ParserFactory.create(parserModel);


        String text = "The quick brown fox jumps over the lazy dog.";

        Parse[] topParses = ParserTool.parseLine(text, parser, 1);
        assertEquals(1, topParses.length);
        // Displays this parse using Penn Treebank-style formatting.
        StringBuffer sb = new StringBuffer();
        topParses[0].show(sb);
        String prResult = sb.toString();
        System.out.println(prResult);

        ByteArrayInputStream bais = new ByteArrayInputStream(prResult.getBytes());
        return bais;
    }

}
