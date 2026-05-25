package dto;

public class ReportDomasticNormalDTO {
	
	private String order_number;
	private String car_plate_number;
	private String start_charging_time;
	private String end_charging_time;
	private String charge_duration;
	private double charge_kWh;
	private double electricity_charge;
	private double total_consumption_amount;
	private double revenue_without_vat_and_ssl;
	private double estimated_profit;
	
	public String getOrder_number() {
		return order_number;
	}
	public void setOrder_number(String order_number) {
		this.order_number = order_number;
	}
	public String getCar_plate_number() {
		return car_plate_number;
	}
	public void setCar_plate_number(String car_plate_number) {
		this.car_plate_number = car_plate_number;
	}
	public String getStart_charging_time() {
		return start_charging_time;
	}
	public void setStart_charging_time(String start_charging_time) {
		this.start_charging_time = start_charging_time;
	}
	public String getEnd_charging_time() {
		return end_charging_time;
	}
	public void setEnd_charging_time(String end_charging_time) {
		this.end_charging_time = end_charging_time;
	}
	public String getCharge_duration() {
		return charge_duration;
	}
	public void setCharge_duration(String charge_duration) {
		this.charge_duration = charge_duration;
	}
	public double getCharge_kWh() {
		return charge_kWh;
	}
	public void setCharge_kWh(double charge_kWh) {
		this.charge_kWh = charge_kWh;
	}
	public double getElectricity_charge() {
		return electricity_charge;
	}
	public void setElectricity_charge(double electricity_charge) {
		this.electricity_charge = electricity_charge;
	}
	public double getTotal_consumption_amount() {
		return total_consumption_amount;
	}
	public void setTotal_consumption_amount(double total_consumption_amount) {
		this.total_consumption_amount = total_consumption_amount;
	}
	public double getRevenue_without_vat_and_ssl() {
		return revenue_without_vat_and_ssl;
	}
	public void setRevenue_without_vat_and_ssl(double revenue_without_vat_and_ssl) {
		this.revenue_without_vat_and_ssl = revenue_without_vat_and_ssl;
	}
	public double getEstimated_profit() {
		return estimated_profit;
	}
	public void setEstimated_profit(double estimated_profit) {
		this.estimated_profit = estimated_profit;
	}
	
	

}
