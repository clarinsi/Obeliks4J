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
echo "To je stavek." "Tudi to je stavek." | java -cp target/classes org.obeliks.Tokenizer
```

Tokenize texts from files 
```
java -cp target\classes org.obeliks.Tokenizer -if text1.txt text2.txt text3.txt
```
or
```
cat text*.txt | java -cp target\classes org.obeliks.Tokenizer
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

Tokenize texts from files 
```
java -cp target\classes org.obeliks.Tokenizer -if text1.txt text2.txt text3.txt
```
or
```
java -cp target\classes org.obeliks.Tokenizer -if text*.txt
```