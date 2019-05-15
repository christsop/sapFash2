import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.OutputStream;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main {

    private static String txtFiles = "/Users/christos/Desktop/fash2/txts/";

//    private static String documentsPath = System.getProperty("user.dir") + "/documents/";
//    private static String queriesIn = System.getProperty("user.dir") + "/queries.txt/";
//    private static String answers = System.getProperty("user.dir") + "/qrels.txt/";

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {


        File folder = new File(txtFiles);
        File[] listOfFiles = folder.listFiles();


        int i = 1;
        for (File file : listOfFiles) {
            String thirty = getPhrase(30, file);
            String sixty = getPhrase(60, file);
            String ninety = getPhrase(90, file);
            System.out.println("" + (100*i++)/18316 + "% parsed");
        }

//        readQueriesEndExportResults();

    }

    private static String getPhrase(int percentage, File file) throws IOException {
        Scanner scanner = new Scanner(file);
        Random rand = new Random();

        int totalPhrases = 0;
        String phrase = "";
        List<String> phrasesList = new ArrayList<String>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            phrasesList.add(line);
            totalPhrases++;
        }
        int totalPhrasesToReturn = (totalPhrases*percentage)/100;

        while(totalPhrasesToReturn > 0){
            int randomIndexOfList = rand.nextInt(totalPhrases);

            phrase = phrase.concat(phrasesList.get(randomIndexOfList) + " ");
            phrasesList.remove(randomIndexOfList);

            totalPhrasesToReturn--;
            totalPhrases--;
        }

        return phrase;
    };


    public static BufferedReader getQuery(String text) throws IOException {

        try {

            URL url = new URL("http://localhost:9200/_search");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");


            String input = "{\n" +
                    "\"size\" : 21,"+
                    "   \"query\":{\n" +
                    "      \"match\":{\n" +
                    "         \"text\":" + "\"" + text + "\"" +
                    "      }\n" +
                    "   }\n" +
                    "}'";

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            return br;

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;
    }

    private static void readQueriesEndExportResults() throws IOException {

        File file = new File("/Users/christos/Downloads/fash1/queries.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/Users/christos/Desktop/answers.txt")));

        String question;
        while ((question = br.readLine()) != null) {
            String text = question.replace("\t", " ");
            String questionNo = text.split(" ")[0];
            text = text.replaceFirst(questionNo, "");

            BufferedReader answer = getQuery(text);

            String output;
            while ((output = answer.readLine()) != null) {
                JSONArray formattedAnswer = getFormattedAnswer(output);
                for(int i=1; i<formattedAnswer.length(); i++) {
                    String similarity = formattedAnswer.getJSONObject(i).get("_score").toString() ;
                    bw.write(questionNo + " 0 " + formattedAnswer.getJSONObject(i).get("_id").toString() + " 0 " + similarity + " myPc \n");
                }
            }
        }
        bw.close();
    }

    private static JSONArray getFormattedAnswer(String output) throws IOException{
        String result = new JSONObject(output).get("hits").toString();
        return new JSONObject(result).getJSONArray("hits");
    }


}