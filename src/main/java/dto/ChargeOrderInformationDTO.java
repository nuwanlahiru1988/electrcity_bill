package dto;

import java.sql.Date;
import java.time.LocalDateTime;

public class ChargeOrderInformationDTO{
	
	private String charge_duration;
	private double actual_payment_amount;
	private double charge_kwh;
	private String delay_duration;
	private LocalDateTime start_charging_time;
	private LocalDateTime end_charging_time;
	private double total_consumption_amount;
	
	public String getCharge_duration() {
		return charge_duration;
	}
	public void setCharge_duration(String charge_duration) {
		this.charge_duration = charge_duration;
	}
	
	public double getActual_payment_amount() {
		return actual_payment_amount;
	}
	public void setActual_payment_amount(double actual_payment_amount) {
		this.actual_payment_amount = actual_payment_amount;
	}
	public double getCharge_kwh() {
		return charge_kwh;
	}
	public void setCharge_kwh(double charge_kwh) {
		this.charge_kwh = charge_kwh;
	}
	public String getDelay_duration() {
		return delay_duration;
	}
	public void setDelay_duration(String delay_duration) {
		this.delay_duration = delay_duration;
	}
	public LocalDateTime getStart_chaging_time() {
		return start_charging_time;
	}
	public void setStart_chaging_time(LocalDateTime start_chaging_time) {
		this.start_charging_time = start_chaging_time;
	}
	public LocalDateTime getEnd_charging_time() {
		return end_charging_time;
	}
	public void setEnd_charging_time(LocalDateTime end_charging_time) {
		this.end_charging_time = end_charging_time;
	}
	public double getTotal_consumption_amount() {
		return total_consumption_amount;
	}
	public void setTotal_consumption_amount(double total_consumption_amount) {
		this.total_consumption_amount = total_consumption_amount;
	}
	
	

}
