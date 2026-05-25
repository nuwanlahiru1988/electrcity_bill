package model;

import java.sql.Date;
import java.sql.Timestamp;

public class ChargeOrderInformation {
	
	private int id;
	private String abnormal_status;
	private double actual_payment_amount;
	private String associated_reservation_order_number;
	private String car_plate_number;
	private String charge_duration;
	private double charge_kwh;
	private String delay_duration;
	private double delay_fee;
	private double discount_amount;
	private double discount_charge_amount;
	private double discount_delay_amount;
	private double discount_service_amount;
	private String elapsed_time;
	private double electricity_charge;
	private String email;
	private Timestamp end_charging_time;
	private double end_soc;
	private String group_name;
	private String gun_id;
	private String gun_name;
	private String member_name;
	private String national_standard;
	private double occupation_reservation_amount;
	private String operator_id;
	private String operator_name;
	private String order_number;
	private String order_status;
	private String order_type;
	private double paid_in_electricity;
	private double paid_service_charge;
	private Date payment_time;
	private String phone_number;
	private String pile_name;
	private String remark;
	private String serial_number;
	private double service_fee;
	private Timestamp start_charging_time;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAbnormal_status() {
		return abnormal_status;
	}
	public void setAbnormal_status(String abnormal_status) {
		this.abnormal_status = abnormal_status;
	}
	public double getActual_payment_amount() {
		return actual_payment_amount;
	}
	public void setActual_payment_amount(double actual_payment_amount) {
		this.actual_payment_amount = actual_payment_amount;
	}
	public String getAssociated_reservation_order_number() {
		return associated_reservation_order_number;
	}
	public void setAssociated_reservation_order_number(String associated_reservation_order_number) {
		this.associated_reservation_order_number = associated_reservation_order_number;
	}
	public String getCar_plate_number() {
		return car_plate_number;
	}
	public void setCar_plate_number(String car_plate_number) {
		this.car_plate_number = car_plate_number;
	}
	public String getCharge_duration() {
		return charge_duration;
	}
	public void setCharge_duration(String charge_duration) {
		this.charge_duration = charge_duration;
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
	public double getDelay_fee() {
		return delay_fee;
	}
	public void setDelay_fee(double delay_fee) {
		this.delay_fee = delay_fee;
	}
	public double getDiscount_amount() {
		return discount_amount;
	}
	public void setDiscount_amount(double discount_amount) {
		this.discount_amount = discount_amount;
	}
	public double getDiscount_charge_amount() {
		return discount_charge_amount;
	}
	public void setDiscount_charge_amount(double discount_charge_amount) {
		this.discount_charge_amount = discount_charge_amount;
	}
	public double getDiscount_delay_amount() {
		return discount_delay_amount;
	}
	public void setDiscount_delay_amount(double discount_delay_amount) {
		this.discount_delay_amount = discount_delay_amount;
	}
	public double getDiscount_service_amount() {
		return discount_service_amount;
	}
	public void setDiscount_service_amount(double discount_service_amount) {
		this.discount_service_amount = discount_service_amount;
	}
	public String getElapsed_time() {
		return elapsed_time;
	}
	public void setElapsed_time(String elapsed_time) {
		this.elapsed_time = elapsed_time;
	}
	public double getElectricity_charge() {
		return electricity_charge;
	}
	public void setElectricity_charge(double electricity_charge) {
		this.electricity_charge = electricity_charge;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public double getEnd_soc() {
		return end_soc;
	}
	public void setEnd_soc(double end_soc) {
		this.end_soc = end_soc;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	public String getGun_id() {
		return gun_id;
	}
	public void setGun_id(String gun_id) {
		this.gun_id = gun_id;
	}
	public String getGun_name() {
		return gun_name;
	}
	public void setGun_name(String gun_name) {
		this.gun_name = gun_name;
	}
	public String getMember_name() {
		return member_name;
	}
	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}
	public String getNational_standard() {
		return national_standard;
	}
	public void setNational_standard(String national_standard) {
		this.national_standard = national_standard;
	}
	public double getOccupation_reservation_amount() {
		return occupation_reservation_amount;
	}
	public void setOccupation_reservation_amount(double occupation_reservation_amount) {
		this.occupation_reservation_amount = occupation_reservation_amount;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getOperator_name() {
		return operator_name;
	}
	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}
	public String getOrder_number() {
		return order_number;
	}
	public void setOrder_number(String order_number) {
		this.order_number = order_number;
	}
	public String getOrder_status() {
		return order_status;
	}
	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}
	public String getOrder_type() {
		return order_type;
	}
	public void setOrder_type(String order_type) {
		this.order_type = order_type;
	}
	public double getPaid_in_electricity() {
		return paid_in_electricity;
	}
	public void setPaid_in_electricity(double paid_in_electricity) {
		this.paid_in_electricity = paid_in_electricity;
	}
	public double getPaid_service_charge() {
		return paid_service_charge;
	}
	public void setPaid_service_charge(double paid_service_charge) {
		this.paid_service_charge = paid_service_charge;
	}
	public Date getPayment_time() {
		return payment_time;
	}
	public void setPayment_time(Date payment_time) {
		this.payment_time = payment_time;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	public String getPile_name() {
		return pile_name;
	}
	public void setPile_name(String pile_name) {
		this.pile_name = pile_name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}
	public double getService_fee() {
		return service_fee;
	}
	public void setService_fee(double service_fee) {
		this.service_fee = service_fee;
	}
	public Timestamp getEnd_charging_time() {
		return end_charging_time;
	}
	public void setEnd_charging_time(Timestamp end_charging_time) {
		this.end_charging_time = end_charging_time;
	}
	public Timestamp getStart_charging_time() {
		return start_charging_time;
	}
	public void setStart_charging_time(Timestamp start_charging_time) {
		this.start_charging_time = start_charging_time;
	}
	
		
	
}
