import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * @author Jeff Offutt & Mongkoldech Rajapakdee
 *         Date: Nov, 2009
 *
 * Wiring the pieces together:
 *    quoteserve.java -- The servlet (entry point)
 *    QuoteList.java -- A list of quotes, representing what's read from the XML file
 *                      Used by quoteserve.java
 *    Quote.java -- A simple Quote bean; two entries, author and quote-text
 *                      Used by QuoteList.java
 *    QuoteSaxHandler.java -- Callback methods for the parser, populates QuoteList
 *                      Used by QuoteSaxParser
 *    QuoteSaxParser.java -- Parses the XML file
 *                      Used by quoteserve.java
 *    quotes.js -- JS used by the HTML created in quoteserve
 *    quotes.xml -- Data file, read by QuoteSaxParser
 */
public class quoteserve
{
 //  private static final String FileURL = "https://cs.gmu.edu/~offutt/classes/642/examples/servlets/quotes";
 //  private static final String FileJavascript = FileURL + "/quotes.js";

   // Data file
    //NOTE: This file location needs to be updated to whereever your quote.xml file is
   // private static final String quoteFileName = "C:\\Users\\white\\Documents\\Documents\\GMU\\2018 Spring\\SWE437_Testing\\quotes\\quotes\\quotes.xml";
    private static final String help = "Commands:\n"+"" +
                                            "\trandom -Prints out a random quote\n"+
                                            "\tsearch <quote|author|both> <search term> -Search through quotes for a quote that contains the search term in the quote, author, or both depending on the mode of search\n"+
                                            "\thistory -Prints out the last 5 terms searched\n"+
                                            "\tadd -Prompts user for quote text and author before added quote\n"+
                                            "\tsave -Saves quotes added this session\n"+
                                            "\texit -Exits the program";

   private static QuoteList quoteList; // Stores all the quotes from the xml file
   private static ArrayList<String> history; // Stores last 5 search terms used
    private static String quoteFileName;
    private static Scanner sc; // Scanner for reading user responses
   /*
        Command Line interface
    */
    public static void main(String[] arg){
        // Get the current directory and find the quotes.xml file within that directory
        File curDirectory = new File("");
        quoteFileName = curDirectory.getAbsolutePath()+"\\src\\quotes.xml";
         // Generate QuoteList, history, and scanner for reading
        QuoteSaxParser qParser = new QuoteSaxParser (quoteFileName);
        quoteList = qParser.getQuoteList();
        history = new ArrayList<String>();
        sc  = new Scanner(System.in);
        boolean run = true;
        // Keep running until the user exits
        while(run){
            // Prompt the user for commands and read the line
            //System.out.println("Enter a command ('help' to list commands, 'exit' to quit): ");
            //String s = sc.nextLine();
            String s = promptUser("Enter a command ('help' to list commands, 'exit' to quit): ");
            // For the inputs 'exit' and 'help' we either exit or print out possible commands
            if(s.equals("exit"))
                run = false;
            else if(s.equals("help"))
                System.out.println(help);
            else // For all other inputs attempt to parse it
                parseInput(s);

        }
        sc.close(); //Close scanner

    }
    /*
        Prompts the user with a message and returns the user's response
        @params String prompt, Prompt to tell the user
        @return String, user's response to the prompt
     */
    private static String promptUser(String prompt){
        System.out.println(prompt);
        return sc.nextLine();
    }
    /*
        Parses string and perform acceptable responses to the the inputs
        @params String user input
     */
    private static void parseInput(String s){
        // First split the string by spaces so we can check each word
        String[] split = s.split(" ");
        if(split.length > 0){   // Make sure there is at least one word in the input
            switch(split[0]){
                case "random": // If the input is random check to see if it is the only work and print out a random quote if it is
                    if(split.length == 1) {
                        Quote q = quoteList.getRandomQuote();
                        System.out.println(q.getQuoteText());
                        System.out.println("\t~ "+q.getAuthor());
                    }
                    return;
                case "search":  //If the input is search, search the QuoteList
                    if(printSearch(split) > 0)
                        return;
                    break;
                case "history": // If the input is history, print the last 5 terms searched
                    for(int x = 0; x < history.size();x++){
                        System.out.println(String.format("%d) %s",x+1,history.get(x)));
                    }
                    return;
                case "add": // If the input is add, we check if the
                    if(split.length == 1){
                        addQuote();
                        return;
                    }
                    break;
                case "save": // If the input is save, save the file
                    saveQuotes();
                    return;
            }
        }
        // If there was a problem parsing in any part of this method or printSearch print Invalid input
        System.out.println("Invalid input, please enter a valid input");
    }
    /*
        Search through the list of Quotes for Quotes that match the search
        @params String[] containing a search mode (optional) and search term
        @return Integer indicating success or failure to search term
     */
    private static int printSearch(String[] split){
        String searchTerm = "";
        String searchMode = "";
        if(split.length == 2){ // If the input only has 2 words we assume we assume the second word is the search term
            searchTerm = split[1];
        }
        else if(split.length >= 3){ // If there are 3 words the second is the search mode and third is the search term
            searchMode = split[1];
            searchTerm = split[2]; // Search term is the rest of the input
            for(int x = 3; x < split.length;x++)
                searchTerm += " "+split[x];
        }
        else
            return -1;
        int searchScopeInt = QuoteList.SearchBothVal; // Default
        if (searchMode != null && !searchMode.equals(""))
        {  // If no parameter value, let it default.
            if (searchMode.equals ("quote"))
                searchScopeInt = QuoteList.SearchTextVal;
            else if (searchMode.equals ("author"))
                searchScopeInt = QuoteList.SearchAuthorVal;
            else if (searchMode.equals ("both"))
                searchScopeInt = QuoteList.SearchBothVal;
            else //If the searchMode was not any of the following
                searchTerm = searchMode+" "+searchTerm; // Assume it is part of the searchTerm
        }
        history.add(searchTerm);
        if(history.size()>5)
            history.remove(0);
        QuoteList searchRes = quoteList.search(searchTerm, searchScopeInt);
        Quote quoteTmp;
        System.out.println("===============================RESULT=========================");
        if (searchRes.getSize() == 0)
        {
            System.out.println ("Your search - "+ searchTerm +" - did not match any quotes.");
        }
        else
        {
            for (int i = 0; i < searchRes.getSize() ; i++)
            {
                quoteTmp = searchRes.getQuote(i);
                System.out.println (quoteTmp.getQuoteText());
                System.out.println ("\t ~ " + quoteTmp.getAuthor() + "");
            }
        }
        System.out.println("==============================================================");
        return 1;
    }
    /*
        Prompts user for information about a the new quote. Checks to see if the
        quote meets minimal requirements and adds the quote to the quote list if it does.
     */
    private static void addQuote(){
        // Get quote text and check if its at least 3 words
        String quoteText = promptUser("Type in the quote text (Must be at least 3 words and not contain > or <)");
        if(quoteText.split(" ").length < 3){
            System.out.println("Quote must be at least 3 words");
            return;
        }
        else if(quoteText.contains("<") || quoteText.contains(">")){
            System.out.println("Quote must not contain < or >");
            return;
        }
        // Get quote author
        String quoteAuthor = promptUser("Type in the quote's author");
        if(quoteAuthor.equals(""))
            quoteAuthor = "Anonymous";
        Quote q = new Quote(quoteAuthor,quoteText);
        quoteList.setQuote(q);
    }
    private static void saveQuotes(){
        QuoteWriter qw = new QuoteWriter(quoteFileName);
        qw.saveQuoteList(quoteList);
    }
} // end quoteserve class
