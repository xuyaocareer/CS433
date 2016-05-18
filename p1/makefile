JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
    
CLASSES = \
	driver.java \
        dict.java \
        term.java \
        Stemmer.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm -f *.class