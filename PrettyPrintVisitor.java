import java.util.ArrayList;
import java.util.List;

public class PrettyPrintVisitor extends liteQLBaseVisitor<String> {
    @Override
    public String visitProgram(liteQLParser.ProgramContext ctx){
        StringBuilder sb = new StringBuilder();
        for (liteQLParser.StmtContext stmt : ctx.stmt()) {
            sb.append(visit(stmt)).append("\n\n");
        }
        return sb.toString();
    }

    @Override public String visitDeleteTable(liteQLParser.DeleteTableContext ctx) { 
        //REMOVE TABLE tablename=ID ';';
        return "DROP TABLE IF EXISTS " + ctx.tablename.getText() + ";"; 
    }
	
	@Override public String visitDeleteRow(liteQLParser.DeleteRowContext ctx) { 
        //REMOVE tableSource whereClause ';';
        return "DELETE FROM " + ctx.tableSource().getText() + " " + visit(ctx.whereClause()) + ";";
     }

	@Override public String visitInsert(liteQLParser.InsertContext ctx) { 
        //insert: ADD tableSource assignList  ';';
        AssignListExtractor ir = splitAssignList(ctx.assignList().getText());
        return "INSERT INTO " + ctx.tableSource().getText() + "(" + String.join(",", ir.labels) 
        + ") VALUES (" + String.join(",",ir.values) +  ");";
     }

	@Override public String visitUpdateRow(liteQLParser.UpdateRowContext ctx) { 
        return "UPDATE " + ctx.tableSource().getText() + " SET " + ctx.assignList().getText() + " " + visit(ctx.whereClause()) + ";"; 
     }

	@Override public String visitSelect(liteQLParser.SelectContext ctx) { 
        if(ctx.joinClause()!=null){
            return createJoinSelect(ctx);
        }
        return createSelectStmt(ctx);
     }

	@Override public String visitCreateTable(liteQLParser.CreateTableContext ctx) { return visitChildren(ctx); }
    
    @Override public String visitWhereClause(liteQLParser.WhereClauseContext ctx) { 
        //WITH conjoinedAttrComparison;
        return "WHERE " + visit(ctx.conjoinedAttrComparison());
     }
    @Override public String visitConjoinedAttrComparison(liteQLParser.ConjoinedAttrComparisonContext ctx) {
        //recursively create this by getting left and right
        if (ctx.conjoinedAttrComparison() != null) {
            String left = visit(ctx.attrComparison());
            String right = visit(ctx.conjoinedAttrComparison());
            String op = ctx.conjunction().getText().toUpperCase();

            return left + " " + op + " " + right;
        }
        return visit(ctx.attrComparison());
    }
    @Override public String visitAttrComparison(liteQLParser.AttrComparisonContext ctx) {
        String comparison = getComparisonSymbol(ctx.comparison().getText());
        return ctx.attribute().getText() + " " + comparison + " " + ctx.value().getText();
    }
    public AssignListExtractor splitAssignList(String assignList){
        /*
        assignList: assignmentStmt ',' assignList
        |   assignmentStmt         
        ;
        assignmentStmt: attr=attribute '=' val=value; 
        */
       String[] splitList = assignList.split(",");
       AssignListExtractor ir = new AssignListExtractor();
       for(int i = 0; i< splitList.length; i++){
            String[] splitExpr = splitList[i].split("=");
            ir.labels.add(splitExpr[0]);
            ir.values.add(splitExpr[1]);
       }
       return ir;
    }
    public String getComparisonSymbol(String comparison){
        switch(comparison){
            case "lessthan":
                return "<";
            case "greaterthan":
                return ">";
            case "atleast":
                return ">=";
            case "atmost":
                return "<=";
            case "is":
                return "=";
            case "isnot":
                return "!=";
        }
        return comparison;
    }
    //when there is no join
    public String createSelectStmt(liteQLParser.SelectContext ctx){
        StringBuilder sb = new StringBuilder();
        String selectList;
        if(ctx.selectList().getText().equals("all")){
            selectList = "*";
        }else{
            selectList = ctx.selectList().getText();
        }
        sb.append("SELECT ").append(selectList).append(" FROM " + ctx.tableSource().getText());
        //where clause
        if (ctx.whereClause() != null) {
            sb.append(" ").append(visit(ctx.whereClause()));// get where
        }
        //group by
        if (ctx.groupClause() != null) {
            sb.append(" ").append("GROUP BY ").append(ctx.groupClause().attributeList().getText());//get group
        }
        if (ctx.havingClause() != null) {
            sb.append(" ").append("HAVING ").append(visit(ctx.havingClause().conjoinedAttrComparison()));//get having
        }
        if (ctx.orderClause() != null) {
            sb.append(" ").append("ORDER BY ").append(ctx.orderClause().attributeList().getText());//get order
            if(ctx.orderClause().order.equals("desc")){
                sb.append( " DESC");
            }
        }
        if (ctx.limitClause() != null) {
            sb.append(" ").append("LIMIT ").append(ctx.limitClause().num.getText());//get limit
        }
        sb.append(";");
        return sb.toString();
    }
    //when there is a join
    //TODO: FIX IT SO THAT THE OTHER CLAUSES ALSO ALIAS (semantics)
    public String createJoinSelect(liteQLParser.SelectContext ctx){
        StringBuilder sb = new StringBuilder();
        String table1 = ctx.tableSource().getText();
        String table2 = ctx.joinClause().tableSource().getText();
        String fullSelectList = addAlias(table1,table2,ctx.selectList().getText(),ctx.joinClause().selectList().getText());
        sb.append("SELECT " + fullSelectList + " FROM " + table1 + " JOIN " + table2 + " ON " + ctx.joinClause().attribute().getText());
        //where clause
        if (ctx.whereClause() != null) {
            sb.append(" ").append(visit(ctx.whereClause()));// get where
        }
        //group by
        if (ctx.groupClause() != null) {
            sb.append(" ").append("GROUP BY ").append(ctx.groupClause().attributeList().getText());//get group
        }
        if (ctx.havingClause() != null) {
            sb.append(" ").append("HAVING ").append(visit(ctx.havingClause().conjoinedAttrComparison()));//get having
        }
        if (ctx.orderClause() != null) {
            sb.append(" ").append("ORDER BY ").append(ctx.orderClause().attributeList().getText());//get order
            if(ctx.orderClause().order.equals("desc")){
                sb.append( " DESC");
            }
        }
        if (ctx.limitClause() != null) {
            sb.append(" ").append("LIMIT ").append(ctx.limitClause().num.getText());//get limit
        }
        sb.append(";");
        return sb.toString();
    }

    //adds alias to select lists
    public String addAlias(String table1, String table2, String firstList, String secondList){
        String fullList = "";
        String[] splitFirst;
        String[] splitSecond;
        if(firstList.equals("all")){
            fullList+=table1 + ".* ";
            splitSecond = secondList.split(",");
            splitFirst = new String[0];
        }else if (secondList.equals("all")){
            fullList+=table2 + ".* ";
            splitFirst = firstList.split(",");
            splitSecond = new String[0];
        }else{
            splitFirst = firstList.split(",");
            splitSecond = secondList.split(",");
        }
        for(int i = 0; i< splitFirst.length; i++){
            splitFirst[i] = table1 + "." + splitFirst[i];
            fullList+= splitFirst[i] + " ";
        }
         for(int i = 0; i< splitSecond.length; i++){
            splitSecond[i] = table2 + "." + splitSecond[i];
            fullList+=splitSecond[i] + " ";
        }
        return fullList;
    }
}