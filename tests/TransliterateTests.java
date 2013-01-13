import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.*;
import org.wikimedia.morelangs.InputMethod;
import org.xml.sax.SAXException;


@RunWith(Parameterized.class)
public class TransliterateTests {

    static int firstDivergence(String str1, String str2) {
        int length = str1.length() > str2.length() ? str2.length() : str1.length();
        for(int i = 0; i < length; i++) {
            if(str1.charAt(i) != str2.charAt(i)) {
                return i;
            }
        }
        return length - 1; // Default
    }
    static String transliterateAll(InputMethod im, String input, ArrayList<Boolean> altGr) {
        String curOutput = "";
        String replacement;
        String context = "";
        for(int i=0; i < input.length(); i++) {
            String c = String.valueOf(input.charAt(i)); 
            int startPos = curOutput.length() > im.getMaxKeyLength() ? curOutput.length() - im.getMaxKeyLength() : 0;
            String toReplace = curOutput.substring(startPos) + c;
            replacement = im.transliterate(toReplace, context, altGr.get(i));
            int divIndex = firstDivergence(toReplace, replacement);
            replacement = replacement.substring(divIndex);
            curOutput = curOutput.substring(0, startPos + divIndex)  + replacement;
            
            context += c;
            if(context.length() > im.getContextLength()) {
                context = context.substring(context.length() - im.getContextLength());
            }
        }
        return curOutput;
    }
    
    @Parameters
    public static ArrayList<Object[]> data() throws SAXException, IOException, ParserConfigurationException {
        ArrayList<Object[]> data = new ArrayList<Object[]>();
        InputStream fixturesFile = TransliterateTests.class.getClassLoader().getResourceAsStream("fixtures.xml");
        Document fixturesDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fixturesFile);
        NodeList fixtures = fixturesDoc.getElementsByTagName("fixture");
        for(int i = 0; i < fixtures.getLength(); i++) {
            Node fixture = fixtures.item(i);
            String name = fixture.getAttributes().getNamedItem("inputmethod").getTextContent();
            if(name.equals("si-wijesekara")) {
                name.length();
            }
//            InputMethod im = InputMethod.fromFile(new FileInputStream("/Users/yuvipanda/code/android/moreLangs/res/" + name + ".xml"));
            InputMethod im = InputMethod.fromName(name);
            NodeList tests = fixture.getChildNodes();
            for(int j = 0; j < tests.getLength(); j++) {
                Node test = tests.item(j);
                if(test.getNodeType() == Node.ELEMENT_NODE && test.getNodeName().equals("test")) {
                    NamedNodeMap attribs = test.getAttributes();
                    String input = attribs.getNamedItem("input").getTextContent();
                    String expectedOutput = attribs.getNamedItem("output").getTextContent();
                    String description = "";
                    if(attribs.getNamedItem("description") != null) {
                        description = attribs.getNamedItem("description").getTextContent();
                    }
                    ArrayList<Boolean> altGr = new ArrayList<Boolean>();
                    if(attribs.getNamedItem("altGr") != null) {
                        String altGrString = attribs.getNamedItem("altGr").getTextContent();
                        for(int k=0; k < altGrString.length(); k++) {
                            altGr.add(altGrString.charAt(k) == '1');
                        }
                    } else {
                        for(int k=0; k < input.length(); k++) {
                           altGr.add(false); 
                        }
                    }
                    data.add(new Object[] { im, input, expectedOutput, description, altGr });
                }
            }
        }
        return data;
    }
    
    
    InputMethod im;
    String expectedOutput;
    String description;
    String input;
    ArrayList<Boolean> altGr;
    public TransliterateTests(InputMethod im, String input, String expectedOutput, String description, ArrayList<Boolean> altGr) {
        this.im = im;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.altGr = altGr;
    }
    
    @Test
    public void testTransliterate() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        String output = transliterateAll(im, input, altGr);
        assertEquals(expectedOutput, output);
    }

}
