JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	dict.java \
        posting.java \
	queryDrive.java \
	similarity.java \
	Stemmer.java \
        term.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm -f *.class