package ir.parser_triples;

import opennlp.tools.parser.Parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure for holding ParseKnowledgeNpVisitedMap constituents. Updated
 * implementation of visited collection using Map. Higher precision and higher
 * recall.
 */
public class ParseKnowledgeNpVisitedMap implements ParseKnowledgeNpVisited{

	/**
	 * Read subject noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param levels
	 *            Tree depth.
	 * @param sent
	 *            Sentence root node.
	 */
	@Override
	public void subjectNounPhrase(Parse p, int levels, Parse sent) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();

		if (p.getType().equals("TK")) {
			return;
		}

		for (int li = 0; li < levels; li++) {
			levelsBuff.append("  ");
		}

		//System.out.println(levelsBuff.toString() + kids.length + " " + p.getType() + " " + p.getCoveredText());

		String subject = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().startsWith("NP")) {
				for (int j = i + 1; j < kids.length; j++) {
					if (kids[j].getType().equals("VP") || kids[j].getType().equals("PP") || kids[j].getType().equals("SBAR")) {
						Map<Parse, String> visited = new HashMap<>();
						int iter = 0;
						while (!(visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("predicate")))) {
							String predicate = predicateVerbPhrase(kids[j], sent, visited).trim();
							if (!(predicate.isEmpty() || predicate.contains("ERR"))) {
								System.out.print("\t\"" + kids[i]);
								System.out.println("\"\t\"" + predicate);
							}
						}
					} else if (!(kids[j].getType().equals(",") || kids[j].getType().equals("CC"))) {
						break;
					}
				}
			}
			subjectNounPhrase(kids[i], levels + 1, sent);
		}
	}

	private List<String> subjectNounPhrase1(Parse p, int levels, Parse sent) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();
		List<String> svo = new ArrayList<>();

		if (p.getType().equals("TK")) {
			return svo;
		}

		levelsBuff.append("  ".repeat(Math.max(0, levels)));

		//System.out.println(levelsBuff.toString() + kids.length + " " + p.getType() + " " + p.getCoveredText());

		String subject = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().startsWith("NP")) {
				for (int j = i + 1; j < kids.length; j++) {
					if (kids[j].getType().equals("VP") || kids[j].getType().equals("PP") || kids[j].getType().equals("SBAR")) {
						Map<Parse, String> visited = new HashMap<Parse, String>();
						int iter = 0;
						while (!(visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("predicate")))) {
							String predicate = predicateVerbPhrase(kids[j], sent, visited).trim();
							if (!(predicate.isEmpty() || predicate.contains("ERR"))) {
								svo.add("\"" + kids[i] + "\"\t\"" + predicate);
							}
						}
					} else if (!(kids[j].getType().equals(",") || kids[j].getType().equals("CC"))) {
						break;
					}
				}
			}
			svo.addAll(subjectNounPhrase1(kids[i], levels + 1, sent));
		}
		return svo;
	}

	/**
	 * Read predicate verb phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return predicate object string.
	 */
	private String predicateVerbPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();

		String predicate = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().equals("VP") || kids[i].getType().equals("S")) {
				if (!(visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("predicate")))) {
					return predicate.concat(" " + predicateVerbPhrase(kids[i], sent, visited).trim());
				}
			} else if (kids[i].getType().startsWith("VB") || kids[i].getType().startsWith("JJ") || kids[i].getType().startsWith("RB") || kids[i].getType().equals("MD")
					|| kids[i].getType().equals("ADVP") || kids[i].getType().equals("DT") || kids[i].getType().startsWith("NN") || kids[i].getType().equals("TO")
					|| ((predicate.length() > 0) && (kids[i].getType().equals("IN")))) {
				predicate = predicate.concat(" " + kids[i].getCoveredText());
				String object = "";

				for (int j = i + 1; j < kids.length; j++) {
					if ((kids[j].getType().startsWith("NP") || kids[j].getType().equals("PP") || kids[j].getType().equals("ADJP") || kids[j].getType().equals("S") || kids[j].getType().equals("SBAR"))) {

						if (visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("object"))) {
							object = object.concat(" " + kids[j].getCoveredText());
						} else {
							object = object.concat(" " + objectNounPhrase(kids[j], sent, visited).trim());

							if (!object.isEmpty()) {
								return predicate.concat("\"\t\"" + object.trim() + "\"");
							}
						}
					} else if (kids[j].getType().equals(",") || kids[j].getType().equals("CC")) {
						object = "";
					} else {
						break;
					}
				}
			} else if (kids[i].getType().equals(",") || kids[i].getType().equals("CC")) {
				predicate = "";
			} else if (!kids[i].getType().equals("WHNP")) {
				break;
			}
		}

		visited.put(p, "predicate object");

		return "ERR";
	}

	/**
	 * Read object noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object noun string.
	 */
	private String objectNounPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();
		boolean found = false;
		String object = "";

		for (Parse kid : kids) {
			if (kid.getType().equals("IN") || kid.getType().equals("TO")) {
				object = object.concat(" " + kid.getCoveredText());
			}
			else if ((kid.getType().startsWith("NP") || kid.getType().equals("S"))) {
				found = true;

				if (visited.containsKey(kid) && (visited.get(kid).contains("object"))) {
					object = object.concat(" " + kid.getCoveredText());
				}
				else {
					return object.concat(" " + objectNounPhrase(kid, sent, visited).trim());
				}
			}
			else if (kid.getType().equals("PP")) {
				if (visited.containsKey(kid) && (visited.get(kid).contains("object"))) {
					object = object.concat(" " + kid.getCoveredText());
				}
				else {
					return object.concat(" " + objectPrepositionPhrase(kid, sent, visited).trim());
				}
			}
			else if (kid.getType().equals(",") || kid.getType().equals("CC")) {
				object = "";
			}
			else {
				break;
			}
		}

		visited.put(p, "object");

		if (!found && p.getType().startsWith("NP")) {
			return object.concat(" " + p.getCoveredText());
		}

		return "ERR";
	}

	/**
	 * Read object trailing preposition phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object preposition string.
	 */
	private String objectPrepositionPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();
		String preposition = "";

		for (Parse kid : kids) {
			if (kid.getType().startsWith("NP") && !(visited.containsKey(kid) && (visited.get(kid).contains("object")))) {
				return preposition.concat(" " + objectNounPhrase(kid, sent, visited).trim());
			}
			if (kid.getType().equals("PP") && !(visited.containsKey(kid) && (visited.get(kid).contains("object")))) {
				return preposition.concat(" " + objectPrepositionPhrase(kid, sent, visited).trim());
			}
			else if (kid.getType().equals("IN") || kid.getType().equals("TO") || kid.getType().equals("JJ") || kid.getType().equals("ADVP")) {
				preposition = preposition.concat(" " + kid.getCoveredText());
			}
			else {
				break;
			}
		}

		visited.put(p, "object");

		return "ERR";
	}

	/**
	 * Debug code tree.
	 *
	 * @param p
	 *            Parse tree.
	 * @param levels
	 *            Tree depth.
	 */
	private static void codeTree(Parse p, int levels) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();

		if (p.getType().equals("TK")) {
			return;
		}

		levelsBuff.append("  ".repeat(Math.max(0, levels)));

		System.out.println(levelsBuff.toString() + p.getType() + " " + p.getCoveredText());

		for (Parse kid : kids) {
			codeTree(kid, levels + 1);
		}
	}

	public void showCodeTree_knowledge1(Parse parse) {
		List<String> svo = subjectNounPhrase1(parse, 0, parse);
		for (String s : svo)
			System.out.println("\t" + s);
	}
}