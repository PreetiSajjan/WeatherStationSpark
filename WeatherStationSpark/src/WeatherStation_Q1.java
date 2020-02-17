import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Serializable;
import scala.Tuple2;

/**
 * Implementing class WeatherStation to analyze the data produced by a couple of weather stations.
 * @author Preeti Sajjan
 */
public class WeatherStation_Q1 implements Serializable {

	//Initializing serialVersionUID with default value
	private static final long serialVersionUID = 1L;
	String city;  //Station location
	List<Measurement_Q1> measurements;  //List of measurement objects
	static List<WeatherStation_Q1> stations = new ArrayList<>();

	/**
	 * Defining a parameterized constructor to initialize the values
	 * @param city is the location of the station
	 * @param measurements is list of objects of class Measurements
	 */
	public WeatherStation_Q1(String city, List<Measurement_Q1> measurements) {
		this.city = city;
		this.measurements = measurements;
	}

	/**
	 * Method calculating the number of times a temperature in given interval [t-r,...,t+r]
	 * has been measured so far by any of the weather stations
	 * @param t1 is first interval
	 * @param t2 is second interval
	 * @param r is offset to be added and subtracted in the interval
	 * @return (key, value) pair as (temperature, count)
	 */
	public static Map<Double, Integer> countTemperatue(double t1, double t2, double r){

		List<KeyValue> list = new ArrayList<>();

		//creating a parallel stream for stations for temperature interval [t1-r,...,t1+r]
		stations.stream()
		.flatMap(station -> station.getMeasurements().parallelStream()) //streaming each station
		.forEach(x -> {
			double temp = x.getTemp();
			//checking temperature based on the interval and adding an entry if present
			if((t1-r) <= temp && temp <= (t1+r)) {
				list.add(new KeyValue(t1,1));
			}
			if((t2-r) <= temp && temp <= (t2+r)) {
				list.add(new KeyValue(t2,1));
			}				
		});

		//Creating a map to following shuffle with groupingBy and reduce with summingInt operation
		Map<Double, Integer> ShuffleReduce = new HashMap<>();		
		ShuffleReduce = list.parallelStream().collect(Collectors.groupingBy(KeyValue::getKey, Collectors.summingInt(KeyValue::getValue)));

		//returning the result
		return ShuffleReduce;		
	}

	/**
	 * Method calculating the number of times a temperature in given interval [t-1,...,t+1]
	 * has been measured so far by any of the weather stations
	 * @param t is the temperature
	 * @return temperature as key and count as value 
	 */
	public static Map<Double, Integer> countTemperatue(double t){

		System.setProperty("hadoop.home.dir", "C:/winutils");
		SparkConf sparkConf = new SparkConf()
				.setAppName("WordCount")
				.setMaster("local[4]").set("spark.executor.memory", "1g"); //4 core processor to work individually with 1 gigabyte of heap memory
		JavaSparkContext ctx = new JavaSparkContext(sparkConf);

		//Parallelizing the stations
		JavaRDD<WeatherStation_Q1> station = ctx.parallelize(stations);

		JavaRDD<Measurement_Q1> list = station.flatMap(s -> s.getMeasurements().iterator()); // obtaining an iterator over list of Measurement class
		JavaRDD<Measurement_Q1> list_filter = list.filter(x -> ( (t-1) <= ((Measurement_Q1) x).temperature && ((Measurement_Q1) x).temperature <= (t+1) )); //filtering out temperature to be in the required interval
		JavaDoubleRDD sd = list_filter.mapToDouble(l -> ((Measurement_Q1) l).getTemp()); 
		JavaPairRDD<Double, Integer> ones = sd.mapToPair(s -> new Tuple2<Double, Integer>(s, 1));  //map each filtered temperature to key-value pair
		JavaPairRDD<Double, Integer> counts = ones.reduceByKey((Integer a, Integer b) -> a + b);   //reduce the pair by key by adding the values
		Map<Double, Integer> output = counts.collectAsMap();  //collect as map

		//sum to add all the occurrence of temperature within the interval [t-1,....,t+1]
		int sum = 0;
		System.out.println("\nOccurrence: [temperature, count]");
		for(Entry<Double, Integer> entry : output.entrySet()) {
			System.out.print("["+entry.getKey()+", "+entry.getValue()+"]");
			sum += entry.getValue();
		}
		System.out.println("\n");

		//result holding temperature as key and count as value
		Map<Double, Integer> result = new HashMap<Double, Integer>();
		result.put(t, sum);

		//closing the resources
		ctx.stop();
		ctx.close();

		//returning the result
		return result;
	}

