import java.util.ArrayList;
import java.util.List;

public class PrettyPrintVisitor extends liteQLBaseVisitor<String> {

    @Override public String visitDeleteTable(liteQLParser.DeleteTableContext ctx) { 
        //REMOVE TABLE tablename=ID ';';
        return "DROP TABLE IF EXISTS " + ctx.tablename.getText() + ";"; 
    }
	
	@Override public String visitDeleteRow(liteQLParser.DeleteRowContext ctx) { 
        //REMOVE tableSource whereClause ';';
        return "DELETE FROM " + visit(ctx.tableSource()) + " " + visit(ctx.whereClause()) + ";";
     }

	@Override public String visitInsert(liteQLParser.InsertContext ctx) { 
        //insert: ADD tableSource assignList  ';';
        return "INSERT INTO " + visit(ctx.tableSource()) + " ";
     }

	@Override public String visitUpdateRow(liteQLParser.UpdateRowContext ctx) { return visitChildren(ctx); }

	@Override public String visitSelect(liteQLParser.SelectContext ctx) { return visitChildren(ctx); }

	@Override public String visitCreateTable(liteQLParser.CreateTableContext ctx) { return visitChildren(ctx); }

    
}