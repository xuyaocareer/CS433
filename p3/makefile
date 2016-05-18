JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	dict_part1.java \
	dict_part2.java \
        posting.java \
	Drive.java \
	similarity.java \
	Stemmer.java \
        term_part1.java \
	term_part2.java  

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm -f *.class