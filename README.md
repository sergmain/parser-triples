# parser-triples
[![DOI](https://zenodo.org/badge/doi/10.5061/dryad.s7j17qp.svg)](https://doi.org/10.5061/dryad.s7j17qp)

Shaun D’Souza. Parser extraction of triples in unstructured text. arXiv preprint arXiv:1811.05768, 2018. url: https://arxiv.org/abs/1811.05768

* Steps to compile and run jar

* System requirements
	* Install Java JDK
	* Add jdk\bin to Windows PATH

* Compile source files to generate [opennlp-parser-svo-new.jar](/opennlp-parser-svo.jar)

```
javac -cp opennlp-tools-1.6.0.jar ParseKnowledgeNpVisited.java ParseKnowledgeNpVisitedMap.java -d .
jar cvf opennlp-parser-svo-new.jar ./opennlp
```

* Unstructured text can be parsed as per the Apache OpenNLP developer guide [Chapter 8. Parser] (https://opennlp.apache.org/docs/1.6.0/manual/opennlp.html#tools.parser)
	* Download [en-parser-chunking.bin](http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin)
	* A sample parsed file is uploaded in [ie-parser.txt](/ie-parser.txt)
```
java -cp opennlp-tools-1.6.0.jar opennlp.tools.cmdline.CLI Parser en-parser-chunking.bin < input.txt > output-parser.txt
```

* SVO Triples are extracted using the command
	* Unix shell uses (:) as the path separator 
```
java -cp opennlp-parser-svo-new.jar;opennlp-tools-1.6.0.jar opennlp.tools.parser.ParseKnowledgeNpVisitedMap -fun -pos head_rules < ie-parser.txt
```

* Expected output

```
Google is located in Mountain view
0       "Google"        "is located"    "in Mountain view"
Mountain view is in California
1       "Mountain view" "is"    "in California"
Google will acquire YouTube , announced the New York Times .
2       "Google"        "will acquire"  "YouTube"
2       "Google"        "announced"     "the New York Times"
Google and Apple are headquartered in California .
3       "Google and Apple"      "are headquartered"     "in California"
```

* Alternate code command

```
java -cp opennlp-parser-svo-new.jar;opennlp-tools-1.6.0.jar opennlp.tools.parser.ParseKnowledgeNpVisited -fun -pos head_rules < ie-parser.txt
```
