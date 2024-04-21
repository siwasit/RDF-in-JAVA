package com.sf311;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import java.util.List;
import java.io.StringWriter;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) {
        TripleManagement tripleManager = new TripleManagement();
        List<TripleManagement.Triple> triples = null;
        
        try {
            triples = tripleManager.getTripleList();
        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
            e.printStackTrace();
        }
        
        String turtle = createRDFModel(triples);
    }

    public static String createRDFModel(List<TripleManagement.Triple> triples) {
        Model model = ModelFactory.createDefaultModel();
        // for (TripleManagement.Triple triple : triples) {
        //     System.out.println(triple);
        // }

        // Define namespaces
        String exNS = "http://example.org/";
        String foafNS = "http://xmlns.com/foaf/0.1/";
        model.setNsPrefix("ex", exNS);
        model.setNsPrefix("foaf", foafNS);

        for (TripleManagement.Triple triple : triples) {
            String subject = triple.getFirst().replaceAll("[\\s']+","_").replaceAll("\\([^()]*\\)", "");;
            String predicate = triple.getSecond();
            String object = triple.getThird();
            String nsObject = triple.getThird().replaceAll("[\\s']+","_").replaceAll("\\([^()]*\\)", "");

            Resource subjectResource = model.createResource(exNS + subject);
            Property predicateProperty = model.createProperty(foafNS + predicate);
            Literal literalValue = model.createTypedLiteral(object, XSDDatatype.XSDstring);
            Resource objectResource = model.createResource(exNS + nsObject);

            boolean ObjectTypeExists = model.contains(subjectResource, RDF.type, "Object");
            if (!ObjectTypeExists) {
                model.add(subjectResource, RDF.type, "Object");
            }
            
            if (predicate.equals("HasAttribute")) {
                model.add(subjectResource, predicateProperty, literalValue);
            } else {
                model.add(subjectResource, predicateProperty, objectResource);
            }
        }

        // Write the RDF model to a StringWriter
        StringWriter stringWriter = new StringWriter();
        model.write(stringWriter, "TURTLE");

        try {
            // Output stream to write the Turtle data
            OutputStream outputStream = new FileOutputStream("output.ttl");

            // Write the model in Turtle format to the output stream
            model.write(outputStream, "TURTLE");

            // Close the output stream
            outputStream.close();

            System.out.println("Turtle file saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving Turtle file: " + e.getMessage());
        }

        // Return the Turtle representation as a String
        return stringWriter.toString();
    }
}

class TripleManagement {
    String filePath = "src/main/java/com/sf311/csv/csvfile.csv";
    public List<Triple> getTripleList() throws IOException {
        ArrayList<Triple> triples = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                Triple triple = new Triple(data[0], data[1], data[2]); // Assuming CSV has at least 3 values
                triples.add(triple);
            }
        }
        return triples;
    }

    static class Triple {
        private String first;
        private String second;
        private String third;
    
        public Triple(String first, String second, String third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        public String getThird() {
            return third;
        }
    
        @Override
        public String toString() {
            return "[" + first + ", " + second + ", " + third + "]";
        }
    }

}