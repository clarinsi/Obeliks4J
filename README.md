Obeliks4J
===

Linux
---

Compile
```
[...]/Obeliks4J$ javac src/main/java/org/obeliks/*.java -d target/classes && cp src/main/resources/* target/classes
```

Run
```
[...]/Obeliks4J$ java -cp target/classes org.obeliks.Tokenizer TODO: arguments
```

Windows
---

Compile
```
[...]\Obeliks4J>javac src\main\java\org\obeliks\*.java -d target\classes
[...]\Obeliks4J>copy src\main\resources\*.* target\classes
```

Run
```
[...]\Obeliks4J>java -cp target\classes org.obeliks.Tokenizer TODO: arguments
```