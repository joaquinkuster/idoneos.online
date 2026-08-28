const fs = require('fs');
const path = require('path');

const imgPath = 'c:/Users/Lazaro/Desktop/TF/idoneos.online/src/main/resources/static/img/logos/image.png';
const base64 = fs.readFileSync(imgPath).toString('base64');
const dataUri = 'data:image/png;base64,' + base64;

const srcHtml = 'c:/Users/Lazaro/Desktop/TF/idoneos.online/docs/diseño/prototipos_reportes_certificados.html';
const raw = fs.readFileSync(srcHtml, 'utf8');

const figmaHtml = `<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Idóneos Online - Figma Import Canvas</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@700;800;900&family=Inter:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root {
            --wf-navy-dark: #081426;
            --wf-navy-card: #0F233D;
            --wf-gold: #D4A03D;
            --wf-gold-dark: #92400E;
            --wf-gold-light: #FEF3C7;
            --wf-bg: #F1F5F9;
            --wf-card-bg: #FFFFFF;
            --wf-text: #0F172A;
            --wf-text-muted: #64748B;
            --wf-border: #CBD5E1;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            -webkit-font-smoothing: antialiased;
        }

        body {
            background-color: var(--wf-bg);
            color: var(--wf-text);
            padding: 60px;
            display: flex;
            flex-direction: column;
            gap: 80px;
            align-items: center;
        }

        .figma-board-title {
            font-size: 28px;
            font-weight: 900;
            color: #081426;
            text-align: center;
            letter-spacing: -0.5px;
        }

        .figma-board-title span {
            color: var(--wf-gold);
        }

        .figma-board-subtitle {
            font-size: 14px;
            color: #64748B;
            text-align: center;
            margin-top: 6px;
        }

        .figma-section-frame {
            display: flex;
            flex-direction: column;
            gap: 24px;
            align-items: center;
            width: 100%;
        }

        .figma-frame-header {
            font-size: 16px;
            font-weight: 800;
            color: #081426;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            padding: 8px 20px;
            border-radius: 20px;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.04);
        }

        .figma-frame-header span.badge {
            background: #081426;
            color: #FFFFFF;
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 12px;
        }

        .pages-row {
            display: flex;
            gap: 40px;
            flex-wrap: wrap;
            justify-content: center;
        }

        /* 1. INFORMES EJECUTIVOS A4 */
        .a4-page {
            width: 794px;
            min-height: 1123px;
            background: #FFFFFF;
            padding: 40px 48px;
            box-shadow: 0 12px 30px rgba(0,0,0,0.08);
            border-radius: 4px;
            border: 1px solid #CBD5E1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .report-header-grid {
            display: grid;
            grid-template-columns: 240px 1fr;
            border: 1.5px solid #081426;
            border-radius: 6px;
            overflow: hidden;
            margin-bottom: 24px;
        }

        .report-header-logo {
            background: #FFFFFF;
            border-right: 1.5px solid #081426;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 12px 18px;
            gap: 10px;
        }

        .report-header-logo img {
            height: 38px;
            width: auto;
            object-fit: contain;
        }

        .report-header-title-box {
            background: #081426;
            color: #FFFFFF;
            padding: 16px 24px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            text-align: right;
        }

        .report-header-title {
            font-size: 16px;
            font-weight: 900;
            letter-spacing: 0.5px;
            text-transform: uppercase;
        }

        .report-header-meta {
            font-size: 10.5px;
            color: #94A3B8;
            margin-top: 4px;
        }

        .kpi-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            margin-bottom: 26px;
        }

        .kpi-box {
            border: 1px solid #CBD5E1;
            border-radius: 6px;
            padding: 12px 14px;
            text-align: center;
            background: #F8FAFC;
        }

        .kpi-num {
            font-size: 20px;
            font-weight: 900;
            color: #081426;
            line-height: 1.1;
        }

        .kpi-label {
            font-size: 9px;
            font-weight: 800;
            color: #64748B;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-top: 4px;
        }

        .chart-section {
            border: 1px solid #E2E8F0;
            border-radius: 8px;
            padding: 16px 20px;
            margin-bottom: 20px;
            background: #FFFFFF;
        }

        .chart-header {
            margin-bottom: 12px;
        }

        .chart-title {
            font-size: 12px;
            font-weight: 800;
            color: #081426;
            text-transform: uppercase;
            letter-spacing: 0.4px;
        }

        .chart-subtitle {
            font-size: 10px;
            color: #64748B;
            margin-top: 2px;
        }

        .hbar-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
            margin-top: 10px;
        }

        .hbar-item {
            display: flex;
            flex-direction: column;
            gap: 3px;
        }

        .hbar-label-row {
            display: flex;
            justify-content: space-between;
            font-size: 10.5px;
            font-weight: 600;
            color: #334155;
        }

        .hbar-track {
            height: 14px;
            background: #F1F5F9;
            border-radius: 3px;
            overflow: hidden;
            display: flex;
        }

        .hbar-fill {
            height: 100%;
            background: #2D4A6E;
            border-radius: 3px;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            padding-right: 6px;
            color: #FFFFFF;
            font-size: 9px;
            font-weight: 700;
        }

        .hbar-fill.highlight {
            background: #081426;
        }

        .hbar-fill.success {
            background: #059669;
        }

        .hbar-fill.gold {
            background: var(--wf-gold);
            color: #081426;
        }

        .line-chart-svg {
            width: 100%;
            height: 160px;
            margin-top: 8px;
        }

        .donut-wrap {
            display: flex;
            align-items: center;
            justify-content: space-around;
            padding: 10px 0;
        }

        .donut-legend {
            display: flex;
            flex-direction: column;
            gap: 8px;
            font-size: 11px;
        }

        .legend-item {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 3px;
        }

        .report-page-footer {
            border-top: 1px solid #E2E8F0;
            padding-top: 10px;
            display: flex;
            justify-content: space-between;
            font-size: 9px;
            color: #94A3B8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* 2. CERTIFICADO LANDSCAPE */
        .cert-landscape {
            width: 960px;
            height: 680px;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            box-shadow: 0 16px 45px rgba(0,0,0,0.12);
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 50px 70px;
            border-radius: 4px;
        }

        .cert-top-wave {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 80px;
            background: linear-gradient(135deg, #081426 0%, #0F233D 70%, #D4A03D 100%);
            clip-path: polygon(0 0, 100% 0, 100% 45px, 0 80px);
            z-index: 2;
        }

        .cert-top-gold-line {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 86px;
            background: var(--wf-gold);
            clip-path: polygon(0 0, 100% 0, 100% 50px, 0 86px);
            z-index: 1;
        }

        .cert-header-logos {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 20px;
            z-index: 10;
        }

        .cert-brand-block {
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .cert-brand-block img {
            height: 48px;
            width: auto;
            object-fit: contain;
        }

        .cert-brand-title {
            font-size: 19px;
            font-weight: 900;
            color: #081426;
            letter-spacing: -0.3px;
        }

        .cert-brand-title span {
            color: var(--wf-gold);
        }

        .cert-endorsers {
            display: flex;
            align-items: center;
            gap: 16px;
            opacity: 0.85;
            font-size: 11px;
            font-weight: 700;
            color: #475569;
        }

        .cert-body-center {
            text-align: center;
            z-index: 10;
            margin: 20px 0;
        }

        .cert-institution-intro {
            font-size: 12px;
            color: #64748B;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            margin-bottom: 12px;
        }

        .cert-main-title {
            font-family: 'Cinzel', serif;
            font-size: 40px;
            font-weight: 900;
            color: #081426;
            letter-spacing: 6px;
            margin-bottom: 20px;
        }

        .cert-awarded-row {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 14px;
            font-size: 16px;
            color: #334155;
            margin-bottom: 18px;
        }

        .cert-student-name {
            font-size: 26px;
            font-weight: 800;
            color: #081426;
            border-bottom: 2px solid var(--wf-gold);
            padding: 0 20px 4px;
            display: inline-block;
        }

        .cert-student-dni {
            font-size: 16px;
            font-weight: 700;
            color: #475569;
        }

        .cert-paragraph {
            max-width: 760px;
            margin: 0 auto;
            font-size: 14px;
            line-height: 1.6;
            color: #334155;
        }

        .cert-paragraph strong {
            color: #081426;
        }

        .cert-footer-signatures {
            display: flex;
            justify-content: space-around;
            align-items: flex-end;
            z-index: 10;
            padding-top: 20px;
        }

        .sig-block {
            text-align: center;
            width: 220px;
        }

        .sig-image-holder {
            height: 50px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Cinzel', cursive;
            font-size: 22px;
            color: #0F233D;
            font-weight: 700;
            transform: rotate(-3deg);
        }

        .sig-line {
            border-top: 1.5px solid #081426;
            margin-top: 4px;
            padding-top: 4px;
        }

        .sig-name {
            font-size: 12px;
            font-weight: 800;
            color: #081426;
        }

        .sig-role {
            font-size: 10px;
            color: #64748B;
        }

        .cert-bottom-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 10px;
            color: #94A3B8;
            z-index: 10;
            border-top: 1px solid #F1F5F9;
            padding-top: 8px;
        }

        .cert-qr-mini {
            display: flex;
            align-items: center;
            gap: 6px;
            font-family: 'JetBrains Mono', monospace;
            font-weight: 700;
            color: #081426;
        }

        /* 3. COMPROBANTE OFICIAL DE PAGO */
        .ticket-page {
            width: 640px;
            background: #FFFFFF;
            padding: 44px 50px;
            box-shadow: 0 12px 35px rgba(0,0,0,0.12);
            border-radius: 8px;
            border: 1px solid #CBD5E1;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        .ticket-brand-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 2px solid var(--wf-gold);
            padding-bottom: 16px;
        }

        .ticket-brand-left {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .ticket-brand-left img {
            height: 42px;
            width: auto;
            object-fit: contain;
        }

        .ticket-brand-text {
            font-size: 17px;
            font-weight: 900;
            color: #081426;
            letter-spacing: -0.3px;
        }

        .ticket-brand-text span {
            color: var(--wf-gold);
        }

        .ticket-corp-tag {
            font-size: 10.5px;
            font-weight: 800;
            color: #64748B;
            text-transform: uppercase;
            background: #F8FAFC;
            padding: 4px 10px;
            border-radius: 4px;
            border: 1px solid #E2E8F0;
        }

        .ticket-item {
            display: flex;
            flex-direction: column;
            gap: 3px;
        }

        .ticket-label {
            font-size: 11px;
            color: #64748B;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.4px;
        }

        .ticket-val-main {
            font-size: 15px;
            font-weight: 800;
            color: #081426;
        }

        .ticket-amount-big {
            font-size: 30px;
            font-weight: 900;
            color: #081426;
            letter-spacing: -0.5px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .ticket-divider {
            border-top: 1px solid #F1F5F9;
            margin: 2px 0;
        }

        .ticket-section-title {
            font-size: 12px;
            font-weight: 800;
            color: #081426;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-top: 4px;
        }

        .ticket-mono-box {
            background: #F8FAFC;
            border: 1px solid #E2E8F0;
            padding: 9px 12px;
            border-radius: 6px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 11.5px;
            color: #081426;
            word-break: break-all;
        }

        .ticket-footer-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 10px;
            color: #94A3B8;
            border-top: 1px solid #E2E8F0;
            padding-top: 12px;
            margin-top: 6px;
        }

        .badge-acreditado {
            background: #DCFCE7;
            color: #166534;
            border: 1px solid #86EFAC;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 800;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }
    </style>
</head>
<body>

    <header style="text-align: center;">
        <h1 class="figma-board-title">IDÓNEOS <span>ONLINE</span> • FIGMA IMPORT CANVAS</h1>
        <p class="figma-board-subtitle">Vistas listas para importar con el plugin 'HTML to Design' o 'Figma to Code' en Figma</p>
    </header>

    <!-- 1. FRAME: CU-96 INFORME DE ALUMNOS (2 PÁGINAS) -->
    <section class="figma-section-frame">
        <div class="figma-frame-header">
            <span class="badge">CU-96</span>
            <span>Informe de Alumnos por Curso (Páginas 1 y 2 con Tasa de Abandono)</span>
        </div>

        <div class="pages-row">
            <!-- PÁGINA 1 -->
            <div class="a4-page">
                <div>
                    <div class="report-header-grid">
                        <div class="report-header-logo">
                            <img src="${dataUri}" alt="Logo">
                            <div>
                                <div style="font-size: 13px; font-weight: 900; color: #081426; line-height: 1.1;">IDÓNEOS ONLINE</div>
                                <div style="font-size: 8px; font-weight: 700; color: #D4A03D;">FINANZAS & MERCADOS</div>
                            </div>
                        </div>
                        <div class="report-header-title-box">
                            <div class="report-header-title">INFORME DE ALUMNOS POR CURSO</div>
                            <div class="report-header-meta">
                                <strong>Período:</strong> 01/03/2026 al 31/03/2026 • <strong>Emitido:</strong> 28/08/2026 por [Admin General]
                            </div>
                        </div>
                    </div>

                    <div style="margin-bottom: 14px; display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 13px; font-weight: 800; color: #081426;">
                            Curso: <strong>Especialización en Idoneidad Bursátil</strong>
                        </span>
                        <span style="background: #FEF3C7; color: #92400E; border: 1px solid #FCD34D; font-size: 10px; font-weight: 800; padding: 4px 8px; border-radius: 4px;">
                            <i class="fa-solid fa-check-circle me-1"></i> CON PROGRAMA VIGENTE (Edición 2026-A)
                        </span>
                    </div>

                    <div class="kpi-row">
                        <div class="kpi-box">
                            <div class="kpi-num">840</div>
                            <div class="kpi-label">TOTAL INSCRIPCIONES</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num" style="color: #16A34A;">312</div>
                            <div class="kpi-label">COMPLETADAS (CERTIF.)</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num" style="color: #2563EB;">461</div>
                            <div class="kpi-label">ACTIVAS / VIGENTES</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num" style="color: #DC2626;">67</div>
                            <div class="kpi-label">DADAS DE BAJA (ABANDONO)</div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">1. Comparación de Inscripciones entre Cursos</div>
                            <div class="chart-subtitle">Volumen total de inscriptos en el período, contrastando el curso seleccionado con el catálogo.</div>
                        </div>
                        <div class="hbar-list">
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <strong>Idoneidad Bursátil (Seleccionado)</strong>
                                    <span>840 alumnos</span>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill highlight" style="width: 84%;">840 unid.</div>
                                </div>
                            </div>
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <span>Operativa en Futuros y Opciones CNV</span>
                                    <span>520 alumnos</span>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill" style="width: 52%;">520 unid.</div>
                                </div>
                            </div>
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <span>Valuación de Bonos y Renta Fija</span>
                                    <span>390 alumnos</span>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill" style="width: 39%;">390 unid.</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">2. Evolución Diaria de Demanda</div>
                            <div class="chart-subtitle">Comportamiento diario de las inscripciones registradas a lo largo del período seleccionado.</div>
                        </div>
                        <svg class="line-chart-svg" viewBox="0 0 700 140">
                            <line x1="40" y1="10" x2="40" y2="120" stroke="#CBD5E1" stroke-width="1.5"/>
                            <line x1="40" y1="120" x2="680" y2="120" stroke="#CBD5E1" stroke-width="1.5"/>
                            <polyline fill="none" stroke="#081426" stroke-width="3" points="60,105 160,85 260,98 380,45 500,68 640,25"/>
                            <circle cx="60" cy="105" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="50" y="120" font-size="9" fill="#64748B">Día 01 (90)</text>
                            <circle cx="160" cy="85" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="150" y="120" font-size="9" fill="#64748B">Día 06 (150)</text>
                            <circle cx="260" cy="98" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="250" y="120" font-size="9" fill="#64748B">Día 12 (120)</text>
                            <circle cx="380" cy="45" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="370" y="120" font-size="9" fill="#64748B">Día 18 (280)</text>
                            <circle cx="500" cy="68" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="490" y="120" font-size="9" fill="#64748B">Día 24 (210)</text>
                            <circle cx="640" cy="25" r="4" fill="#D4A03D" stroke="#081426" stroke-width="2"/>
                            <text x="625" y="120" font-size="9" fill="#64748B">Día 30 (350)</text>
                        </svg>
                    </div>
                </div>

                <div class="report-page-footer">
                    <span>SISTEMA DE INFORME DE ALUMNOS POR CURSO • IDÓNEOS ONLINE S.A.S.</span>
                    <span>PÁGINA 1 DE 2</span>
                </div>
            </div>

            <!-- PÁGINA 2 -->
            <div class="a4-page">
                <div>
                    <div class="report-header-grid">
                        <div class="report-header-logo">
                            <img src="${dataUri}" alt="Logo">
                            <div>
                                <div style="font-size: 13px; font-weight: 900; color: #081426; line-height: 1.1;">IDÓNEOS ONLINE</div>
                                <div style="font-size: 8px; font-weight: 700; color: #D4A03D;">FINANZAS & MERCADOS</div>
                            </div>
                        </div>
                        <div class="report-header-title-box">
                            <div class="report-header-title">INFORME DE ALUMNOS (CONT.)</div>
                            <div class="report-header-meta">
                                <strong>Curso:</strong> Especialización en Idoneidad Bursátil • Período: 01/03/2026 al 31/03/2026
                            </div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">3. Tasa de Abandono y Retención de Cursada</div>
                            <div class="chart-subtitle">Proporción de alumnos activos/completados frente a bajas o deserciones registradas.</div>
                        </div>
                        <div class="donut-wrap">
                            <div style="width: 140px; height: 140px; border-radius: 50%; background: conic-gradient(#059669 0% 92%, #DC2626 92% 100%); display: flex; align-items: center; justify-content: center;">
                                <div style="width: 80px; height: 80px; border-radius: 50%; background: white; display: flex; flex-direction: column; align-items: center; justify-content: center;">
                                    <span style="font-size: 16px; font-weight: 900; color: #081426;">8.0%</span>
                                    <span style="font-size: 8px; font-weight: 700; color: #DC2626;">ABANDONO</span>
                                </div>
                            </div>

                            <div class="donut-legend">
                                <div class="legend-item">
                                    <div class="legend-color" style="background: #059669;"></div>
                                    <div>
                                        <strong>92.0% Retención Exitosa</strong>
                                        <div style="font-size: 10px; color: #64748B;">773 alumnos (312 completados + 461 vigentes)</div>
                                    </div>
                                </div>
                                <div class="legend-item">
                                    <div class="legend-color" style="background: #DC2626;"></div>
                                    <div>
                                        <strong>8.0% Tasa de Abandono / Bajas</strong>
                                        <div style="font-size: 10px; color: #64748B;">67 alumnos dados de baja durante la cursada</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">4. Efectividad por Programa Académico</div>
                            <div class="chart-subtitle">Comparativa entre versiones de programa para identificar cuál captó mayor volumen de alumnos.</div>
                        </div>
                        <div class="hbar-list">
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <strong>Programa 2026-A (Intensivo de Verano - Vigente)</strong>
                                    <strong style="color: #059669;">520 alumnos (62%)</strong>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill success" style="width: 62%;">520 unid.</div>
                                </div>
                            </div>
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <span>Programa 2025-B (Regular Semestral - Histórico)</span>
                                    <span>320 alumnos (38%)</span>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill" style="width: 38%;">320 unid.</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="report-page-footer">
                    <span>SISTEMA DE INFORME DE ALUMNOS POR CURSO • IDÓNEOS ONLINE S.A.S.</span>
                    <span>PÁGINA 2 DE 2</span>
                </div>
            </div>
        </div>
    </section>

    <!-- 2. FRAME: CU-97 INFORME DE INGRESOS (2 PÁGINAS) -->
    <section class="figma-section-frame">
        <div class="figma-frame-header">
            <span class="badge">CU-97</span>
            <span>Informe de Ingresos por Curso (Páginas 1 y 2 Financieras)</span>
        </div>

        <div class="pages-row">
            <!-- PÁGINA 1 -->
            <div class="a4-page">
                <div>
                    <div class="report-header-grid">
                        <div class="report-header-logo">
                            <img src="${dataUri}" alt="Logo">
                            <div>
                                <div style="font-size: 13px; font-weight: 900; color: #081426; line-height: 1.1;">IDÓNEOS ONLINE</div>
                                <div style="font-size: 8px; font-weight: 700; color: #D4A03D;">FINANZAS & MERCADOS</div>
                            </div>
                        </div>
                        <div class="report-header-title-box">
                            <div class="report-header-title">INFORME DE INGRESOS POR CURSO</div>
                            <div class="report-header-meta">
                                <strong>Período:</strong> 01/03/2026 al 31/03/2026 • <strong>Emitido:</strong> 28/08/2026 por [Admin General]
                            </div>
                        </div>
                    </div>

                    <div class="kpi-row">
                        <div class="kpi-box">
                            <div class="kpi-num" style="color: #059669;">$100.800.000</div>
                            <div class="kpi-label">NETO ACREDITADO</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num">$120.000.000</div>
                            <div class="kpi-label">PRECIO LISTA BRUTO</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num" style="color: #D97706;">$19.200.000</div>
                            <div class="kpi-label">DESCUENTOS APLICADOS</div>
                        </div>
                        <div class="kpi-box">
                            <div class="kpi-num">840</div>
                            <div class="kpi-label">PAGOS ACREDITADOS</div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">1. Comparación de Facturación entre Cursos</div>
                            <div class="chart-subtitle">Monto total recaudado en el período, contrastando el curso seleccionado frente a la oferta total.</div>
                        </div>
                        <div class="hbar-list">
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <strong>Idoneidad Bursátil (Seleccionado)</strong>
                                    <strong style="color: #059669;">$100.800.000 ARS</strong>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill success" style="width: 85%;">$100.8M</div>
                                </div>
                            </div>
                            <div class="hbar-item">
                                <div class="hbar-label-row">
                                    <span>Operativa en Futuros y Opciones CNV</span>
                                    <span>$62.400.000 ARS</span>
                                </div>
                                <div class="hbar-track">
                                    <div class="hbar-fill" style="width: 55%;">$62.4M</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="chart-section">
                        <div class="chart-header">
                            <div class="chart-title">2. Evolución Diaria de Facturación</div>
                            <div class="chart-subtitle">Comportamiento de los ingresos diarios percibidos a lo largo del período seleccionado.</div>
                        </div>
                        <svg class="line-chart-svg" viewBox="0 0 700 140">
                            <line x1="40" y1="10" x2="40" y2="120" stroke="#CBD5E1" stroke-width="1.5"/>
                            <line x1="40" y1="120" x2="680" y2="120" stroke="#CBD5E1" stroke-width="1.5"/>
                            <polyline fill="none" stroke="#059669" stroke-width="3" points="60,110 160,90 260,95 380,40 500,65 640,20"/>
                            <circle cx="60" cy="110" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="45" y="125" font-size="9" fill="#64748B">Día 01 ($10.8M)</text>
                            <circle cx="160" cy="90" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="145" y="125" font-size="9" fill="#64748B">Día 06 ($18.0M)</text>
                            <circle cx="260" cy="95" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="245" y="125" font-size="9" fill="#64748B">Día 12 ($14.4M)</text>
                            <circle cx="380" cy="40" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="365" y="125" font-size="9" fill="#64748B">Día 18 ($33.6M)</text>
                            <circle cx="500" cy="65" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="485" y="125" font-size="9" fill="#64748B">Día 24 ($25.2M)</text>
                            <circle cx="640" cy="20" r="4" fill="#081426" stroke="#059669" stroke-width="2"/>
                            <text x="615" y="125" font-size="9" fill="#64748B">Día 30 ($42.0M)</text>
                        </svg>
                    </div>
                </div>

                <div class="report-page-footer">
                    <span>SISTEMA DE INFORME DE INGRESOS POR CURSO • IDÓNEOS ONLINE S.A.S.</span>
                    <span>PÁGINA 1 DE 2</span>
                </div>
            </div>

            <!-- PÁGINA 2 -->
            <div class="a4-page">
                <div>
                    <div class="report-header-grid">
                        <div class="report-header-logo">
                            <img src="${dataUri}" alt="Logo">
                            <div>
                                <div style="font-size: 13px; font-weight: 900; color: #081426; line-height: 1.1;">IDÓNEOS ONLINE</div>
                                <div style="font-size: 8px; font-weight: 700; color: #D4A03D;">FINANZAS & MERCADOS</div>
                            </div>
                        </div>
                        <div class="report-header-title-box">
                            <div class="report-header-title">INFORME DE INGRESOS (ANÁLISIS DE MÁRGENES)</div>
                            <div class="report-header-meta">
                                <strong>Curso:</strong> Especialización en Idoneidad Bursátil • Período: 01/03/2026 al 31/03/2026
                            </div>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                        <div class="chart-section">
                            <div class="chart-header">
                                <div class="chart-title">3. Ingresos por Categoría Temática</div>
                                <div class="chart-subtitle">Participación porcentual en la facturación global.</div>
                            </div>
                            <div class="donut-wrap" style="flex-direction: column; gap: 14px;">
                                <div style="width: 120px; height: 120px; border-radius: 50%; background: conic-gradient(#081426 0% 55%, #D4A03D 55% 80%, #64748B 80% 100%);"></div>
                                <div class="donut-legend">
                                    <div class="legend-item">
                                        <div class="legend-color" style="background: #081426;"></div>
                                        <span>Mercado de Capitales (55%)</span>
                                    </div>
                                    <div class="legend-item">
                                        <div class="legend-color" style="background: #D4A03D;"></div>
                                        <span>Finanzas Corporativas (25%)</span>
                                    </div>
                                    <div class="legend-item">
                                        <div class="legend-color" style="background: #64748B;"></div>
                                        <span>Cripto & DeFi (20%)</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="chart-section">
                            <div class="chart-header">
                                <div class="chart-title">4. Bruto vs Descuentos vs Neto</div>
                                <div class="chart-subtitle">Relación entre precio de lista y acreditación real.</div>
                            </div>
                            <div class="hbar-list" style="margin-top: 14px;">
                                <div class="hbar-item">
                                    <div class="hbar-label-row">
                                        <span>Precio Bruto (100%)</span>
                                        <strong>$120.0M</strong>
                                    </div>
                                    <div class="hbar-track">
                                        <div class="hbar-fill highlight" style="width: 100%;">$120M</div>
                                    </div>
                                </div>
                                <div class="hbar-item">
                                    <div class="hbar-label-row">
                                        <span>Descuentos (-16%)</span>
                                        <strong style="color: #D97706;">$19.2M</strong>
                                    </div>
                                    <div class="hbar-track">
                                        <div class="hbar-fill gold" style="width: 16%;">$19.2M</div>
                                    </div>
                                </div>
                                <div class="hbar-item">
                                    <div class="hbar-label-row">
                                        <span>Neto Acreditado (84%)</span>
                                        <strong style="color: #059669;">$100.8M</strong>
                                    </div>
                                    <div class="hbar-track">
                                        <div class="hbar-fill success" style="width: 84%;">$100.8M</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="report-page-footer">
                    <span>SISTEMA DE INFORME DE INGRESOS POR CURSO • IDÓNEOS ONLINE S.A.S.</span>
                    <span>PÁGINA 2 DE 2</span>
                </div>
            </div>
        </div>
    </section>

    <!-- 3. FRAME: CERTIFICADO DIGITAL OFICIAL -->
    <section class="figma-section-frame">
        <div class="figma-frame-header">
            <span class="badge">CERTIFICADO</span>
            <span>Certificado Oficial de Aprobación Académica (Landscape 960x680)</span>
        </div>

        <div class="cert-landscape">
            <div class="cert-top-gold-line"></div>
            <div class="cert-top-wave"></div>

            <div class="cert-header-logos">
                <div class="cert-brand-block">
                    <img src="${dataUri}" alt="Logo">
                    <div>
                        <div class="cert-brand-title">IDÓNEOS <span>ONLINE</span></div>
                        <div style="font-size: 9px; font-weight: 800; color: #64748B; letter-spacing: 0.8px;">EDUCACIÓN FINANCIERA & MERCADO DE CAPITALES</div>
                    </div>
                </div>

                <div class="cert-endorsers">
                    <span>FCEQyN — UNaM</span> • <span>CNV RÉGIMEN IDÓNEOS</span>
                </div>
            </div>

            <div class="cert-body-center">
                <div class="cert-institution-intro">Idóneos Online S.A.S. otorga el presente</div>
                <div class="cert-main-title">CERTIFICADO</div>

                <div class="cert-awarded-row">
                    <span>A</span>
                    <span class="cert-student-name">Joaquin Küster</span>
                    <span class="cert-student-dni">D.N.I.: <strong>44.652.101</strong></span>
                </div>

                <p class="cert-paragraph">
                    Por haber completado y aprobado satisfactoriamente las exigencias académicas y evaluaciones de la 
                    <strong>"Especialización en Idoneidad Bursátil"</strong> (Programa 2026-A), dictado durante el período 
                    Marzo - Agosto 2026, con una carga horaria total de <strong>120 horas cátedra</strong>.
                </p>
            </div>

            <div class="cert-footer-signatures">
                <div class="sig-block">
                    <div class="sig-image-holder">Fausto Spotorno</div>
                    <div class="sig-line">
                        <div class="sig-name">Lic. Fausto Spotorno</div>
                        <div class="sig-role">Docente Titular • Matrícula CNV 4821</div>
                    </div>
                </div>

                <div class="sig-block">
                    <div class="sig-image-holder" style="font-family: 'Cinzel', cursive;">Lazaro Martinez</div>
                    <div class="sig-line">
                        <div class="sig-name">Lic. Lazaro Martinez</div>
                        <div class="sig-role">Director Académico • Idóneos Online</div>
                    </div>
                </div>
            </div>

            <div class="cert-bottom-meta">
                <div class="cert-qr-mini">
                    <i class="fa-solid fa-qrcode"></i> CERT-2026-000142
                </div>
                <div>Posadas, Misiones, Argentina • 28 de Agosto de 2026</div>
                <div>Verificación: <code>https://idoneos.online/validar/CERT-2026-000142</code></div>
            </div>
        </div>
    </section>

    <!-- 4. FRAME: COMPROBANTE DE PAGO DIGITAL -->
    <section class="figma-section-frame">
        <div class="figma-frame-header">
            <span class="badge">COMPROBANTE</span>
            <span>Comprobante de Pago Oficial del Sistema (Idóneos Online S.A.S.)</span>
        </div>

        <div class="ticket-page">
            <div class="ticket-brand-row">
                <div class="ticket-brand-left">
                    <img src="${dataUri}" alt="Logo">
                    <div>
                        <div class="ticket-brand-text">IDÓNEOS <span>ONLINE</span></div>
                        <div style="font-size: 9px; font-weight: 700; color: #64748B;">IDÓNEOS ONLINE S.A.S. • CUIT 30-71884920-4</div>
                    </div>
                </div>
                <div class="ticket-corp-tag">Comprobante de Pago Oficial</div>
            </div>

            <div class="ticket-item">
                <div class="ticket-label">Concepto de Facturación / Curso</div>
                <div class="ticket-val-main">Matrícula: Especialización en Idoneidad Bursátil (Cohorte 2026-1)</div>
            </div>

            <div class="ticket-item">
                <div class="ticket-label">Monto Total Abonado</div>
                <div class="ticket-amount-big">
                    <span>$ 100.800,00 ARS</span>
                    <span class="badge-acreditado"><i class="fa-solid fa-check"></i> Pago Acreditado</span>
                </div>
            </div>

            <div class="ticket-divider"></div>

            <div class="ticket-item">
                <div class="ticket-section-title">Datos del Titular / Alumno:</div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 4px;">
                    <div>
                        <div class="ticket-label">Apellido y Nombre</div>
                        <div class="ticket-val-main" style="font-size: 13.5px;">KUSTER JOAQUIN</div>
                    </div>
                    <div>
                        <div class="ticket-label">D.N.I. / CUIT</div>
                        <div style="font-size: 13.5px; font-weight: 700;">20-44652101-3</div>
                    </div>
                </div>
                <div style="margin-top: 6px;">
                    <div class="ticket-label">Medio de Pago / Pasarela</div>
                    <div style="font-size: 13px; color: #334155;">Tarjeta de Débito Visa •••• 5112 (MercadoPago / Transferencia)</div>
                </div>
            </div>

            <div class="ticket-divider"></div>

            <div class="ticket-item">
                <div class="ticket-section-title">Datos del Prestador:</div>
                <div style="font-size: 12.5px; color: #334155; line-height: 1.4;">
                    <strong>IDÓNEOS ONLINE S.A.S.</strong> • Av. Corrientes 1230, Posadas, Misiones<br>
                    Condición IVA: Responsable Inscripto • CBU: <code>0000003100013104357547</code>
                </div>
            </div>

            <div class="ticket-divider"></div>

            <div class="ticket-item">
                <div class="ticket-label">Código de Referencia Operativa (reference_code)</div>
                <div class="ticket-mono-box">Z4K8DVNDP1XP01DK95J8LQ</div>
            </div>

            <div class="ticket-item">
                <div class="ticket-label">Identificador de Transacción API (external_intention_id)</div>
                <div class="ticket-mono-box" style="font-size: 11px;">babb0815d85744bd9302b26824d0b84e</div>
            </div>

            <div class="ticket-footer-row">
                <span>Fecha de Operación: <strong>28/08/2026 07:45:18</strong></span>
                <span>N° Comprobante: <strong>COMP-2026-000840</strong></span>
                <span>Página 1 de 1</span>
            </div>
        </div>
    </section>

</body>
</html>`;

fs.writeFileSync('c:/Users/Lazaro/Desktop/TF/idoneos.online/docs/diseño/figma_canvas_reportes_certificados.html', figmaHtml, 'utf8');
console.log('OK_FIGMA_SAVED');
