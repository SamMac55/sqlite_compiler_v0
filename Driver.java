import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Driver {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromStream(System.in);

        liteQLLexer lexer = new liteQLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        liteQLParser parser = new liteQLParser(tokens);

        // use program rule
        ParseTree tree = parser.program();

        PrettyPrintVisitor visitor = new PrettyPrintVisitor();
        String result = visitor.visit(tree);

        System.out.println(result);
    }
}
