<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>Report Generate</title>

<!-- Bootstrap -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>

<!-- Bootstrap Icons -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet"/>

<!-- Google Fonts -->
<link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet"/>

<style>
:root{
    --bg:#0d0f14;
    --card-bg:#13161d;
    --border:#1f2330;
    --accent:#c8f135;
    --accent-dark:#a8d020;
    --text:#e8eaf0;
    --muted:#6b7280;
    --input-bg:#0d0f14;
}

*{
    box-sizing:border-box;
}

body{
    margin:0;
    min-height:100vh;
    background:var(--bg);
    font-family:'DM Sans',sans-serif;
    display:flex;
    align-items:center;
    justify-content:center;
    padding:40px 15px;
}

/* Background Glow */
body::before,
body::after{
    content:'';
    position:fixed;
    border-radius:50%;
    filter:blur(120px);
    z-index:0;
}

body::before{
    width:500px;
    height:500px;
    background:rgba(200,241,53,.06);
    top:-100px;
    right:-100px;
}

body::after{
    width:400px;
    height:400px;
    background:rgba(100,120,255,.05);
    bottom:-100px;
    left:-100px;
}

.form-wrapper{
    position:relative;
    z-index:1;
    width:100%;
    max-width:650px;
}

.logo-mark{
    width:50px;
    height:50px;
    background:var(--accent);
    border-radius:14px;
    display:grid;
    place-items:center;
    margin-bottom:20px;
}

.logo-mark i{
    color:#000;
    font-size:1.4rem;
}

h1{
    font-family:'DM Serif Display',serif;
    color:var(--text);
    margin-bottom:8px;
}

.subtitle{
    color:var(--muted);
    margin-bottom:30px;
}

.card{
    background:var(--card-bg);
    border:1px solid var(--border);
    border-radius:24px;
    padding:35px;
}

label{
    color:var(--muted);
    font-size:.78rem;
    font-weight:600;
    letter-spacing:.06em;
    text-transform:uppercase;
    margin-bottom:8px;
}

.input-group-text{
    background:var(--input-bg);
    border:1px solid var(--border);
    border-right:none;
    color:var(--muted);
}

.form-control,
.form-select{
    background:var(--input-bg);
    border:1px solid var(--border);
    color:var(--text);
    border-left:none;
    padding:.75rem .9rem;
}

.form-select{
    border-left:1px solid var(--border);
}

.form-control:focus,
.form-select:focus{
    background:var(--input-bg);
    color:var(--text);
    border-color:var(--accent);
    box-shadow:none;
}

.input-group:focus-within .input-group-text{
    border-color:var(--accent);
}

.form-control::placeholder{
    color:#444b5e;
}

.btn-submit{
    background:var(--accent);
    border:none;
    color:#000;
    font-weight:700;
    padding:.9rem;
    border-radius:14px;
    transition:.2s;
}

.btn-submit:hover{
    background:var(--accent-dark);
    transform:translateY(-2px);
}

.form-check-label{
    color:var(--muted);
    text-transform:none;
    letter-spacing:0;
}

.form-check-input{
    background-color:var(--input-bg);
    border-color:var(--border);
}

.form-check-input:checked{
    background-color:var(--accent);
    border-color:var(--accent);
}
</style>
</head>

<body>

<div class="form-wrapper">



   

    <div class="card">
		<div class="card mt-4">
  <form action="adminreport" method="post">

    <div class="row g-3">

      <!-- Station List -->
      <div class="col-md-6">
        <label>Station List</label>
        <select name="station" class="form-select" id="statusFilter">
          <option value="">Select Station</option>
          <c:forEach var="c" items="${stationsLists}">
            <option value="${c.stationName}">${c.stationName}</option>
          </c:forEach>
        </select>
      </div>

      <!-- Date From -->
      <div class="col-md-3">
        <label>Start Date</label>
        <input class="form-control" type="date" name="start_date" id="dateFrom">
      </div>

      <!-- Date To -->
      <div class="col-md-3">
        <label>End Date</label>
        <input class="form-control" type="date" name="end_date" id="dateTo">
      </div>

      <!-- Main Tariff Category -->
      <div class="col-md-6">
        <label>Select Tariff Category</label>
        <select id="category"
                onchange="tiggerTariffCatglog()"
                class="form-select"
                name="category">
          <option value="0">Select Your Category</option>
          <option value="1">Domestic Category</option>
          <option value="2">Domestic Consumers (Optional)</option>
          <option value="3">Domestic Category Religious</option>
          <option value="4">Other Consumer Category</option>
        </select>
      </div>

      <!-- Tariff Sub Category -->
      <div id="visibility_lock" class="col-md-6">
        <label>Select Tariff Sub Category</label>
        <select id="charger" class="form-select" name="tariff_category">
          <option value="">Select Sub Category</option>
        </select>
      </div>

      <!-- Buttons -->
      <div class="col-12 d-flex gap-2 mt-4">
        <button type="submit" class="btn btn-submit">
          <i class="bi bi-funnel-fill me-1"></i>
          Apply
        </button>

        <button type="reset" class="btn btn-secondary">
          <i class="bi bi-arrow-counterclockwise me-1"></i>
          Reset
        </button>
      </div>

    </div>

  </form>
</div>

    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
 <script type="text/javascript">
  	function tiggerTariffCatglog(){
  		let category = document.getElementById("category").value;
  		
  		if(category == 1){
  				
  		}else if(category == 2){
  			
  			
  		}else if(category == 3){
  			
  			
  		}else if(category == 4){
  			
  			fetch("getTariffList")
  		  .then(response => {
  		    if (!response.ok) {
  		      throw new Error("Network response was not ok");
  		    }
  		    return response.json();
  		  })
  		  .then(data => {
  		    const charger = document.getElementById("charger");
			
  		    // Clear existing options
  		    charger.innerHTML = "";

  		    // Default option
  		    const defaultOption = document.createElement("option");
  		    defaultOption.text = "Select Charger";
  		    defaultOption.value = "";
  		    charger.appendChild(defaultOption);

  		    console.log(data);

  		    data.forEach(z => {
  		      const option = document.createElement("option");
  		      option.value = z.id;
  		      option.text = z.description;
  		      charger.appendChild(option);
  		    });
  		  })
  		  .catch(error => {
  		    console.error("Error fetching tariff list:", error);
  		  });
  		}
  		
  	}
  </script>
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