<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Orders Table</title>

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.8.2/jspdf.plugin.autotable.min.js"></script>
</head>
<body>

  <!-- Page Header -->
  <div class="page-header" style="max-width:1200px;margin:0 auto 1.8rem">
   
   
    
  </div>
	
  <div style="max-width:1200px;margin:0 auto">
	<div class="mb-3 text-end">
  <button type="button" class="btn btn-success" onclick="downloadExcel()">
    <i class="bi bi-file-earmark-excel"></i> Download Excel
  </button>

  <button type="button" class="btn btn-danger" onclick="downloadPDF()">
    <i class="bi bi-file-earmark-pdf"></i> Download PDF
  </button>
</div>
    <!-- ── FILTER BAR ── -->
    
    <!-- Active filter chips -->
    <div class="active-filters" id="activeFilters"></div>

    <!-- Bulk action bar -->
 

    <!-- ── TABLE CARD ── -->
    <div class="table-card">
      <div class="table-card-header">
        
      </div>

      <div>
        <table class="table" id="reportTable">
          <thead>
            <tr>
              <th></th>
              <th class="sortable" data-col="id">Order Number</i></th>
              <th class="sortable" data-col="customer">Car Plate Number</th>
              <th class="sortable" data-col="category">Start Charging Time</th>
              <th class="sortable" data-col="amount">End Charging Time</th>
              <th class="sortable" data-col="date">Charge Duration </th>
              <th class="sortable" data-col="status">Charge (kWh)</th>
           	  <th class="sortable" data-col="status">Electricity </th>
           	  <th class="sortable" data-col="status">Total Consumption Amount</th>
           	  <th class="sortable" data-col="status">Revenue Without VAT and SSCL</th>
              <th class="sortable" data-col="status">Estimated Profit</th>
             
              
            </tr>
          </thead>
          <tbody >
    			  <c:forEach var="c" items="${reports}">
    			  	<tr>
    			  		<td>#</td>
    			  		<td>${c.order_number}</td>
         				<td>${c.car_plate_number}</td>
         				<td>${c.start_charging_time}</td>    				
         				<td>${c.end_charging_time}</td>
         				<td>${c.charge_duration}</td>
         				<td><fmt:formatNumber 
					    value="${c.charge_kWh}" 
					    type="number" 
					    minFractionDigits="2" 
					    maxFractionDigits="2"/></td>
         				<td><fmt:formatNumber 
					    value="${c.electricity_charge}" 
					    type="number" 
					    minFractionDigits="2" 
					    maxFractionDigits="2"/></td>
         				<td>
         				<fmt:formatNumber 
					    value="${c.total_consumption_amount}" 
					    type="number" 
					    minFractionDigits="2" 
					    maxFractionDigits="2"/>
         				</td>
         				<td>
         				<fmt:formatNumber 
					    value="${c.revenue_without_vat_and_ssl}" 
					    type="number" 
					    minFractionDigits="2" 
					    maxFractionDigits="2"/>
         				</td>
         				<td><fmt:formatNumber 
					    value="${c.estimated_profit}" 
					    type="number" 
					    minFractionDigits="2" 
					    maxFractionDigits="2"/></td>
         			
         			</tr>
        		  </c:forEach>
		  </tbody>
        </table>
      </div>

      

      <div class="table-footer">
        <div class="page-info" id="pageInfo"></div>
        <ul class="pagination" id="pagination"></ul>
      </div>
    </div>

  </div><!-- /max-width -->

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
 
  <script>
  function downloadExcel() {
    const table = document.getElementById("reportTable");
    const workbook = XLSX.utils.table_to_book(table, {
      sheet: "Electricity Billing"
    });

    XLSX.writeFile(workbook, "electricity_billing_report.xlsx");
  }

  function downloadPDF() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF("l", "pt", "a4");

    doc.setFontSize(14);
    doc.text("Electricity Billing Report", 40, 30);

    doc.autoTable({
      html: "#reportTable",
      startY: 50,
      theme: "grid",
      styles: {
        fontSize: 8,
        cellPadding: 3
      },
      headStyles: {
        fillColor: [40, 40, 40]
      }
    });

    doc.save("electricity_billing_report.pdf");
  }
</script>
</body>
</html>