import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import scala.Serializable;

/*
 * Implementing class Measurement to store every occurrence of temperature with corresponding time
 * @author Preeti Sajjan
 */
public class Measurement_Q1 implements Serializable {

	//Initializing serialVersionUID with default value
	private static final long serialVersionUID = 1L;
	int time;			//time of measurement
	double temperature;	//temperature at time of measurement
	TreeMap<Integer, Double> measurement = new TreeMap<>();

	/**
	 * Defining a parameterized constructor to initialize the values
	 * @param time being time of measurement
	 * @param temperature being temperature at time of measurement
	 */
	public Measurement_Q1(int time, double temperature) {
		this.time = time;
		this.temperature = temperature;
	}

	public Measurement_Q1(List<?> list) {
		for (Object o : list) {
			measurement.put(((Measurement_Q1) o).getTime() , ((Measurement_Q1) o).getTemp());
		}
	}

	/**
	 * Method calculating the maximum temperature recorded in the interval startTime and endTime
	 * @param startTime being the starting of the interval
	 * @param endTime being the ending of the interval
	 * @return double maximum temperature value
	 */
	double maxTemperature(int startTime, int endTime) {

		//Getting the entrySet of our Map and creating a stream of it
		Entry<Integer, Double> maxTemp = measurement.entrySet()
				.stream()
				.filter(x -> (startTime < x.getKey() && x.getKey() < endTime)) //condition to check if temperature is in the interval
				.max(Comparator.comparingDouble(Map.Entry::getValue))  //calculating the maximum of it
				.get();
		return maxTemp.getValue();
	}

	/**
	 * Method to get Time
	 * @return time
	 */
	public int getTime() {
		return time;
	}
	/**
	 * Method to get Temperature
	 * @return temperature
	 */
	public double getTemp() {
		return temperature;
	}
}

/*
 * Implementing class KeyValue to depict key value pair in our further MapReduce operations
 */
class KeyValue{
	double key;
	int value;

	/**
	 * Defining a parameterized constructor to initialize the values
	 * @param key is key of (key,value) pair
	 * @param value is value of (key,value) pair
	 */
	public KeyValue(double key, int value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Method to get Key
	 * @return key
	 */
	public double getKey() {
		return key;
	}

	/**
	 * Method to get Value
	 * @return value
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "["+this.getKey() + ", " + this.getValue()+"]";
	}
}