	/**
	 * Method to get measurement
	 * @return a list of objects of class Measurement
	 */
	public List<Measurement_Q1> getMeasurements() {
		return measurements;
	}

	/**
	 * Method to get city
	 * @return city
	 */
	public String getCity() {
		return city;
	}		

	public static void main(String args[]) {

		//Creating objects of class Measurement
		Measurement_Q1 m1 = new Measurement_Q1(9, 20.0);
		Measurement_Q1 m2 = new Measurement_Q1(15, 11.7);
		Measurement_Q1 m3 = new Measurement_Q1(18, -5.4);
		Measurement_Q1 m4 = new Measurement_Q1(14, 18.7);
		Measurement_Q1 m5 = new Measurement_Q1(23, 20.9);

		//Creating list1 and adding the created Measurement objects to it
		List<Measurement_Q1> list1 = new ArrayList<>();
		list1.add(m1);
		list1.add(m2);
		list1.add(m3);
		list1.add(m4);
		list1.add(m5);

		//Creating our first WeatherStation
		WeatherStation_Q1 WStation1 = new WeatherStation_Q1("Galway", list1);
		stations.add(WStation1); //adding our first WeatherStation to list of stations		
		Measurement_Q1 measure1 = new Measurement_Q1(list1);

		//Calling maxTemperature to calculate the maximum temperature measured by weather station
		Double d1 = measure1.maxTemperature(12, 20);
		System.out.println("The maximum temperation in second Weather Station is "+d1);

		//Creating objects of class Measurement
		Measurement_Q1 l1 = new Measurement_Q1(9, 8.4);
		Measurement_Q1 l2 = new Measurement_Q1(15, 19.2);
		Measurement_Q1 l3 = new Measurement_Q1(18, 7.2);

		//Creating list2 and adding the created Measurement objects to it
		ArrayList<Measurement_Q1> list2 = new ArrayList<Measurement_Q1>();
		list2.add(l1);
		list2.add(l2);
		list2.add(l3);

		//Creating our second WeatherStation
		WeatherStation_Q1 WStation2 = new WeatherStation_Q1("Dublin", list2);
		stations.add(WStation2);   //adding our second WeatherStation to list of stations		
		Measurement_Q1 measure2 = new Measurement_Q1(list2);

		//Calling maxTemperature to calculate the maximum temperature measured by weather station
		Double d2 = measure2.maxTemperature(12, 20);
		System.out.println("The maximum temperation in second Weather Station is "+d2);

		//Calling countTemperature to calculate number of times an temperature (t1, t2) is so far measured by our stations 
		Map<Double, Integer> result = countTemperatue(19.0, 10.8, 2.1);	
		System.out.println("\nNumber of times temperature t1 and t2 measured so far by any of the weather stations in stations: ");
		for(Entry<Double, Integer> entry : result.entrySet()) {
			System.out.print("["+entry.getKey()+", "+entry.getValue()+"]");
		}
		System.out.println("\n");

		//Calling countTemperature to calculate number of times an temperature t is so far measured by our stations 
		//Implemented using Spark
		Map<Double, Integer> result_t = countTemperatue(19.0);	
		for(Entry<Double, Integer> entry : result_t.entrySet()) {
			System.out.println("\nNumber of times temperature " + entry.getKey() + " measured so far by any of the weather stations in stations is: " + entry.getValue() + "\n");			
		}

	}
}