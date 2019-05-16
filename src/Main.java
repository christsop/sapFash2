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

    private static String txtFiles = System.getProperty("user.dir") + "/txts/";
    private static String questions = System.getProperty("user.dir") + "/questions/";
    private static String results = System.getProperty("user.dir") + "/results/";

    public static void main(String[] args) throws IOException {

        createNewQuestions();

        readQueriesEndExportResults("queries.txt", "answers.txt");
        readQueriesEndExportResults("queries30.txt", "answers30.txt");
        readQueriesEndExportResults("queries60.txt", "answers60.txt");
        readQueriesEndExportResults("queries90.txt", "answers90.txt");

    }

    private static void createNewQuestions() throws IOException { // creates 3 new queries.txt
        List<String> listOfFiles = fileNames();

        BufferedWriter bw30 = new BufferedWriter(new FileWriter(new File(questions + "queries30.txt")));
        BufferedWriter bw60 = new BufferedWriter(new FileWriter(new File(questions + "queries60.txt")));
        BufferedWriter bw90 = new BufferedWriter(new FileWriter(new File(questions + "queries90.txt")));

        int questionIndex = 0;
        for (String fileName : listOfFiles) {
            File file = new File(fileName);
            String thirty = getPhrase(30, file);
            String sixty = getPhrase(60, file);
            String ninety = getPhrase(90, file);

            questionIndex++;
            bw30.write("Q" + questionIndex + " " +thirty + "\n");
            bw60.write("Q" + questionIndex + " " +sixty + "\n");
            bw90.write("Q" + questionIndex + " " +ninety + "\n");
        }

        bw30.close();
        bw60.close();
        bw90.close();
    }

    private static List<String> fileNames(){
        List<String> listOfFiles = new ArrayList<>();

        listOfFiles.add(txtFiles.concat("193378.txt"));
        listOfFiles.add(txtFiles.concat("213164.txt"));
        listOfFiles.add(txtFiles.concat("204146.txt"));
        listOfFiles.add(txtFiles.concat("214253.txt"));
        listOfFiles.add(txtFiles.concat("212490.txt"));
        listOfFiles.add(txtFiles.concat("210133.txt"));
        listOfFiles.add(txtFiles.concat("213097.txt"));
        listOfFiles.add(txtFiles.concat("193715.txt"));
        listOfFiles.add(txtFiles.concat("197346.txt"));
        listOfFiles.add(txtFiles.concat("199879.txt"));

        return listOfFiles;
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

    private static void readQueriesEndExportResults(String queryFileName, String resultFileName) throws IOException {

        File file = new File(questions + queryFileName);

        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(results + resultFileName)));

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