<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Dashboard</title>

  <!-- Bootstrap 5 CDN -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <!-- Bootstrap Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet"/>
  <!-- Chart.js CDN -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>

  <style>
    :root {
      --bg:        #0d0f14;
      --sidebar:   #0b0d12;
      --card-bg:   #13161d;
      --card2-bg:  #111318;
      --border:    #1f2330;
      --accent:    #c8f135;
      --accent-dk: #a8d020;
      --accent-dim:#c8f13522;
      --text:      #e8eaf0;
      --muted:     #6b7280;
      --danger:    #ff5c5c;
      --info:      #5cb8ff;
      --warn:      #ffb547;
      --sidebar-w: 240px;
    }

    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    body {
      font-family: 'DM Sans', sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      overflow-x: hidden;
    }

    /* ── SIDEBAR ───────────────────────────── */
    .sidebar {
      width: var(--sidebar-w);
      min-height: 100vh;
      background: var(--sidebar);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      position: fixed;
      top: 0; left: 0;
      z-index: 100;
      transition: transform .3s ease;
    }

    .sidebar-brand {
      display: flex;
      align-items: center;
      gap: .75rem;
      padding: 1.6rem 1.4rem 1.4rem;
      border-bottom: 1px solid var(--border);
    }
    .logo-mark {
      width: 36px; height: 36px;
      background: var(--accent);
      border-radius: 10px;
      display: grid; place-items: center;
      flex-shrink: 0;
    }
    .logo-mark i { font-size: 1.1rem; color: #0d0f14; }
    .brand-name {
      font-family: 'DM Serif Display', serif;
      font-size: 1.15rem;
      color: var(--text);
      letter-spacing: -.01em;
    }

    .sidebar-section-label {
      padding: 1.5rem 1.4rem .5rem;
      font-size: .68rem;
      font-weight: 700;
      letter-spacing: .1em;
      text-transform: uppercase;
      color: var(--muted);
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: .75rem;
      padding: .6rem 1.4rem;
      border-radius: 10px;
      margin: .1rem .7rem;
      color: var(--muted);
      font-size: .88rem;
      font-weight: 500;
      text-decoration: none;
      transition: background .15s, color .15s;
    }
    .nav-link i { font-size: 1rem; flex-shrink: 0; }
    .nav-link:hover { background: var(--accent-dim); color: var(--text); }
    .nav-link.active {
      background: var(--accent-dim);
      color: var(--accent);
    }
    .nav-link .badge-dot {
      margin-left: auto;
      width: 18px; height: 18px;
      background: var(--accent);
      color: #0d0f14;
      border-radius: 50%;
      font-size: .65rem;
      font-weight: 700;
      display: grid; place-items: center;
    }

    .sidebar-footer {
      margin-top: auto;
      padding: 1rem .7rem;
      border-top: 1px solid var(--border);
    }
    .user-row {
      display: flex;
      align-items: center;
      gap: .75rem;
      padding: .6rem .7rem;
      border-radius: 10px;
      cursor: pointer;
      transition: background .15s;
    }
    .user-row:hover { background: var(--accent-dim); }
    .avatar {
      width: 34px; height: 34px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--accent) 0%, #6ee7b7 100%);
      display: grid; place-items: center;
      font-weight: 700;
      font-size: .8rem;
      color: #0d0f14;
      flex-shrink: 0;
    }
    .user-info { flex: 1; min-width: 0; }
    .user-name { font-size: .84rem; font-weight: 600; color: var(--text); line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .user-role { font-size: .72rem; color: var(--muted); }

    /* ── MAIN ──────────────────────────────── */
    .main {
      margin-left: var(--sidebar-w);
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    /* Topbar */
    .topbar {
      height: 64px;
      background: var(--bg);
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      padding: 0 2rem;
      gap: 1rem;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .topbar-title {
      font-family: 'DM Serif Display', serif;
      font-size: 1.25rem;
      color: var(--text);
      flex: 1;
    }
    .topbar-subtitle { font-size: .8rem; color: var(--muted); font-family: 'DM Sans', sans-serif; display: block; margin-top: 1px; }
    .search-box {
      display: flex;
      align-items: center;
      gap: .5rem;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: .45rem .9rem;
      color: var(--muted);
      font-size: .84rem;
      transition: border-color .2s;
    }
    .search-box:focus-within { border-color: var(--accent); }
    .search-box input {
      background: transparent;
      border: none;
      outline: none;
      color: var(--text);
      font-family: 'DM Sans', sans-serif;
      font-size: .84rem;
      width: 180px;
    }
    .search-box input::placeholder { color: var(--muted); }

    .icon-btn {
      width: 38px; height: 38px;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 10px;
      display: grid; place-items: center;
      cursor: pointer;
      color: var(--muted);
      font-size: 1rem;
      transition: border-color .2s, color .2s;
      text-decoration: none;
    }
    .icon-btn:hover { border-color: var(--accent); color: var(--accent); }
    .notif-wrap { position: relative; }
    .notif-pip {
      position: absolute;
      top: 6px; right: 7px;
      width: 7px; height: 7px;
      background: var(--accent);
      border-radius: 50%;
      border: 2px solid var(--bg);
    }

    /* Content */
    .content { padding: 2rem; flex: 1; }

    /* ── STAT CARDS ─────────────────────────── */
    .stat-card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 1.4rem 1.5rem;
      position: relative;
      overflow: hidden;
      animation: fadeUp .5s ease both;
      transition: border-color .2s, transform .2s;
    }
    .stat-card:hover { border-color: #2e3445; transform: translateY(-2px); }
    .stat-card::after {
      content: '';
      position: absolute;
      top: -30px; right: -30px;
      width: 100px; height: 100px;
      border-radius: 50%;
      background: var(--accent-dim);
      pointer-events: none;
    }
    .stat-card.danger::after  { background: rgba(255,92,92,.08); }
    .stat-card.info::after    { background: rgba(92,184,255,.08); }
    .stat-card.warn::after    { background: rgba(255,181,71,.08); }

    .stat-label {
      font-size: .72rem;
      font-weight: 700;
      letter-spacing: .08em;
      text-transform: uppercase;
      color: var(--muted);
      margin-bottom: .6rem;
    }
    .stat-value {
      font-family: 'DM Serif Display', serif;
      font-size: 2rem;
      line-height: 1;
      color: var(--text);
      margin-bottom: .5rem;
    }
    .stat-change {
      font-size: .78rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: .2rem;
    }
    .stat-change.up   { color: var(--accent); }
    .stat-change.down { color: var(--danger); }
    .stat-icon {
      position: absolute;
      top: 1.4rem; right: 1.4rem;
      width: 40px; height: 40px;
      border-radius: 10px;
      display: grid; place-items: center;
      font-size: 1.1rem;
      background: var(--accent-dim);
      color: var(--accent);
    }
    .stat-card.danger .stat-icon { background: rgba(255,92,92,.12);  color: var(--danger); }
    .stat-card.info   .stat-icon { background: rgba(92,184,255,.12); color: var(--info);   }
    .stat-card.warn   .stat-icon { background: rgba(255,181,71,.12); color: var(--warn);   }

    /* ── CHART CARD ─────────────────────────── */
    .chart-card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 1.5rem;
      animation: fadeUp .5s .15s ease both;
    }
    .card-header-row {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 1.4rem;
    }
    .card-title {
      font-family: 'DM Serif Display', serif;
      font-size: 1.1rem;
      color: var(--text);
    }
    .card-meta { font-size: .78rem; color: var(--muted); margin-top: 2px; }
    .chip {
      background: var(--accent-dim);
      color: var(--accent);
      font-size: .72rem;
      font-weight: 700;
      padding: .25rem .65rem;
      border-radius: 20px;
      letter-spacing: .03em;
    }

    /* ── TABLE CARD ─────────────────────────── */
    .table-card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      overflow: hidden;
      animation: fadeUp .5s .25s ease both;
    }
    .table-card-header {
      padding: 1.2rem 1.5rem;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .table { margin: 0; }
    .table thead th {
      background: var(--card2-bg);
      border-color: var(--border);
      color: var(--muted);
      font-size: .72rem;
      font-weight: 700;
      letter-spacing: .07em;
      text-transform: uppercase;
      padding: .75rem 1.5rem;
    }
    .table tbody td {
      background: transparent;
      border-color: var(--border);
      color: var(--text);
      font-size: .86rem;
      padding: .85rem 1.5rem;
      vertical-align: middle;
    }
    .table tbody tr:hover td { background: var(--accent-dim); }
    .status-pill {
      display: inline-flex;
      align-items: center;
      gap: .3rem;
      padding: .22rem .65rem;
      border-radius: 20px;
      font-size: .72rem;
      font-weight: 700;
    }
    .status-pill.active  { background: rgba(200,241,53,.12); color: var(--accent); }
    .status-pill.pending { background: rgba(255,181,71,.12); color: var(--warn);   }
    .status-pill.failed  { background: rgba(255,92,92,.12);  color: var(--danger); }
    .status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

    /* ── ACTIVITY FEED ──────────────────────── */
    .activity-card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      animation: fadeUp .5s .3s ease both;
    }
    .activity-item {
      display: flex;
      gap: .9rem;
      padding: .9rem 1.4rem;
      border-bottom: 1px solid var(--border);
      transition: background .15s;
    }
    .activity-item:last-child { border-bottom: none; }
    .activity-item:hover { background: var(--accent-dim); }
    .act-icon {
      width: 34px; height: 34px;
      border-radius: 10px;
      display: grid; place-items: center;
      font-size: .9rem;
      flex-shrink: 0;
    }
    .act-icon.g { background: rgba(200,241,53,.12); color: var(--accent); }
    .act-icon.b { background: rgba(92,184,255,.12); color: var(--info);   }
    .act-icon.r { background: rgba(255,92,92,.12);  color: var(--danger); }
    .act-icon.y { background: rgba(255,181,71,.12); color: var(--warn);   }
    .act-body { flex: 1; min-width: 0; }
    .act-title { font-size: .84rem; font-weight: 500; color: var(--text); line-height: 1.3; }
    .act-time  { font-size: .72rem; color: var(--muted); margin-top: 2px; }

    /* ── PROGRESS CARD ──────────────────────── */
    .progress-card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 1.4rem;
      animation: fadeUp .5s .35s ease both;
    }
    .progress-row { margin-bottom: 1.1rem; }
    .progress-row:last-child { margin-bottom: 0; }
    .progress-meta { display: flex; justify-content: space-between; margin-bottom: .4rem; }
    .progress-label { font-size: .82rem; font-weight: 500; color: var(--text); }
    .progress-val   { font-size: .8rem; font-weight: 700; color: var(--accent); }
    .progress {
      height: 6px;
      border-radius: 99px;
      background: var(--border);
    }
    .progress-bar {
      border-radius: 99px;
      background: var(--accent);
      transition: width 1.2s cubic-bezier(.4,0,.2,1);
    }
    .progress-bar.info   { background: var(--info); }
    .progress-bar.warn   { background: var(--warn); }
    .progress-bar.danger { background: var(--danger); }

    /* ── ANIMATIONS ─────────────────────────── */
    @keyframes fadeUp {
      from { opacity: 0; transform: translateY(18px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    /* Stagger stat cards */
    .stat-card:nth-child(1) { animation-delay: .05s; }
    .stat-card:nth-child(2) { animation-delay: .10s; }
    .stat-card:nth-child(3) { animation-delay: .15s; }
    .stat-card:nth-child(4) { animation-delay: .20s; }

    /* ── MOBILE ─────────────────────────────── */
    .sidebar-toggle {
      display: none;
      background: none;
      border: none;
      color: var(--text);
      font-size: 1.4rem;
      cursor: pointer;
      padding: 0;
    }
    @media (max-width: 991px) {
      .sidebar { transform: translateX(-100%); }
      .sidebar.open { transform: translateX(0); }
      .main { margin-left: 0; }
      .sidebar-toggle { display: block; }
      .search-box { display: none; }
    }
  </style>
</head>
<body>

  <!-- ── SIDEBAR ── -->
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-brand">
      <div class="logo-mark"><i class="bi bi-layers-fill"></i></div>
      <span class="brand-name">GreenEV Electricity Bill</span>
    </div>

    <div class="sidebar-section-label">Main</div>
    <nav>
      <a href="#" class="nav-link active"><i class="bi bi-grid-1x2-fill"></i> Overview</a>
     
    </nav>

    <div class="sidebar-section-label">Manage</div>
    <nav>
      <a target="_blank" href="reportform" class="nav-link"><i class="bi bi-people-fill"></i>Detail Report</a>
      
    </nav>

    <div class="sidebar-footer">
      <div class="user-row">
        <div class="avatar">AK</div>
        <div class="user-info">
          <div class="user-name">Alex Kim</div>
          <div class="user-role">Admin</div>
        </div>
        <i class="bi bi-three-dots-vertical" style="color:var(--muted);font-size:.9rem"></i>
      </div>
    </div>
  </aside>

  <!-- ── MAIN ── -->
  <div class="main">

    <!-- Topbar -->
    <header class="topbar">
      <button class="sidebar-toggle me-2" onclick="document.getElementById('sidebar').classList.toggle('open')">
        <i class="bi bi-list"></i>
      </button>
      <div class="topbar-title">
        Overview
        <span class="topbar-subtitle">Sunday, April 19 2026</span>
      </div>

      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" placeholder="Search anything…"/>
      </div>

      <div class="notif-wrap">
        <a href="#" class="icon-btn"><i class="bi bi-bell"></i></a>
        <span class="notif-pip"></span>
      </div>
      <a href="logout" class="icon-btn" title="Log out"><i class="bi bi-box-arrow-right"></i></a>
    </header>

    <!-- Content -->
    <div class="content">

      <!-- ── STAT CARDS ── -->
      <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-currency-dollar"></i></div>
            <div class="stat-label">Total Revenue</div>
            <div class="stat-value">$84,320</div>
            <div class="stat-change up"></div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="stat-card info">
            <div class="stat-icon"><i class="bi bi-people-fill"></i></div>
            <div class="stat-label">Active Users</div>
            <div class="stat-value">3,741</div>
            <div class="stat-change up"></div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="stat-card warn">
            <div class="stat-icon"><i class="bi bi-cart3"></i></div>
            <div class="stat-label">New Orders</div>
            <div class="stat-value">1,208</div>
            <div class="stat-change down"></div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="stat-card danger">
            <div class="stat-icon"><i class="bi bi-x-circle-fill"></i></div>
            <div class="stat-label">Churn Rate</div>
            <div class="stat-value">2.6%</div>
            <div class="stat-change down"></div>
          </div>
        </div>
      </div>

      <!-- ── CHART + ACTIVITY ── -->
      <div class="row g-3 mb-4">
        <!-- Revenue Chart -->
        <div class="col-lg-8">
          <div class="chart-card h-100">
            <div class="card-header-row">
              <div>
                <div class="card-title">Revenue Overview</div>
                <div class="card-meta">Monthly performance · 2026</div>
              </div>
              <span class="chip">↑ Live</span>
            </div>
            <canvas id="revenueChart" style="max-height:260px"></canvas>
          </div>
        </div>

        <!-- Activity Feed -->
        <div class="col-lg-4">
          <div class="activity-card h-100 d-flex flex-column">
            <div class="table-card-header">
              <div class="card-title">Activity</div>
              <a href="#" style="color:var(--accent);font-size:.8rem;text-decoration:none">View all</a>
            </div>
            <div class="activity-item">
              <div class="act-icon g"><i class="bi bi-check-lg"></i></div>
              <div class="act-body">
                <div class="act-title">Payment received from <strong>Mia Chen</strong></div>
                <div class="act-time">2 minutes ago</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="act-icon b"><i class="bi bi-person-plus-fill"></i></div>
              <div class="act-body">
                <div class="act-title">New user <strong>Jake Warren</strong> signed up</div>
                <div class="act-time">17 minutes ago</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="act-icon r"><i class="bi bi-exclamation-triangle-fill"></i></div>
              <div class="act-body">
                <div class="act-title">Server CPU spike — <strong>us-east-2</strong></div>
                <div class="act-time">43 minutes ago</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="act-icon y"><i class="bi bi-arrow-repeat"></i></div>
              <div class="act-body">
                <div class="act-title">Scheduled report <strong>Q2 Summary</strong> generated</div>
                <div class="act-time">1 hour ago</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="act-icon g"><i class="bi bi-shield-check"></i></div>
              <div class="act-body">
                <div class="act-title">Security scan passed — no threats</div>
                <div class="act-time">2 hours ago</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── TABLE + PROGRESS ── -->
      <div class="row g-3">
        <!-- Orders Table -->
        <div class="col-lg-8">
          <div class="table-card">
            <div class="table-card-header">
              <div class="card-title">Recent Orders</div>
              <span class="chip">Last 24h</span>
            </div>
            <div class="table-responsive">
              <table class="table mb-0">
                <thead>
                  <tr>
                    <th>Order</th>
                    <th>Customer</th>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td><span style="color:var(--muted)">#ORD-4821</span></td>
                    <td>Lena Park</td>
                    <td style="color:var(--accent);font-weight:600">$340.00</td>
                    <td style="color:var(--muted)">Apr 19, 2026</td>
                    <td><span class="status-pill active"><span class="status-dot"></span>Completed</span></td>
                  </tr>
                  <tr>
                    <td><span style="color:var(--muted)">#ORD-4820</span></td>
                    <td>Tom Nguyen</td>
                    <td style="color:var(--accent);font-weight:600">$128.50</td>
                    <td style="color:var(--muted)">Apr 19, 2026</td>
                    <td><span class="status-pill pending"><span class="status-dot"></span>Pending</span></td>
                  </tr>
                  <tr>
                    <td><span style="color:var(--muted)">#ORD-4819</span></td>
                    <td>Sara Okonkwo</td>
                    <td style="color:var(--accent);font-weight:600">$875.00</td>
                    <td style="color:var(--muted)">Apr 18, 2026</td>
                    <td><span class="status-pill active"><span class="status-dot"></span>Completed</span></td>
                  </tr>
                  <tr>
                    <td><span style="color:var(--muted)">#ORD-4818</span></td>
                    <td>Marco Ricci</td>
                    <td style="color:var(--accent);font-weight:600">$59.99</td>
                    <td style="color:var(--muted)">Apr 18, 2026</td>
                    <td><span class="status-pill failed"><span class="status-dot"></span>Failed</span></td>
                  </tr>
                  <tr>
                    <td><span style="color:var(--muted)">#ORD-4817</span></td>
                    <td>Anya Sharma</td>
                    <td style="color:var(--accent);font-weight:600">$212.00</td>
                    <td style="color:var(--muted)">Apr 17, 2026</td>
                    <td><span class="status-pill active"><span class="status-dot"></span>Completed</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Goals Progress -->
        <div class="col-lg-4">
          <div class="progress-card h-100">
            <div class="card-header-row mb-3">
              <div class="card-title">Monthly Goals</div>
            </div>
            <div class="progress-row">
              <div class="progress-meta">
                <span class="progress-label">Revenue Target</span>
                <span class="progress-val">84%</span>
              </div>
              <div class="progress"><div class="progress-bar" style="width:84%"></div></div>
            </div>
            <div class="progress-row">
              <div class="progress-meta">
                <span class="progress-label">New Signups</span>
                <span class="progress-val" style="color:var(--info)">61%</span>
              </div>
              <div class="progress"><div class="progress-bar info" style="width:61%"></div></div>
            </div>
            <div class="progress-row">
              <div class="progress-meta">
                <span class="progress-label">Support Tickets Closed</span>
                <span class="progress-val" style="color:var(--warn)">72%</span>
              </div>
              <div class="progress"><div class="progress-bar warn" style="width:72%"></div></div>
            </div>
            <div class="progress-row">
              <div class="progress-meta">
                <span class="progress-label">Churn Reduction</span>
                <span class="progress-val" style="color:var(--danger)">45%</span>
              </div>
              <div class="progress"><div class="progress-bar danger" style="width:45%"></div></div>
            </div>
            <div class="progress-row">
              <div class="progress-meta">
                <span class="progress-label">Upsell Conversions</span>
                <span class="progress-val">93%</span>
              </div>
              <div class="progress"><div class="progress-bar" style="width:93%"></div></div>
            </div>
          </div>
        </div>
      </div>

    </div><!-- /content -->
  </div><!-- /main -->

  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <script>
    // Revenue Chart
    const ctx = document.getElementById('revenueChart').getContext('2d');

    const gradient = ctx.createLinearGradient(0, 0, 0, 260);
    gradient.addColorStop(0, 'rgba(200,241,53,0.18)');
    gradient.addColorStop(1, 'rgba(200,241,53,0.00)');

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
        datasets: [{
          label: 'Revenue',
          data: [42000, 51000, 47000, 58000, 63000, 57000, 71000, 68000, 75000, 80000, 78000, 84320],
          borderColor: '#c8f135',
          borderWidth: 2.5,
          pointBackgroundColor: '#c8f135',
          pointBorderColor: '#13161d',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
          fill: true,
          backgroundColor: gradient,
          tension: 0.42,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#13161d',
            borderColor: '#1f2330',
            borderWidth: 1,
            titleColor: '#6b7280',
            bodyColor: '#e8eaf0',
            padding: 12,
            callbacks: {
              label: ctx => ' $' + ctx.parsed.y.toLocaleString()
            }
          }
        },
        scales: {
          x: {
            grid: { color: '#1f2330' },
            ticks: { color: '#6b7280', font: { family: 'DM Sans', size: 11 } }
          },
          y: {
            grid: { color: '#1f2330' },
            ticks: {
              color: '#6b7280',
              font: { family: 'DM Sans', size: 11 },
              callback: v => '$' + (v/1000).toFixed(0) + 'k'
            }
          }
        }
      }
    });

    // Animate progress bars on load
    document.querySelectorAll('.progress-bar').forEach(bar => {
      const w = bar.style.width;
      bar.style.width = '0';
      setTimeout(() => bar.style.width = w, 200);
    });
  </script>
</body>
</html>