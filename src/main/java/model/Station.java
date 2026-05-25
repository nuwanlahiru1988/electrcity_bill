package model;

public class Station{
	private int id;
	private String stationName;
	private int status;
	private int tariff_category;
	private int other_consumer_type;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	public int getTariff_category() {
		return tariff_category;
	}
	public void setTariff_category(int tariff_category) {
		this.tariff_category = tariff_category;
	}
	public int getOther_consumer_type() {
		return other_consumer_type;
	}
	public void setOther_consumer_type(int other_consumer_type) {
		this.other_consumer_type = other_consumer_type;
	}
	
	
		
}
