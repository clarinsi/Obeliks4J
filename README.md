Obeliks4J
===

Sentence splitting & tokenization
===

Linux
---

Compile (execute from `Obeliks4J`)
```
javac src/main/java/org/obeliks/*.java -d target/classes && cp src/main/resources/* target/classes
```

Tokenize texts 
```
java -cp target/classes org.obeliks.Tokenizer "To je stavek." "Tudi to je stavek."
```
or
```
echo -e "To je stavek.\nTudi to je stavek." | java -cp target/classes org.obeliks.Tokenizer
```

Tokenize to output file
```
java -cp target/classes org.obeliks.Tokenizer "To je stavek." "Tudi to je stavek." -o output.txt
```
or
```
echo -e "To je stavek.\nTudi to je stavek." | java -cp target/classes org.obeliks.Tokenizer > output.txt
```

Tokenize from files 
```
java -cp target/classes org.obeliks.Tokenizer -if text*.txt
```
or
```
cat text*.txt | java -cp target/classes org.obeliks.Tokenizer
```
or
```
ls -A1 text*.txt | java -cp target/classes org.obeliks.Tokenizer -sif
```

Windows
---

Compile (execute from `Obeliks4J`)
```
javac src\main\java\org\obeliks\*.java -d target\classes
copy src\main\resources\*.* target\classes
```

Tokenize texts 
```
java -cp target\classes org.obeliks.Tokenizer "To je stavek." "Tudi to je stavek."
```

Tokenize to output file
```
java -cp target\classes org.obeliks.Tokenizer "To je stavek." "Tudi to je stavek." -o output.txt
```

Tokenize from files 
```
java -cp target\classes org.obeliks.Tokenizer -if text1.txt text2.txt text3.txt
```
or
```
java -cp target\classes org.obeliks.Tokenizer -if text*.txt
```