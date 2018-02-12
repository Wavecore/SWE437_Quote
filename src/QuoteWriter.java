import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class QuoteWriter {
    private String fileName;
    public QuoteWriter()
    {
        super();
    }
    public QuoteWriter(String fileName){
        this.fileName = fileName;
    }
    /*
        Saves the quote list in the quote writer file
        @params QuoteList
     */
    public void saveQuoteList(QuoteList quoteList){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write("<?xml version=\"1.0\"?>\n" +
                    "<quote-list>\n");
            for(int x = 0; x < quoteList.getSize(); x++){
                Quote q = quoteList.getQuote(x);
                writer.append("\t<quote>\n");
                writer.append("\t\t<quote-text>"+q.getQuoteText()+"</quote-text>\n");
                writer.append("\t\t<author>"+q.getAuthor()+"</author>\n");
                writer.append("\t</quote>\n");
            }
            writer.append("</quote-list>");
            writer.close();
        }
        catch (IOException e){
            System.out.println("Error: Unable to save quote list");
        }
    }
}
