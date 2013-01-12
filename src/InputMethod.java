import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class InputMethod  {
    static class InputPattern {
        private Pattern inputPattern;
        private Pattern contextPattern;
        private String replacement;
        private Boolean altGr;
       
        public InputPattern(String input, String context, String replacement, Boolean altGr) {
            this.inputPattern = Pattern.compile(input + "$");
            if(context != null) {
                this.contextPattern = Pattern.compile(context + "$");
            } else {
                this.contextPattern = null;
            }
            this.replacement = replacement;
            this.altGr = altGr;
        }
        
        public InputPattern(String input, String replacement, Boolean altGr) {
            this(input, null, replacement, altGr);
        }
        
        public InputPattern(String input, String context, String replacement) {
            this(input, context, replacement, false);
        }
        
        public InputPattern(String input, String replacement) {
            this(input, null, replacement, false);
        }
    }
    
    private String id;
    private String name;
    private String description;
    private String author;
    private String version;
    private int maxKeyLength;
    private int contextLength;
   
    private ArrayList<InputPattern> patterns; 
   
    public InputMethod(String id, String name, String description, String author, String version, int contextLenght, int maxKeyLength, ArrayList<InputPattern> patterns) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.version = version;
        this.patterns = patterns;
        this.maxKeyLength = maxKeyLength;
        this.contextLength = contextLenght;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getVersion() {
        return version;
    }
    
    public int getContextLength() {
        return contextLength;
    }
    
    public int getMaxKeyLength() {
        return maxKeyLength;
    }
    
    public String transliterate(String input, String context, Boolean altGr) {
       for(InputPattern pattern: patterns) {
           Matcher inputMatcher = pattern.inputPattern.matcher(input);
          if(inputMatcher.find()) {
              if(pattern.contextPattern == null || pattern.contextPattern.matcher(context).find()) {
                  if(pattern.altGr == altGr) {
                      return inputMatcher.replaceAll(pattern.replacement);
                  }
              }
          }
       }
       return input;
    }
    
    public static InputMethod fromFile(InputStream input) throws SAXException, IOException, ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        Node root = doc.getDocumentElement();
        String id = root.getAttributes().getNamedItem("id").getTextContent();
        String name = root.getAttributes().getNamedItem("name").getTextContent();
        String description = root.getAttributes().getNamedItem("description").getTextContent();
        String author = root.getAttributes().getNamedItem("author").getTextContent();
        String version = root.getAttributes().getNamedItem("version").getTextContent();
        int maxKeyLength = Integer.parseInt(root.getAttributes().getNamedItem("maxKeyLength").getTextContent());
        int contextLength = Integer.parseInt(root.getAttributes().getNamedItem("contextLength").getTextContent());
       
        ArrayList<InputPattern> patterns = new ArrayList<InputMethod.InputPattern>();
        NodeList patternsXML = doc.getElementsByTagName("pattern");
        for(int i=0; i< patternsXML.getLength(); i++) {
           NamedNodeMap attribs = patternsXML.item(i).getAttributes();
           InputPattern p;
           System.out.println(attribs.getNamedItem("input").getTextContent());
           String inputPattern = attribs.getNamedItem("input").getTextContent();
           String replacement = attribs.getNamedItem("replacement").getTextContent();
           String context = null;
           Boolean altGr = false;
           if(attribs.getNamedItem("context") != null) {
              context = attribs.getNamedItem("context").getTextContent();
           }
           if(attribs.getNamedItem("altGr") != null) {
               altGr = attribs.getNamedItem("altGr").getTextContent().equals("true");
           }
           p = new InputPattern(inputPattern, context, replacement, altGr);
           patterns.add(p);
        }
        return new InputMethod(id, name, description, author, version, contextLength, maxKeyLength, patterns);
    }
}    
