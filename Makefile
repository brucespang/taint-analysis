all: compile run

compile:
	javac -cp vendor/soot/soot-2.5.0.jar src/Taint.java
	javac test/*.java

run:
	java -cp vendor/soot/soot-2.5.0.jar:src/:test/ Taint Test
