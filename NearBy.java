package com.quoraquestion1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 *
 */
public class NearBy 
{
    static class Topic{
        int topicId;
        Point location;
        List<Integer> questionIds = new ArrayList<Integer>();
        Topic(int topicId, Point loc){
            this.topicId = topicId;
            this.location = loc;
        }
    }
    
    static class Question{
        int questionId;
        int noOfTopics;
        HashSet<Integer> topicIds = new HashSet<Integer>();
        Question(int questionId, int noOfTopics){
            this.questionId = questionId;
            this.noOfTopics = noOfTopics;
        }
    }
    
   
    static class Point{
        double x;
        double y;
        Point(double x, double y){
            this.x = x;
            this.y = y;
        }
    }
    
    static class Distance {
        Topic topic;
        double distance;
        
        Distance(Topic topic, double distance){
            this.topic = topic;
            this.distance = distance;
        }
        
    }
    static class NearestNeighbors{
        static int noOfTopics;
        static int noOfQuestions;
        static int noOfQueries;
        // List of topics.
        static List<Topic> topics = new LinkedList<Topic>();
        // List of questions.
        static List<Question> questions = new LinkedList<Question>();
        // Maps topicids to its questionids.
        static HashMap<Integer, ArrayList<Integer>> tqMap = new HashMap<Integer, ArrayList<Integer>>();
        
        static double getDistance(Point p1, Point p2) {
            /*
               cartesian distance between two point = √(x1-x2)²+(y1-y2)²
            */
            return  Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
        }
        
        static void calculateNearestNeighbors(Point tcenter, int neighbors, char query) {
            List<Distance> SortedTopics = new LinkedList<Distance>();
            HashSet<Integer> uniqueTopicIds = new HashSet<Integer>()    ;
            int SIZE;
            for(Topic topic: topics) {
                double distance = NearestNeighbors.getDistance(tcenter, topic.location);
                SortedTopics.add(new Distance(topic, distance));
            }
            Collections.sort(SortedTopics, new Comparator<Distance>(){
                public int compare(Distance d1 , Distance d2) {                    
                    return (d1.distance > d2.distance) ? 1 : 
                           (d1.distance < d2.distance) ? -1 : 0;
                }  
            });
            
            switch(query){
                case 't':
                    for (int i = 0; i < neighbors; i++) {
                        System.out.print(SortedTopics.get(i).topic.topicId+" ");
                    }
                    System.out.println("");
                    break;
                case 'q':
                    SIZE = SortedTopics.size();
                    for (int i = 0; i < neighbors; ) {
                        if(i>=SIZE) break;
                        int topicId = SortedTopics.get(i).topic.topicId;
                        for(int questionid: NearestNeighbors.tqMap.get(topicId)) {
                            if(!uniqueTopicIds.contains(questionid)) {
                                uniqueTopicIds.add(questionid);
                                System.out.print(questionid+" ");
                                i++;
                            }
                        }
                    }
                    System.out.println("");
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }
    
    static class NearByFileParser{
        static String FILENAME;
        static int noOfLines;
        static enum parseStages {INIT, POPULATETOPICS, POPULATEQUESTIONS, PRINTQUERY};     
        static boolean parseNearByFile() {
            try{
                Scanner scanner = new Scanner(new BufferedReader(new FileReader(FILENAME)));
                int lineCount = -1;
                while(scanner.hasNextLine()){
                    if(lineCount==-1) {
                        processLine(scanner.nextLine(), parseStages.INIT);
                        lineCount++;
                    }
                    if(NearestNeighbors.noOfTopics>0){
                        processLine(scanner.nextLine(), parseStages.POPULATETOPICS);
                        NearestNeighbors.noOfTopics--;
                        lineCount++; continue;
                    }
                    if(NearestNeighbors.noOfQuestions>0) {
                        processLine(scanner.nextLine(), parseStages.POPULATEQUESTIONS);
                        NearestNeighbors.noOfQuestions--;
                        lineCount++; continue;
                    }
                    if(NearestNeighbors.noOfQueries>0) {
                        processLine(scanner.nextLine(), parseStages.PRINTQUERY);
                        NearestNeighbors.noOfQueries--;
                        lineCount++;
                    }
                }
                return (lineCount==noOfLines);
            } catch(Exception e) {
                System.out.println(e);
            }   
            return false;
        }
        
        private static void processLine(String line, parseStages stage) {
            Scanner s = new Scanner(line);
            try{
                switch (stage) {
                    case INIT:
                        NearestNeighbors.noOfTopics = s.nextInt();
                        NearestNeighbors.noOfQuestions = s.nextInt();
                        NearestNeighbors.noOfQueries = s.nextInt();
                        noOfLines = NearestNeighbors.noOfTopics + NearestNeighbors.noOfQuestions + NearestNeighbors.noOfQueries;
                        break;
                    case POPULATETOPICS:
                        NearestNeighbors.topics.add(new Topic(s.nextInt(), new Point(s.nextDouble(), s.nextDouble())));
                        break;
                    case POPULATEQUESTIONS:
                        int questionId = s.nextInt();
                        int noOfTopics = s.nextInt();
                        Question q = new Question(questionId, noOfTopics);
                        for (int i = 0; i < noOfTopics ; i++) {
                           int topicId = s.nextInt();
                           q.topicIds.add(topicId);
                           if(NearestNeighbors.tqMap.containsKey(topicId)==true) {
                               ArrayList<Integer> questions = NearestNeighbors.tqMap.get(topicId);
                               questions.add(questionId);
                               NearestNeighbors.tqMap.put(topicId, questions);
                           } else {
                               NearestNeighbors.tqMap.put(topicId, new ArrayList<Integer>());
                               NearestNeighbors.tqMap.get(topicId).add(questionId);
                           }                      
                        }
                        break;
                    case PRINTQUERY:
                        char query = (s.next().compareTo("t")==0) ? 't' : 'q';
                        int noOfQueries = s.nextInt();
                        Point center = new Point(s.nextDouble(), s.nextDouble());
                        NearestNeighbors.calculateNearestNeighbors(center, noOfQueries, query);
                        break;
                    default:
                        throw new AssertionError();
                }
            } catch (Exception e) {
                System.out.println(e);
            }        
        }
    }
    
    public static void main( String[] args ) throws FileNotFoundException
    {
        NearByFileParser.FILENAME = args[0];
        System.out.println(NearByFileParser.parseNearByFile() ? "File parsed successfully" : "Malformed file");
    }
}
