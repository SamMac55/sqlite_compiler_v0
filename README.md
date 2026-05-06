# Scripted Language for SQLite + Compiler

## Overview
In v0 of the scripted language + compiler, the goal was to create a high-level grammar that could be translated into SQL statements that are compatible specifically with SQLite. The grammar, liteQL.g4 allows for SQL select statements including single joins and the different clauses (group, order, where, having, limit). Further, it allows for the creation/deletion of tables and inserting/updating/deleting rows from tables.  v0 **does not** handle semantic processing, but includes some code that prepares to allow for semantics. This program only uses syntax-driven translation by taking in scripted statements and "pretty printing" them into SQL statements.

## How to build and run
- Ensure you download [antlr4](https://www.antlr.org/)
- Assuming you downloaded correctly (3 jar files) get the class path to antlr4 and initialize it into CP using:  
```CP=$(grep '^CLASSPATH=' "$(which antlr4)" | cut -d= -f2-):.```
- Generate the antlr-related files using antlr4 -no-listener -visitor liteQL.g4
- Compile the java files related to Driver using:
``` javac -cp $CP *.java```
- Run + give an appropriate input/output file name
``` java -cp $CP Driver < io/INPUTFILENAME.txt > OUTPUTFILENAME.txt```
- See the results in the output file!

## The grammar
In this version, the main grammar is liteQL.g4. The grammar contains rules for the different statements, clauses, and all keywords. The goal of the grammar is to make the flow of the statement similar to how someone would describe a query verbally. The main differences between the language and typical SQL is the ordering of the SELECT statement, some keywords, and the way that new tables are created.  
## The code 
The heart of the code in this version is in PrettyPrintVisitor.java. This visitor uses syntax-driven translation to construct the different statements and returns SQL Strings that are emitted to stdout.  
Select statements required more work, so a couple of intermediate representations were made to accommodate for this. The apporach itself is naive and not scalable/ sustainable, so this was changed in [v1](https://github.com/SamMac55/sqlite_compiler_v1/).  
## Next steps: [v1 semantic processing](https://github.com/SamMac55/sqlite_compiler_v1/)
The next steps for the compiler is to allow for semantic processing to take place and ensure that each statement "makes sense". This version includes some non-implemented toy-versions of this as seen in schema_grammar.g4 and liteQL.g4 (delcare block). 
