<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Login</title>

  <!-- Bootstrap 5 CDN -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <!-- Bootstrap Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet"/>

  <style>
    :root {
      --bg: #0d0f14;
      --card-bg: #13161d;
      --border: #1f2330;
      --accent: #c8f135;
      --accent-dark: #a8d020;
      --text: #e8eaf0;
      --muted: #6b7280;
      --input-bg: #0d0f14;
    }

    * { box-sizing: border-box; }

    body {
      margin: 0;
      min-height: 100vh;
      background: var(--bg);
      font-family: 'DM Sans', sans-serif;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
    }

    /* Ambient glow blobs */
    body::before, body::after {
      content: '';
      position: fixed;
      border-radius: 50%;
      filter: blur(120px);
      pointer-events: none;
      z-index: 0;
    }
    body::before {
      width: 500px; height: 500px;
      background: rgba(200, 241, 53, 0.06);
      top: -100px; right: -150px;
    }
    body::after {
      width: 400px; height: 400px;
      background: rgba(100, 120, 255, 0.05);
      bottom: -100px; left: -100px;
    }

    .login-wrapper {
      position: relative;
      z-index: 1;
      width: 100%;
      max-width: 420px;
      padding: 1.5rem;
      animation: fadeUp .6s ease both;
    }

    @keyframes fadeUp {
      from { opacity: 0; transform: translateY(24px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    /* Logo mark */
    .logo-mark {
      width: 44px; height: 44px;
      background: var(--accent);
      border-radius: 12px;
      display: grid;
      place-items: center;
      margin-bottom: 2rem;
    }
    .logo-mark i { font-size: 1.3rem; color: #0d0f14; }

    h1 {
      font-family: 'DM Serif Display', serif;
      font-size: 2rem;
      color: var(--text);
      margin: 0 0 .4rem;
      line-height: 1.1;
    }
    .subtitle {
      color: var(--muted);
      font-size: .9rem;
      margin-bottom: 2.2rem;
    }

    /* Card */
    .card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 20px;
      padding: 2rem;
    }

    /* Labels */
    label {
      color: var(--muted);
      font-size: .78rem;
      font-weight: 600;
      letter-spacing: .06em;
      text-transform: uppercase;
      margin-bottom: .4rem;
      display: block;
    }

    /* Inputs */
    .input-group-text {
      background: var(--input-bg);
      border: 1px solid var(--border);
      border-right: none;
      color: var(--muted);
    }
    .form-control {
      background: var(--input-bg);
      border: 1px solid var(--border);
      border-left: none;
      color: var(--text);
      font-family: 'DM Sans', sans-serif;
      padding: .65rem .9rem;
      transition: border-color .2s;
    }
    .form-control::placeholder { color: #3a3f50; }
    .form-control:focus {
      background: var(--input-bg);
      border-color: var(--accent);
      color: var(--text);
      box-shadow: none;
    }
    .form-control:focus + .input-group-text,
    .input-group:focus-within .input-group-text {
      border-color: var(--accent);
    }
    .input-group:focus-within .input-group-text {
      border-color: var(--accent);
    }

    /* Checkbox */
    .form-check-input {
      background-color: var(--input-bg);
      border-color: var(--border);
    }
    .form-check-input:checked {
      background-color: var(--accent);
      border-color: var(--accent);
    }
    .form-check-label { color: var(--muted); font-size: .86rem; }

    /* Forgot link */
    .forgot-link {
      color: var(--accent);
      font-size: .86rem;
      text-decoration: none;
      font-weight: 500;
    }
    .forgot-link:hover { color: var(--accent-dark); }

    /* Submit btn */
    .btn-login {
      background: var(--accent);
      color: #0d0f14;
      border: none;
      border-radius: 12px;
      font-weight: 700;
      font-size: .95rem;
      padding: .75rem;
      letter-spacing: .02em;
      transition: background .2s, transform .15s;
    }
    .btn-login:hover {
      background: var(--accent-dark);
      color: #0d0f14;
      transform: translateY(-1px);
    }
    .btn-login:active { transform: translateY(0); }

    /* Divider */
    .divider {
      display: flex;
      align-items: center;
      gap: .8rem;
      color: var(--muted);
      font-size: .78rem;
      margin: 1.4rem 0;
    }
    .divider::before, .divider::after {
      content: '';
      flex: 1;
      height: 1px;
      background: var(--border);
    }

    /* Social btn */
    .btn-social {
      background: transparent;
      border: 1px solid var(--border);
      border-radius: 12px;
      color: var(--text);
      font-size: .88rem;
      font-weight: 500;
      padding: .62rem;
      transition: border-color .2s, background .2s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: .5rem;
    }
    .btn-social:hover {
      border-color: #3a3f50;
      background: #1a1d26;
      color: var(--text);
    }

    /* Footer */
    .card-footer-text {
      text-align: center;
      color: var(--muted);
      font-size: .84rem;
      margin-top: 1.4rem;
    }
    .card-footer-text a {
      color: var(--accent);
      text-decoration: none;
      font-weight: 600;
    }
    .card-footer-text a:hover { color: var(--accent-dark); }
  </style>
</head>
<body>

  <div class="login-wrapper">
    <div class="logo-mark"><i class="bi bi-layers-fill"></i></div>
    <h1>Welcome back.</h1>
    <p class="subtitle">Sign in to continue to your workspace.</p>

    <div class="card">
      <form action="login" method="post">
        <!-- Email -->
        <div class="mb-3">
          <label for="email">Email address</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-envelope"></i></span>
            <input type="text" name="username" class="form-control" id="text" placeholder="username" autocomplete="username"/>
          </div>
        </div>

        <!-- Password -->
        <div class="mb-3">
          <label for="password">Password</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-lock"></i></span>
            <input type="password" name="password" class="form-control" id="password" placeholder="••••••••" autocomplete="current-password"/>
          </div>
        </div>

        <!-- Remember / Forgot -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div class="form-check mb-0">
            <input class="form-check-input" type="checkbox" id="remember"/>
            <label class="form-check-label" for="remember">Remember me</label>
          </div>
          <a href="#" class="forgot-link">Forgot password?</a>
        </div>

        <!-- Submit -->
        <button type="submit" class="btn btn-login w-100">Sign in &nbsp;<i class="bi bi-arrow-right"></i></button>
      </form>

      <div class="divider">or continue with</div>

      <div class="row g-2">
        <div class="col-6">
          <button class="btn btn-social w-100">
            <!-- Google SVG icon -->
            <svg width="18" height="18" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
              <path fill="#FFC107" d="M43.6 20.1H42V20H24v8h11.3C33.7 32.7 29.2 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34.5 6.5 29.5 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.7-.4-3.9z"/>
              <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.6 15.1 18.9 12 24 12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34.5 6.5 29.5 4 24 4 16.3 4 9.7 8.3 6.3 14.7z"/>
              <path fill="#4CAF50" d="M24 44c5.2 0 9.9-1.9 13.5-5.1l-6.2-5.2C29.4 35.5 26.8 36 24 36c-5.2 0-9.6-3.2-11.3-7.9l-6.5 5C9.6 39.6 16.3 44 24 44z"/>
              <path fill="#1976D2" d="M43.6 20.1H42V20H24v8h11.3c-.8 2.3-2.3 4.3-4.3 5.7l6.2 5.2C37 38.3 44 33 44 24c0-1.3-.1-2.7-.4-3.9z"/>
            </svg>
            Google
          </button>
        </div>
        <div class="col-6">
          <button class="btn btn-social w-100">
            <i class="bi bi-github" style="font-size:1.1rem"></i>
            GitHub
          </button>
        </div>
      </div>
    </div>

    
  </div>

  <!-- Bootstrap 5 JS Bundle CDN -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>