package model;

public class OtherConsumer {
	
	private int id;
	private String category;
	private String range_descrition;
	private int type;
	private double day_price;
	private double peak_price;
	private double off_peak_price;
	private double fixed_charge;
	private double demand_charge;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getRange_descrition() {
		return range_descrition;
	}
	public void setRange_descrition(String range_descrition) {
		this.range_descrition = range_descrition;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getDay_price() {
		return day_price;
	}
	public void setDay_price(double day_price) {
		this.day_price = day_price;
	}
	public double getPeak_price() {
		return peak_price;
	}
	public void setPeak_price(double peak_price) {
		this.peak_price = peak_price;
	}
	public double getOff_peak_price() {
		return off_peak_price;
	}
	public void setOff_peak_price(double off_peak_price) {
		this.off_peak_price = off_peak_price;
	}
	public double getFixed_charge() {
		return fixed_charge;
	}
	public void setFixed_charge(double fixed_charge) {
		this.fixed_charge = fixed_charge;
	}
	public double getDemand_charge() {
		return demand_charge;
	}
	public void setDemand_charge(double demand_charge) {
		this.demand_charge = demand_charge;
	}
	
}
