package ir.parser_triples;

import opennlp.tools.parser.Parse;

import java.util.HashSet;
import java.util.Set;

/**
 * Data structure for holding ParseKnowledgeNpVisitedSet constituents.
 * Implementation of IJAI algorithm
 */
public class ParseKnowledgeNpVisitedSet implements ParseKnowledgeNpVisited {

	/**
	 * Read subject noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 */
	@Override
	public void subjectNounPhrase(Parse p, int levels, Parse sent) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();

		if (p.getType().equals("TK")) {
			return;
		}

		levelsBuff.append("  ".repeat(Math.max(0, levels)));

		// System.out.println(levelsBuff.toString() + kids.length + " " +
		// p.getType() + " " + p.getCoveredText());

		String subject = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().equals("NP")) {
				for (int j = i + 1; j < kids.length; j++) {
					if (kids[j].getType().equals("VP") || kids[j].getType().equals("PP") || kids[j].getType().equals("SBAR")) {
						Set<Parse> visited = new HashSet<>();
						int iter = 0;
						while (!visited.contains(kids[j])) {
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

	/**
	 * Read predicate verb phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param visited
	 *            Visited tree nodes.
	 * @return predicate object string.
	 */
	private String predicateVerbPhrase(Parse p, Parse sent, Set<Parse> visited) {
		Parse[] kids = p.getChildren();

		String predicate = "";

		for (int i = 0; i < kids.length; i++) {
			if ((kids[i].getType().equals("VP") || kids[i].getType().equals("S"))) {
				if (!visited.contains(kids[i])) {
					return predicate.concat(" " + predicateVerbPhrase(kids[i], sent, visited).trim());
				}
			} else if (kids[i].getType().startsWith("VB") || kids[i].getType().startsWith("JJ") || kids[i].getType().startsWith("RB") || kids[i].getType().equals("MD")
					|| kids[i].getType().equals("ADVP") || kids[i].getType().equals("DT") || kids[i].getType().startsWith("NN") || kids[i].getType().equals("TO")
					|| ((predicate.length() > 0) && (kids[i].getType().equals("IN")))) {
				predicate = predicate.concat(" " + kids[i].getCoveredText());
				String object = "";

				for (int j = i + 1; j < kids.length; j++) {
					if ((kids[j].getType().equals("NP") || kids[j].getType().equals("PP") || kids[j].getType().equals("ADJP") || kids[j].getType().equals("S") || kids[j].getType().equals("SBAR"))) {
						if (!visited.contains(kids[j])) {
							object = object.concat(" " + objectNounPhrase(kids[j], sent, visited).trim());

							if (!object.isEmpty()) {
								return predicate.concat("\"\t\"" + object.trim() + "\"");
							}
						} else {
							object = object.concat(" " + kids[j].getCoveredText());
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

		visited.add(p);

		return "ERR";
	}

	/**
	 * Read object noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object noun string.
	 */
	private String objectNounPhrase(Parse p, Parse sent, Set<Parse> visited) {
		Parse[] kids = p.getChildren();
		boolean found = false;
		String object = "";

		for (Parse kid : kids) {
			if (kid.getType().equals("IN") || kid.getType().equals("TO")) {
				object = object.concat(" " + kid.getCoveredText());
			}
			else if ((kid.getType().equals("NP") || kid.getType().equals("S"))) {
				found = true;

				if (!visited.contains(kid)) {
					return object.concat(" " + objectNounPhrase(kid, sent, visited).trim());
				}
				else {
					object = object.concat(" " + kid.getCoveredText());
				}
			}
			else if (kid.getType().equals("PP")) {
				if (!visited.contains(kid)) {
					return object.concat(" " + objectPrepositionPhrase(kid, sent, visited).trim());
				}
				else {
					object = object.concat(" " + kid.getCoveredText());
				}
			}
			else if (kid.getType().equals(",") || kid.getType().equals("CC")) {
				object = "";
			}
			else {
				break;
			}
		}

		visited.add(p);

		if (!found && p.getType().equals("NP")) {
			return object.concat(" " + p.getCoveredText());
		}

		return "ERR";
	}

	/**
	 * Read object trailing preposition phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object preposition string.
	 */
	private String objectPrepositionPhrase(Parse p, Parse sent, Set<Parse> visited) {
		Parse[] kids = p.getChildren();
		String preposition = "";

		for (Parse kid : kids) {
			if (kid.getType().equals("NP") && !visited.contains(kid)) {
				return preposition.concat(" " + objectNounPhrase(kid, sent, visited).trim());
			}
			if (kid.getType().equals("PP") && !visited.contains(kid)) {
				return preposition.concat(" " + objectPrepositionPhrase(kid, sent, visited).trim());
			}
			else if (kid.getType().equals("IN") || kid.getType().equals("TO") || kid.getType().equals("JJ") || kid.getType().equals("ADVP")) {
				preposition = preposition.concat(" " + kid.getCoveredText());
			}
			else {
				break;
			}
		}

		visited.add(p);

		return "ERR";
	}

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

	/**
	 * Show code tree knowledge.
	 *
	 * @param parse Parse tree.
	 */
	public void showCodeTreeKnowledge(Parse parse) {
		subjectNounPhrase(parse, 0, parse);
	}

}